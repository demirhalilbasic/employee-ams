package views;

import controllers.EmployeeService;
import controllers.MongoDBController;
import models.Employee;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class ManagerDashboard extends JFrame {
    private JPanel mainPanel;
    private JTable employeeTable;
    private JButton viewLogsButton;
    private JButton logoutButton;
    private JButton exportDataButton;
    private JButton exportAllDataButton;
    private JLabel nameLabel;
    private JButton switchViewButton;
    private JLabel statusLabel;
    private JLabel viewLabel;
    private JButton refreshButton;
    private JLabel refreshLabel;
    private JButton workTimeButton;
    private JButton searchButton;
    private JPanel statusCirclePanel;
    private Timer updateTimer;

    private EmployeeService employeeService;
    private Employee loggedInManager;
    private boolean showingEmployees = true;

    public ManagerDashboard(Employee manager, EmployeeService service, boolean isDarkMode) {
        this.loggedInManager = manager;
        this.employeeService = service;

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

        setTitle("Manager Dashboard | Emplo\\Yee");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        String currentTime = java.time.LocalDateTime.now().toString();
        String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String checkInLog = currentTime + " | Checked In - " + formattedTime;

        loggedInManager.addLog(checkInLog);
        employeeService.updateEmployeeLogs(loggedInManager.getId(), checkInLog);

        refreshManagerLogs();
        System.out.println("Logovi nakon osvežavanja: " + loggedInManager.getLogs());

        setManagerPresence(true);

        mainPanel = new JPanel(new BorderLayout());

        statusCirclePanel = new JPanel();
        statusCirclePanel.setPreferredSize(new Dimension(20, 20));
        statusCirclePanel.setOpaque(true);
        statusCirclePanel.setBackground(Color.GREEN);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nameLabel = new JLabel("      Welcome, " + manager.getName() + " " + manager.getSurname() + "      ");

        statusLabel = new JLabel("Status: Checked In");

        viewLabel = new JLabel("      Current View: Employees      ");

        refreshLabel = new JLabel("Last Update: Few moments ago");

        topPanel.add(nameLabel);
        topPanel.add(statusCirclePanel);
        topPanel.add(statusLabel);
        topPanel.add(viewLabel);
        topPanel.add(refreshLabel);

        employeeTable = new JTable();
        initializeEmployeeTable();
        JScrollPane scrollPane = new JScrollPane(employeeTable);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        searchButton = new JButton("Search");
        workTimeButton = new JButton("Work Time");
        viewLogsButton = new JButton("View Logs");
        exportDataButton = new JButton("Export Selected");
        exportAllDataButton = new JButton("Export All");
        switchViewButton = new JButton("Switch View");
        logoutButton = new JButton("Logout");

        bottomPanel.add(refreshButton);
        bottomPanel.add(searchButton);
        bottomPanel.add(workTimeButton);
        bottomPanel.add(viewLogsButton);
        bottomPanel.add(exportDataButton);
        bottomPanel.add(exportAllDataButton);
        bottomPanel.add(switchViewButton);
        bottomPanel.add(logoutButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        addListeners();

        workTimeButton.setEnabled(false);
        viewLogsButton.setEnabled(false);
        exportDataButton.setEnabled(false);

        refreshButton.addActionListener(e -> refreshTableData());

        updateTimer = new Timer(60000, e -> updateLastUpdateLabel());
        updateTimer.start();
    }

    private void refreshTableData() {
        String role = showingEmployees ? "Employee" : "Manager";
        initializeEmployeeTableWithRole(role);

        employeeTable.setRowSorter(null);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(employeeTable.getModel());
        employeeTable.setRowSorter(sorter);

        refreshLabel.setText("Last Update: Few moments ago");

        String message = showingEmployees ? "Employee data successfully updated." : "Manager data successfully updated.";
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);

        viewLabel.setText("      Current View: " + (showingEmployees ? "Employees      " : "Managers      "));
    }

    private void updateLastUpdateLabel() {
        String currentText = refreshLabel.getText().trim();
        currentText = currentText.replaceAll("\\s+", " ").trim();

        if (currentText.startsWith("Last Update: Few moments ago")) {
            refreshLabel.setText("Last Update: 1 minute ago");
        } else if (currentText.startsWith("Last Update: ")) {
            String[] parts = currentText.split(" ");
            try {
                int minutes = Integer.parseInt(parts[2]);
                minutes++;
                refreshLabel.setText("Last Update: " + minutes + " minutes ago");
            } catch (NumberFormatException e) {
                refreshLabel.setText("Last Update: Few moments ago");
                e.printStackTrace();
            }
        }
    }

    private void initializeEmployeeTable() {
        String[] columnNames = {"ID", "Name", "Surname", "Email", "Position", "Role", "Active"};
        List<Employee> employees = employeeService.getEmployeesByRole("Employee");

        Object[][] data = new Object[employees.size()][7];
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            data[i][0] = emp.getId();
            data[i][1] = emp.getName();
            data[i][2] = emp.getSurname();
            data[i][3] = emp.getEmail();
            data[i][4] = emp.getPosition();
            data[i][5] = emp.getRole();
            data[i][6] = emp.isPresent();
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return Boolean.class;
                return String.class;
            }
        };

        employeeTable.setModel(model);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        employeeTable.setRowSorter(sorter);

        employeeTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());
        employeeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = employeeTable.rowAtPoint(e.getPoint());
                if (row == -1) {
                    employeeTable.clearSelection();
                }
            }
        });

        employeeTable.setDefaultEditor(Object.class, null);

        employeeTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setBackground((boolean) value ? Color.GREEN : Color.RED);
                cell.setForeground(Color.WHITE);
                return cell;
            }
        });

        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                employeeTable.clearSelection();
            }
        });

        viewLabel.setText("      Current View: Employees      ");
        updateButtonState();
    }

    private void addListeners() {
        searchButton.addActionListener(e -> searchEmployee());
        workTimeButton.addActionListener(e -> calculateWorkTimeForEmployee());
        viewLogsButton.addActionListener(e -> viewEmployeeLogs());
        exportDataButton.addActionListener(e -> exportSelectedEmployeeData());
        exportAllDataButton.addActionListener(e -> exportAllEmployeesData());
        switchViewButton.addActionListener(e -> switchTableView());

        logoutButton.addActionListener(e -> {
            String checkOutLog = java.time.LocalDateTime.now().toString() + " | Checked Out";
            loggedInManager.addLog(checkOutLog);
            employeeService.updateEmployeeLogs(loggedInManager.getId(), checkOutLog);

            setManagerPresence(false);
            new LoginForm().setVisible(true);
            dispose();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String checkOutLog = java.time.LocalDateTime.now().toString() + " | Force Quit - AUTO Checked Out";
                loggedInManager.addLog(checkOutLog);
                employeeService.updateEmployeeLogs(loggedInManager.getId(), checkOutLog);
                setManagerPresence(false);
                MongoDBController.getInstance().closeConnection();

            }
        });
    }

    private void searchEmployee() {
        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField surnameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextField positionField = new JTextField(15);

        JCheckBox employeeCheckbox = new JCheckBox("Employee");
        JCheckBox managerCheckbox = new JCheckBox("Manager");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Surname:"), gbc);
        gbc.gridx = 1;
        panel.add(surnameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 1;
        panel.add(positionField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rolePanel.add(employeeCheckbox);
        rolePanel.add(managerCheckbox);
        panel.add(rolePanel, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Search Employee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String email = emailField.getText().trim();
            String position = positionField.getText().trim();
            boolean searchEmployeeRole = employeeCheckbox.isSelected();
            boolean searchManagerRole = managerCheckbox.isSelected();

            List<Employee> searchResults = employeeService.searchEmployees(id, name, surname, email, position, searchEmployeeRole, searchManagerRole, null);
            updateTableData(searchResults);
        }
    }

    private void updateTableData(List<Employee> searchResults) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Name", "Surname", "Email", "Position", "Role", "Active"}, 0);

        for (Employee employee : searchResults) {
            model.addRow(new Object[]{
                    employee.getId(),
                    employee.getName(),
                    employee.getSurname(),
                    employee.getEmail(),
                    employee.getPosition(),
                    employee.getRole(),
                    employee.isPresent() ? "Yes" : "No"
            });
        }

        employeeTable.setModel(model);
        employeeTable.setRowSorter(null);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(employeeTable.getModel());
        employeeTable.setRowSorter(sorter);

        refreshLabel.setText("Last Update: Few moments ago");

        JOptionPane.showMessageDialog(null, "Table data successfully updated with search results.", "Information", JOptionPane.INFORMATION_MESSAGE);

        employeeTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setBackground("Yes".equals(value) ? Color.GREEN : Color.RED);
                cell.setForeground(Color.WHITE);
                return cell;
            }
        });

        employeeTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());

        employeeTable.repaint();

        viewLabel.setText("      Current View: Custom Search Results      ");
    }

    private void calculateWorkTimeForEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) return;

        String email = (String) employeeTable.getValueAt(selectedRow, 3);
        Employee employee = employeeService.getEmployeeByEmail(email);

        if (employee == null) return;

        List<String> logs = employee.getLogs();

        if (!logs.isEmpty() && (logs.get(0).toLowerCase().contains("created") ||
                logs.get(0).toLowerCase().contains("edited") ||
                logs.get(0).toLowerCase().contains("cleared") ||
                logs.get(0).toLowerCase().contains("deleted"))) {
            logs.remove(0);
        }

        logs = logs.stream()
                .filter(log -> log.toLowerCase().contains("checked in") ||
                        log.toLowerCase().contains("checked out") ||
                        log.toLowerCase().contains("took break") ||
                        log.toLowerCase().contains("continued with work") ||
                        log.toLowerCase().contains("force quit"))
                .collect(Collectors.toList());

        if (logs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No work time recorded.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        long totalWorkTimeMinutes = 0;
        long totalBreakTimeMinutes = 0;
        LocalDateTime checkInTime = null;
        LocalDateTime breakStartTime = null;
        LocalDateTime earliestDate = null;
        LocalDateTime latestDate = null;

        for (String log : logs) {
            String timestamp = log.split(" \\|")[0];
            LocalDateTime logTime = LocalDateTime.parse(timestamp);

            if (log.toLowerCase().contains("checked in")) {
                checkInTime = logTime;
                if (earliestDate == null || logTime.isBefore(earliestDate)) {
                    earliestDate = logTime;
                }
            } else if (log.toLowerCase().contains("took break")) {
                if (checkInTime != null) {
                    totalWorkTimeMinutes += java.time.Duration.between(checkInTime, logTime).toMinutes();
                    checkInTime = null;
                }
                breakStartTime = logTime;
            } else if (log.toLowerCase().contains("continued with work")) {
                if (breakStartTime != null) {
                    totalBreakTimeMinutes += java.time.Duration.between(breakStartTime, logTime).toMinutes();
                    breakStartTime = null;
                }
                checkInTime = logTime;
            } else if (log.toLowerCase().contains("checked out") || log.toLowerCase().contains("force quit")) {
                if (checkInTime != null) {
                    totalWorkTimeMinutes += java.time.Duration.between(checkInTime, logTime).toMinutes();
                    checkInTime = null;
                }
                if (latestDate == null || logTime.isAfter(latestDate)) {
                    latestDate = logTime;
                }
            }
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String earliestDateStr = earliestDate != null ? earliestDate.format(dateFormatter) : "N/A";
        String latestDateStr = latestDate != null ? latestDate.format(dateFormatter) : "N/A";

        long workHours = totalWorkTimeMinutes / 60;
        long workMinutes = totalWorkTimeMinutes % 60;
        long breakHours = totalBreakTimeMinutes / 60;
        long breakMinutes = totalBreakTimeMinutes % 60;

        String timeMessage = "";
        String breakMessage = "";
        if (totalWorkTimeMinutes == 0) {
            timeMessage = "No work time recorded.";
        } else if (totalWorkTimeMinutes < 60) {
            timeMessage = String.format("Total working time: %d minute%s", workMinutes, (workMinutes == 1 ? "" : "s"));
        } else if (workMinutes == 0) {
            timeMessage = String.format("Total working time: %d hour%s", workHours, (workHours == 1 ? "" : "s"));
        } else {
            timeMessage = String.format("Total working time: %d hour%s %d minute%s", workHours, (workHours == 1 ? "" : "s"), workMinutes, (workMinutes == 1 ? "" : "s"));
        }

        if (totalBreakTimeMinutes == 0) {
            breakMessage = "No break time recorded.";
        } else if (totalBreakTimeMinutes < 60) {
            breakMessage = String.format("Total break time: %d minute%s", breakMinutes, (breakMinutes == 1 ? "" : "s"));
        } else if (breakMinutes == 0) {
            breakMessage = String.format("Total break time: %d hour%s", breakHours, (breakHours == 1 ? "" : "s"));
        } else {
            breakMessage = String.format("Total break time: %d hour%s %d minute%s", breakHours, (breakHours == 1 ? "" : "s"), breakMinutes, (breakMinutes == 1 ? "" : "s"));
        }

        String role = employee.getRole();
        String name = employee.getName();
        String surname = employee.getSurname();

        StringBuilder message = new StringBuilder();
        if (totalWorkTimeMinutes > 0 || totalBreakTimeMinutes > 0) {
            message.append(String.format("Period: %s / %s\n", earliestDateStr, latestDateStr));
        }
        message.append(String.format("%s\n%s\n", timeMessage, breakMessage));

        if (employee.isPresent()) {
            message.append(String.format("Warning: %s %s %s is currently in a work session that is not included in the total calculation - it will be included after checking out.", role, name, surname));
        }

        JOptionPane.showMessageDialog(this, message.toString(), String.format("Work Time - %s %s %s", role, name, surname), JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewEmployeeLogs() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) return;

        String email = (String) employeeTable.getValueAt(selectedRow, 3);
        Employee employee = employeeService.getEmployeeByEmail(email);

        if (employee == null) return;

        List<String> logs = employee.getLogs();
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        logs.forEach(log -> textArea.append(log + "\n"));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        String role = employee.getRole();
        String name = employee.getName();
        String surname = employee.getSurname();

        JOptionPane.showMessageDialog(this, scrollPane, String.format("%s %s %s Logs", role, name, surname), JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportSelectedEmployeeData() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) return;

        String email = (String) employeeTable.getValueAt(selectedRow, 3);
        Employee employee = employeeService.getEmployeeByEmail(email);

        if (employee == null) return;

        String directoryPath = "ExportedData/Individual_Logs";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String baseFileName = directoryPath + "/" + employee.getName() + "_" + employee.getSurname() + "_Data.txt";
        String fileName = baseFileName;
        int fileIndex = 1;
        while (new File(fileName).exists()) {
            fileName = directoryPath + "/" + employee.getName() + "_" + employee.getSurname() + "_Data_" + fileIndex + ".txt";
            fileIndex++;
        }

        exportEmployeeDataToFile(fileName, employee);
    }

    private void exportAllEmployeesData() {
        JCheckBox employeesCheck = new JCheckBox("Employees");
        JCheckBox managersCheck = new JCheckBox("Managers");
        Object[] options = {employeesCheck, managersCheck, "Confirm"};

        int choice = JOptionPane.showOptionDialog(this, "Select Roles to Export:", "Export Data",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[2]);

        if (choice != 2) return;

        if (!employeesCheck.isSelected() && !managersCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "No data selected for export.", "Export Canceled", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exported = false;
        if (employeesCheck.isSelected() && managersCheck.isSelected()) {
            exported = exportAllLogs();
        } else {
            if (employeesCheck.isSelected()) {
                exported = exportRoleData("Employee", "Employees_Exported_Data");
            }
            if (managersCheck.isSelected()) {
                exported = exportRoleData("Manager", "Managers_Exported_Data") || exported;
            }
        }

        if (exported) {
            String message;
            if (employeesCheck.isSelected() && managersCheck.isSelected()) {
                message = "All Employees and Managers data successfully exported.";
            } else if (employeesCheck.isSelected()) {
                message = "All Employees data successfully exported.";
            } else {
                message = "All Managers data successfully exported.";
            }
            JOptionPane.showMessageDialog(this, message, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean exportAllLogs() {
        String directoryPath = "ExportedData/All_Logs";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = directoryPath + "/All_Logs.txt";
        int fileIndex = 1;
        while (new File(fileName).exists()) {
            fileName = directoryPath + "/All_Logs_" + fileIndex + ".txt";
            fileIndex++;
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            List<Employee> employees = employeeService.getEmployeesByRole("Employee");
            for (Employee emp : employees) {
                writeEmployeeData(writer, emp);
            }
            List<Employee> managers = employeeService.getEmployeesByRole("Manager");
            for (Employee mgr : managers) {
                writeEmployeeData(writer, mgr);
            }
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean exportRoleData(String role, String baseFileName) {
        String directoryPath = "ExportedData/" + (role.equals("Employee") ? "Employees_Exported_Data" : "Managers_Exported_Data");
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = directoryPath + "/" + baseFileName + ".txt";
        int fileIndex = 1;
        while (new File(fileName).exists()) {
            fileName = directoryPath + "/" + baseFileName + "_" + fileIndex + ".txt";
            fileIndex++;
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            List<Employee> employees = employeeService.getEmployeesByRole(role);
            for (Employee emp : employees) {
                writeEmployeeData(writer, emp);
            }
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting " + role.toLowerCase() + " data: " + e.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void exportEmployeeDataToFile(String fileName, Employee employee) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writeEmployeeData(writer, employee);
            String message = employee.getRole().equals("Manager") ?
                    "Manager " + employee.getName() + " data successfully exported to " + fileName + "." :
                    "Employee " + employee.getName() + " data successfully exported to " + fileName + ".";
            JOptionPane.showMessageDialog(this, message, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeEmployeeData(FileWriter writer, Employee employee) throws IOException {
        writer.write("Company Name\n");
        writer.write("Exported On: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        writer.write("Exported By: " + loggedInManager.getName() + " " + loggedInManager.getSurname() + "\n\n");

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

    private void switchTableView() {
        showingEmployees = !showingEmployees;
        employeeTable.clearSelection();

        String role = showingEmployees ? "Employee" : "Manager";
        initializeEmployeeTableWithRole(role);

        viewLabel.setText("      Current View: " + (showingEmployees ? "Employees      " : "Managers      "));
    }

    private void initializeEmployeeTableWithRole(String role) {
        String[] columnNames = {"ID", "Name", "Surname", "Email", "Position", "Role", "Currently Active"};
        List<Employee> employees = employeeService.getEmployeesByRole(role);

        Object[][] data = new Object[employees.size()][7];
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            data[i][0] = emp.getId();
            data[i][1] = emp.getName();
            data[i][2] = emp.getSurname();
            data[i][3] = emp.getEmail();
            data[i][4] = emp.getPosition();
            data[i][5] = emp.getRole();
            data[i][6] = emp.isPresent();
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return Boolean.class;
                return String.class;
            }
        };

        employeeTable.setModel(model);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        employeeTable.setRowSorter(sorter);

        employeeTable.getSelectionModel().addListSelectionListener(e -> updateButtonState());
        employeeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = employeeTable.rowAtPoint(e.getPoint());
                if (row == -1) {
                    employeeTable.clearSelection();
                }
            }
        });

        employeeTable.setDefaultEditor(Object.class, null);

        employeeTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setBackground((boolean) value ? Color.GREEN : Color.RED);
                cell.setForeground(Color.WHITE);
                return cell;
            }
        });

        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                employeeTable.clearSelection();
            }
        });

        updateButtonState();
    }

    private void setStatus(String status, boolean isPresent) {
        statusLabel.setText("Status: " + status);
        statusCirclePanel.setBackground(isPresent ? Color.GREEN : Color.RED);
    }

    private void updateButtonState() {
        boolean hasSelection = employeeTable.getSelectedRow() != -1;
        workTimeButton.setEnabled(hasSelection);
        viewLogsButton.setEnabled(hasSelection);
        exportDataButton.setEnabled(hasSelection);
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void setManagerPresence(boolean isPresent) {
        loggedInManager.setPresent(isPresent);
        employeeService.updateEmployee(loggedInManager.getId(), loggedInManager);
        System.out.println("Manager presence updated to: " + isPresent);
    }

    public void refreshManagerLogs() {
        Employee employee = employeeService.getEmployeeById(loggedInManager.getId());
        if (employee != null) {
            List<String> logs = employee.getLogs();
            loggedInManager.setLogs(logs != null ? logs : new ArrayList<>());
        }
    }

    private void logout() {
        setStatus("Checked Out", false);
        new LoginForm().setVisible(true);
        dispose();
    }
}