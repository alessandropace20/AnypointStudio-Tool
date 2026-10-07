package com.muletool.model;

/** Describes a single editable field inside a component/activity form. */
public class FieldDef {

    public enum FieldType { TEXT, COMBO, PASSWORD, SPINNER, TEXTAREA, CHECKBOX }

    public final String    key;
    public final String    label;
    public final String    placeholder;
    public final FieldType type;
    public final String[]  options;     // only for COMBO
    public final boolean   required;
    public final String    tooltip;

    public FieldDef(String key, String label, String placeholder,
                    FieldType type, String[] options,
                    boolean required, String tooltip) {
        this.key         = key;
        this.label       = label;
        this.placeholder = placeholder;
        this.type        = type;
        this.options     = options;
        this.required    = required;
        this.tooltip     = tooltip;
    }

    // --- convenience factory ------------------------------------------
    public static FieldDef text(String key, String label, String ph, boolean req, String tip) {
        return new FieldDef(key, label, ph, FieldType.TEXT, null, req, tip);
    }
    public static FieldDef combo(String key, String label, String[] opts, boolean req, String tip) {
        return new FieldDef(key, label, "", FieldType.COMBO, opts, req, tip);
    }
    public static FieldDef password(String key, String label, boolean req, String tip) {
        return new FieldDef(key, label, "", FieldType.PASSWORD, null, req, tip);
    }
    public static FieldDef spinner(String key, String label, String defaultVal, String tip) {
        return new FieldDef(key, label, defaultVal, FieldType.SPINNER, null, false, tip);
    }
    public static FieldDef textarea(String key, String label, String ph, boolean req, String tip) {
        return new FieldDef(key, label, ph, FieldType.TEXTAREA, null, req, tip);
    }
    public static FieldDef checkbox(String key, String label, String tip) {
        return new FieldDef(key, label, "", FieldType.CHECKBOX, null, false, tip);
    }
}
