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
                    response = "authorized";
                } else {
                    exchange.sendResponseHeaders(401, 0); // unauthorized status code
                    response = "unauthorized";

                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
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

    static class CheckForCustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle requests for "/api/checkforcustomer" context
            if ("POST".equals(exchange.getRequestMethod())) {
                // parse json from frontend
                String requestBodyJsonString = readRequestBody(exchange.getRequestBody());
                JsonStructures.CheckForCustomerJson checkForCustomer = new Gson().fromJson(requestBodyJsonString,
                        JsonStructures.CheckForCustomerJson.class);

                // send query to snowflake to check if PHONE_NUMBER is already in the Customer table
                String sqlQuery = "SELECT * FROM Customer WHERE PHONE_NUMBER = '" + checkForCustomer.PHONE_NUMBER + "';";
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
        backendServer.createContext("/api/checkforcustomer", new CheckForCustomerHandler());
        backendServer.createContext("/api/vieworder", new ViewOrderHandler());
        backendServer.createContext("/api/editemployees", new EditEmployeesHandler());

        // start the backend server
        backendServer.start();
    }
}