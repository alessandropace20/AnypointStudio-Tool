package com.muletool.generator;

import com.muletool.model.ConfigEntry;
import com.muletool.model.ConfigMode;
import java.util.Map;

/**
 * Facade: delegates to GlobalXmlGenerator or FlowXmlGenerator based on mode.
 * Call generate(entry) to get the XML snippet for any ConfigEntry.
 */
public class XmlGenerator {

    public static String generate(ConfigEntry entry) {
        if (entry == null) return "";
        String id = entry.getDefinition().id;
        Map<String, String> v = entry.getValues();
        if (entry.getDefinition().mode == ConfigMode.GLOBAL) {
            return GlobalXmlGenerator.generate(id, v);
        } else {
            return FlowXmlGenerator.generate(id, v);
        }
    }

    /**
     * Builds the complete mule XML document wrapping all entries in the list.
     */
    public static String buildDocument(java.util.List<ConfigEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n");
        sb.append("      xmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\"\n");
        sb.append("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("      xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core\n");
        sb.append("          http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n\n");
        for (ConfigEntry e : entries) {
            sb.append(generate(e)).append("\n");
        }
        sb.append("</mule>\n");
        return sb.toString();
    }
}
