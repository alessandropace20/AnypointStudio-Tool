package com.muletool.model;

import java.util.List;

/**
 * Describes a configurable item (global component or flow activity).
 * Both GlobalComponents and FlowComponents produce instances of this class.
 */
public class ComponentDef {

    public enum Category { CONFIG, HTTP, DATA, SECURITY }

    public final String          id;
    public final String          label;
    public final String          description;
    public final String          badge;       // short tag shown in the sidebar row
    public final Category        category;
    public final ConfigMode      mode;        // GLOBAL or FLOW
    public final List<FieldDef>  fields;

    public ComponentDef(String id, String label, String description,
                        String badge, Category category, ConfigMode mode,
                        List<FieldDef> fields) {
        this.id          = id;
        this.label       = label;
        this.description = description;
        this.badge       = badge;
        this.category    = category;
        this.mode        = mode;
        this.fields      = fields;
    }
}
