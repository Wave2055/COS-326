package db;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.exceptions.Neo4jException;

/**
 * Question 4.1 - holds the single Neo4j driver used by the application.
 *
 * The driver is created once and verified with verifyConnectivity() so that a
 * failure is reported immediately instead of when the first query is run.
 */
public final class Neo4jConnection {

    private static final String URI = "bolt://localhost:7687";
    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "prac5pass";
    private static final String DATABASE_NAME = "COS326Prac5";

    private static Driver driver;

    private Neo4jConnection() {
    }

    /**
     * Opens and verifies the connection to the COS326Prac5 database.
     *
     * @throws Neo4jException if the database cannot be reached.
     */
    public static void connect() throws Neo4jException {
        if (driver != null) {
            return;
        }

        Driver newDriver = GraphDatabase.driver(URI, AuthTokens.basic(USERNAME, PASSWORD));

        try {
            newDriver.verifyConnectivity();
        } catch (Neo4jException e) {
            newDriver.close();
            throw e;
        }

        driver = newDriver;
    }

    /**
     * Opens a session against COS326Prac5. Callers are responsible for closing
     * the session, normally with try-with-resources.
     */
    public static Session openSession() {
        if (driver == null) {
            connect();
        }
        return driver.session(SessionConfig.forDatabase(DATABASE_NAME));
    }

    public static String getUri() {
        return URI;
    }

    public static String getDatabaseName() {
        return DATABASE_NAME;
    }

    /** Closes the driver when the application shuts down. */
    public static void close() {
        if (driver != null) {
            driver.close();
            driver = null;
        }
    }
}
