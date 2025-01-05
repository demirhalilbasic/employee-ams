package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BugReport {
    private final String ticketId;
    private final String employeeId;
    private final String dateTime;
    private final String category;
    private final String description;
    private String status;

    public static final String CATEGORY_UI_ISSUE = "User Interface Issue";
    public static final String CATEGORY_FUNCTIONALITY_ISSUE = "Functionality Issue";
    public static final String CATEGORY_PERFORMANCE_ISSUE = "Performance Issue";
    public static final String CATEGORY_OTHER = "Other";

    public static final String STATUS_NOT_RESOLVED = "Not Resolved";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED = "Resolved";

    public BugReport(String employeeId, String category, String description) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        }
        if (category == null || (!category.equals(CATEGORY_UI_ISSUE) && !category.equals(CATEGORY_FUNCTIONALITY_ISSUE) && !category.equals(CATEGORY_PERFORMANCE_ISSUE) && !category.equals(CATEGORY_OTHER))) {
            throw new IllegalArgumentException("Category must be one of the predefined options.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        this.ticketId = UUID.randomUUID().toString();
        this.employeeId = employeeId;
        this.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.category = category;
        this.description = description;
        this.status = STATUS_NOT_RESOLVED;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || (!status.equals(STATUS_NOT_RESOLVED) && !status.equals(STATUS_IN_PROGRESS) && !status.equals(STATUS_RESOLVED))) {
            throw new IllegalArgumentException("Status must be one of the predefined options.");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return "Ticket ID: " + ticketId +
                "\nEmployee ID: " + employeeId +
                "\nDate/Time: " + dateTime +
                "\nCategory: " + category +
                "\nDescription: " + description +
                "\nStatus: " + status;
    }
}
