package ch.yvesguillo.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Singleton class responsible for managing user-level application settings.
 *
 * Saves and loads values from a config file located in a standard system path,
 * depending on the platform (Windows/macOS/Linux).
 */
public final class UserSettings {

    private static UserSettings instance;

    private final Map<CliOption, Object> storedValues = new HashMap<>();
    private final File configFile;

    private UserSettings(String appName) {
        if (appName == null || appName.isBlank()) {
            throw new IllegalArgumentException("Missing app name for UserSettings.");
        }
        this.configFile = resolveUserConfigFile(appName);
    }

    /**
     * Initializes the singleton with the given application name.
     *
     * @param appName name used for the config folder.
     */
    public static synchronized void initialize(String appName) {
        if (instance != null) {
            throw new IllegalStateException("UserSettings has already been initialized.");
        }
        instance = new UserSettings(appName);
    }

    /**
     * Returns the singleton instance, initializing it if needed.
     *
     * @param appName name used for the config folder (used only once).
     * @return instance of {@link UserSettings}.
     */
    public static synchronized UserSettings lazyGetInstance(String appName) {
        if (instance == null) {
            instance = new UserSettings(appName);
        }
        return instance;
    }

    /**
     * Returns the already-initialized singleton instance.
     *
     * @return instance of {@link UserSettings}.
     * @throws IllegalStateException if not yet initialized.
     */
    public static UserSettings getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserSettings has not been initialized.");
        }
        return instance;
    }

    /**
     * Resolves the path of the user config file, depending on the operating system.
     *
     * @param appName application name for folder naming.
     * @return config file object.
     */
    private File resolveUserConfigFile(String appName) {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        File configFile;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            configFile = (appData != null)
                    ? new File(appData, appName + "/config.json")
                    : new File(userHome, "AppData/Roaming/" + appName + "/config.json");
        } else if (os.contains("mac")) {
            configFile = new File(userHome, "Library/Application Support/" + appName + "/config.json");
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            configFile = (xdg != null)
                    ? new File(xdg, appName + "/config.json")
                    : new File(userHome, ".config/" + appName + "/config.json");
        }

        File parent = configFile.getParentFile();
        if (!parent.exists()) {
            boolean created = parent.mkdirs();
            System.out.println("[Config] Creating config directory: " + parent.getAbsolutePath() +" → " + (created ? "OK" : "FAILED"));
        }

        return configFile;
    }

    /**
     * Saves the current stored values to disk as JSON.
     */
    public void saveConfig( Map<CliOption, Object> newConfig) {
        // Convert CliOption keys to String keys (primary flags) before saving.
        Map<String, Object> simpleMap = new HashMap<>();

        for (Map.Entry<CliOption, Object> entry : newConfig.entrySet()) {
            simpleMap.put(entry.getKey().getPrimaryFlag(), entry.getValue());
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(writer, simpleMap);
            System.out.println("[Config] Saved to: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Config] Save failed: " + e.getMessage());
        }
    }

    /**
     * Loads settings from disk and maps them to known CLI options.
     * Matching is done by each option's primary flag.
     *
     * @return a map of loaded settings with {@link CliOption} keys.
     */
    public Map<CliOption, Object> loadConfig() {
        storedValues.clear(); // Optional: reset before loading

        if (!configFile.exists()) {
            System.out.println("[Config] No config file found at: " + configFile.getAbsolutePath());
            return Collections.emptyMap();
        }

        try {
            Map<String, Object> simpleMap = new ObjectMapper().readValue(
                configFile,
                new TypeReference<Map<String, Object>>() {}
            );

            for (CliOption option : CliSchemaParser.getInstance().getAllOptions()) {
                String key = option.getPrimaryFlag();
                if (simpleMap.containsKey(key)) {
                    storedValues.put(option, simpleMap.get(key));
                }
            }

            System.out.println("[Config] Loaded from: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Config] Load failed: " + e.getMessage());
        }

        return new HashMap<>(storedValues);
    }
}