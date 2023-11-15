import java.sql.*;

public class SnowFlakeConnector
{
    public static ResultSet sendQuery(String sqlQuery) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet result = null;

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
            connection = DriverManager.getConnection(
                    "jdbc:snowflake://" + sfAccount + ".snowflakecomputing.com",
                    sfUsername,
                    sfPassword
            );

            // Set the session parameters (optional)
            statement = connection.createStatement();
            statement.execute("USE WAREHOUSE " + sfWarehouse);
            statement.execute("USE DATABASE " + sfDatabase);
            statement.execute("USE SCHEMA " + sfSchema);
            statement.execute("USE ROLE " + sfRole);
            statement.executeQuery("ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON'");

            // send, execute, and return results of requested query 'sqlQuery'
            result = statement.executeQuery(sqlQuery);
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
