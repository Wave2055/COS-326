import db.DbConfig;
import db.SqlScriptRunner;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Demo harness for the Aegis Digital Forensics schema. Connects to Postgres
 * via JDBC and runs the four submission .sql scripts in order, printing each
 * statement's result so the DDL, sample data, reports, and trigger tests can
 * all be demonstrated from one command instead of a psql session.
 */
public class Main {

    private static final String SQL_DIR = System.getProperty("user.dir");

    public static void main(String[] args) {
        String stage = args.length > 0 ? args[0].toLowerCase() : "all";

        DbConfig config = DbConfig.fromEnvironment();

        try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword())) {
            connection.setAutoCommit(true);

            if (stage.equals("create")) {
                runScript(connection, "CreateStatements.sql", false);
            } else if (stage.equals("insert")) {
                runScript(connection, "InsertQueries.sql", false);
            } else if (stage.equals("select")) {
                runScript(connection, "SelectQueries.sql", false);
            } else if (stage.equals("test")) {
                runScript(connection, "TestTriggerQueries.sql", true);
            } else if (stage.equals("all")) {
                runScript(connection, "CreateStatements.sql", false);
                runScript(connection, "InsertQueries.sql", false);
                runScript(connection, "SelectQueries.sql", false);
                runScript(connection, "TestTriggerQueries.sql", true);
            } else {
                printUsage();
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runScript(Connection connection, String fileName, boolean continueOnError) {
        Path scriptPath = Path.of(SQL_DIR, fileName);
        System.out.println();
        System.out.println("=== Running " + fileName + " ===");
        SqlScriptRunner.run(connection, scriptPath, continueOnError);
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar u23621372-Moodally-J-Practical2.jar [create|insert|select|test|all]");
        System.out.println("Run from within the prac-2 directory so the .sql files can be found.");
    }
}
