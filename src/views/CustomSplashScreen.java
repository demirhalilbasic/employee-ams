package views;

import controllers.MongoDBController;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.bson.Document;

public class CustomSplashScreen extends JWindow {

    public CustomSplashScreen() {
        JLayeredPane content = new JLayeredPane();
        content.setLayout(null);

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

        JPanel backgroundPanel = new ImagePanel();
        backgroundPanel.setBounds(0, 0, 800, 600);
        content.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        JPanel loadingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Font font = new Font("Comic Sans MS", Font.PLAIN, 26);
                g2d.setFont(font);
                String text = "loading app...";
                FontMetrics metrics = g.getFontMetrics(g2d.getFont());

                int textWidth = metrics.stringWidth(text);
                int rectWidth = textWidth + 40;
                int rectHeight = metrics.getHeight() + 20;
                int rectX = (getWidth() - rectWidth) / 2;
                int rectY = 75;

                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(rectX, rectY, rectWidth, rectHeight, 30, 30);
                g2d.setColor(new Color(16, 123, 206, 150));
                g2d.fill(roundedRectangle);

                g2d.setColor(Color.WHITE);
                int textX = (getWidth() - textWidth) / 2;
                int textY = rectY + ((rectHeight - metrics.getHeight()) / 2) + metrics.getAscent();
                g2d.drawString(text, textX, textY);

                g2d.dispose();
            }
        };
        loadingPanel.setBounds(0, 0, 800, 600);
        loadingPanel.setOpaque(false);
        content.add(loadingPanel, JLayeredPane.PALETTE_LAYER);

        try {
            ImageIcon loadingAnimation = new ImageIcon(new ImageIcon(getClass().getResource("/icons/loading.gif")).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));

            JLabel loadingAnimationLabel = new JLabel(loadingAnimation);
            loadingAnimationLabel.setBounds((800 - 50) / 2, 150, 50, 50);
            loadingAnimationLabel.setHorizontalAlignment(SwingConstants.CENTER);
            content.add(loadingAnimationLabel, JLayeredPane.PALETTE_LAYER);
        } catch (Exception e) {
            System.err.println("GIF resource not found or failed to load.");
            e.printStackTrace();
        }

        setContentPane(content);
        setSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
    }

    public void showSplash() {
        setVisible(true);
        new Thread(() -> initializeApp()).start();
    }

    private void initializeApp() {
        try {
            MongoDatabase database = MongoDBController.getInstance().getDatabase();

            MongoCollection<Document> employeesCollection = database.getCollection(MongoDBController.getCollectionName());
            if (employeesCollection != null) {
                System.out.println("Successfully connected to employees collection!");
            } else {
                throw new Exception("Failed to connect to employees collection.");
            }

            MongoCollection<Document> bugReportsCollection = database.getCollection("bug_reports");
            if (bugReportsCollection != null) {
                System.out.println("Successfully connected to bug_reports collection!");
            } else {
                throw new Exception("Failed to connect to bug_reports collection.");
            }

            Thread.sleep(2000);

            if (database != null) {
                SwingUtilities.invokeLater(() -> {
                    setVisible(false);
                    dispose();
                    new LoginForm().setVisible(true);
                });
            } else {
                throw new Exception("Database connection is null.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize app: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class ImagePanel extends JPanel {
        private Image backgroundImage;
        private String[] imagePaths = {
                "/images/splash_screen/splash_screen_background_1.png",
                "/images/splash_screen/splash_screen_background_2.png",
                "/images/splash_screen/splash_screen_background_3.png",
                "/images/splash_screen/splash_screen_background_4.png",
                "/images/splash_screen/splash_screen_background_5.png",
                "/images/splash_screen/splash_screen_background_6.png"
        };

        public ImagePanel() {
            loadRandomBackgroundImage();
        }

        private void loadRandomBackgroundImage() {
            Random rand = new Random();
            int randomIndex = rand.nextInt(imagePaths.length);
            try (InputStream is = getClass().getResourceAsStream(imagePaths[randomIndex])) {
                if (is != null) {
                    backgroundImage = ImageIO.read(is);
                } else {
                    System.err.println("Image resource not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
