package ch.yvesguillo.model;

/**
 * Simple data wrapper for representing items in combo boxes.
 *
 * Each {@code ComboItem} consists of:
 * - a {@code label}: the text shown to the user in the UI.
 * - a {@code value}: the actual value associated with the option (used internally).
 */
public class ComboItem {

    private final String label;
    private final String value;

    /**
     * Constructs a new ComboItem with the specified label and value.
     *
     * @param label The text displayed in the combo box.
     * @param value The value associated with this item (e.g., CLI flag argument).
     */
    public ComboItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    /**
     * Returns the internal value associated with this item.
     *
     * @return the item's value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the label to be displayed in the combo box.
     * This method overrides {@code toString()} to ensure the label is shown in the UI.
     *
     * @return the item's display label.
     */
    @Override
    public String toString() {
        return label;
    }
}