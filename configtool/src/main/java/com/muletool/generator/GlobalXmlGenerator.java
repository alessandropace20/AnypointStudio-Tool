package com.muletool.generator;

import java.util.Map;

/** Generates XML snippets for global.xml components. */
public class GlobalXmlGenerator {

    public static String generate(String id, Map<String, String> v) {
        switch (id) {
            case "config_properties":    return configProperties(v);
            case "global_property":      return globalProperty(v);
            case "http_listener_config": return httpListenerConfig(v);
            case "http_request_config":  return httpRequestConfig(v);
            case "object_store":         return objectStore(v);
            case "db_config":            return dbConfig(v);
            default: return "<!-- Unknown global component: " + id + " -->\n";
        }
    }

    // ── Config Properties ────────────────────────────────────────────────
    private static String configProperties(Map<String, String> v) {
        boolean secure = "true".equals(v.get("secure"));
        StringBuilder sb = new StringBuilder();
        sb.append(hdr("Configuration Properties"));
        if (secure) {
            sb.append("<secure-properties:config\n");
            sb.append("    xmlns:secure-properties=\"http://www.mulesoft.org/schema/mule/secure-properties\"\n");
        } else {
            sb.append("<configuration-properties\n");
        }
        attr(sb, "name",     v.get("name"),     true);
        attr(sb, "file",     v.get("file"),     true);
        attr(sb, "encoding", v.get("encoding"), false);
        sb.append("    doc:name=\"Configuration Properties\"/>\n");
        return sb.toString();
    }

    // ── Global Property ──────────────────────────────────────────────────
    private static String globalProperty(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(hdr("Global Property"));
        sb.append("<global-property\n");
        attr(sb, "name",  v.get("name"),  true);
        attr(sb, "value", v.get("value"), true);
        sb.append("    doc:name=\"Global Property\"/>\n");
        return sb.toString();
    }

    // ── HTTP Listener Config ─────────────────────────────────────────────
    private static String httpListenerConfig(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(hdr("HTTP Listener Config"));
        sb.append("<http:listener-config\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        attr(sb, "name", v.get("name"), true);
        sb.append("    doc:name=\"HTTP Listener Config\">\n");
        sb.append("    <http:listener-connection\n");
        attr2(sb, "host",     v.get("host"),     true);
        attr2(sb, "port",     v.get("port"),     true);
        attr2(sb, "protocol", v.get("protocol"), false);
        if (ok(v.get("tlsContext")))    attr2(sb, "tlsContext",            v.get("tlsContext"),    false);
        if (ok(v.get("connectionIdle"))) attr2(sb, "connectionIdleTimeout", v.get("connectionIdle"), false);
        sb.append("    />\n");
        if (ok(v.get("basePath")))
            sb.append("    <!-- Suggested base path prefix: ").append(esc(v.get("basePath"))).append(" -->\n");
        sb.append("</http:listener-config>\n");
        return sb.toString();
    }

    // ── HTTP Request Config ──────────────────────────────────────────────
    private static String httpRequestConfig(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(hdr("HTTP Request Config"));
        sb.append("<http:request-config\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        attr(sb, "name",     v.get("name"),     true);
        attr(sb, "basePath", v.get("basePath"), false);
        sb.append("    doc:name=\"HTTP Request Config\">\n");
        sb.append("    <http:request-connection\n");
        attr2(sb, "host",     v.get("host"),     true);
        attr2(sb, "port",     v.get("port"),     true);
        attr2(sb, "protocol", v.get("protocol"), false);
        if ("true".equals(v.get("followRedirects"))) sb.append("        followRedirects=\"true\"\n");
        boolean hasProxy = ok(v.get("proxy"));
        sb.append(hasProxy ? "    >\n" : "    />\n");
        if (hasProxy) {
            sb.append("        <http:proxy-config host=\"").append(esc(v.get("proxy"))).append("\"");
            if (ok(v.get("proxyPort"))) sb.append(" port=\"").append(esc(v.get("proxyPort"))).append("\"");
            sb.append("/>\n");
            sb.append("    </http:request-connection>\n");
        }
        if (ok(v.get("responseTimeout"))) {
            sb.append("    <http:response-timeout-policy\n");
            sb.append("        maxResponse=\"").append(esc(v.get("responseTimeout"))).append("\"\n");
            sb.append("        unit=\"MILLISECONDS\"/>\n");
        }
        sb.append("</http:request-config>\n");
        return sb.toString();
    }

    // ── Object Store ─────────────────────────────────────────────────────
    private static String objectStore(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(hdr("Object Store Config"));
        sb.append("<os:object-store\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        attr(sb, "name",          v.get("name"),          true);
        attr(sb, "maxEntries",    v.get("maxEntries"),    false);
        attr(sb, "entryTtl",      v.get("entryTtl"),      false);
        attr(sb, "partitionName", v.get("partitionName"), false);
        if ("true".equals(v.get("persistent"))) sb.append("    persistent=\"true\"\n");
        if (ok(v.get("expirationInterval"))) {
            attr(sb, "expirationInterval",     v.get("expirationInterval"), false);
            sb.append("    expirationIntervalUnit=\"SECONDS\"\n");
        }
        sb.append("    doc:name=\"Object Store\"/>\n");
        return sb.toString();
    }

    // ── Database Config ──────────────────────────────────────────────────
    private static String dbConfig(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(hdr("Database Config"));
        sb.append("<db:config\n");
        sb.append("    xmlns:db=\"http://www.mulesoft.org/schema/mule/db\"\n");
        attr(sb, "name", v.get("name"), true);
        sb.append("    doc:name=\"Database Config\">\n");
        String type = v.getOrDefault("dbType", "Generic");
        sb.append("    <").append(dbTag(type)).append("\n");
        attr2(sb, "host",     v.get("host"),     true);
        attr2(sb, "port",     v.get("port"),     true);
        attr2(sb, "database", v.get("database"), true);
        attr2(sb, "user",     v.get("user"),     true);
        attr2(sb, "password", v.get("password"), true);
        if ("Generic".equals(type) && ok(v.get("driverClass")))
            sb.append("        driverClassName=\"").append(esc(v.get("driverClass"))).append("\"\n");
        sb.append("    />\n");
        if (ok(v.get("maxPoolSize")) || ok(v.get("minPoolSize"))) {
            sb.append("    <db:pooling-profile\n");
            if (ok(v.get("maxPoolSize"))) sb.append("        maxPoolSize=\"").append(esc(v.get("maxPoolSize"))).append("\"\n");
            if (ok(v.get("minPoolSize"))) sb.append("        minPoolSize=\"").append(esc(v.get("minPoolSize"))).append("\"\n");
            if (ok(v.get("connectionTimeout")))
                sb.append("        maxWaitMillis=\"").append(Long.parseLong(v.get("connectionTimeout").trim()) * 1000).append("\"\n");
            sb.append("    />\n");
        }
        sb.append("</db:config>\n");
        return sb.toString();
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private static String dbTag(String t) {
        switch (t) {
            case "Oracle":               return "db:oracle-connection";
            case "MySQL":                return "db:mysql-connection";
            case "PostgreSQL":           return "db:postgresql-connection";
            case "Microsoft SQL Server": return "db:mssql-connection";
            default:                     return "db:generic-connection";
        }
    }
    private static String hdr(String t) { return "<!-- ===== " + t + " ===== -->\n"; }
    private static void attr(StringBuilder sb, String k, String v, boolean req) {
        if (ok(v)) sb.append("    ").append(k).append("=\"").append(esc(v)).append("\"\n");
        else if (req) sb.append("    ").append(k).append("=\"\"\n");
    }
    private static void attr2(StringBuilder sb, String k, String v, boolean req) {
        if (ok(v)) sb.append("        ").append(k).append("=\"").append(esc(v)).append("\"\n");
        else if (req) sb.append("        ").append(k).append("=\"\"\n");
    }
    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
    static boolean ok(String s) { return s != null && !s.trim().isEmpty(); }
}
