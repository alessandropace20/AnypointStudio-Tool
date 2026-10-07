package tool.code.implementation;

import java.util.Map;

import tool.code.type.GlobalComponents;

/**
 * Generates XML snippets for global.xml configurations.
 */
public class GlobalGenerator {

    public static String generate(GlobalComponents.ComponentDef def, Map<String, String> values) {
        switch (def.id) {
            case "config_properties":  return configProperties(values);
            case "global_property":    return globalProperty(values);
            case "http_listener_config": return httpListenerConfig(values);
            case "http_request_config":  return httpRequestConfig(values);
            case "object_store":       return objectStore(values);
            case "db_config":          return dbConfig(values);
            default:                   return "<!-- Component not implemented: " + def.id + " -->";
        }
    }

    private static String configProperties(Map<String, String> v) {
        boolean secure = "true".equals(v.get("secure"));
        String tag = secure
            ? "secure-properties:config"
            : "configuration-properties";
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Configuration Properties"));
        if (secure) {
            sb.append("<secure-properties:config\n");
            sb.append("    xmlns:secure-properties=\"http://www.mulesoft.org/schema/mule/secure-properties\"\n");
        } else {
            sb.append("<configuration-properties\n");
        }
        appendAttr(sb, "name",     v.get("name"),     true);
        appendAttr(sb, "file",     v.get("file"),     true);
        appendAttr(sb, "encoding", v.get("encoding"), false);
        if (secure) {
            sb.append("    doc:name=\"Configuration Properties\"/>\n");
        } else {
            sb.append("    doc:name=\"Configuration Properties\"/>\n");
        }
        return sb.toString();
    }

    private static String globalProperty(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Global Property"));
        sb.append("<global-property\n");
        appendAttr(sb, "name",  v.get("name"),  true);
        appendAttr(sb, "value", v.get("value"), true);
        sb.append("    doc:name=\"Global Property\"/>\n");
        return sb.toString();
    }

    private static String httpListenerConfig(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("HTTP Listener Config"));
        sb.append("<http:listener-config\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        appendAttr(sb, "name", v.get("name"), true);
        sb.append("    doc:name=\"HTTP Listener Config\">\n");
        sb.append("    <http:listener-connection\n");
        appendAttrIndented(sb, "host",     v.get("host"),     true);
        appendAttrIndented(sb, "port",     v.get("port"),     true);
        appendAttrIndented(sb, "protocol", v.get("protocol"), false);
        if (notBlank(v.get("tlsContext"))) {
            appendAttrIndented(sb, "tlsContext", v.get("tlsContext"), false);
        }
        if (notBlank(v.get("connectionIdle"))) {
            appendAttrIndented(sb, "connectionIdleTimeout", v.get("connectionIdle"), false);
        }
        sb.append("    />\n");
        if (notBlank(v.get("basePath"))) {
            sb.append("    <!-- Base path configured per-listener as path prefix -->\n");
            sb.append("    <!-- Suggested path prefix: ").append(v.get("basePath")).append(" -->\n");
        }
        sb.append("</http:listener-config>\n");
        return sb.toString();
    }

    private static String httpRequestConfig(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("HTTP Request Config"));
        sb.append("<http:request-config\n");
        sb.append("    xmlns:http=\"http://www.mulesoft.org/schema/mule/http\"\n");
        appendAttr(sb, "name",     v.get("name"),     true);
        appendAttr(sb, "basePath", v.get("basePath"), false);
        sb.append("    doc:name=\"HTTP Request Config\">\n");
        sb.append("    <http:request-connection\n");
        appendAttrIndented(sb, "host",     v.get("host"),     true);
        appendAttrIndented(sb, "port",     v.get("port"),     true);
        appendAttrIndented(sb, "protocol", v.get("protocol"), false);
        if ("true".equals(v.get("followRedirects"))) {
            sb.append("        followRedirects=\"true\"\n");
        }
        if (notBlank(v.get("proxy"))) {
            sb.append("    >\n");
            sb.append("        <http:proxy-config host=\"").append(esc(v.get("proxy"))).append("\"");
            if (notBlank(v.get("proxyPort"))) sb.append(" port=\"").append(esc(v.get("proxyPort"))).append("\"");
            sb.append("/>\n");
            sb.append("    </http:request-connection>\n");
        } else {
            sb.append("    />\n");
        }
        if (notBlank(v.get("responseTimeout"))) {
            sb.append("    <http:response-timeout-policy\n");
            sb.append("        maxResponse=\"").append(esc(v.get("responseTimeout"))).append("\"\n");
            sb.append("        unit=\"MILLISECONDS\"/>\n");
        }
        sb.append("</http:request-config>\n");
        return sb.toString();
    }

    private static String objectStore(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Object Store Config"));
        sb.append("<os:object-store\n");
        sb.append("    xmlns:os=\"http://www.mulesoft.org/schema/mule/os\"\n");
        appendAttr(sb, "name",          v.get("name"),          true);
        appendAttr(sb, "maxEntries",    v.get("maxEntries"),    false);
        appendAttr(sb, "entryTtl",      v.get("entryTtl"),      false);
        appendAttr(sb, "partitionName", v.get("partitionName"), false);
        if ("true".equals(v.get("persistent"))) {
            sb.append("    persistent=\"true\"\n");
        }
        if (notBlank(v.get("expirationInterval"))) {
            appendAttr(sb, "expirationInterval", v.get("expirationInterval"), false);
            sb.append("    expirationIntervalUnit=\"SECONDS\"\n");
        }
        sb.append("    doc:name=\"Object Store\"/>\n");
        return sb.toString();
    }

    private static String dbConfig(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        sb.append(xmlHeader("Database Config"));
        sb.append("<db:config\n");
        sb.append("    xmlns:db=\"http://www.mulesoft.org/schema/mule/db\"\n");
        appendAttr(sb, "name", v.get("name"), true);
        sb.append("    doc:name=\"Database Config\">\n");

        String dbType = v.getOrDefault("dbType", "Generic");
        String connTag = resolveDbConnectionTag(dbType);
        sb.append("    <").append(connTag).append("\n");
        appendAttrIndented(sb, "host",     v.get("host"),     true);
        appendAttrIndented(sb, "port",     v.get("port"),     true);
        appendAttrIndented(sb, "database", v.get("database"), true);
        appendAttrIndented(sb, "user",     v.get("user"),     true);
        appendAttrIndented(sb, "password", v.get("password"), true);
        if ("Generic".equals(dbType) && notBlank(v.get("driverClass"))) {
            sb.append("        driverClassName=\"").append(esc(v.get("driverClass"))).append("\"\n");
        }
        sb.append("    />\n");

        if (notBlank(v.get("maxPoolSize")) || notBlank(v.get("minPoolSize"))) {
            sb.append("    <db:pooling-profile\n");
            if (notBlank(v.get("maxPoolSize")))
                sb.append("        maxPoolSize=\"").append(esc(v.get("maxPoolSize"))).append("\"\n");
            if (notBlank(v.get("minPoolSize")))
                sb.append("        minPoolSize=\"").append(esc(v.get("minPoolSize"))).append("\"\n");
            if (notBlank(v.get("connectionTimeout")))
                sb.append("        maxWaitMillis=\"").append(esc(v.get("connectionTimeout"))).append("000\"\n");
            sb.append("    />\n");
        }
        sb.append("</db:config>\n");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private static String resolveDbConnectionTag(String dbType) {
        switch (dbType) {
            case "Oracle":               return "db:oracle-connection";
            case "MySQL":                return "db:mysql-connection";
            case "PostgreSQL":           return "db:postgresql-connection";
            case "Microsoft SQL Server": return "db:mssql-connection";
            default:                     return "db:generic-connection";
        }
    }

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

    private static void appendAttrIndented(StringBuilder sb, String attr, String val, boolean required) {
        if (notBlank(val)) {
            sb.append("        ").append(attr).append("=\"").append(esc(val)).append("\"\n");
        } else if (required) {
            sb.append("        ").append(attr).append("=\"\"\n");
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
