package ch.yvesguillo.logic;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CliOption {
    public String group;
    public List<String> flags;
    public String type;
    public List<String> choices;
    public boolean required;
    public String metavar;
    public String help;

    // Annotate the translation for Jackson ("default" mapped to "defaultValue") as Python Argparse will output "default" which is a Java keyword.
    @JsonProperty("default")
    public String defaultValue;

    // Derived fields.
    public boolean isBoolean;
    public boolean hasChoices;

    public CliOption() {}

    public void postProcess() {
        this.isBoolean = (flags.stream().anyMatch(f -> f.startsWith("--no-")) || type.isEmpty());
        this.hasChoices = (choices != null && !choices.isEmpty());
    }

    public String getPrimaryFlag() {
        return flags.stream().filter(f -> f.startsWith("--")).findFirst().orElse(flags.get(0));
    }

    public String getShortFlag() {
        return flags.stream().filter(f -> f.startsWith("-") && !f.startsWith("--")).findFirst().orElse("");
    }
}