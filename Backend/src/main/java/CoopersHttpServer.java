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

    static class CreateOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Create Order API Called");
            // Handle requests for "/api/createorder" context
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

                String response;
                try {
                    // create a new CUSTOMER_ORDER record
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(201, 0);

                    // grab the ORDER_NUMBER associated with the above created new CUSTOMER_ORDER
                    // record
                    sqlQuery = "SELECT MAX(ORDER_NUMBER) FROM CUSTOMER_ORDER;";
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    int ORDER_NUMBER = resultSet.getInt("MAX(ORDER_NUMBER)");
                    // System.out.println("ORDER_NUMBER: " + ORDER_NUMBER);

                    // for each ORDER_DETAIL, add a new record
                    for (var detail : createOrder.ORDER_DETAILS) {
                        sqlQuery = "INSERT INTO ORDER_DETAIL (PRODUCT_ID, ORDER_NUMBER, PRICE_PAID, QUANTITY, NOTES) VALUES ("
                                + detail.PRODUCT_ID + ", " + ORDER_NUMBER + ", " + detail.PRICE_PAID + ", "
                                + detail.QUANTITY + ", '" + detail.NOTES + "');";
                        // System.out.println("sqlQuery: " + sqlQuery);
                        SnowFlakeConnector.sendQuery(sqlQuery);
                    }

                    response = "{\"orderSuccessfullySubmitted:\": \"true\"}";
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
                    // query CUSTOMER_ORDER table
                    String sqlQuery = "SELECT * FROM CUSTOMER_ORDER WHERE ORDER_NUMBER = " + orderDetail.ORDER_NUMBER
                            + ";";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    var resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    int EMPLOYEE_ID = resultSet.getInt("EMPLOYEE_ID");
                    String PHONE_NUMBER = resultSet.getString("PHONE_NUMBER");
                    String TIME = resultSet.getString("TIME");

                    // query EMPLOYEE table
                    sqlQuery = "SELECT FIRST_NAME, LAST_NAME FROM EMPLOYEE WHERE EMPLOYEE_ID = " + EMPLOYEE_ID + ";";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
                    String FIRST_NAME = resultSet.getString("FIRST_NAME");
                    String LAST_NAME = resultSet.getString("LAST_NAME");

                    // query
                    sqlQuery = "SELECT ZIPCODE_KEY FROM CUSTOMER WHERE PHONE_NUMBER = '" + PHONE_NUMBER + "';";
                    // System.out.println("sqlQuery: " + sqlQuery);
                    resultSet = SnowFlakeConnector.sendQuery(sqlQuery);
                    resultSet.next();
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

    static class ViewMultipleOrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/api/viewmultipleorders" context
            System.out.println("View Multiple Orders API Called");
            // order_number, employeee first_name last_name (id), time, customer phone
            // number, customer zipcode

            // ...
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
        backendServer.createContext("/api/createorder", new CreateOrderHandler());
        backendServer.createContext("/api/checkforcustomer", new CheckForCustomerHandler());
        backendServer.createContext("/api/viewoneorder", new ViewOneOrderHandler());
        backendServer.createContext("/api/viewmultipleorders", new ViewMultipleOrdersHandler());
        backendServer.createContext("/api/addemployee", new AddEmployeeHandler());
        backendServer.createContext("/api/showemployees", new ShowEmployeesHandler());
        backendServer.createContext("/api/updateemployee", new UpdateEmployeeHandler());

        // start the backend server
        System.out.println("Running on port: 8001\n");
        backendServer.start();
    }
}