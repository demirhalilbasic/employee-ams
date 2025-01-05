package controllers;

import models.Employee;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.mindrot.jbcrypt.BCrypt;

public class EmployeeService {
    private final MongoCollection<Document> employeeCollection;

    public EmployeeService() {
        MongoDatabase database = MongoDBController.getInstance().getDatabase();
        this.employeeCollection = database.getCollection("employees");
    }

    public class AddEmployeePopup extends JDialog {
        private JTextField nameField;
        private JTextField surnameField;
        private JTextField emailField;
        private JTextField passwordField;
        private JTextField positionField;
        private JRadioButton employeeRole;
        private JRadioButton managerRole;
        private JButton saveButton;
        private JButton cancelButton;
        private EmployeeService employeeService;
        private Runnable onCloseCallback;
        private Employee superAdmin;

        public AddEmployeePopup(EmployeeService employeeService, Runnable onCloseCallback, Employee superAdmin) {
            this.employeeService = employeeService;
            this.onCloseCallback = onCloseCallback;
            this.superAdmin = superAdmin;
            setTitle("Add Employee");
            setSize(400, 350);
            setLayout(new GridBagLayout());
            setLocationRelativeTo(null);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            nameField = new JTextField(20);
            surnameField = new JTextField(20);
            emailField = new JTextField(20);
            passwordField = new JTextField(20);
            positionField = new JTextField(20);
            employeeRole = new JRadioButton("Employee");
            managerRole = new JRadioButton("Manager");
            ButtonGroup roleGroup = new ButtonGroup();
            roleGroup.add(employeeRole);
            roleGroup.add(managerRole);
            employeeRole.setSelected(true);
            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");

            add(new JLabel("Name:"), gbc);
            gbc.gridx = 1;
            add(nameField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Surname:"), gbc);
            gbc.gridx = 1;
            add(surnameField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            add(emailField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Position:"), gbc);
            gbc.gridx = 1;
            add(positionField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Role:"), gbc);
            gbc.gridx = 1;
            add(employeeRole, gbc);

            gbc.gridy++;
            add(managerRole, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            add(saveButton, gbc);

            gbc.gridy++;
            add(cancelButton, gbc);

            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String email = emailField.getText();
                    String password = passwordField.getText();
                    String name = nameField.getText();
                    String surname = surnameField.getText();
                    String position = positionField.getText();

                    if (!isValidEmail(email)) {
                        JOptionPane.showMessageDialog(null, "Email nije u validnom formatu. Molimo unesite ispravan email.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isValidPassword(password)) {
                        JOptionPane.showMessageDialog(null, "Šifra mora imati veliko, malo slovo i jedan simbol. Molimo unesite ispravnu šifru.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isValidName(name)) {
                        JOptionPane.showMessageDialog(null, "Ime mora počinjati velikim slovom. Molimo unesite ispravno ime.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isValidSurname(surname)) {
                        JOptionPane.showMessageDialog(null, "Prezime mora počinjati velikim slovom. Molimo unesite ispravno prezime.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isValidPosition(position)) {
                        JOptionPane.showMessageDialog(null, "Radna pozicija mora počinjati velikim slovom. Molimo unesite ispravnu radnu poziciju.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String role = employeeRole.isSelected() ? "Employee" : "Manager";

                    Employee newEmployee = new Employee(
                            UUID.randomUUID().toString(),
                            name,
                            surname,
                            email,
                            password,
                            position,
                            role
                    );

                    String logEntry = role + " created by " + superAdmin.getName() + " " + superAdmin.getSurname() + " on " +
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    newEmployee.getLogs().add(logEntry);

                    employeeService.addEmployee(newEmployee);
                    onCloseCallback.run();
                    dispose();
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            setVisible(true);
        }

        private boolean isValidEmail(String email) {
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            Pattern pattern = Pattern.compile(emailRegex);
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
        }

        private boolean isValidPassword(String password) {
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,30}$";
            Pattern pattern = Pattern.compile(passwordRegex);
            Matcher matcher = pattern.matcher(password);
            return matcher.matches();
        }

        private boolean isValidName(String name) {
            return Character.isUpperCase(name.charAt(0));
        }

        private boolean isValidSurname(String surname) {
            return Character.isUpperCase(surname.charAt(0));
        }

        private boolean isValidPosition(String position) {
            return Character.isUpperCase(position.charAt(0));
        }
    }

    public class EditEmployeePopup extends JDialog {
        private JTextField nameField;
        private JTextField surnameField;
        private JTextField emailField;
        private JTextField passwordField;
        private JTextField positionField;
        private JRadioButton employeeRole;
        private JRadioButton managerRole;
        private JRadioButton superAdminRole;
        private JButton saveButton;
        private JButton cancelButton;
        private EmployeeService employeeService;
        private Employee employee;
        private Runnable onCloseCallback;
        private Employee superAdmin;

        public EditEmployeePopup(Employee employee, EmployeeService employeeService, Runnable onCloseCallback, Employee superAdmin) {
            this.employee = employee;
            this.employeeService = employeeService;
            this.onCloseCallback = onCloseCallback;
            this.superAdmin = superAdmin;
            setTitle("Edit Employee");
            setSize(400, 400);
            setLayout(new GridBagLayout());
            setLocationRelativeTo(null);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            nameField = new JTextField(employee.getName(), 20);
            surnameField = new JTextField(employee.getSurname(), 20);
            emailField = new JTextField(employee.getEmail(), 20);
            passwordField = new JTextField(20);
            PromptSupport.setPrompt("Leave empty to keep the current PW", passwordField);
            positionField = new JTextField(employee.getPosition(), 20);
            employeeRole = new JRadioButton("Employee");
            managerRole = new JRadioButton("Manager");
            superAdminRole = new JRadioButton("SuperAdmin");

            ButtonGroup roleGroup = new ButtonGroup();
            roleGroup.add(employeeRole);
            roleGroup.add(managerRole);
            roleGroup.add(superAdminRole);

            if (employee.getRole().equalsIgnoreCase("Manager")) {
                managerRole.setSelected(true);
            } else if (employee.getRole().equalsIgnoreCase("SuperAdmin")) {
                superAdminRole.setSelected(true);
            } else {
                employeeRole.setSelected(true);
            }

            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");

            add(new JLabel("Name:"), gbc);
            gbc.gridx = 1;
            add(nameField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Surname:"), gbc);
            gbc.gridx = 1;
            add(surnameField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            add(emailField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Position:"), gbc);
            gbc.gridx = 1;
            add(positionField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Role:"), gbc);
            gbc.gridx = 1;
            JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            rolePanel.add(employeeRole);
            rolePanel.add(managerRole);
            if (employee.getRole().equalsIgnoreCase("SuperAdmin")) {
                rolePanel.add(superAdminRole);
                employeeRole.setEnabled(true);
                managerRole.setEnabled(true);
                superAdminRole.setEnabled(true);
            }
            add(rolePanel, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            add(saveButton, gbc);

            gbc.gridy++;
            add(cancelButton, gbc);

            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String email = emailField.getText();
                    String password = passwordField.getText();
                    String name = nameField.getText();
                    String surname = surnameField.getText();
                    String position = positionField.getText();

                    if (!isValidEmail(email)) {
                        JOptionPane.showMessageDialog(null, "Email nije u validnom formatu. Molimo unesite ispravan email.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isValidName(name)) {
                        JOptionPane.showMessageDialog(null, "Ime mora počinjati velikim slovom. Molimo unesite ispravno ime.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isValidSurname(surname)) {
                        JOptionPane.showMessageDialog(null, "Prezime mora počinjati velikim slovom. Molimo unesite ispravno prezime.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isValidPosition(position)) {
                        JOptionPane.showMessageDialog(null, "Radna pozicija mora počinjati velikim slovom. Molimo unesite ispravnu radnu poziciju.", "Greška", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String role = employeeRole.isSelected() ? "Employee" : (managerRole.isSelected() ? "Manager" : "SuperAdmin");
                    employee.setName(name);
                    employee.setSurname(surname);
                    employee.setEmail(email);
                    employee.setPosition(position);
                    employee.setRole(role);

                    if (!password.isEmpty()) {
                        if (!isValidPassword(password)) {
                            JOptionPane.showMessageDialog(null, "Šifra mora imati 8-30 karaktera, veliko, malo slovo, broj i specijalni karakter. Molimo unesite ispravnu šifru.", "Greška", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        employee.setPassword(password);
                    }

                    String logEntry = "Edited by " + superAdmin.getName() + " " + superAdmin.getSurname();
                    employee.addLog(logEntry);

                    employeeService.updateEmployee(employee.getId(), employee);
                    onCloseCallback.run();
                    dispose();
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            setVisible(true);
        }

        private boolean isValidEmail(String email) {
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            Pattern pattern = Pattern.compile(emailRegex);
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
        }

        private boolean isValidPassword(String password) {
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,30}$";
            Pattern pattern = Pattern.compile(passwordRegex);
            Matcher matcher = pattern.matcher(password);
            return matcher.matches();
        }

        private boolean isValidName(String name) {
            return Character.isUpperCase(name.charAt(0));
        }

        private boolean isValidSurname(String surname) {
            return Character.isUpperCase(surname.charAt(0));
        }

        private boolean isValidPosition(String position) {
            return Character.isUpperCase(position.charAt(0));
        }
    }

    public void addEmployee(Employee employee) {
        String hashedPassword = BCrypt.hashpw(employee.getPassword(), BCrypt.gensalt());

        Document doc = new Document("_id", new ObjectId())
                .append("id", employee.getId())
                .append("name", employee.getName())
                .append("surname", employee.getSurname())
                .append("email", employee.getEmail())
                .append("password", hashedPassword)
                .append("position", employee.getPosition())
                .append("role", employee.getRole())
                .append("isPresent", employee.isPresent())
                .append("logs", employee.getLogs());

        employeeCollection.insertOne(doc);
        System.out.println("Employee added successfully!");
    }

    public Employee getEmployeeById(String employeeId) {
        Document doc = employeeCollection.find(Filters.eq("id", employeeId)).first();
        if (doc != null) {
            return convertDocumentToEmployee(doc);
        }
        return null;
    }

    public Employee getEmployeeByEmail(String email) {
        Document doc = employeeCollection.find(Filters.eq("email", email)).first();
        if (doc != null) {
            return convertDocumentToEmployee(doc);
        }
        return null;
    }

    public List<Employee> getEmployeesByRole(String role) {
        List<Employee> employees = new ArrayList<>();
        for (Document doc : employeeCollection.find(Filters.eq("role", role))) {
            employees.add(convertDocumentToEmployee(doc));
        }
        return employees;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        for (Document doc : employeeCollection.find()) {
            employees.add(convertDocumentToEmployee(doc));
        }
        return employees;
    }

    public List<Employee> searchEmployees(String id, String name, String surname, String email, String position, boolean searchEmployeeRole, boolean searchManagerRole, Boolean isPresent) {
        List<Bson> filters = new ArrayList<>();

        if (id != null && !id.isEmpty()) {
            filters.add(Filters.eq("id", id));
        }
        if (name != null && !name.isEmpty()) {
            filters.add(Filters.regex("name", name, "i"));
        }
        if (surname != null && !surname.isEmpty()) {
            filters.add(Filters.regex("surname", surname, "i"));
        }
        if (email != null && !email.isEmpty()) {
            filters.add(Filters.regex("email", email, "i"));
        }
        if (position != null && !position.isEmpty()) {
            filters.add(Filters.regex("position", position, "i"));
        }
        if (searchEmployeeRole && !searchManagerRole) {
            filters.add(Filters.eq("role", "Employee"));
        } else if (!searchEmployeeRole && searchManagerRole) {
            filters.add(Filters.eq("role", "Manager"));
        }
        if (isPresent != null) {
            filters.add(Filters.eq("active", isPresent));
        }

        Bson filter = filters.isEmpty() ? new Document() : Filters.and(filters);
        List<Employee> employees = new ArrayList<>();
        for (Document doc : employeeCollection.find(filter)) {
            employees.add(convertDocumentToEmployee(doc));
        }
        return employees;
    }

    public Employee convertDocumentToEmployee(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Provided document is null");
        }
        Employee employee = new Employee(
                doc.getString("id"),
                doc.getString("name"),
                doc.getString("surname"),
                doc.getString("email"),
                doc.getString("password"),
                doc.getString("position"),
                doc.getString("role")
        );
        employee.setPresent(doc.getBoolean("isPresent", false));
        employee.setLogs(doc.getList("logs", String.class));
        return employee;
    }

    public void updateEmployee(String employeeId, Employee employee) {
        Document currentDoc = employeeCollection.find(Filters.eq("id", employeeId)).first();
        String currentHashedPassword = currentDoc.getString("password");

        String newHashedPassword;
        if (employee.isPasswordChanged()) {
            newHashedPassword = BCrypt.hashpw(employee.getPassword(), BCrypt.gensalt());
            employee.resetPasswordChanged();
        } else {
            newHashedPassword = currentHashedPassword;
        }

        Document updatedDoc = new Document()
                .append("name", employee.getName())
                .append("surname", employee.getSurname())
                .append("email", employee.getEmail())
                .append("password", newHashedPassword)
                .append("position", employee.getPosition())
                .append("role", employee.getRole())
                .append("isPresent", employee.isPresent())
                .append("logs", employee.getLogs());

        employeeCollection.updateOne(Filters.eq("id", employeeId), new Document("$set", updatedDoc));
        System.out.println("Employee updated successfully!");
    }

    public void updateEmployeeLogs(String employeeId, String newLog) {
        Document employeeDoc = employeeCollection.find(Filters.eq("id", employeeId)).first();

        if (employeeDoc != null) {
            employeeCollection.updateOne(
                    Filters.eq("id", employeeId),
                    new Document("$push", new Document("logs", newLog))
            );
            System.out.println("Employee logs updated successfully!");
        } else {
            System.err.println("Employee with ID " + employeeId + " not found!");
        }
    }

    public void deleteEmployee(String employeeId) {
        employeeCollection.deleteOne(Filters.eq("id", employeeId));
        System.out.println("Employee deleted successfully!");
    }
}