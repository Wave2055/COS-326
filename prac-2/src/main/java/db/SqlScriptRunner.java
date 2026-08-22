package db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a .sql script, splits it into individual statements (respecting
 * dollar-quoted PL/pgSQL bodies, quoted strings and comments), and executes
 * each statement in turn against a live connection.
 */
public class SqlScriptRunner {

    public static void run(Connection connection, Path scriptPath, boolean continueOnError) {
        String content;
        try {
            content = Files.readString(scriptPath);
        } catch (IOException e) {
            System.err.println("Could not read " + scriptPath + ": " + e.getMessage());
            return;
        }

        List<String> statements = splitStatements(content);

        for (String sql : statements) {
            if (sql.isBlank()) {
                continue;
            }

            System.out.println("--> " + firstLine(sql));

            try (Statement statement = connection.createStatement()) {
                boolean hasResultSet = statement.execute(sql);

                if (hasResultSet) {
                    printResultSet(statement.getResultSet());
                } else {
                    int updateCount = statement.getUpdateCount();
                    System.out.println(updateCount >= 0 ? "OK (" + updateCount + " row(s) affected)" : "OK");
                }
            } catch (SQLException e) {
                System.out.println("ERROR: " + e.getMessage().trim());

                if (!continueOnError) {
                    System.err.println("Aborting " + scriptPath.getFileName() + " on first error.");
                    return;
                }
            }

            System.out.println();
        }
    }

    private static String firstLine(String sql) {
        for (String rawLine : sql.split("\n")) {
            String line = rawLine.strip();
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            return line.length() > 100 ? line.substring(0, 100) + "..." : line;
        }
        return sql.strip();
    }

    private static void printResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int columnCount = meta.getColumnCount();

        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            header.append(meta.getColumnLabel(i));
            if (i < columnCount) {
                header.append(" | ");
            }
        }
        System.out.println(header);

        int rowCount = 0;
        while (resultSet.next()) {
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                row.append(resultSet.getString(i));
                if (i < columnCount) {
                    row.append(" | ");
                }
            }
            System.out.println(row);
            rowCount++;
        }
        System.out.println("(" + rowCount + " row(s))");
    }

    /**
     * Splits a script on top-level semicolons, treating text inside
     * '...' strings, $$...$$ / $tag$...$tag$ bodies, and -- / block
     * comments as opaque so semicolons within PL/pgSQL function bodies
     * don't split a statement in two.
     */
    static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inSingleQuote = false;
        String dollarTag = null;

        int length = script.length();
        for (int i = 0; i < length; i++) {
            char c = script.charAt(i);
            char next = (i + 1 < length) ? script.charAt(i + 1) : '\0';

            if (inLineComment) {
                current.append(c);
                inLineComment = c != '\n';
                continue;
            }

            if (inBlockComment) {
                current.append(c);
                if (c == '*' && next == '/') {
                    current.append(next);
                    i++;
                    inBlockComment = false;
                }
                continue;
            }

            if (dollarTag != null) {
                current.append(c);
                if (c == '$' && script.startsWith(dollarTag, i)) {
                    current.append(dollarTag.substring(1));
                    i += dollarTag.length() - 1;
                    dollarTag = null;
                }
                continue;
            }

            if (inSingleQuote) {
                current.append(c);
                if (c == '\'') {
                    if (next == '\'') {
                        current.append(next);
                        i++;
                    } else {
                        inSingleQuote = false;
                    }
                }
                continue;
            }

            if (c == '-' && next == '-') {
                inLineComment = true;
                current.append(c);
                continue;
            }

            if (c == '/' && next == '*') {
                inBlockComment = true;
                current.append(c);
                continue;
            }

            if (c == '\'') {
                inSingleQuote = true;
                current.append(c);
                continue;
            }

            if (c == '$') {
                String tag = matchDollarTag(script, i);
                if (tag != null) {
                    dollarTag = tag;
                    current.append(tag);
                    i += tag.length() - 1;
                    continue;
                }
            }

            if (c == ';') {
                statements.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        if (!current.toString().isBlank()) {
            statements.add(current.toString());
        }

        return statements;
    }

    private static String matchDollarTag(String script, int start) {
        int end = script.indexOf('$', start + 1);
        if (end == -1) {
            return null;
        }

        String candidate = script.substring(start, end + 1);
        for (int i = 1; i < candidate.length() - 1; i++) {
            char c = candidate.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return null;
            }
        }
        return candidate;
    }
}
