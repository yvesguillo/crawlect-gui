package ch.yvesguillo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton class that parses a JSON CLI schema and creates usable {@link CliOption} parameter objects.
 *
 * You can use {@link #initialize(String)} to explicitly initialize then {@link #getInstance(String)} to retrive the singleton, or {@link #lazyGetInstance(String)} to auto-initialize on first access.
 */
public final class CliSchemaParser {

    private static CliSchemaParser instance;
    private final List<CliOption> options;

    private CliSchemaParser(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.options = mapper.readValue(jsonContent, new TypeReference<>() {});
        for (CliOption option : options) {
            option.postProcess();
        }
    }

    /**
     * Explicitly initializes the singleton. Fails if already initialized.
     *
     * @param jsonContent JSON CLI schema.
     */
    public static synchronized void initialize(String jsonContent) {
        if (instance != null) {
            throw new IllegalStateException("CliSchemaParser has already been initialized.");
        }
        try {
            instance = new CliSchemaParser(jsonContent);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize CliSchemaParser: " + e.getMessage(), e);
        }
    }

    /**
     * Lazily returns the singleton, initializing it if necessary.
     *
     * @param jsonContent JSON CLI schema (used only if uninitialized).
     * @return CliSchemaParser instance.
     */
    public static synchronized CliSchemaParser lazyGetInstance(String jsonContent) {
        if (instance == null) {
            try {
                instance = new CliSchemaParser(jsonContent);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to lazily initialize CliSchemaParser: " + e.getMessage(), e);
            }
        }
        return instance;
    }

    /**
     * Returns the singleton, assuming it has already been initialized.
     *
     * @return CliSchemaParser instance.
     * @throws IllegalStateException if not yet initialized.
     */
    public static CliSchemaParser getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CliSchemaParser has not been initialized.");
        }
        return instance;
    }

    /**
     * Returns all parsed CLI options.
     *
     * @return List of {@link CliOption}.
     */
    public List<CliOption> getAllOptions() {
        return options;
    }

    /**
     * Returns a list of unique groups defined in the CLI options.
     *
     * @return List of group names.
     */
    public List<String> getGroups() {
        return options.stream()
                .map(o -> o.group)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Returns all CLI options that belong to the specified group.
     *
     * @param groupName Name of the group.
     * @return List of {@link CliOption} in that group.
     */
    public List<CliOption> getOptionsForGroup(String groupName) {
        return options.stream()
                .filter(o -> o.group.equals(groupName))
                .collect(Collectors.toList());
    }
}