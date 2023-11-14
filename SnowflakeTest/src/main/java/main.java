import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class main {
    public static void main(String[] args) {
        try {
            // Replace with your actual Snowflake credentials
            String sfAccount = "YJNIQQS-PM93114";
            String sfUsername = "gracelifi";
            String sfPassword = "CoopersCorp1";
            String sfWarehouse = "COMPUTE_WH";
            String sfDatabase = "PIZZA";
            String sfSchema = "PUBLIC";
            String sfRole = "ACCOUNTADMIN";

            // Load the Snowflake JDBC driver
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");

            // Create a connection
            Connection connection = DriverManager.getConnection(
                    "jdbc:snowflake://" + sfAccount + ".snowflakecomputing.com",
                    sfUsername,
                    sfPassword
            );

            // Set the session parameters (optional)
            Statement statement = connection.createStatement();
            statement.execute("USE WAREHOUSE " + sfWarehouse);
            statement.execute("USE DATABASE " + sfDatabase);
            statement.execute("USE SCHEMA " + sfSchema);
            statement.execute("USE ROLE " + sfRole);

            // Execute your SQL queries
            statement.executeQuery("ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON'");

            String sqlQuery = "SELECT * FROM EMPLOYEE";
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            // Process the result set
            while (resultSet.next()) {
                String columnValue = resultSet.getString("FIRST_NAME");
                System.out.println(columnValue);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}