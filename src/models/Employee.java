package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Employee {
    private final String id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String position;
    private String role;
    private boolean isPresent;
    private List<String> logs;
    private boolean passwordChanged = false;

    public Employee(String id, String name, String surname, String email, String password, String position, String role) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ime ne može biti prazno.");
        }
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Prezime ne može biti prazno.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("Email nije u validnom formatu.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Lozinka ne može biti prazna.");
        }
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("Pozicija ne može biti prazna.");
        }
        if (role == null || (!role.equals("Employee") && !role.equals("Manager") && !role.equals("SuperAdmin"))) {
            throw new IllegalArgumentException("Role mora biti: 'Employee', 'Manager' ili 'SuperAdmin'.");
        }

        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.position = position;
        this.role = role;
        this.isPresent = false;
        this.logs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    public void setPassword(String password) {
        this.password = password;
        this.passwordChanged = true;
    }

    public void resetPasswordChanged() {
        this.passwordChanged = false;
    }

    public String getPosition() { return position; }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }

    public List<String> getLogs() { return logs; }

    public void setLogs(List<String> logs) { this.logs = logs; }

    public void addLog(String action) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        logs.add(action + " - " + timestamp);
    }

    @Override
    public String toString() {
        return id + " - " + name + " " + surname + ",\nPozicija: " + position + ", Uloga: " + role + ", Prisutan: " + isPresent;
    }
}