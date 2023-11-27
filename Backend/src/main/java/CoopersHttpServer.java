import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import com.google.gson.Gson;

public class CoopersHttpServer {

    // Helper method to read the request body from an InputStream
    private static String readRequestBody(InputStream requestBody) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    // Helper method to authenticate the user
    public static boolean authenticateUser(int employee_ID, String password) throws SQLException {
        // send corresponding query to snowflake
        String sqlQuery = "SELECT * FROM EMPLOYEE WHERE EMPLOYEE_ID = " + employee_ID + " AND PASSWORD = '" + password
                + "';";

        ResultSet resultSet;
        try {
            resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // check if there are any matching records
        return resultSet.next();
    }

    // Handlers for each of the different endpoints
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/api/login" context
            System.out.println("Login API called");
            if ("POST".equals(exchange.getRequestMethod())) {
                // Parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.LoginJson login = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.LoginJson.class);

                // Authenticate user
                boolean userIsAuthenticated = false;
                try {
                    userIsAuthenticated = authenticateUser(login.EMPLOYEE_ID, login.PASSWORD);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                // Send corresponding response to frontend
                String response;
                if (userIsAuthenticated) {
                    exchange.sendResponseHeaders(200, 0); // authorized status code
                    response = "{\"isAuthorized\": true}";
                } else {
                    exchange.sendResponseHeaders(401, 0); // unauthorized status code
                    response = "{\"isAuthorized\": false}";

                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response\n");
                }
            }
        }
    }

    static class AddCustomerAndOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Add Customer And Order API Called");
            // Handle requests for "/api/addcustomerandorder" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // Get the current date and time as a LocalDateTime object
                LocalDateTime now = LocalDateTime.now();

                // Create a formatter with the desired pattern
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // parse JSON from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.AddCustomerAndOrderJson addCustomerAndOrderJson = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.AddCustomerAndOrderJson.class);

                String response;
                try {
                    // update CUSTOMER table
                    String sqlQuery = "MERGE INTO Customer AS target USING (SELECT '" + addCustomerAndOrderJson.PHONE_NUMBER
                        + "' AS phone_number, '" + addCustomerAndOrderJson.ADDRESS + "' AS address, '"
                        + addCustomerAndOrderJson.ZIPCODE_KEY
                        + "' AS zipcode_key) AS source ON target.phone_number = source.phone_number WHEN MATCHED THEN UPDATE SET target.address = source.address, target.zipcode_key = source.zipcode_key WHEN NOT MATCHED THEN INSERT (phone_number, address, zipcode_key) VALUES (source.phone_number, source.address, source.zipcode_key);";
                    SnowFlakeConnector.sendQuery(sqlQuery);

                    // update CUSTOMER_ORDER table (add new record)
                    sqlQuery = "INSERT INTO CUSTOMER_ORDER (ORDER_NUMBER, EMPLOYEE_ID, PHONE_NUMBER, TIME, TOTAL_PAID) VALUES (ORDER_NUMBER_SEQ.nextval, "
                        +
                        addCustomerAndOrderJson.EMPLOYEE_ID + ", '" + addCustomerAndOrderJson.PHONE_NUMBER + "', TO_TIMESTAMP_NTZ('"
                        + now.format(formatter) + "'), 0.0);";
                    SnowFlakeConnector.sendQuery(sqlQuery);

                    // grab the ORDER_NUMBER associated with the above created new CUSTOMER_ORDER record
                    sqlQuery = "SELECT MAX(ORDER_NUMBER) FROM CUSTOMER_ORDER;";
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    int ORDER_NUMBER = resultSet.getInt("MAX(ORDER_NUMBER)");
                    // System.out.println("ORDER_NUMBER: " + ORDER_NUMBER);
                    exchange.sendResponseHeaders(201, 0);
                    response = "{\n\t\"ORDER_NUMBER\": " + ORDER_NUMBER + "\n}";
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(422, 0);
                    response = "SQL error";
                    e.printStackTrace();
                }

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response\n");
                }
            }
        }
    }

    static class CancelOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Cancel Order API Called");
            // Handle requests for "/api/cancelorder" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.OrderDetailJson orderDetail = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.OrderDetailJson.class);

                String response;
                try {
                    String sqlQuery = "DELETE FROM CUSTOMER_ORDER WHERE ORDER_NUMBER = " + orderDetail.ORDER_NUMBER + ";";
                    // System.out.println(sqlQuery);
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    sqlQuery = "DELETE FROM ORDER_DETAIL WHERE ORDER_NUMBER = " + orderDetail.ORDER_NUMBER + ";";
                    // System.out.println(sqlQuery);
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(200, 0);
                    response = "{\n\t\"DELETION_SUCCESSFUL\": true\n}";
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(422, 0);
                    response = "{\n\tSQL error\n}";
                    e.printStackTrace();
                }

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response\n");
                }
            }
        }
    }
    
    static class AddOrderDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Add Order Detail API Called");
            // Handle requests for "/api/addorderdetail" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.OrderDetailEntryJson orderDetailEntryJson = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.OrderDetailEntryJson.class);

                ArrayList<JsonStructures.OrderDetailEntryJson> listOfOrderDetailEntries = new ArrayList<>();

                double CART_TOTAL = 0.0;
                String response;
                try {
                    // grab the PRODUCT_ID, PRICE associated with the PRODUCT_NAME
                    String sqlQuery = "SELECT PRODUCT_ID, PRICE FROM PRODUCT WHERE PRODUCT_NAME = '" + orderDetailEntryJson.PRODUCT_NAME + "';";
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    int PRODUCT_ID = resultSet.getInt("PRODUCT_ID");
                    double PRODUCT_PRICE = resultSet.getDouble("PRICE");
                    double PRICE_PAID = PRODUCT_PRICE * orderDetailEntryJson.QUANTITY;

                    // insert new ORDER_DETAIL record
                    sqlQuery = "INSERT INTO ORDER_DETAIL (ORDER_NUMBER, PRODUCT_ID, PRICE_PAID, QUANTITY, NOTES, ORDER_DETAIL_KEY) VALUES ("
                        + orderDetailEntryJson.ORDER_NUMBER + ", " + PRODUCT_ID + ", " + PRICE_PAID + ", "
                        + orderDetailEntryJson.QUANTITY + ", '" + orderDetailEntryJson.NOTES + "', " + "ORDER_DETAIL_SEQ.nextval);";
                    // System.out.println(sqlQuery);
                    SnowFlakeConnector.sendQuery(sqlQuery);

                    // grab current cart
                    sqlQuery = "SELECT * FROM ORDER_DETAIL O\nJOIN PRODUCT P\n\tON O.PRODUCT_ID = P.PRODUCT_ID\nWHERE ORDER_NUMBER = " + orderDetailEntryJson.ORDER_NUMBER + ";";
                    // System.out.println(sqlQuery);
                    resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    while ( resultSet.next() ) {
                        JsonStructures.OrderDetailEntryJson detail = new JsonStructures.OrderDetailEntryJson();
                        detail.ORDER_DETAIL_KEY = resultSet.getInt("ORDER_DETAIL_KEY");
                        detail.ORDER_NUMBER = resultSet.getInt("ORDER_NUMBER");
                        detail.PRODUCT_NAME = resultSet.getString("PRODUCT_NAME");
                        detail.PRICE_PAID = resultSet.getDouble("PRICE_PAID");
                        detail.QUANTITY = resultSet.getInt("QUANTITY");
                        detail.NOTES = resultSet.getString("NOTES");
                        listOfOrderDetailEntries.add(detail);
                        CART_TOTAL += detail.PRICE_PAID;
                    }
                    CART_TOTAL = Math.round(CART_TOTAL*100.0) / 100.0;
                    sqlQuery = "UPDATE CUSTOMER_ORDER SET TOTAL_PAID = " + CART_TOTAL + " WHERE ORDER_NUMBER = " + orderDetailEntryJson.ORDER_NUMBER + ";";
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(201, 0);
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(422, 0);
                    response = "{\n\tSQL error\n}";
                    e.printStackTrace();
                }

                // Converts ArrayList to JSON format
                Gson gson = new Gson();
                response = "{\n\t\"CART_TOTAL\": " + CART_TOTAL + ",\n\t\"CART\": \n\t" + gson.toJson(listOfOrderDetailEntries) + "\n}";
                // System.out.println("Converting to JSON");

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response\n");
                }
            }
        }
    }

    static class RemoveOrderDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Remove Order Detail API Called");
            // Handle requests for "/api/removeorderdetail" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.OrderDetailKeyJson orderDetailKeyJson = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.OrderDetailKeyJson.class);

                ArrayList<JsonStructures.OrderDetailEntryJson> listOfOrderDetailEntries = new ArrayList<>();
                double CART_TOTAL = 0.0;
                String response;
                try {
                    String sqlQuery = "DELETE FROM ORDER_DETAIL WHERE ORDER_DETAIL_KEY = " + orderDetailKeyJson.ORDER_DETAIL_KEY + ";";
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    //System.out.println(sqlQuery);

                    sqlQuery = "SELECT * FROM ORDER_DETAIL O\nJOIN PRODUCT P\n\tON O.PRODUCT_ID = P.PRODUCT_ID\nWHERE ORDER_NUMBER = " + orderDetailKeyJson.ORDER_NUMBER + ";";
                    //System.out.println(sqlQuery);
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    while ( resultSet.next() ) {
                        JsonStructures.OrderDetailEntryJson detail = new JsonStructures.OrderDetailEntryJson();
                        detail.ORDER_DETAIL_KEY = resultSet.getInt("ORDER_DETAIL_KEY");
                        detail.ORDER_NUMBER = resultSet.getInt("ORDER_NUMBER");
                        detail.PRODUCT_NAME = resultSet.getString("PRODUCT_NAME");
                        detail.PRICE_PAID = resultSet.getDouble("PRICE_PAID");
                        detail.QUANTITY = resultSet.getInt("QUANTITY");
                        detail.NOTES = resultSet.getString("NOTES");
                        CART_TOTAL += detail.PRICE_PAID;
                        listOfOrderDetailEntries.add(detail);
                    }

                    CART_TOTAL = Math.round(CART_TOTAL*100.0) / 100.0;
                    sqlQuery = "UPDATE CUSTOMER_ORDER SET TOTAL_PAID = " + CART_TOTAL + " WHERE ORDER_NUMBER = " + orderDetailKeyJson.ORDER_NUMBER + ";";
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(422, 0);
                    response = "{\n\tSQL error\n}";
                    e.printStackTrace();
                }

                // Converts ArrayList to JSON format
                Gson gson = new Gson();
                response = "{\n\t\"CART_TOTAL\": " + CART_TOTAL + ",\n\t\"CART\": \n\t" + gson.toJson(listOfOrderDetailEntries) + "\n}";
                // System.out.println("Converting to JSON");

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response\n");
                }
            }
        }
    }

    static class CheckForCustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Check For Customer API Called");
            // Handle requests for "/api/checkforcustomer" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.CheckForCustomerJson checkForCustomer = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.CheckForCustomerJson.class);

                // send query to snowflake to check if PHONE_NUMBER is already in the Customer
                // table
                String sqlQuery = "SELECT * FROM Customer WHERE PHONE_NUMBER = '" + checkForCustomer.PHONE_NUMBER
                        + "';";
                ResultSet resultSet;
                try {
                    resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                // grab query results
                int ZIPCODE_KEY;
                String ADDRESS, response;
                try {
                    resultSet.next();
                    ZIPCODE_KEY = resultSet.getInt("ZIPCODE_KEY");
                    ADDRESS = resultSet.getString("ADDRESS");
                    exchange.sendResponseHeaders(200, 0); // PHONE_NUMBER found
                    response = "{\n\t\"ZIPCODE_KEY\": " + ZIPCODE_KEY + "\n\t\"ADDRESS\": \'" + ADDRESS + "'\n}";
                } catch (SQLException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(404, 0); // PHONE_NUMBER not found
                    response = "PHONE_NUMBER not found";
                }

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                System.out.println("Sent response");
            }
        }
    }

    static class ViewOneOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/api/viewoneorder" context
            System.out.println("View One Order API Called");
            // order_number, employee_id, employeee first_name last_name, time, customer
            // phone number, customer zipcode
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.OrderDetailJson orderDetail = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.OrderDetailJson.class);

                String response;
                JsonStructures.OrderDetail orderDetailOut = new JsonStructures.OrderDetail();
                // send queries to snowflake
                try {
                    String sqlQuery = "SELECT * FROM CUSTOMER_ORDER O\nJOIN EMPLOYEE E\n\tON E.EMPLOYEE_ID = O.EMPLOYEE_ID\nJOIN CUSTOMER C\n\tON C.PHONE_NUMBER = O.PHONE_NUMBER\nWHERE O.ORDER_NUMBER = "
                            + orderDetail.ORDER_NUMBER + ";";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    orderDetailOut.ORDER_NUMBER = orderDetail.ORDER_NUMBER;
                    orderDetailOut.EMPLOYEE_ID = resultSet.getInt("EMPLOYEE_ID");
                    orderDetailOut.PHONE_NUMBER = resultSet.getString("PHONE_NUMBER");
                    orderDetailOut.TIME = resultSet.getString("TIME");
                    orderDetailOut.FIRST_NAME = resultSet.getString("FIRST_NAME");
                    orderDetailOut.LAST_NAME = resultSet.getString("LAST_NAME");
                    orderDetailOut.ZIPCODE_KEY = resultSet.getInt("ZIPCODE_KEY");

                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "{\n\tSQL ERROR\n}";
                    exchange.sendResponseHeaders(404, 0);
                }

                // Convert to JSON format
                Gson gson = new Gson();
                response = gson.toJson(orderDetailOut);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.toString().getBytes());
                }
                System.out.println("Sent response");
            }
        }
    }

    static class ViewOrdersByZipcodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/api/viewordersbyzipcode" context
            System.out.println("View Orders By Zipcode API Called");
            // order_number, employeee first_name last_name (id), time, customer phone
            // number, customer zipcode
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.OrdersByZipcodeJson ordersByZipcode = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.OrdersByZipcodeJson.class);

                ArrayList<JsonStructures.OrderDetail> listOfOrders = new ArrayList<>();
                int count = 0;

                // send queries to snowflake
                try {
                    // query and join CUSTOMER_ORDER, CUSTOMER, EMPLOYEE tables
                    String sqlQuery = "SELECT * FROM CUSTOMER_ORDER O\nJOIN CUSTOMER C \n\tON O.PHONE_NUMBER = C.PHONE_NUMBER\nJOIN EMPLOYEE E\n\tON O.EMPLOYEE_ID = E.EMPLOYEE_ID\nWHERE O.TIME BETWEEN TO_TIMESTAMP_NTZ('"
                            + ordersByZipcode.TIME_BEGIN + "')" + " AND TO_TIMESTAMP_NTZ('" + ordersByZipcode.TIME_END
                            + "') AND C.ZIPCODE_KEY = " + ordersByZipcode.ZIPCODE_KEY + ";";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);

                    while (resultSet.next()) {
                        JsonStructures.OrderDetail order = new JsonStructures.OrderDetail();
                        order.ORDER_NUMBER = resultSet.getInt("ORDER_NUMBER");
                        order.EMPLOYEE_ID = resultSet.getInt("EMPLOYEE_ID");
                        order.FIRST_NAME = resultSet.getString("FIRST_NAME");
                        order.LAST_NAME = resultSet.getString("LAST_NAME");
                        order.TIME = resultSet.getString("TIME");
                        order.PHONE_NUMBER = resultSet.getString("PHONE_NUMBER");
                        order.ZIPCODE_KEY = resultSet.getInt("ZIPCODE_KEY");
                        listOfOrders.add(order);
                        count++;
                    }
                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(422, 0);
                }

                // Converts ArrayList to JSON format
                Gson gson = new Gson();
                String response = "{\n\t\"COUNT\": " + count + ",\n\t\"ORDER_DETAILS_LIST\":\n\t\t" + gson.toJson(listOfOrders) + "\n}";
                // System.out.println("Converting to JSON");

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.toString().getBytes());
                }
                System.out.println("Sent response");
            }
        }
    }

    static class ViewOrdersByEmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/api/viewordersbyemployee" context
            System.out.println("View Orders By Employee API Called");
            // order_number, employeee first_name last_name (id), time, customer phone
            // number, customer zipcode
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.OrdersByEmployeeJson ordersByEmployee = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.OrdersByEmployeeJson.class);

                ArrayList<JsonStructures.OrderDetail> listOfOrders = new ArrayList<>();
                int count = 0;
                // send queries to snowflake
                try {
                    // query and join CUSTOMER_ORDER, CUSTOMER, EMPLOYEE tables
                    String sqlQuery = "SELECT * FROM CUSTOMER_ORDER O\nJOIN CUSTOMER C\n\tON C.PHONE_NUMBER = O.PHONE_NUMBER\nJOIN EMPLOYEE E\n\tON E.EMPLOYEE_ID = O.EMPLOYEE_ID\nWHERE O.EMPLOYEE_ID = "
                            + ordersByEmployee.EMPLOYEE_ID + " AND O.TIME BETWEEN TO_TIMESTAMP_NTZ('"
                            + ordersByEmployee.TIME_BEGIN + "')" + " AND TO_TIMESTAMP_NTZ('" + ordersByEmployee.TIME_END
                            + "');";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);

                    while (resultSet.next()) {
                        JsonStructures.OrderDetail order = new JsonStructures.OrderDetail();
                        order.ORDER_NUMBER = resultSet.getInt("ORDER_NUMBER");
                        order.EMPLOYEE_ID = resultSet.getInt("EMPLOYEE_ID");
                        order.FIRST_NAME = resultSet.getString("FIRST_NAME");
                        order.LAST_NAME = resultSet.getString("LAST_NAME");
                        order.TIME = resultSet.getString("TIME");
                        order.PHONE_NUMBER = resultSet.getString("PHONE_NUMBER");
                        order.ZIPCODE_KEY = resultSet.getInt("ZIPCODE_KEY");
                        listOfOrders.add(order);
                        count++;
                    }
                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(422, 0);
                }

                // Converts ArrayList to JSON format
                Gson gson = new Gson();
                String response = "{\n\t\"COUNT\": " + count + ",\n\t\"ORDER_DETAILS_LIST\":\n\t\t" + gson.toJson(listOfOrders) +"\n}";
                // System.out.println("Converting to JSON");

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.toString().getBytes());
                }
                System.out.println("Sent response");
            }
        }
    }

    static class ShowEmployeesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Show Employees API Called");
            if ("GET".equals(exchange.getRequestMethod())) {
                String sqlQuery = "SELECT employee_id, first_name, last_name, status FROM Employee";
                ResultSet resultSet;

                // Sends query to get all employees (employee_id, first_name, last_name)
                try {
                    System.out.println("Query Sent");
                    resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(404, 0);
                    throw new RuntimeException(e);
                }

                ArrayList<JsonStructures.employeeDetails> list = new ArrayList<>();

                try {
                    // Creates employee objects and adds them to an ArrayList
                    while (resultSet.next()) {
                        JsonStructures.employeeDetails employee = new JsonStructures.employeeDetails();
                        employee.setEmployeeID(resultSet.getString("EMPLOYEE_ID"));
                        employee.setFirstName(resultSet.getString("FIRST_NAME"));
                        employee.setLastName(resultSet.getString("LAST_NAME"));
                        employee.setStatus(resultSet.getString("STATUS"));
                        list.add(employee);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // Converts ArrayList to JSON format
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(list);
                System.out.println("Converting to JSON");

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                    System.out.println("Sent response\n");
                }

            }

        }
    }

    static class AddEmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/createorder" context
            System.out.println("Add Employee API Called");
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.AddEmployeeJson addEmployee = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.AddEmployeeJson.class);

                String sqlQuery = "INSERT INTO Employee VALUES ('" + addEmployee.FIRST_NAME + "', '"
                        + addEmployee.LAST_NAME + "', 'active', '"
                        + addEmployee.PASSWORD + "', EMPLOYEE_ID_SEQ.nextval);";

                String response;
                try {
                    //System.out.println("Sent Query");
                    SnowFlakeConnector.sendQuery(sqlQuery);

                    // grab the EMPLOYEE_ID associated with the above created new EMPLOYEE record
                    sqlQuery = "SELECT MAX(EMPLOYEE_ID) FROM EMPLOYEE;";
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    int EMPLOYEE_ID = resultSet.getInt("MAX(EMPLOYEE_ID)");

                    // send response
                    exchange.sendResponseHeaders(201, 0);
                    response = "{\n\t\"EMPLOYEE_ID\": " + EMPLOYEE_ID + "\n}";
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(422, 0);
                    response = "SQL error";
                    e.printStackTrace();
                }

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response\n");
                }

            }
        }
    }

    static class UpdateEmployeeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Update Employee API Called");
            if ("POST".equals(exchange.getRequestMethod())) {
                // Parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.UpdateEmployeeJson updateEmployee = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.UpdateEmployeeJson.class);

                // Create query
                String sqlQuery = "UPDATE Employee SET FIRST_NAME = '" + updateEmployee.FIRST_NAME
                        + "', LAST_NAME = '" + updateEmployee.LAST_NAME
                        + "', STATUS = '" + updateEmployee.STATUS + "' WHERE EMPLOYEE_ID = '"
                        + updateEmployee.EMPLOYEE_ID + "';";

                String response;

                try {
                    // Sends query
                    System.out.println("Sent Query");
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(200, 0);
                    response = "{\"isUpdated\": \"true\"}";
                } catch (SQLException e) {
                    // Failed response
                    exchange.sendResponseHeaders(422, 0);
                    response = "{\"isUpdated\": \"false\"}";
                    e.printStackTrace();
                }

                // Sends reponse to frontend
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response\n");
                }

            }

        }
    }

    static class ShowMenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Show Menu API Called");
            if ("GET".equals(exchange.getRequestMethod())) {

                String sqlQuery = "SELECT price, product_name, size_name FROM PRODUCT P\n" + //
                        "JOIN SIZE S\n" + //
                        "ON P.SIZE_ID = S.SIZE_ID";
                ResultSet resultSet;

                // Sends query to get all employees (employee_id, first_name, last_name)
                try {
                    System.out.println("Query Sent");
                    resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(404, 0);
                    throw new RuntimeException(e);
                }

                ArrayList<JsonStructures.MenuDetails> list = new ArrayList<>();

                try {
                    // Creates employee objects and adds them to an ArrayList
                    while (resultSet.next()) {
                        JsonStructures.MenuDetails menu = new JsonStructures.MenuDetails(resultSet.getFloat("PRICE"),
                                resultSet.getString("PRODUCT_NAME"), resultSet.getString("SIZE_NAME"));
                        list.add(menu);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // Converts ArrayList to JSON format
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(list);
                System.out.println("Converting to JSON");

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                    System.out.println("Sent response\n");
                }

            }

        }
    }

    static class CalculateCartTotalHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Calculate Cart Total API Called");
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.CartTotalJson cartTotal = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.CartTotalJson.class);

                double CART_TOTAL = 0.0;
                String response;
                // send queries to snowflake
                try {
                    String sqlQuery = "SELECT PRICE_PAID FROM ORDER_DETAIL WHERE ORDER_NUMBER = " + cartTotal.ORDER_NUMBER + ";";
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);

                    while (resultSet.next()) {
                        CART_TOTAL += resultSet.getDouble("PRICE_PAID");
                    }
                    double DISCOUNTED_CART_TOTAL = Math.round(CART_TOTAL*(100.0 - cartTotal.DISCOUNT)) / 100.0;
                    sqlQuery = "UPDATE CUSTOMER_ORDER SET TOTAL_PAID = " + DISCOUNTED_CART_TOTAL + " WHERE ORDER_NUMBER = " + cartTotal.ORDER_NUMBER + ";";
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    response = "{\n\t\"DISCOUNTED_CART_TOTAL\": " + DISCOUNTED_CART_TOTAL + "\n}";
                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "{\n\tERROR\n}";
                    exchange.sendResponseHeaders(422, 0);
                }

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.toString().getBytes());
                }
                System.out.println("Sent response");
            }
        }
    }



    public static void main(String[] args) throws SQLException {
        // start the backend server
        HttpServer backendServer;
        try {
            backendServer = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create contexts to handle different endpoints
        backendServer.createContext("/api/login", new LoginHandler());
        backendServer.createContext("/api/addcustomerandorder", new AddCustomerAndOrderHandler());
        backendServer.createContext("/api/cancelorder", new CancelOrderHandler());
        backendServer.createContext("/api/addorderdetail", new AddOrderDetailHandler());
        backendServer.createContext("/api/removeorderdetail", new RemoveOrderDetailHandler());
        backendServer.createContext("/api/checkforcustomer", new CheckForCustomerHandler());
        backendServer.createContext("/api/viewoneorder", new ViewOneOrderHandler());
        backendServer.createContext("/api/viewordersbyzipcode", new ViewOrdersByZipcodeHandler());
        backendServer.createContext("/api/viewordersbyemployee", new ViewOrdersByEmployeeHandler());
        backendServer.createContext("/api/addemployee", new AddEmployeeHandler());
        backendServer.createContext("/api/showemployees", new ShowEmployeesHandler());
        backendServer.createContext("/api/updateemployee", new UpdateEmployeeHandler());
        backendServer.createContext("/api/showmenu", new ShowMenuHandler());
        backendServer.createContext("/api/calculatecarttotal", new CalculateCartTotalHandler());

        // start the backend server
        System.out.println("Running on port: 8001\n");
        backendServer.start();
    }
}