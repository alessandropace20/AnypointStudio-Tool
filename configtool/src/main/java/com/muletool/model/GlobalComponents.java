package com.muletool.model;

import java.util.*;
import static com.muletool.model.FieldDef.*;
import static com.muletool.model.ComponentDef.Category.*;

/**
 * Registry of all configurable properties/components for global.xml.
 * Add or remove entries here to extend the tool's global configuration support.
 */
public class GlobalComponents {

    private static final List<ComponentDef> REGISTRY = new ArrayList<>();

    static {
        // ── Configuration Properties ────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "config_properties",
            "Configuration Properties",
            "Carica proprietà da file .yaml/.properties",
            "CFG", ComponentDef.Category.CONFIG, ConfigMode.GLOBAL,
            Arrays.asList(
                text   ("name",     "Config Name",  "e.g. appProperties",      true,  "Nome del bean di configurazione"),
                text   ("file",     "File Path",    "e.g. config-${env}.yaml", true,  "Percorso del file (supporta placeholder ${env})"),
                combo  ("encoding", "Encoding",     new String[]{"UTF-8","ISO-8859-1","US-ASCII"}, false, "Encoding del file"),
                checkbox("secure",  "Secure File",  "Abilita Secure Configuration Properties")
            )
        ));

        // ── Global Property ──────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "global_property",
            "Global Property",
            "Variabile globale riutilizzabile in tutto il progetto",
            "PROP", ComponentDef.Category.CONFIG, ConfigMode.GLOBAL,
            Arrays.asList(
                text("name",  "Property Name", "e.g. apiVersion", true,  "Nome della proprietà"),
                text("value", "Value",          "e.g. v1",         true,  "Valore della proprietà")
            )
        ));

        // ── HTTP Listener Config ─────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "http_listener_config",
            "HTTP Listener Config",
            "Configurazione globale per HTTP Listener",
            "HTTP", ComponentDef.Category.HTTP, ConfigMode.GLOBAL,
            Arrays.asList(
                text    ("name",           "Config Name",    "e.g. HTTP_Listener_config", true,  "Nome della configurazione"),
                text    ("host",           "Host",           "0.0.0.0",                   true,  "Indirizzo di ascolto"),
                text    ("port",           "Port",           "8081",                       true,  "Porta di ascolto"),
                text    ("basePath",       "Base Path",      "/api",                       false, "Prefisso comune a tutti i listener"),
                combo   ("protocol",       "Protocol",       new String[]{"HTTP","HTTPS"}, false, "Protocollo"),
                text    ("tlsContext",     "TLS Context Ref","",                            false, "Riferimento TLS context (HTTPS)"),
                spinner ("connectionIdle", "Connection Idle Timeout (ms)", "30000",         "Timeout connessione idle in ms")
            )
        ));

        // ── HTTP Request Config ──────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "http_request_config",
            "HTTP Request Config",
            "Configurazione globale per HTTP Request (client)",
            "REQ", ComponentDef.Category.HTTP, ConfigMode.GLOBAL,
            Arrays.asList(
                text    ("name",            "Config Name",         "e.g. HTTP_Request_config",  true,  "Nome della configurazione"),
                text    ("host",            "Host",                "e.g. api.example.com",      true,  "Host del servizio remoto"),
                text    ("port",            "Port",                "443",                        true,  "Porta del servizio remoto"),
                combo   ("protocol",        "Protocol",            new String[]{"HTTP","HTTPS"}, false, "Protocollo"),
                text    ("basePath",        "Base Path",           "/api/v1",                   false, "Base path comune a tutte le request"),
                checkbox("followRedirects", "Follow Redirects",    "Segui redirect automaticamente"),
                combo   ("sendBodyMode",    "Send Body Mode",       new String[]{"AUTO","ALWAYS","NEVER"}, false, "Modalità invio body"),
                spinner ("responseTimeout", "Response Timeout (ms)","30000",                    "Timeout risposta in ms"),
                text    ("proxy",           "Proxy Host",          "",                           false, "Host proxy (opzionale)"),
                text    ("proxyPort",       "Proxy Port",          "",                           false, "Porta proxy (opzionale)")
            )
        ));

        // ── Object Store Config ──────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "object_store",
            "Object Store Config",
            "Object Store condiviso a livello di applicazione",
            "OS", ComponentDef.Category.DATA, ConfigMode.GLOBAL,
            Arrays.asList(
                text    ("name",               "Config Name",           "e.g. appObjectStore", true,  "Nome dell'Object Store"),
                spinner ("maxEntries",          "Max Entries",           "1000",                "Numero massimo di entry"),
                spinner ("entryTtl",            "Entry TTL (sec)",       "3600",                "Time-to-live delle entry in secondi"),
                checkbox("persistent",          "Persistent",            "Persistente su disco"),
                text    ("partitionName",       "Partition Name",        "e.g. myPartition",   false, "Nome della partizione"),
                spinner ("expirationInterval",  "Expiration Interval (sec)", "3600",            "Intervallo di controllo scadenza")
            )
        ));

        // ── Database Config ──────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "db_config",
            "Database Config",
            "Configurazione connessione al database",
            "DB", ComponentDef.Category.DATA, ConfigMode.GLOBAL,
            Arrays.asList(
                text    ("name",              "Config Name",          "e.g. Database_Config",         true,  "Nome della configurazione DB"),
                combo   ("dbType",            "DB Type",              new String[]{"Oracle","MySQL","PostgreSQL","Microsoft SQL Server","Generic"}, true, "Tipo di database"),
                text    ("host",              "Host",                 "e.g. db.example.com",           true,  "Host del database"),
                text    ("port",              "Port",                 "1521",                          true,  "Porta del database"),
                text    ("database",          "Database / SID",       "e.g. ORCL",                    true,  "Nome del database o SID Oracle"),
                text    ("user",              "Username",             "e.g. app_user",                 true,  "Utente del database"),
                password("password",          "Password",             true,                            "Password del database"),
                spinner ("maxPoolSize",        "Max Pool Size",        "10",                            "Dimensione massima del connection pool"),
                spinner ("minPoolSize",        "Min Pool Size",        "1",                             "Dimensione minima del connection pool"),
                spinner ("connectionTimeout",  "Connection Timeout (sec)", "30",                       "Timeout connessione al DB in secondi"),
                text    ("driverClass",        "Driver Class",         "e.g. oracle.jdbc.OracleDriver", false, "Classe JDBC driver (solo tipo Generic)")
            )
        ));
    }

    public static List<ComponentDef> getAll() { return Collections.unmodifiableList(REGISTRY); }
    public static ComponentDef findById(String id) {
        return REGISTRY.stream().filter(c -> c.id.equals(id)).findFirst().orElse(null);
    }
}
