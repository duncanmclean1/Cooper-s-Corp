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
        String sqlQuery = "SELECT * FROM Employee WHERE EMPLOYEE_ID = " + employee_ID + " AND PASSWORD = '" + password
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
            System.out.println("Login Handler API called");
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
                    response = "{\"isAuthorized\": \"true\"}";
                } else {
                    exchange.sendResponseHeaders(401, 0); // unauthorized status code
                    response = "{\"isAuthorized\": \"false\"}";

                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    System.out.println("Sent response");
                }
            }
        }
    }

    static class CreateOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/createorder" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.CreateOrderJson createOrder = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.CreateOrderJson.class);
                System.out.println(createOrder);
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

                String sqlQuery = "INSERT INTO Employee VALUES (" + addEmployee.EMPLOYEE_ID + ", '"
                        + addEmployee.FIRST_NAME + "', '" + addEmployee.LAST_NAME + "', 'active', '"
                        + addEmployee.PASSWORD + "');";

                String response;

                try {
                    System.out.println("Sent Query");
                    SnowFlakeConnector.sendQuery(sqlQuery);
                    exchange.sendResponseHeaders(200, 0);
                    response = "Employee added";
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(422, 0);
                    response = "SQL error";
                    e.printStackTrace();
                }

               // System.out.println("Query executed");

                
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
        backendServer.createContext("/api/createorder", new CreateOrderHandler());
        // backendServer.createContext("/api/vieworder", new ViewOrderHandler());
        // backendServer.createContext("/api/editemployees", new
        // EditEmployeesHandler());
        backendServer.createContext("/api/addemployee", new AddEmployeeHandler());

        // start the backend server
        System.out.println("Running on port: 8001\n");
        backendServer.start();
    }
}