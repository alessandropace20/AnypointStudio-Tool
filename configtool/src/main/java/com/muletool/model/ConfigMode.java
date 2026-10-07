package com.muletool.model;

/** Represents the two operating modes of the tool. */
public enum ConfigMode {
    GLOBAL("Global XML", "global.xml"),
    FLOW("Flow XML",     "flow.xml");

    public final String label;
    public final String defaultFileName;

    ConfigMode(String label, String defaultFileName) {
        this.label = label;
        this.defaultFileName = defaultFileName;
    }
}
