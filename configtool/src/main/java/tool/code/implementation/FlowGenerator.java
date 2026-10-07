package tool.code.implementation;

import java.util.Map;

import tool.code.type.FlowComponents;

/**
 * Generates XML snippets for flow XML activities.
 */
public class FlowGenerator {

    public static String generate(FlowComponents.ActivityDef def, Map<String, String> values) {
        switch (def.id) {
            case "flow":         return flow(values);
            case "sub_flow":     return subFlow(values);
            case "http_listener":return httpListener(values);
            case "http_request": return httpRequest(values);
            case "os_retrieve":  return osRetrieve(values);
            case "os_store":     return osStore(values);
            case "os_remove":    return osRemove(values);
            case "db_select":    return dbSelect(values);
            case "db_insert":    return dbInsert(values);
            case "db_update":    return dbUpdate(values);
            case "db_delete":    return dbDelete(values);
            default:             return "<!-- Activity not implemented: " + def.id + " -->";
        }
    }

    // -----------------------------------------------------------------------
    // Flow / Sub-Flow
    // -----------------------------------------------------------------------
    private static String flow(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Flow: " + v.getOrDefault("name", "")));
        sb.append("<flow\n");
        appendAttr(sb, "name", v.get("name"), true);
        appendAttr(sb, "doc:name", v.get("name"), false);
        if (notBlank(v.get("initialState"))) appendAttr(sb, "initialState", v.get("initialState"), false);
        if (notBlank(v.get("maxConcurrency"))) appendAttr(sb, "maxConcurrency", v.get("maxConcurrency"), false);
        sb.append(">\n\n");
        sb.append("    <!-- TODO: add source and processors here -->\n\n");
        sb.append("</flow>\n");
        return sb.toString();
    }

    private static String subFlow(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Sub Flow: " + v.getOrDefault("name", "")));
        sb.append("<sub-flow\n");
        appendAttr(sb, "name", v.get("name"), true);
        appendAttr(sb, "doc:name", v.get("name"), false);
        sb.append(">\n\n");
        sb.append("    <!-- TODO: add processors here -->\n\n");
        sb.append("</sub-flow>\n");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // HTTP
    // -----------------------------------------------------------------------
    private static String httpListener(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("HTTP Listener"));
        sb.append("<http:listener\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        appendAttr(sb, "config-ref",    v.get("configRef"),    true);
        appendAttr(sb, "path",          v.get("path"),         true);
        appendAttr(sb, "allowedMethods",v.get("method"),       false);
        appendAttr(sb, "outputMimeType",v.get("outputMimeType"),false);
        appendAttr(sb, "doc:name",      notBlank(v.get("displayName")) ? v.get("displayName") : "Listener", false);
        sb.append(">\n");
        sb.append("    <http:response statusCode=\"#[vars.httpStatus default 200]\">\n");
        sb.append("        <http:headers>#[vars.outboundHeaders default {}]</http:headers>\n");
        sb.append("    </http:response>\n");
        sb.append("    <http:error-response statusCode=\"#[vars.httpStatus default 500]\">\n");
        sb.append("        <http:body>#[payload]</http:body>\n");
        sb.append("    </http:error-response>\n");
        sb.append("</http:listener>\n");
        return sb.toString();
    }

    private static String httpRequest(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("HTTP Request"));
        sb.append("<http:request\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        appendAttr(sb, "config-ref",   v.get("configRef"), true);
        appendAttr(sb, "method",       v.get("method"),    true);
        appendAttr(sb, "path",         v.get("path"),      true);
        appendAttr(sb, "doc:name",     notBlank(v.get("displayName")) ? v.get("displayName") : "Request", false);
        if (notBlank(v.get("targetValue"))) appendAttr(sb, "target", v.get("targetValue"), false);
        if (notBlank(v.get("outputMimeType"))) appendAttr(sb, "outputMimeType", v.get("outputMimeType"), false);
        if ("true".equals(v.get("followRedirects"))) sb.append("    followRedirects=\"true\"\n");
        sb.append(">\n");
        if (notBlank(v.get("body"))) {
            sb.append("    <http:body><![CDATA[").append(v.get("body")).append("]]></http:body>\n");
        }
        sb.append("</http:request>\n");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Object Store
    // -----------------------------------------------------------------------
    private static String osRetrieve(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Object Store — Retrieve"));
        sb.append("<os:retrieve\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        appendAttr(sb, "key",            v.get("key"),            true);
        appendAttr(sb, "objectStore",    v.get("objectStoreRef"), true);
        appendAttr(sb, "doc:name",       notBlank(v.get("displayName")) ? v.get("displayName") : "Retrieve", false);
        if (notBlank(v.get("targetVariable"))) {
            appendAttr(sb, "target",      v.get("targetVariable"), false);
            appendAttr(sb, "targetValue", notBlank(v.get("targetValue")) ? v.get("targetValue") : "#[payload]", false);
        }
        sb.append(">\n");
        if (notBlank(v.get("defaultValue"))) {
            sb.append("    <os:default-value><![CDATA[").append(v.get("defaultValue")).append("]]></os:default-value>\n");
        }
        sb.append("</os:retrieve>\n");
        return sb.toString();
    }

    private static String osStore(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Object Store — Store"));
        sb.append("<os:store\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        appendAttr(sb, "key",           v.get("key"),            true);
        appendAttr(sb, "objectStore",   v.get("objectStoreRef"), true);
        if ("true".equals(v.get("failIfPresent"))) sb.append("    failIfPresent=\"true\"\n");
        appendAttr(sb, "doc:name",      notBlank(v.get("displayName")) ? v.get("displayName") : "Store", false);
        sb.append(">\n");
        if (notBlank(v.get("value"))) {
            sb.append("    <os:value><![CDATA[").append(v.get("value")).append("]]></os:value>\n");
        }
        sb.append("</os:store>\n");
        return sb.toString();
    }

    private static String osRemove(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Object Store — Remove"));
        sb.append("<os:remove\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        appendAttr(sb, "key",         v.get("key"),            true);
        appendAttr(sb, "objectStore", v.get("objectStoreRef"), true);
        if ("true".equals(v.get("failIfAbsent"))) sb.append("    failIfAbsent=\"true\"\n");
        appendAttr(sb, "doc:name",    notBlank(v.get("displayName")) ? v.get("displayName") : "Remove", false);
        sb.append("/>\n");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Database
    // -----------------------------------------------------------------------
    private static String dbSelect(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("DB Select"));
        sb.append("<db:select\n");
        sb.append("    xmlns:db=\"http://www.mulesoft.org/schema/mule/db\"\n");
        appendAttr(sb, "config-ref", v.get("configRef"), true);
        appendAttr(sb, "doc:name",   notBlank(v.get("displayName")) ? v.get("displayName") : "Select", false);
        if (notBlank(v.get("fetchSize"))) appendAttr(sb, "fetchSize", v.get("fetchSize"), false);
        if (notBlank(v.get("maxRows")))   appendAttr(sb, "maxRows",   v.get("maxRows"),   false);
        if (notBlank(v.get("targetVariable"))) appendAttr(sb, "target", v.get("targetVariable"), false);
        sb.append(">\n");
        sb.append("    <db:sql><![CDATA[\n        ");
        sb.append(v.getOrDefault("sql", "SELECT * FROM table_name"));
        sb.append("\n    ]]></db:sql>\n");
        sb.append("</db:select>\n");
        return sb.toString();
    }

    private static String dbInsert(Map<String, String> v) {
        return dbDml("insert", "Insert", v);
    }

    private static String dbUpdate(Map<String, String> v) {
        return dbDml("update", "Update", v);
    }

    private static String dbDelete(Map<String, String> v) {
        return dbDml("delete", "Delete", v);
    }

    private static String dbDml(String op, String label, Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("DB " + label));
        sb.append("<db:").append(op).append("\n");
        sb.append("    xmlns:db=\"http://www.mulesoft.org/schema/mule/db\"\n");
        appendAttr(sb, "config-ref", v.get("configRef"), true);
        appendAttr(sb, "doc:name",   notBlank(v.get("displayName")) ? v.get("displayName") : label, false);
        if (notBlank(v.get("targetVariable"))) appendAttr(sb, "target", v.get("targetVariable"), false);
        sb.append(">\n");
        sb.append("    <db:sql><![CDATA[\n        ");
        sb.append(v.getOrDefault("sql", ""));
        sb.append("\n    ]]></db:sql>\n");
        sb.append("</db:").append(op).append(">\n");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private static String xmlHeader(String title) {
        return "<!-- ===== " + title + " — generated by MuleXML Tool ===== -->\n";
    }

    private static void appendAttr(StringBuilder sb, String attr, String val, boolean required) {
        if (notBlank(val)) {
            sb.append("    ").append(attr).append("=\"").append(esc(val)).append("\"\n");
        } else if (required) {
            sb.append("    ").append(attr).append("=\"\"\n");
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
