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

    static class AddCustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Add Customer API Called");
            // Handle requests for "/api/addcustomer" context
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.AddCustomerJson addCustomerJson = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.AddCustomerJson.class);

                String sqlQuery = "MERGE INTO Customer AS target USING (SELECT '" + addCustomerJson.PHONE_NUMBER
                        + "' AS phone_number, '" + addCustomerJson.ADDRESS + "' AS address, '"
                        + addCustomerJson.ZIPCODE_KEY
                        + "' AS zipcode_key) AS source ON target.phone_number = source.phone_number WHEN MATCHED THEN UPDATE SET target.address = source.address, target.zipcode_key = source.zipcode_key WHEN NOT MATCHED THEN INSERT (phone_number, address, zipcode_key) VALUES (source.phone_number, source.address, source.zipcode_key);";

                String response;
                try {
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(201, 0);
                    response = "{\"isAdded:\": \"true\"}";
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

    static class AddCustomerOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Add Customer Order API Called");
            // Handle requests for "/api/addcustomerorder" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // Get the current date and time as a LocalDateTime object
                LocalDateTime now = LocalDateTime.now();

                // Create a formatter with the desired pattern
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.AddCustomerOrderJson addCustomerOrder = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.AddCustomerOrderJson.class);

                String sqlQuery = "INSERT INTO CUSTOMER_ORDER (ORDER_NUMBER, EMPLOYEE_ID, PHONE_NUMBER, TIME) VALUES (ORDER_NUMBER_SEQ.nextval, "
                        +
                        addCustomerOrder.EMPLOYEE_ID + ", '" + addCustomerOrder.PHONE_NUMBER + "', TO_TIMESTAMP_NTZ('"
                        + now.format(formatter) + "'));";
                // System.out.println(sqlQuery);

                String response;
                try {
                    // create a new CUSTOMER_ORDER record
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
                JsonStructures.CreateOrderJson createOrder = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.CreateOrderJson.class);
                // System.out.println(createOrder);

                String sqlQuery = "INSERT INTO CUSTOMER_ORDER (ORDER_NUMBER, EMPLOYEE_ID, PHONE_NUMBER, TIME) VALUES (ORDER_NUMBER_SEQ.nextval, "
                        +
                        createOrder.EMPLOYEE_ID + ", '" + createOrder.PHONE_NUMBER + "', TO_TIMESTAMP_NTZ('"
                        + createOrder.TIME + "'));";
                // System.out.println(sqlQuery);

                  

                // for each ORDER_DETAIL, add a new record
                for (var detail : createOrder.ORDER_DETAILS) {
                    sqlQuery = "INSERT INTO ORDER_DETAIL (PRODUCT_ID, ORDER_NUMBER, PRICE_PAID, QUANTITY, NOTES) VALUES ("
                            + detail.PRODUCT_ID + ", " + ORDER_NUMBER + ", " + detail.PRICE_PAID + ", "
                            + detail.QUANTITY + ", '" + detail.NOTES + "');";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    SnowFlakeConnector.sendQuery(sqlQuery);
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

                StringBuilder response = new StringBuilder();
                // send queries to snowflake
                try {
                    String sqlQuery = "SELECT * FROM CUSTOMER_ORDER O\nJOIN EMPLOYEE E\n\tON E.EMPLOYEE_ID = O.EMPLOYEE_ID\nJOIN CUSTOMER C\n\tON C.PHONE_NUMBER = O.PHONE_NUMBER\nWHERE O.ORDER_NUMBER = "
                            + orderDetail.ORDER_NUMBER + ";";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    int EMPLOYEE_ID = resultSet.getInt("EMPLOYEE_ID");
                    String PHONE_NUMBER = resultSet.getString("PHONE_NUMBER");
                    String TIME = resultSet.getString("TIME");
                    String FIRST_NAME = resultSet.getString("FIRST_NAME");
                    String LAST_NAME = resultSet.getString("LAST_NAME");
                    int ZIPCODE_KEY = resultSet.getInt("ZIPCODE_KEY");

                    // send results
                    response.append("{\n\t\"ORDER_NUMBER:\": " + orderDetail.ORDER_NUMBER + ",");
                    response.append("\n\t\"EMPLOYEE_ID\": " + EMPLOYEE_ID + ",");
                    response.append("\n\t\"FIRST_NAME\": \'" + FIRST_NAME + "',");
                    response.append("\n\t\"LAST_NAME\": \'" + LAST_NAME + "',");
                    response.append("\n\t\"TIME\": \'" + TIME + "',");
                    response.append("\n\t\"PHONE_NUMBER\": \'" + PHONE_NUMBER + "',");
                    response.append("\n\t\"ZIPCODE_KEY\": " + ZIPCODE_KEY + "\n}");
                    exchange.sendResponseHeaders(200, 0);
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.append("{\n\tSQL ERROR\n}");
                    exchange.sendResponseHeaders(404, 0);
                }

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
                    System.out.println("Sent Query");
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(200, 0);
                    response = "{\"isAdded:\": \"true\"}";
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
                    response = "{\"isUpdated:\": \"true\"}";
                } catch (SQLException e) {
                    // Failed response
                    exchange.sendResponseHeaders(422, 0);
                    response = "{\"isUpdated:\": \"false\"}";
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
        backendServer.createContext("/api/addcustomer", new AddCustomerHandler());
        backendServer.createContext("/api/addcustomerorder", new AddCustomerOrderHandler());
        backendServer.createContext("/api/cancelorder", new CancelOrderHandler());
        //backendServer.createContext("/api/addorderdetail", new AddOrderDetailHandler());
        //backendServer.createContext("/api/removeorderdetail", new RemoveOrderDetailHandler());
        backendServer.createContext("/api/checkforcustomer", new CheckForCustomerHandler());
        backendServer.createContext("/api/viewoneorder", new ViewOneOrderHandler());
        backendServer.createContext("/api/viewordersbyzipcode", new ViewOrdersByZipcodeHandler());
        backendServer.createContext("/api/viewordersbyemployee", new ViewOrdersByEmployeeHandler());
        backendServer.createContext("/api/addemployee", new AddEmployeeHandler());
        backendServer.createContext("/api/showemployees", new ShowEmployeesHandler());
        backendServer.createContext("/api/updateemployee", new UpdateEmployeeHandler());
        backendServer.createContext("/api/showmenu", new ShowMenuHandler());

        // start the backend server
        System.out.println("Running on port: 8001\n");
        backendServer.start();
    }
}