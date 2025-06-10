package ch.yvesguillo;

import ch.yvesguillo.controller.PythonRunner;
import ch.yvesguillo.controller.UserSettings;
import ch.yvesguillo.controller.MainController;
import ch.yvesguillo.model.CliSchemaParser;
import ch.yvesguillo.view.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.formdev.flatlaf.FlatDarkLaf;

/**
 * Main entry point for the Crawlect GUI application.
 *
 * Responsibilities:
 * - Set the application theme and icon
 * - Initialize the CLI schema from the Python backend
 * - Load versioning info from resources
 * - Launch the Swing GUI
 */
public class CrawlectGUI {

    /** Application display name (loaded from version.properties or fallback). */
    public static String appName;

    /** Application version string (loaded from version.properties or fallback). */
    public static String appVersion;

    /** Reference to the main GUI window. */
    public static MainWindow view;

    /**
     * Main method: launches the GUI.
     *
     * @param args Optional CLI args (not used in current version).
     */
    public static void main(String[] args) {
        try {
            // Set FlatLaf dark theme (look and feel)
            UIManager.setLookAndFeel(new FlatDarkLaf());

            // Fetch CLI schema (JSON) from the Crawlect Python backend
            String json = PythonRunner.getCliSchemaJson();
            CliSchemaParser.initialize(json);

            // Default fallback values
            String name = "Crawlect-GUI";
            String version = "DEV";

            // Load app name and version from embedded properties file
            try (InputStream stream = CrawlectGUI.class.getResourceAsStream("/version.properties")) {
                if (stream != null) {
                    Properties props = new Properties();
                    props.load(stream);
                    appName = props.getProperty("project.name", name);
                    appVersion = props.getProperty("project.version", version);
                }
            } catch (Exception e) {
                System.err.println("[Init] Could not load version.properties: " + e.getMessage());
                appName = name;
                appVersion = version;
            }

            // Load user settings (config.json)
            UserSettings.initialize(appName.toLowerCase() + " " + appVersion);

            // UI-related work must run on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                // Initialize and display the GUI window
                MainWindow.initialize(CliSchemaParser.getInstance().getGroups(), appName, appVersion);
                view = MainWindow.getInstance();

                // Initialize controller
                MainController.initialize(view);

                setAppIcon(view);
                view.setVisible(true);
            });

        } catch (Exception error) {
            // Graceful error fallback: show dialog and exit
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

    /**
     * Sets the application icon depending on the platform (macOS, Windows, Linux).
     *
     * @param win The main application JFrame.
     */
    private static void setAppIcon(JFrame win) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("mac")) {
                // macOS: only dock icon is needed
                Taskbar.getTaskbar().setIconImage(
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_64-mac.png")).getImage()
                );
            } else {
                // Windows & Linux: full icon list
                List<Image> icons = List.of(
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_16.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_32.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_64.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_256.png")).getImage()
                );
                win.setIconImages(icons);

                if (os.contains("linux")) {
                    // Linux-specific hint for Look & Feel to use the icon
                    // Use 64px icon
                    UIManager.put("Frame.iconImage", icons.get(2));
                }
            }
        } catch (Exception e) {
            System.err.println("[GUI] Could not set icon: " + e.getMessage());
        }
    }
}