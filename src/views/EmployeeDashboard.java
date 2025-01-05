package views;

import controllers.EmployeeService;
import controllers.MongoDBController;
import models.Employee;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class EmployeeDashboard extends JFrame {
    private JPanel mainPanel;
    private JLabel nameLabel;
    private JButton checkInButton;
    private JLabel statusLabel;
    private JButton logoutButton;
    private JTextArea logTextArea;
    private JScrollPane logScrollPane;
    private JPanel statusCirclePanel;

    private final EmployeeService employeeService;
    private final Employee currentEmployee;
    private boolean isCheckedIn = false;

    public EmployeeDashboard(Employee employee, EmployeeService employeeService, boolean isDarkMode) {
        this.currentEmployee = employee;
        this.employeeService = employeeService;

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

        setTitle("Employee Dashboard | Emplo\\Yee");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        nameLabel = new JLabel("    Welcome, " + employee.getName() + " " + employee.getSurname());

        statusCirclePanel = new JPanel();
        statusCirclePanel.setPreferredSize(new Dimension(20, 20));
        statusCirclePanel.setOpaque(true);
        statusCirclePanel.setBackground(Color.RED);

        statusLabel = new JLabel("      Status: Checked Out");
        checkInButton = new JButton("Check In");
        logoutButton = new JButton("Logout");

        topPanel.add(statusCirclePanel);
        topPanel.add(nameLabel);
        topPanel.add(statusLabel);
        topPanel.add(checkInButton);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setLineWrap(true);
        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);
        mainPanel.add(logoutButton, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        loadLogsFromDatabase();

        checkInButton.addActionListener(e -> {
            setEmployeePresence(true);
            String newLog = java.time.LocalDateTime.now().toString() + " | Checked In";
            currentEmployee.addLog(newLog);
            employeeService.updateEmployeeLogs(currentEmployee.getId(), newLog);

            statusLabel.setText("      Status: Checked In");
            checkInButton.setEnabled(false);
            isCheckedIn = true;
            updateLogArea();

            statusCirclePanel.setBackground(Color.GREEN);

            openCheckInPopup();
        });

        logoutButton.addActionListener(e -> {
            if (isCheckedIn) {
                int option = JOptionPane.showConfirmDialog(this, "You need to check out first before logging out.", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                }
            } else {
                setEmployeePresence(false);
                new LoginForm().setVisible(true);
                dispose();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isCheckedIn) {
                    String newLog = java.time.LocalDateTime.now().toString() + " | Force quit - AUTO Checked Out";
                    currentEmployee.addLog(newLog);
                    employeeService.updateEmployeeLogs(currentEmployee.getId(), newLog);
                }
                setEmployeePresence(false);
                MongoDBController.getInstance().closeConnection();
            }
        });
    }

    private void openCheckInPopup() {
        JDialog checkInDialog = new JDialog(this, "Shift in Progress", true);
        checkInDialog.setSize(400, 300);
        checkInDialog.setLocationRelativeTo(this);
        checkInDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("The shift has started! Good luck", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(welcomeLabel);

        contentPanel.add(Box.createVerticalStrut(20));

        JLabel timerLabel = new JLabel("Current work time: 0 minute", SwingConstants.CENTER);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(timerLabel);

        contentPanel.add(Box.createVerticalStrut(20));

        JButton startWorkButton = new JButton("Start with Work");
        startWorkButton.setFont(new Font("Arial", Font.PLAIN, 16));
        startWorkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(startWorkButton);

        contentPanel.add(Box.createVerticalStrut(20));

        JLabel workLeftLabel = new JLabel("Work left: 10 minutes", SwingConstants.CENTER);
        workLeftLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(workLeftLabel);

        JButton minimizeWindowButton = new JButton("Minimize Window");
        minimizeWindowButton.setFont(new Font("Arial", Font.PLAIN, 16));
        minimizeWindowButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        minimizeWindowButton.setVisible(false);
        contentPanel.add(minimizeWindowButton);

        checkInDialog.add(contentPanel, BorderLayout.CENTER);

        JButton endShiftButton = new JButton("End Shift Early");
        endShiftButton.setFont(new Font("Arial", Font.PLAIN, 16));
        endShiftButton.setBackground(Color.RED);
        checkInDialog.add(endShiftButton, BorderLayout.SOUTH);

        checkInDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        checkInDialog.setUndecorated(false);

        final int[] minutes = {0};
        final int[] remainingMinutes = {10};
        final Timer breakReminderTimer = new Timer();
        final boolean[] isOnBreak = {false};
        final Timer[] timer = {new Timer()};
        TimerTask[] workTimerTask = new TimerTask[1];

        workTimerTask[0] = new TimerTask() {
            @Override
            public void run() {
                minutes[0]++;
                remainingMinutes[0]--;
                if (minutes[0] == 1) {
                    timerLabel.setText("Current work time: " + minutes[0] + " minute");
                } else {
                    timerLabel.setText("Current work time: " + minutes[0] + " minutes");
                }
                if (remainingMinutes[0] == 1) {
                    workLeftLabel.setText("Work left: " + remainingMinutes[0] + " minute");
                } else {
                    workLeftLabel.setText("Work left: " + remainingMinutes[0] + " minutes");
                }

                if (minutes[0] >= 10) {
                    timer[0].cancel();
                    JOptionPane.showMessageDialog(checkInDialog, "Congratulations! Your shift is complete.", "Shift Completed", JOptionPane.INFORMATION_MESSAGE);
                    performCheckOutAction("Checked Out");
                    checkInDialog.dispose();
                }
            }
        };
        timer[0].scheduleAtFixedRate(workTimerTask[0], 60000, 60000);

        startWorkButton.addActionListener(e -> {
            if (startWorkButton.getText().equals("Start with Work")) {
                startWorkButton.setBackground(Color.YELLOW);
                startWorkButton.setText("Take a Break");

                welcomeLabel.setText("The shift is still ongoing");

                this.setExtendedState(JFrame.ICONIFIED);

                JOptionPane.showMessageDialog(checkInDialog, "You can start working now. The app is minimized.", "Start Work", JOptionPane.INFORMATION_MESSAGE);

                minimizeWindowButton.setVisible(true);

            } else if (startWorkButton.getText().equals("Take a Break")) {
                int confirm = JOptionPane.showConfirmDialog(checkInDialog, "Are you sure you want to take a break?", "Take a Break", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    startWorkButton.setBackground(Color.GREEN);
                    startWorkButton.setText("Continue with Work");
                    statusCirclePanel.setBackground(Color.YELLOW);
                    statusLabel.setText("      Status: On break");
                    welcomeLabel.setText("The shift is paused");

                    currentEmployee.addLog(LocalDateTime.now().toString() + " | took break");

                    timerLabel.setText("Current work time: " + minutes[0] + " minutes (paused)");

                    workTimerTask[0].cancel();

                    isOnBreak[0] = true;

                    breakReminderTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(checkInDialog, "You're currently on break. Press 'Continue with Work' to resume your shift.", "Break Reminder", JOptionPane.WARNING_MESSAGE);
                        }
                    }, 300000, 300000);

                    JOptionPane.showMessageDialog(checkInDialog, "You are now on break. Press 'Continue with Work' when you're ready to resume.", "Break Started", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(checkInDialog, "Continuing the shift...");
                }

            } else if (startWorkButton.getText().equals("Continue with Work")) {
                currentEmployee.addLog(LocalDateTime.now().toString() + " | continued with work");

                startWorkButton.setBackground(Color.YELLOW);
                startWorkButton.setText("Take a Break");
                statusCirclePanel.setBackground(Color.GREEN);
                statusLabel.setText("      Status: Checked In");
                welcomeLabel.setText("The shift is still ongoing");

                timerLabel.setText("Current work time: " + minutes[0] + " minutes");

                timer[0] = new Timer();
                workTimerTask[0] = new TimerTask() {
                    @Override
                    public void run() {
                        minutes[0]++;
                        remainingMinutes[0]--;
                        if (minutes[0] == 1) {
                            timerLabel.setText("Current work time: " + minutes[0] + " minute");
                        } else {
                            timerLabel.setText("Current work time: " + minutes[0] + " minutes");
                        }
                        workLeftLabel.setText("Work left: " + remainingMinutes[0] + " minutes");

                        if (minutes[0] >= 10) {
                            timer[0].cancel();
                            JOptionPane.showMessageDialog(checkInDialog, "Congratulations! Your shift is complete.", "Shift Completed", JOptionPane.INFORMATION_MESSAGE);
                            performCheckOutAction("Checked Out");
                            checkInDialog.dispose();
                        }
                    }
                };
                timer[0].scheduleAtFixedRate(workTimerTask[0], 60000, 60000);

                breakReminderTimer.cancel();

                isOnBreak[0] = false;

                JOptionPane.showMessageDialog(checkInDialog, "Welcome back from your break! You can continue working now. The app will be minimized.", "Continue Work", JOptionPane.INFORMATION_MESSAGE);
                this.setExtendedState(JFrame.ICONIFIED);
            }
        });

        minimizeWindowButton.addActionListener(e -> this.setExtendedState(JFrame.ICONIFIED));

        endShiftButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(checkInDialog, "Are you sure you want to end the shift early?", "End Shift", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (isOnBreak[0]) {
                    currentEmployee.addLog(LocalDateTime.now().toString() + " | continued with work");
                }

                timer[0].cancel();
                breakReminderTimer.cancel();
                performCheckOutAction("Checked Out before regular work time");
                checkInDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(checkInDialog, "Continuing the shift...");
            }
        });

        checkInDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(checkInDialog, "Are you sure you want to end the shift early?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (isOnBreak[0]) {
                        currentEmployee.addLog(LocalDateTime.now().toString() + " | continued with work");
                    }

                    timer[0].cancel();
                    breakReminderTimer.cancel();
                    performCheckOutAction("Checked Out before regular work time");
                    checkInDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(checkInDialog, "Continuing the shift...");
                    checkInDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                }
            }
        });

        checkInDialog.setAlwaysOnTop(true);
        checkInDialog.setVisible(true);
    }

    private void performCheckOutAction(String logMessage) {
        setEmployeePresence(false);
        String newLog = java.time.LocalDateTime.now().toString() + " | " + logMessage;
        currentEmployee.addLog(newLog);
        employeeService.updateEmployeeLogs(currentEmployee.getId(), newLog);

        statusLabel.setText("      Status: Checked Out");
        checkInButton.setEnabled(true);
        isCheckedIn = false;
        updateLogArea();

        statusCirclePanel.setBackground(Color.RED);
    }

    private void loadLogsFromDatabase() {
        Employee employee = employeeService.getEmployeeById(currentEmployee.getId());
        if (employee != null) {
            List<String> logs = employee.getLogs();
            if (logs != null) {
                currentEmployee.setLogs(logs);
                updateLogArea();
            }
        } else {
            System.err.println("Failed to load logs for employee!");
        }
    }

    private void updateLogArea() {
        logTextArea.setText(String.join("\n", currentEmployee.getLogs()));
    }

    private void setEmployeePresence(boolean isPresent) {
        currentEmployee.setPresent(isPresent);
        employeeService.updateEmployee(currentEmployee.getId(), currentEmployee);
        System.out.println("Employee presence updated to: " + isPresent);
    }
}