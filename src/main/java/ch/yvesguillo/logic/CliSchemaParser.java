package ch.yvesguillo.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class CliSchemaParser {
    private static CliSchemaParser instance;

    private final List<CliOption> options;

    private CliSchemaParser(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.options = mapper.readValue(jsonContent, new TypeReference<>() {});
        for (CliOption option : options) {
            option.postProcess(); // Manually trigger to set isBoolean, hasChoices, etc.
        }
    }

    // This method sets the instance (must be called once, I'm not sure I'm implementing Singleton properly).
    public static void initialize(String jsonContent) throws IOException {
        instance = new CliSchemaParser(jsonContent);
    }

    // This returns the singleton.
    public static CliSchemaParser getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CliSchemaParser has not been initialized.");
        }
        return instance;
    }

    public List<CliOption> getAllOptions() {
        return options;
    }

    public List<String> getGroups() {
        return options.stream()
                .map(o -> o.group)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<CliOption> getOptionsForGroup(String groupName) {
        return options.stream()
                .filter(o -> o.group.equals(groupName))
                .collect(Collectors.toList());
    }
}
