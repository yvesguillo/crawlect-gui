package ch.yvesguillo.gui;

public class ComboItem {
    private final String label;
    private final String value;

    public ComboItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        // What gets displayed in the combo box.
        return label;
    }
}