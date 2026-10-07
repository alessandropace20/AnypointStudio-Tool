package com.muletool.model;

import java.util.*;

/**
 * Represents a single configured instance of a ComponentDef with its field values.
 * This is the unit stored in the session (list in the left panel).
 */
public class ConfigEntry {

    private final ComponentDef         definition;
    private final Map<String, String>  values;
    private final long                 createdAt;

    public ConfigEntry(ComponentDef definition) {
        this.definition = definition;
        this.values     = new LinkedHashMap<>();
        this.createdAt  = System.currentTimeMillis();
        // initialise fields with empty strings
        for (FieldDef f : definition.fields) {
            values.put(f.key, "");
        }
    }

    /** Copy constructor for undo support. */
    public ConfigEntry(ConfigEntry source) {
        this.definition = source.definition;
        this.values     = new LinkedHashMap<>(source.values);
        this.createdAt  = source.createdAt;
    }

    public ComponentDef getDefinition()           { return definition; }
    public Map<String, String> getValues()        { return values; }
    public String getValue(String key)            { return values.getOrDefault(key, ""); }
    public void   setValue(String key, String v)  { values.put(key, v != null ? v : ""); }

    /**
     * Display label shown in the sidebar list.
     * Tries "name" field first, then falls back to the component label.
     */
    public String getDisplayLabel() {
        String name = values.getOrDefault("name", "").trim();
        if (!name.isEmpty()) return definition.label + "  [" + name + "]";
        return definition.label;
    }

    /** True if all required fields have a non-blank value. */
    public boolean isComplete() {
        for (FieldDef f : definition.fields) {
            if (f.required && values.getOrDefault(f.key, "").trim().isEmpty()) return false;
        }
        return true;
    }

    public long getCreatedAt() { return createdAt; }

    @Override public String toString() { return getDisplayLabel(); }
}
