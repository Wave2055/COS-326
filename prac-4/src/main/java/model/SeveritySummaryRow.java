package model;

/** One row of the security event summary: a severity level and its event count. */
public class SeveritySummaryRow {

    private final String severity;
    private final int numberOfEvents;

    public SeveritySummaryRow(String severity, int numberOfEvents) {
        this.severity = severity;
        this.numberOfEvents = numberOfEvents;
    }

    public String getSeverity() {
        return severity;
    }

    public int getNumberOfEvents() {
        return numberOfEvents;
    }
}
