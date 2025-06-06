package ch.yvesguillo;

import ch.yvesguillo.logic.CliSchemaParser;
import ch.yvesguillo.logic.PythonRunner;
import ch.yvesguillo.logic.UserSettings;
import ch.yvesguillo.gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.formdev.flatlaf.FlatDarkLaf;

public class CrawlectGUI {
    public static String appName;
    public static String appVersion;
    public static void main(String[] args) {
        try {
            // Set Look & Feel
            UIManager.setLookAndFeel(new FlatDarkLaf());

            // Get CLI schema from Python backend
            String json = PythonRunner.getCliSchemaJson();
            CliSchemaParser.initialize(json);

            // Load app name and version from properties
            String name = "Crawlect-GUI";
            String version = "DEV";

            try (InputStream stream = CrawlectGUI.class.getResourceAsStream("/version.properties")) {
                if (stream != null) {
                    Properties props = new Properties();
                    props.load(stream);
                    appName = props.getProperty("project.name", name);
                    appVersion = props.getProperty("project.version", version);
                }
            } catch (Exception e) {
                System.err.println("[Init] Could not load version.properties: " + e.getMessage());
            }

            // Init users settings.
            UserSettings.lazyGetInstance(appName.toLowerCase() + " " + appVersion);

            // Launch UI
            SwingUtilities.invokeLater(() -> {
                MainWindow win = new MainWindow(CliSchemaParser.getInstance().getGroups(), appName, appVersion);
                setAppIcon(win);
                win.setVisible(true);
            });

        } catch (Exception error) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Crawlect is not available on this system.\n" + error.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            });
        }
    }

    private static void setAppIcon(JFrame frame) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("mac")) {
                // macOS: Dock icon only
                Taskbar.getTaskbar().setIconImage(
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_64-mac.png")).getImage()
                );
            } else {
                // Fallback icon (for Windows + Linux)
                List<Image> icons = List.of(
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_16.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_32.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_64.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_256.png")).getImage()
                );
                frame.setIconImages(icons);

                if (os.contains("linux")) {
                    // Linux hint (may help under some L&Fs)
                    UIManager.put("Frame.iconImage", icons.get(2)); // 64px
                }
            }
        } catch (Exception e) {
            System.err.println("[GUI] Could not set icon: " + e.getMessage());
        }
    }
}