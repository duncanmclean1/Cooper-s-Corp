import java.sql.*;

public class SnowFlakeConnector
{
    static Statement snowflakeStatement;

    public static void OpenConnection() {
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
            snowflakeStatement = connection.createStatement();
            snowflakeStatement.execute("USE WAREHOUSE " + sfWarehouse);
            snowflakeStatement.execute("USE DATABASE " + sfDatabase);
            snowflakeStatement.execute("USE SCHEMA " + sfSchema);
            snowflakeStatement.execute("USE ROLE " + sfRole);
            snowflakeStatement.executeQuery("ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet sendQuery(String sqlQuery) throws SQLException {
        try {
            // send, execute, and return results of requested query 'sqlQuery'
            return snowflakeStatement.executeQuery(sqlQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}