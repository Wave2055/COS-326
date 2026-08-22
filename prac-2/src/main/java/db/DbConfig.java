package db;

public class DbConfig {

    private final String url;
    private final String user;
    private final String password;

    private DbConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DbConfig fromEnvironment() {
        String host = getEnvOrDefault("PGHOST", "localhost");
        String port = getEnvOrDefault("PGPORT", "5432");
        String database = getEnvOrDefault("PGDATABASE", "aegis_forensics");
        String user = getEnvOrDefault("PGUSER", "postgres");
        String password = getEnvOrDefault("PGPASSWORD", "");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        return new DbConfig(url, user, password);
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
