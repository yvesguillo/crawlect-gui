package ch.yvesguillo.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JComponent;

/**
 * Manage users settings.
 * Retrive previous session parameters and update these on demand.
 */
public final class UserSettings {

    // Cache values when refreshing option panel for input persistance.
    private static Map<CliOption, Object> storedValues;

    public UserSettings() {
        storedValues = new HashMap<>();
    }

    public static File getUserConfigFile() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        File configFile;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            configFile = (appData != null)
                    ? new File(appData, "crawlect-gui/config.json")
                    : new File(userHome, "AppData/Roaming/crawlect-gui/config.json");
        } else if (os.contains("mac")) {
            configFile = new File(userHome, "Library/Application Support/crawlect-gui/config.json");
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            configFile = (xdg != null)
                    ? new File(xdg, "crawlect-gui/config.json")
                    : new File(userHome, ".config/crawlect-gui/config.json");
        }

        File parent = configFile.getParentFile();
        if (!parent.exists()) {
            boolean created = parent.mkdirs();
            System.out.println("[Config] Creating config directory: " + parent.getAbsolutePath() +
                    " → " + (created ? "OK" : "FAILED"));
        }

        return configFile;
    }

    public static void saveConfig() {
        File configFile = getUserConfigFile();
        Map<String, Object> simpleMap = new HashMap<>();

        for (Map.Entry<CliOption, Object> entry : storedValues.entrySet()) {
            simpleMap.put(entry.getKey().getPrimaryFlag(), entry.getValue());
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, simpleMap);
            System.out.println("[Config] Saved to: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Config] Save failed: " + e.getMessage());
        }
    }

    public static void loadConfig() {
        File configFile = getUserConfigFile();
        if (!configFile.exists()) {
            System.out.println("[Config] No config file found at: " + configFile.getAbsolutePath());
            return;
        }

        try {
            Map<String, Object> simpleMap = new ObjectMapper().readValue(
                configFile,
                new TypeReference<Map<String, Object>>() {}
            );

            for (CliOption option : CliSchemaParser.getInstance().getAllOptions()) {
                String key = option.getPrimaryFlag();
                Object val = simpleMap.get(key);
                if (val != null) {
                    storedValues.put(option, val);
                }
            }
            System.out.println("[Config] Loaded from: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Config] Load failed: " + e.getMessage());
        }
    }
}