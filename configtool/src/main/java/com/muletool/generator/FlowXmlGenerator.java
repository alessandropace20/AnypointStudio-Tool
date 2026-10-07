package com.muletool.generator;

import java.util.Map;
import static com.muletool.generator.GlobalXmlGenerator.*;

/** Generates XML snippets for flow XML activities. */
public class FlowXmlGenerator {

    public static String generate(String id, Map<String, String> v) {
        switch (id) {
            case "flow":         return flow(v);
            case "sub_flow":     return subFlow(v);
            case "http_listener":return httpListener(v);
            case "http_request": return httpRequest(v);
            case "os_retrieve":  return osRetrieve(v);
            case "os_store":     return osStore(v);
            case "os_remove":    return osRemove(v);
            case "db_select":    return dbOp("select", "Select", v, true);
            case "db_insert":    return dbOp("insert", "Insert", v, false);
            case "db_update":    return dbOp("update", "Update", v, false);
            case "db_delete":    return dbOp("delete", "Delete", v, false);
            default: return "<!-- Unknown flow activity: " + id + " -->\n";
        }
    }

    private static String flow(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ===== Flow: ").append(esc(v.getOrDefault("name",""))).append(" ===== -->\n");
        sb.append("<flow\n");
        a(sb, "name", v.get("name"), true);
        a(sb, "doc:name", v.get("name"), false);
        if (ok(v.get("initialState")))  a(sb, "initialState",  v.get("initialState"),  false);
        if (ok(v.get("maxConcurrency"))) a(sb, "maxConcurrency", v.get("maxConcurrency"), false);
        sb.append(">\n    <!-- TODO: add source and processors -->\n</flow>\n");
        return sb.toString();
    }

    private static String subFlow(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ===== Sub Flow: ").append(esc(v.getOrDefault("name",""))).append(" ===== -->\n");
        sb.append("<sub-flow\n");
        a(sb, "name", v.get("name"), true);
        a(sb, "doc:name", v.get("name"), false);
        sb.append(">\n    <!-- TODO: add processors -->\n</sub-flow>\n");
        return sb.toString();
    }

    private static String httpListener(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ===== HTTP Listener ===== -->\n");
        sb.append("<http:listener\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        a(sb, "config-ref",    v.get("configRef"),    true);
        a(sb, "path",          v.get("path"),         true);
        a(sb, "allowedMethods",v.get("method"),       false);
        a(sb, "outputMimeType",v.get("outputMimeType"),false);
        a(sb, "doc:name",      ok(v.get("displayName")) ? v.get("displayName") : "Listener", false);
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
        sb.append("<!-- ===== HTTP Request ===== -->\n");
        sb.append("<http:request\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        a(sb, "config-ref",   v.get("configRef"), true);
        a(sb, "method",       v.get("method"),    true);
        a(sb, "path",         v.get("path"),      true);
        a(sb, "doc:name",     ok(v.get("displayName")) ? v.get("displayName") : "Request", false);
        if (ok(v.get("targetValue")))    a(sb, "target",        v.get("targetValue"),   false);
        if (ok(v.get("outputMimeType"))) a(sb, "outputMimeType",v.get("outputMimeType"),false);
        if ("true".equals(v.get("followRedirects"))) sb.append("    followRedirects=\"true\"\n");
        sb.append(">\n");
        if (ok(v.get("body")))
            sb.append("    <http:body><![CDATA[").append(v.get("body")).append("]]></http:body>\n");
        sb.append("</http:request>\n");
        return sb.toString();
    }

    private static String osRetrieve(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ===== Object Store — Retrieve ===== -->\n");
        sb.append("<os:retrieve\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        a(sb, "key",         v.get("key"),            true);
        a(sb, "objectStore", v.get("objectStoreRef"), true);
        a(sb, "doc:name",    ok(v.get("displayName")) ? v.get("displayName") : "Retrieve", false);
        if (ok(v.get("targetVariable"))) {
            a(sb, "target",      v.get("targetVariable"), false);
            a(sb, "targetValue", ok(v.get("targetValue")) ? v.get("targetValue") : "#[payload]", false);
        }
        sb.append(">\n");
        if (ok(v.get("defaultValue")))
            sb.append("    <os:default-value><![CDATA[").append(v.get("defaultValue")).append("]]></os:default-value>\n");
        sb.append("</os:retrieve>\n");
        return sb.toString();
    }

    private static String osStore(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ===== Object Store — Store ===== -->\n");
        sb.append("<os:store\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        a(sb, "key",         v.get("key"),            true);
        a(sb, "objectStore", v.get("objectStoreRef"), true);
        if ("true".equals(v.get("failIfPresent"))) sb.append("    failIfPresent=\"true\"\n");
        a(sb, "doc:name", ok(v.get("displayName")) ? v.get("displayName") : "Store", false);
        sb.append(">\n");
        if (ok(v.get("value")))
            sb.append("    <os:value><![CDATA[").append(v.get("value")).append("]]></os:value>\n");
        sb.append("</os:store>\n");
        return sb.toString();
    }

    private static String osRemove(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ===== Object Store — Remove ===== -->\n");
        sb.append("<os:remove\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        a(sb, "key",         v.get("key"),            true);
        a(sb, "objectStore", v.get("objectStoreRef"), true);
        if ("true".equals(v.get("failIfAbsent"))) sb.append("    failIfAbsent=\"true\"\n");
        a(sb, "doc:name", ok(v.get("displayName")) ? v.get("displayName") : "Remove", false);
        sb.append("/>\n");
        return sb.toString();
    }

    private static String dbOp(String op, String label, Map<String, String> v, boolean hasSelectOpts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ===== DB ").append(label).append(" ===== -->\n");
        sb.append("<db:").append(op).append("\n");
        sb.append("    xmlns:db=\"http://www.mulesoft.org/schema/mule/db\"\n");
        a(sb, "config-ref", v.get("configRef"), true);
        a(sb, "doc:name",   ok(v.get("displayName")) ? v.get("displayName") : label, false);
        if (hasSelectOpts) {
            if (ok(v.get("fetchSize"))) a(sb, "fetchSize", v.get("fetchSize"), false);
            if (ok(v.get("maxRows")))   a(sb, "maxRows",   v.get("maxRows"),   false);
        }
        if (ok(v.get("targetVariable"))) a(sb, "target", v.get("targetVariable"), false);
        sb.append(">\n");
        sb.append("    <db:sql><![CDATA[\n        ");
        sb.append(v.getOrDefault("sql", "")).append("\n    ]]></db:sql>\n");
        sb.append("</db:").append(op).append(">\n");
        return sb.toString();
    }

    private static void a(StringBuilder sb, String k, String v, boolean req) {
        if (ok(v)) sb.append("    ").append(k).append("=\"").append(esc(v)).append("\"\n");
        else if (req) sb.append("    ").append(k).append("=\"\"\n");
    }
}
