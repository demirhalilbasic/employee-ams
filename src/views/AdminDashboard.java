package views;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import controllers.EmployeeService;
import controllers.MongoDBController;
import models.BugReport;
import models.Employee;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

import org.bson.Document;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class AdminDashboard extends JFrame {
    private JPanel mainPanel;
    private JButton addEmployeeButton;
    private JButton editEmployeeButton;
    private JButton deleteEmployeeButton;
    private JButton logoutButton;
    private JTextField searchEmployee;
    private JButton refreshButton;
    private JLabel refreshLabel;
    private JLabel nameLabel;
    private JLabel employeesOnlineLabel;
    private JLabel employeesOfflineLabel;
    private JTable employeesTable;
    private JButton feedbackButton;

    private EmployeeService employeeService;
    private DefaultTableModel tableModel;
    private Timer timer;
    private Timer refreshTimer;
    private int updateCounter = 0;
    private Employee superAdmin;

    public AdminDashboard(Employee superAdmin, EmployeeService employeeService, boolean isDarkMode) {
        this.employeeService = employeeService;
        this.superAdmin=superAdmin;

        try {
            InputStream imgStream = getClass().getResourceAsStream("/images/app_taskbar_logo.png");
            if (imgStream != null) {
                Image appIcon = ImageIO.read(imgStream);
                setIconImage(appIcon);
            } else {
                System.err.println("Icon resource not found.");
            }
        } catch (IOException e) {
            System.err.println("Failed to load icon resource.");
            e.printStackTrace();
        }

        if (isDarkMode) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        setTitle("SuperAdmin Dashboard | Emplo\\Yee");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initializeComponents();

        nameLabel.setText("      Welcome, " + superAdmin.getName() + " " + superAdmin.getSurname());
        refreshLabel.setText("      Last Update: Few moments ago");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logLogoutEvent(true);
                MongoDBController.getInstance().closeConnection();
            }
        });

        initializeSuperAdminLog();

        setupTable();
        setupButtons();
        timer = new Timer();
        setupSearch();
        setupRefreshTimer();

        refreshData();
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nameLabel = new JLabel();
        refreshLabel = new JLabel();
        welcomePanel.add(nameLabel);
        welcomePanel.add(refreshLabel);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchEmployee = new JTextField(20);
        refreshButton = new JButton("Refresh");
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchEmployee);
        searchPanel.add(refreshButton);

        topPanel.add(welcomePanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        employeesTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(employeesTable);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        employeesOnlineLabel = new JLabel("Employees online: 0");
        employeesOfflineLabel = new JLabel("Employees offline: 0");
        leftPanel.add(employeesOnlineLabel);
        leftPanel.add(employeesOfflineLabel);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addEmployeeButton = new JButton("Add");
        editEmployeeButton = new JButton("Edit");
        deleteEmployeeButton = new JButton("Delete");
        centerPanel.add(addEmployeeButton);
        centerPanel.add(editEmployeeButton);
        centerPanel.add(deleteEmployeeButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        feedbackButton = new JButton("Feedback");
        logoutButton = new JButton("Logout");
        rightPanel.add(feedbackButton);
        rightPanel.add(logoutButton);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(centerPanel, BorderLayout.CENTER);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupTable() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Surname", "Email", "Password", "Position", "Role", "Currently Active"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 7 ? Boolean.class : String.class;
            }
        };

        employeesTable.setModel(tableModel);
        employeesTable.setRowSorter(new TableRowSorter<>(tableModel));

        employeesTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setBackground((Boolean) value ? Color.GREEN : Color.RED);
                cell.setForeground(Color.WHITE);
                return cell;
            }
        });

        employeesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = employeesTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String employeeId = (String) employeesTable.getValueAt(selectedRow, 0);
                        Employee employee = employeeService.getEmployeeById(employeeId);
                        if (employee != null) {
                            showEmployeeLogsPopup(employee, employeeId, superAdmin);
                        }
                    }
                }
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!employeesTable.getBounds().contains(e.getPoint())) {
                    employeesTable.clearSelection();
                }
            }
        });
    }

    private String generatePasswordMask(int length) {
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < length; i++) {
            mask.append('*');
        }
        return mask.toString();
    }

    private void setupButtons() {
        editEmployeeButton.setEnabled(false);
        deleteEmployeeButton.setEnabled(false);

        employeesTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = employeesTable.getSelectedRow() >= 0;
            editEmployeeButton.setEnabled(rowSelected);
            deleteEmployeeButton.setEnabled(rowSelected);
        });

        addEmployeeButton.addActionListener(e -> new EmployeeService().new AddEmployeePopup(employeeService, this::refreshData, superAdmin));
        editEmployeeButton.addActionListener(e -> editEmployee());
        deleteEmployeeButton.addActionListener(e -> deleteEmployee());
        refreshButton.addActionListener(e -> {
            searchEmployee.setText("");
            refreshData();
            employeesTable.setRowSorter(null);
            employeesTable.setAutoCreateRowSorter(true);
            JOptionPane.showMessageDialog(null, "Data successfully updated.", "Information", JOptionPane.INFORMATION_MESSAGE);
        });
        feedbackButton.addActionListener(e -> showBugReportsPopup());
        logoutButton.addActionListener(e -> {
            logLogoutEvent(false);
            new LoginForm().setVisible(true);
            dispose();
        });
    }

    private void setupSearch() {
        searchEmployee.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                startSearchTimer();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                startSearchTimer();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                startSearchTimer();
            }

            private void startSearchTimer() {
                timer.cancel();
                timer = new Timer();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        filterTable();
                    }
                }, 1000);
            }

            private void filterTable() {
                String query = searchEmployee.getText().toLowerCase();
                List<Employee> filteredEmployees = employeeService.getAllEmployees().stream()
                        .filter(emp -> emp.getName().toLowerCase().contains(query) || emp.getSurname().toLowerCase().contains(query))
                        .collect(Collectors.toList());
                updateTable(filteredEmployees);
            }
        });
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    updateCounter++;
                    if (updateCounter < 3) {
                        updateRefreshLabel();
                    } else {
                        String currentQuery = searchEmployee.getText().toLowerCase();
                        refreshData();
                        if (!currentQuery.isEmpty()) {
                            filterTable(currentQuery);
                        }
                        refreshLabel.setText("      Last Update: Few moments ago");
                        updateCounter = 0;
                    }
                });
            }
        }, 60000, 60000);
    }

    private void updateRefreshLabel() {
        switch (updateCounter) {
            case 0 -> refreshLabel.setText("      Last Update: Few moments ago");
            case 1 -> refreshLabel.setText("      Last Update: 1 minute ago");
            case 2 -> refreshLabel.setText("      Last Update: 2 minutes ago");
        }
    }

    private void refreshData() {
        refreshLabel.setText("      Last Update: Few moments ago");
        updateCounter = 0;
        List<Employee> employees = employeeService.getAllEmployees();
        updateTable(employees);
        long onlineCount = employees.stream().filter(Employee::isPresent).count();
        employeesOnlineLabel.setText("      Employees online: " + onlineCount);
        employeesOfflineLabel.setText("Employees offline: " + (employees.size() - onlineCount));
    }

    private void filterTable(String query) {
        List<Employee> filteredEmployees = employeeService.getAllEmployees().stream()
                .filter(emp -> emp.getName().toLowerCase().contains(query) || emp.getSurname().toLowerCase().contains(query))
                .collect(Collectors.toList());
        updateTable(filteredEmployees);
    }

    private void updateTable(List<Employee> employees) {
        tableModel.setRowCount(0);
        for (Employee emp : employees) {
            String maskedPassword = generatePasswordMask(10);
            tableModel.addRow(new Object[]{emp.getId(), emp.getName(), emp.getSurname(), emp.getEmail(), maskedPassword, emp.getPosition(), emp.getRole(), emp.isPresent()});
        }

        employeesTable.setRowSorter(new TableRowSorter<>(tableModel));
    }

    private void editEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String employeeId = tableModel.getValueAt(selectedRow, 0).toString();
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee != null) {
                new EmployeeService().new EditEmployeePopup(employee, employeeService, this::refreshData, superAdmin);
            }
        }
    }

    private void deleteEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String employeeId = tableModel.getValueAt(selectedRow, 0).toString();

            if (employeeId.equals(superAdmin.getId())) {
                JOptionPane.showMessageDialog(this, "SuperAdmin cannot delete themselves!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Employee employee = employeeService.getEmployeeById(employeeId);
                if (employee != null) {
                    String role = employee.getRole();
                    String logEntry = role + " " + employee.getName() + " " + employee.getSurname() + " deleted by " + superAdmin.getName() + " " + superAdmin.getSurname();
                    superAdmin.addLog(logEntry);
                    employeeService.updateEmployee(superAdmin.getId(), superAdmin);
                }
                employeeService.deleteEmployee(employeeId);
                refreshData();
            }
        }
    }

    private void showEmployeeLogsPopup(Employee employee, String employeeId, Employee superAdmin) {
        List<String> logs = employee.getLogs();
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        logs.forEach(log -> textArea.append(log + "\n"));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JButton clearLogsButton = new JButton("Clear Logs");
        JButton cancelButton = new JButton("Cancel");

        clearLogsButton.addActionListener(e -> {
            int exportConfirmation = JOptionPane.showConfirmDialog(this, "Would you like to export logs for " +
                    employee.getName() + " " + employee.getSurname() + " before clearing them?", "Export Logs", JOptionPane.YES_NO_OPTION);
            if (exportConfirmation == JOptionPane.YES_OPTION) {
                String fileName = employee.getName() + "_" + employee.getSurname() + "_Data.txt";
                exportEmployeeDataToFile(fileName, employee, superAdmin);
            }

            int clearConfirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear logs for " +
                    employee.getName() + " " + employee.getSurname() + "?", "Confirm Clear Logs", JOptionPane.YES_NO_OPTION);
            if (clearConfirmation == JOptionPane.YES_OPTION) {
                employee.setLogs(new ArrayList<>());
                String clearLogEntry = "Logs cleared by " + superAdmin.getName() + " " + superAdmin.getSurname() + " on " +
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                employee.addLog(clearLogEntry);
                employeeService.updateEmployee(employeeId, employee);
                textArea.setText(clearLogEntry + "\n");
            }
        });

        cancelButton.addActionListener(e -> ((JDialog) SwingUtilities.getWindowAncestor((Component) e.getSource())).dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearLogsButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        String role = employee.getRole();
        String title = role + " " + employee.getName() + " " + employee.getSurname() + " Logs";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.getContentPane().add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void exportEmployeeDataToFile(String fileName, Employee employee, Employee superAdmin) {
        String directoryPath = "ExportedData/Individual_Logs";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String baseFileName = directoryPath + "/" + employee.getName() + "_" + employee.getSurname() + "_Data.txt";
        String fullPath = baseFileName;
        int fileIndex = 1;
        while (new File(fullPath).exists()) {
            fullPath = directoryPath + "/" + employee.getName() + "_" + employee.getSurname() + "_Data_" + fileIndex + ".txt";
            fileIndex++;
        }

        try (FileWriter writer = new FileWriter(fullPath)) {
            writeEmployeeData(writer, employee, superAdmin);
            JOptionPane.showMessageDialog(this, "Data exported successfully to " + fullPath, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeEmployeeData(FileWriter writer, Employee employee, Employee superAdmin) throws IOException {
        writer.write("Company Name\n");
        writer.write("Exported On: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        writer.write("Exported By: " + superAdmin.getName() + " " + superAdmin.getSurname() + "\n\n");

        writer.write("Employee Data:\n");
        writer.write("Name: " + employee.getName() + "\n");
        writer.write("Surname: " + employee.getSurname() + "\n");
        writer.write("Email: " + employee.getEmail() + "\n");
        writer.write("Position: " + employee.getPosition() + "\n");
        writer.write("Is Present: " + (employee.isPresent() ? "Yes" : "No") + "\n\n");

        writer.write("Logs:\n");
        for (String log : employee.getLogs()) {
            writer.write(log + "\n");
        }
        writer.write("\n");
    }

    private void showBugReportsPopup() {
        try {
            MongoDatabase database = MongoDBController.getInstance().getDatabase();
            if (database == null) {
                JOptionPane.showMessageDialog(this, "Nema konekcije sa bazom!", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MongoCollection<Document> bugReportsCollection = database.getCollection("bug_reports");
            FindIterable<Document> bugReports = bugReportsCollection.find().sort(Sorts.descending("dateTime"));

            DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Ticket ID", "Date", "Category", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            for (Document report : bugReports) {
                String ticketId = report.getString("ticketId");
                String date = report.getString("dateTime");
                String category = report.getString("category");
                String status = report.getString("status");
                tableModel.addRow(new Object[]{ticketId, date, category, status});
            }

            table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow != -1) {
                            String ticketId = (String) tableModel.getValueAt(selectedRow, 0);
                            showBugReportDetailsPopup(ticketId, tableModel, selectedRow);
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);

            JDialog dialog = new JDialog((Frame) null, "Bug Reports", true);
            dialog.add(panel);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error retrieving bug reports: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showBugReportDetailsPopup(String ticketId, DefaultTableModel tableModel, int rowIndex) {
        try {
            MongoDatabase database = MongoDBController.getInstance().getDatabase();
            if (database == null) {
                JOptionPane.showMessageDialog(this, "Nema konekcije sa bazom!", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MongoCollection<Document> bugReportsCollection = database.getCollection("bug_reports");
            Document report = bugReportsCollection.find(new Document("ticketId", ticketId)).first();

            if (report == null) {
                JOptionPane.showMessageDialog(this, "Report not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String employeeId = report.getString("employeeId");
            MongoCollection<Document> employeesCollection = database.getCollection(MongoDBController.getCollectionName());
            Document employeeDoc = employeesCollection.find(new Document("id", employeeId)).first();

            if (employeeDoc == null) {
                JOptionPane.showMessageDialog(this, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String employeeName = employeeDoc.getString("name");
            String employeeSurname = employeeDoc.getString("surname");
            String employeeRole = employeeDoc.getString("role");

            String rolePrefix = "";
            if ("Manager".equals(employeeRole)) {
                rolePrefix = "Manager ";
            } else if ("Employee".equals(employeeRole)) {
                rolePrefix = "Employee ";
            }

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Ticket ID: " + report.getString("ticketId")));
            panel.add(new JLabel(rolePrefix + employeeName + " " + employeeSurname));
            panel.add(new JLabel("Date/Time: " + report.getString("dateTime")));
            panel.add(new JLabel("Category: " + report.getString("category")));
            panel.add(new JLabel("Description: " + report.getString("description")));
            panel.add(new JLabel("Status: "));

            JComboBox<String> statusComboBox = new JComboBox<>(new String[]{
                    BugReport.STATUS_NOT_RESOLVED,
                    BugReport.STATUS_IN_PROGRESS,
                    BugReport.STATUS_RESOLVED
            });
            statusComboBox.setRenderer(new StatusListCellRenderer());
            statusComboBox.setSelectedItem(report.getString("status"));
            panel.add(statusComboBox);

            int result = JOptionPane.showConfirmDialog(null, panel, "Bug Report Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newStatus = (String) statusComboBox.getSelectedItem();
                bugReportsCollection.updateOne(new Document("ticketId", ticketId), new Document("$set", new Document("status", newStatus)));
                tableModel.setValueAt(newStatus, rowIndex, 3);
                JOptionPane.showMessageDialog(this, "Status updated successfully!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error retrieving bug report details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value.toString();
            if (status.equals(BugReport.STATUS_NOT_RESOLVED)) {
                cell.setBackground(Color.RED);
            } else if (status.equals(BugReport.STATUS_IN_PROGRESS)) {
                cell.setBackground(Color.YELLOW);
            } else if (status.equals(BugReport.STATUS_RESOLVED)) {
                cell.setBackground(Color.GREEN);
            }
            return cell;
        }
    }

    class StatusListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component cell = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String status = value.toString();
            if (status.equals(BugReport.STATUS_NOT_RESOLVED)) {
                cell.setBackground(Color.RED);
            } else if (status.equals(BugReport.STATUS_IN_PROGRESS)) {
                cell.setBackground(Color.YELLOW);
            } else if (status.equals(BugReport.STATUS_RESOLVED)) {
                cell.setBackground(Color.GREEN);
            }
            return cell;
        }
    }

    private void initializeSuperAdminLog() {
        if (superAdmin == null) {
            throw new IllegalStateException("SuperAdmin is not initialized.");
        }
        String checkInLog = LocalDateTime.now() + " | Logged In";
        superAdmin.addLog(checkInLog);
        employeeService.updateEmployeeLogs(superAdmin.getId(), checkInLog);

        refreshSuperAdminLogs();
        setSuperAdminPresence(true);
    }

    private void refreshSuperAdminLogs() {
        Employee employee = employeeService.getEmployeeById(superAdmin.getId());
        if (employee != null) {
            List<String> logs = employee.getLogs();
            superAdmin.setLogs(logs != null ? logs : new ArrayList<>());
        }
    }

    private void setSuperAdminPresence(boolean isPresent) {
        superAdmin.setPresent(isPresent);
        employeeService.updateEmployee(superAdmin.getId(), superAdmin);
    }

    private void logLogoutEvent(boolean isForced) {
        String logoutLog = isForced
                ? LocalDateTime.now() + " | Force Quit"
                : LocalDateTime.now() + " | Logged Out";

        if (superAdmin != null) {
            superAdmin.addLog(logoutLog);
            employeeService.updateEmployeeLogs(superAdmin.getId(), logoutLog);

            refreshSuperAdminLogs();
            setSuperAdminPresence(false);
        }
    }
}