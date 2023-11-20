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
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.LoginJson login = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.LoginJson.class);

                // authenticate user
                boolean userIsAuthenticated = false;
                try {
                    userIsAuthenticated = authenticateUser(login.EMPLOYEE_ID, login.PASSWORD);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                // send corresponding response to frontend
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
                    System.out.println("Sent response");
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

                String sqlQuery = "INSERT INTO CUSTOMER VALUES ('" + addCustomerJson.PHONE_NUMBER
                        + "', " + addCustomerJson.ZIPCODE_KEY
                        + ", '" + addCustomerJson.ADDRESS + "');";

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

                String sqlQuery = "INSERT INTO CUSTOMER_ORDER VALUES (ORDER_NUMBER_SEQ.nextval, employee_id, phone_number, date) VALUES (...)";

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

    static class ViewOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/vieworder" context
            // ...
        }
    }

    static class EditEmployeesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/editemployees" context
            // ...
        }
    }

    static class ShowEmployeesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Show Employees API Called");
            if ("GET".equals(exchange.getRequestMethod())) {
                String sqlQuery = "SELECT employee_id, first_name, last_name FROM Employee";
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
                    System.out.println("Sent response");
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
        backendServer.createContext("/api/vieworder", new ViewOrderHandler());
        backendServer.createContext("/api/editemployees", new EditEmployeesHandler());
        backendServer.createContext("/api/addemployee", new AddEmployeeHandler());
        backendServer.createContext("/api/showemployees", new ShowEmployeesHandler());

        // start the backend server
        System.out.println("Running on port: 8001\n");
        backendServer.start();
    }
}