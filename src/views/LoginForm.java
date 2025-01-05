/*
| Emplo\Yee App                                         |
| Attendance Management System                          |
| Predmet: Programiranje u Javi                         |
| Na projektu radio: Demir Halilbasic                   |
| Student III godine                                    |
| Smijer: Informatika i Racunarstvo                     |
| Datum pocetka: 24.12.2024.                            |
| Datum zavrsetka: 05.01.2025.                          |
| Verzija koda: 0.4.7                                   |
| IPI Akademija Tuzla                                   |
*/

package views;

import controllers.EmployeeService;
import controllers.MongoDBController;
import models.BugReport;
import models.Employee;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.prefs.Preferences;

import org.bson.Document;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.mindrot.jbcrypt.BCrypt;

public class LoginForm extends JFrame {
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton themeSwitchButton;
    private BufferedImage sunImage;
    private BufferedImage moonImage;
    private JButton infoButton;

    private boolean isDarkMode;
    private static final String PREFS_THEME_KEY = "app_theme";

    public LoginForm() {
        Preferences prefs = Preferences.userRoot().node(getClass().getName());
        isDarkMode = prefs.getBoolean(PREFS_THEME_KEY, true);

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

        try {
            sunImage = ImageIO.read(getClass().getResource("/icons/light_mode_sun.png"));
            moonImage = ImageIO.read(getClass().getResource("/icons/dark_mode_moon.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        applyTheme();

        setTitle("Emplo\\Yee | Attendance Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 500);
        setMinimumSize(new Dimension(450, 500));
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setOpaque(false);

        JPanel glassPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, isDarkMode ? 0.9f : 0.8f));
                g2d.setColor(new Color(isDarkMode ? 0 : 255, isDarkMode ? 0 : 255, isDarkMode ? 0 : 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setPreferredSize(new Dimension(300, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        LogoPanel logoPanel = new LogoPanel();

        GridBagConstraints logoGbc = new GridBagConstraints();
        logoGbc.gridx = 0;
        logoGbc.gridy = 0;
        logoGbc.gridwidth = 2;
        logoGbc.insets = new Insets(10, 10, 20, 10);

        glassPanel.add(logoPanel, logoGbc);

        usernameField = new PlaceholderTextField("Email");
        usernameField.setFont(usernameField.getFont().deriveFont(Font.BOLD));
        gbc.gridy = 1;
        glassPanel.add(usernameField, gbc);

        passwordField = new PlaceholderPasswordField("Password");
        passwordField.setFont(passwordField.getFont().deriveFont(Font.BOLD));
        gbc.gridy = 2;
        glassPanel.add(passwordField, gbc);

        loginButton = new RoundedButton("Login");
        gbc.gridy = 3;
        glassPanel.add(loginButton, gbc);

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.NONE;
        mainGbc.insets = new Insets(10, 10, 10, 10);
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.anchor = GridBagConstraints.CENTER;

        mainPanel.add(glassPanel, mainGbc);

        setContentPane(mainPanel);

        JLayeredPane layeredPane = getLayeredPane();
        themeSwitchButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                BufferedImage img = isDarkMode ? sunImage : moonImage;
                g2d.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2d.dispose();
            }
        };
        themeSwitchButton.setPreferredSize(new Dimension(20, 20));
        themeSwitchButton.setContentAreaFilled(false);
        themeSwitchButton.setBorder(BorderFactory.createEmptyBorder());
        themeSwitchButton.setFocusPainted(false);
        themeSwitchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeSwitchButton.setToolTipText("Switch Theme");
        themeSwitchButton.setHorizontalAlignment(SwingConstants.CENTER);
        themeSwitchButton.setVerticalAlignment(SwingConstants.CENTER);

        layeredPane.add(themeSwitchButton, JLayeredPane.PALETTE_LAYER);

        try {
            BufferedImage infoImage = ImageIO.read(getClass().getResource("/icons/info.png"));
            infoButton = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.drawImage(infoImage, 0, 0, getWidth(), getHeight(), null);
                    g2d.dispose();
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }

        infoButton.setPreferredSize(new Dimension(20, 20));
        infoButton.setContentAreaFilled(false);
        infoButton.setBorder(BorderFactory.createEmptyBorder());
        infoButton.setFocusPainted(false);
        infoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        infoButton.setToolTipText("Application Info");

        layeredPane.add(infoButton, JLayeredPane.PALETTE_LAYER);

        loginButton.addActionListener(e -> handleLogin(usernameField.getText().trim(), new String(passwordField.getPassword()).trim()));
        themeSwitchButton.addActionListener(e -> toggleTheme(prefs));

        themeSwitchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                themeSwitchButton.setBounds(themeSwitchButton.getX() - 2, themeSwitchButton.getY() - 2, 25, 25);
                themeSwitchButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                themeSwitchButton.setBounds(themeSwitchButton.getX() + 2, themeSwitchButton.getY() + 2, 20, 20);
                themeSwitchButton.repaint();
            }
        });

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Graphics2D g2d = (Graphics2D) loginButton.getGraphics();
                g2d.setTransform(AffineTransform.getScaleInstance(1.1, 1.1));
                loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                loginButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Graphics2D g2d = (Graphics2D) loginButton.getGraphics();
                g2d.setTransform(AffineTransform.getScaleInstance(1.0, 1.0));
                loginButton.setCursor(Cursor.getDefaultCursor());
                loginButton.repaint();
            }
        });

        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String email = usernameField.getText().trim();
                    String password = new String(passwordField.getPassword()).trim();
                    handleLogin(email, password);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
                    if (e.getSource() == usernameField) {
                        passwordField.requestFocus();
                    } else if (e.getSource() == passwordField) {
                        usernameField.requestFocus();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    MongoDBController.getInstance().closeConnection();
                    Window window = SwingUtilities.getWindowAncestor((Component) e.getSource());
                    if (window != null) {
                        window.dispose();
                    }
                }
            }
        };

        usernameField.addKeyListener(keyListener);
        passwordField.addKeyListener(keyListener);
        loginButton.addKeyListener(keyListener);

        infoButton.addActionListener(e -> showAppInfo());

        infoButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                infoButton.setBounds(infoButton.getX() - 2, infoButton.getY() - 2, 25, 25);
                infoButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                infoButton.setBounds(infoButton.getX() + 2, infoButton.getY() + 2, 20, 20);
                infoButton.repaint();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                themeSwitchButton.setBounds(10, getHeight() - 50, 20, 20);
                infoButton.setBounds(getWidth() - 50, getHeight() - 50, 20, 20);
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                themeSwitchButton.setBounds(10, getHeight() - 50, 20, 20);
                infoButton.setBounds(getWidth() - 50, getHeight() - 50, 20, 20);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MongoDBController.getInstance().closeConnection();
            }
        });

        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                usernameField.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));
                if (usernameField.getText().isEmpty()) {
                    usernameField.setBackground(new Color(0, 0, 0, 0));
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                usernameField.setCursor(Cursor.getDefaultCursor());
                usernameField.setBackground(new Color(0, 0, 0, 0));
            }
        });

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));
                if (passwordField.getText().isEmpty()) {
                    passwordField.setBackground(new Color(0, 0, 0, 0));
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setCursor(Cursor.getDefaultCursor());
                passwordField.setBackground(new Color(0, 0, 0, 0));
            }
        });
    }

    private void showAppInfo() {
        String appVersion = "0.4.6";
        String githubLink = "https://github.com/demirhalilbasic/employee-ams";
        String description = "This is an Attendance Management System that helps with time tracking.";

        JPanel panel = new JPanel(new BorderLayout());
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(
                "<html><body style='width: 300px;'>" +
                        "<h3>Application Version: " + appVersion + "</h3>" +
                        "<p><a href='" + githubLink + "'>View full code on GitHub</a></p>" +
                        "<p>" + description + "</p>" +
                        "<p>If you are facing problems, we are terribly sorry. Please report them using the form below.</p>" +
                        "</body></html>"
        );
        textPane.setEditable(false);
        textPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton bugReportButton = new JButton("Bug Report");
        bugReportButton.setPreferredSize(new Dimension(300, 30));
        bugReportButton.addActionListener(e -> showBugReportForm());
        buttonPanel.add(bugReportButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Application Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showBugReportForm() {
        showBugReportForm(null, null, null);
    }

    private void showBugReportForm(String prefilledCategory, String prefilledDescription, String prefilledEmail) {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        ButtonGroup categoryGroup = new ButtonGroup();
        JRadioButton uiIssueButton = new JRadioButton(BugReport.CATEGORY_UI_ISSUE);
        JRadioButton functionalityIssueButton = new JRadioButton(BugReport.CATEGORY_FUNCTIONALITY_ISSUE);
        JRadioButton performanceIssueButton = new JRadioButton(BugReport.CATEGORY_PERFORMANCE_ISSUE);
        JRadioButton otherButton = new JRadioButton(BugReport.CATEGORY_OTHER);
        categoryGroup.add(uiIssueButton);
        categoryGroup.add(functionalityIssueButton);
        categoryGroup.add(performanceIssueButton);
        categoryGroup.add(otherButton);

        if (BugReport.CATEGORY_UI_ISSUE.equals(prefilledCategory)) {
            uiIssueButton.setSelected(true);
        } else if (BugReport.CATEGORY_FUNCTIONALITY_ISSUE.equals(prefilledCategory)) {
            functionalityIssueButton.setSelected(true);
        } else if (BugReport.CATEGORY_PERFORMANCE_ISSUE.equals(prefilledCategory)) {
            performanceIssueButton.setSelected(true);
        } else if (BugReport.CATEGORY_OTHER.equals(prefilledCategory)) {
            otherButton.setSelected(true);
        } else {
            uiIssueButton.setSelected(true);
        }

        panel.add(new JLabel("Category:"));
        panel.add(uiIssueButton);
        panel.add(functionalityIssueButton);
        panel.add(performanceIssueButton);
        panel.add(otherButton);

        JTextField descriptionField = new JTextField(prefilledDescription != null ? prefilledDescription : "");
        JTextField emailField = new JTextField(prefilledEmail != null ? prefilledEmail : "");
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Bug Report",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String category = null;
            if (uiIssueButton.isSelected()) {
                category = BugReport.CATEGORY_UI_ISSUE;
            } else if (functionalityIssueButton.isSelected()) {
                category = BugReport.CATEGORY_FUNCTIONALITY_ISSUE;
            } else if (performanceIssueButton.isSelected()) {
                category = BugReport.CATEGORY_PERFORMANCE_ISSUE;
            } else if (otherButton.isSelected()) {
                category = BugReport.CATEGORY_OTHER;
            }

            String description = descriptionField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            handleBugReport(category, description, email, password);
        }
    }

    private void handleBugReport(String category, String description, String email, String password) {
        if (description == null || description.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description of the issue cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            showBugReportForm(category, description, email);
            return;
        }

        try {
            MongoDatabase database = MongoDBController.getInstance().getDatabase();
            if (database == null) {
                JOptionPane.showMessageDialog(this, "Nema konekcije sa bazom!", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MongoCollection<Document> employeesCollection = database.getCollection(MongoDBController.getCollectionName());
            Document employeeDoc = employeesCollection.find(new Document("email", email)).first();

            if (employeeDoc != null && BCrypt.checkpw(password, employeeDoc.getString("password"))) {
                String employeeId = employeeDoc.getString("id");
                BugReport bugReport = new BugReport(employeeId, category, description);

                MongoCollection<Document> bugReportsCollection = database.getCollection("bug_reports");
                bugReportsCollection.insertOne(
                        new Document("ticketId", bugReport.getTicketId())
                                .append("employeeId", bugReport.getEmployeeId())
                                .append("dateTime", bugReport.getDateTime())
                                .append("category", bugReport.getCategory())
                                .append("description", bugReport.getDescription())
                                .append("status", bugReport.getStatus())
                );
                JOptionPane.showMessageDialog(this, "Bug report submitted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login credentials for bug report submission.", "Error", JOptionPane.ERROR_MESSAGE);
                showBugReportForm(category, description, email);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error submitting bug report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) {
                usernameField.setBorder(BorderFactory.createLineBorder(Color.RED));
                usernameField.requestFocus();
            } else {
                usernameField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
            }

            if (password.isEmpty()) {
                passwordField.setBorder(BorderFactory.createLineBorder(Color.RED));
                if (!username.isEmpty()) {
                    passwordField.requestFocus();
                }
            } else {
                passwordField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("PasswordField.border"));
            }
            JOptionPane.showMessageDialog(this, "Polja za email i lozinku ne smiju biti prazna!", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            MongoDatabase database = MongoDBController.getInstance().getDatabase();
            if (database == null) {
                JOptionPane.showMessageDialog(this, "Nema konekcije sa bazom!", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MongoCollection<Document> collection = database.getCollection(MongoDBController.getCollectionName());
            MongoCursor<Document> cursor = collection.find().iterator();

            while (cursor.hasNext()) {
                Document document = cursor.next();
                String email = document.getString("email");
                String hashedPassword = document.getString("password");
                String role = document.getString("role");

                if (username.equals(email) && BCrypt.checkpw(password, hashedPassword)) {
                    Employee employee = new Employee(
                            document.getString("id"),
                            document.getString("name"),
                            document.getString("surname"),
                            email,
                            hashedPassword,
                            document.getString("position"),
                            role
                    );

                    switch (role) {
                        case "Employee":
                            new EmployeeDashboard(employee, new EmployeeService(), isDarkMode).setVisible(true);
                            break;
                        case "Manager":
                            new ManagerDashboard(employee, new EmployeeService(), isDarkMode).setVisible(true);
                            break;
                        case "SuperAdmin":
                            new AdminDashboard(employee, new EmployeeService(), isDarkMode).setVisible(true);
                            break;
                    }
                    dispose();
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Pogrešni podaci za prijavu!", "Greška", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju baze: " + e.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void toggleTheme(Preferences prefs) {
        isDarkMode = !isDarkMode;
        prefs.putBoolean(PREFS_THEME_KEY, isDarkMode);
        applyTheme();

        themeSwitchButton.repaint();
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void applyTheme() {
        if (isDarkMode) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomSplashScreen().showSplash());
    }

    class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setColumns(20);
            setFont(getFont().deriveFont(Font.BOLD));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, 10, getHeight() / 2 + getFont().getSize() / 2 - 2);
                g2.dispose();
            }
        }

        @Override
        protected void paintBorder(Graphics g) {
            super.paintBorder(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    }

    class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;

        public PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setColumns(20);
            setFont(getFont().deriveFont(Font.BOLD));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, 10, getHeight() / 2 + getFont().getSize() / 2 - 2);
                g2.dispose();
            }
        }

        @Override
        protected void paintBorder(Graphics g) {
            super.paintBorder(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    }

    class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 122, 204));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g2.dispose();
        }

        @Override
        public boolean isContentAreaFilled() {
            return false;
        }
    }

    class LogoPanel extends JPanel {
        private BufferedImage logoImage;

        public LogoPanel() {
            try {
                logoImage = ImageIO.read(getClass().getResource("/images/app_transparent_logo.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setOpaque(false);
            setPreferredSize(new Dimension(200, 142));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (logoImage != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Image scaledImage = logoImage.getScaledInstance(200, 142, Image.SCALE_SMOOTH);
                int x = (getWidth() - 200) / 2;
                int y = (getHeight() - 142) / 2;
                g2d.drawImage(scaledImage, x, y, this);
            }
        }
    }

    class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
        private String[] imagePaths = {
                "/images/login_screen/blurred_login_background_1.png",
                "/images/login_screen/blurred_login_background_2.png",
                "/images/login_screen/blurred_login_background_3.png",
                "/images/login_screen/blurred_login_background_4.png",
                "/images/login_screen/blurred_login_background_5.png",
                "/images/login_screen/blurred_login_background_6.png"
        };

        public BackgroundPanel() {
            loadRandomBackgroundImage();
        }

        private void loadRandomBackgroundImage() {
            Random rand = new Random();
            int randomIndex = rand.nextInt(imagePaths.length);
            try {
                backgroundImage = ImageIO.read(getClass().getResource(imagePaths[randomIndex]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int imageWidth = backgroundImage.getWidth();
                int imageHeight = backgroundImage.getHeight();
                float aspectRatio = (float) imageWidth / imageHeight;

                int canvasWidth = getWidth();
                int canvasHeight = getHeight();

                int newWidth, newHeight;
                if (canvasWidth / (float) canvasHeight > aspectRatio) {
                    newWidth = canvasWidth;
                    newHeight = (int) (canvasWidth / aspectRatio);
                } else {
                    newHeight = canvasHeight;
                    newWidth = (int) (canvasHeight * aspectRatio);
                }

                int x = (canvasWidth - newWidth) / 2;
                int y = (canvasHeight - newHeight) / 2;

                BufferedImage blurredImage = createBlurredImage(backgroundImage);

                g2d.drawImage(blurredImage, x, y, newWidth, newHeight, this);
            }
        }

        private BufferedImage createBlurredImage(BufferedImage image) {
            float[] matrix = {
                    1/9f, 1/9f, 1/9f,
                    1/9f, 1/9f, 1/9f,
                    1/9f, 1/9f, 1/9f
            };
            BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null);
            return op.filter(image, null);
        }
    }
}