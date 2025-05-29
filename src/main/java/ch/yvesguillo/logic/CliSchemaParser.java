package ch.yvesguillo.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import ch.yvesguillo.logic.CliOption;

public class CliSchemaParser {
    private final List<CliOption> options;

    public CliSchemaParser(File jsonFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        this.options = mapper.readValue(jsonFile, new TypeReference<>() {});
        this.options.forEach(CliOption::postProcess);
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