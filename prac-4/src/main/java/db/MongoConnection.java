package db;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

/**
 * Question 3.1 - holds the single MongoDB connection used by the application.
 *
 * The connection is opened once and verified with a ping command so that a
 * failure is reported immediately instead of when the first query is run.
 */
public final class MongoConnection {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "cybershield";
    public static final String COLLECTION_NAME = "securityEvents";

    private static MongoClient client;
    private static MongoDatabase database;

    private MongoConnection() {
    }

    /**
     * Opens and verifies the connection to the cybershield database.
     *
     * @throws MongoException if the database cannot be reached.
     */
    public static void connect() throws MongoException {
        if (database != null) {
            return;
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                // Keep the wait short so the GUI can report a failure quickly.
                .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .build();

        MongoClient newClient = MongoClients.create(settings);
        MongoDatabase newDatabase = newClient.getDatabase(DATABASE_NAME);

        try {
            newDatabase.runCommand(new Document("ping", 1));
        } catch (MongoException e) {
            newClient.close();
            throw e;
        }

        client = newClient;
        database = newDatabase;
    }

    /** Returns the connected database, connecting first if required. */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            connect();
        }
        return database;
    }

    public static String getConnectionString() {
        return CONNECTION_STRING;
    }

    public static String getDatabaseName() {
        return DATABASE_NAME;
    }

    /** Closes the connection when the application shuts down. */
    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}
