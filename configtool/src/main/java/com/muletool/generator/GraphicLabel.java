package com.muletool.generator;

import java.awt.Color;

/**
 * Defines the visual appearance of a component in the graphic preview panel.
 * Each component id is mapped to a label, icon text, and colour scheme.
 * Add entries here when new components are defined in the model registries.
 */
public class GraphicLabel {

    public final String componentId;
    public final String title;       // short display title on the card
    public final String iconText;    // 2-3 char icon shown in the coloured chip
    public final Color  chipColor;   // background of the icon chip
    public final Color  borderColor; // border of the card
    public final String namespace;   // e.g. "http:", "db:", "os:"

    public GraphicLabel(String componentId, String title, String iconText,
                        Color chipColor, Color borderColor, String namespace) {
        this.componentId = componentId;
        this.title       = title;
        this.iconText    = iconText;
        this.chipColor   = chipColor;
        this.borderColor = borderColor;
        this.namespace   = namespace;
    }

    // ── Palette ──────────────────────────────────────────────────────────
    private static final Color BLUE_CHIP   = new Color(0,  120, 215);
    private static final Color BLUE_BORDER = new Color(0,   90, 170);
    private static final Color GREEN_CHIP  = new Color(39, 174,  96);
    private static final Color GREEN_BDR   = new Color(25, 130,  65);
    private static final Color ORANGE_CHIP = new Color(230,126,  34);
    private static final Color ORANGE_BDR  = new Color(180, 90,  20);
    private static final Color GRAY_CHIP   = new Color(90, 100, 120);
    private static final Color GRAY_BDR    = new Color(60,  68,  82);
    private static final Color TEAL_CHIP   = new Color(0,  150, 136);
    private static final Color TEAL_BDR    = new Color(0,  110, 100);
    private static final Color RED_CHIP    = new Color(192,  57,  43);
    private static final Color RED_BDR     = new Color(140,  30,  20);

    // ── Registry ─────────────────────────────────────────────────────────
    private static final java.util.Map<String, GraphicLabel> MAP = new java.util.HashMap<>();

    static {
        // Global
        reg("config_properties",    "Config Properties",  "CFG", GRAY_CHIP,   GRAY_BDR,   "");
        reg("global_property",      "Global Property",    "PRO", GRAY_CHIP,   GRAY_BDR,   "");
        reg("http_listener_config", "HTTP Listener Cfg",  "LST", BLUE_CHIP,   BLUE_BORDER,"http:");
        reg("http_request_config",  "HTTP Request Cfg",   "REQ", BLUE_CHIP,   BLUE_BORDER,"http:");
        reg("object_store",         "Object Store Cfg",   "OS",  TEAL_CHIP,   TEAL_BDR,   "os:");
        reg("db_config",            "Database Config",    "DB",  ORANGE_CHIP, ORANGE_BDR, "db:");

        // Flow
        reg("flow",         "Flow",              "FLW", GRAY_CHIP,   GRAY_BDR,   "");
        reg("sub_flow",     "Sub Flow",          "SUB", GRAY_CHIP,   GRAY_BDR,   "");
        reg("http_listener","HTTP Listener",     "LST", BLUE_CHIP,   BLUE_BORDER,"http:");
        reg("http_request", "HTTP Request",      "REQ", BLUE_CHIP,   BLUE_BORDER,"http:");
        reg("os_retrieve",  "OS Retrieve",       "OS↓", TEAL_CHIP,   TEAL_BDR,   "os:");
        reg("os_store",     "OS Store",          "OS↑", TEAL_CHIP,   TEAL_BDR,   "os:");
        reg("os_remove",    "OS Remove",         "OS✕", RED_CHIP,    RED_BDR,    "os:");
        reg("db_select",    "DB Select",         "SEL", GREEN_CHIP,  GREEN_BDR,  "db:");
        reg("db_insert",    "DB Insert",         "INS", ORANGE_CHIP, ORANGE_BDR, "db:");
        reg("db_update",    "DB Update",         "UPD", ORANGE_CHIP, ORANGE_BDR, "db:");
        reg("db_delete",    "DB Delete",         "DEL", RED_CHIP,    RED_BDR,    "db:");
    }

    private static void reg(String id, String title, String icon,
                             Color chip, Color border, String ns) {
        MAP.put(id, new GraphicLabel(id, title, icon, chip, border, ns));
    }

    /** Returns the GraphicLabel for a given component id, or a default if unknown. */
    public static GraphicLabel of(String componentId) {
        return MAP.getOrDefault(componentId,
            new GraphicLabel(componentId, componentId, "?", GRAY_CHIP, GRAY_BDR, ""));
    }
}
