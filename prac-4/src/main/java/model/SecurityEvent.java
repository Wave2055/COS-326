package model;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * A single security event as stored in the securityEvents collection.
 *
 * The documents are semi-structured, so every field is read defensively -
 * events from different security devices do not all carry the same fields.
 */
public class SecurityEvent {

    private final String eventId;
    private final String timestamp;
    private final String sourceIP;
    private final String destinationIP;
    private final String eventType;
    private final String severity;
    private final String description;
    private final String source;
    private final List<String> tags;
    private final List<String> responseActions;

    public SecurityEvent(Document document) {
        this.eventId = document.getString("eventId");
        this.timestamp = document.getString("timestamp");
        this.sourceIP = document.getString("sourceIP");
        this.destinationIP = document.getString("destinationIP");
        this.eventType = document.getString("eventType");
        this.severity = document.getString("severity");
        this.description = document.getString("description");
        this.source = document.getString("source");
        this.tags = readStringList(document, "tags");
        this.responseActions = readResponseActions(document);
    }

    @SuppressWarnings("unchecked")
    private static List<String> readStringList(Document document, String field) {
        List<String> values = (List<String>) document.get(field);
        return values == null ? new ArrayList<>() : values;
    }

    /** Flattens the embedded response action documents into "action (status)" text. */
    @SuppressWarnings("unchecked")
    private static List<String> readResponseActions(Document document) {
        List<String> actions = new ArrayList<>();
        List<Document> embedded = (List<Document>) document.get("responseActions");

        if (embedded != null) {
            for (Document action : embedded) {
                actions.add(action.getString("action") + " (" + action.getString("status") + ")");
            }
        }
        return actions;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public String getTags() {
        return String.join(", ", tags);
    }

    public String getResponseActions() {
        return String.join(", ", responseActions);
    }
}
