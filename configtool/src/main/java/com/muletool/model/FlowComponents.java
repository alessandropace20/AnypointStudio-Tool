package com.muletool.model;

import java.util.*;
import static com.muletool.model.FieldDef.*;
import static com.muletool.model.ComponentDef.Category.*;

/**
 * Registry of all configurable activities for flow XML files.
 * Add or remove entries here to extend the tool's flow activity support.
 */
public class FlowComponents {

    private static final List<ComponentDef> REGISTRY = new ArrayList<>();

    static {
        // ── Flow ─────────────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "flow", "Flow",
            "Definizione di un flusso principale",
            "FLW", CONFIG, ConfigMode.FLOW,
            Arrays.asList(
                text   ("name",          "Flow Name",      "e.g. main-flow",  true,  "Nome univoco del flusso"),
                combo  ("initialState",  "Initial State",  new String[]{"started","stopped"}, false, "Stato iniziale"),
                spinner("maxConcurrency","Max Concurrency","1",  "Massima concorrenza (0 = illimitata)")
            )
        ));

        // ── Sub Flow ─────────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "sub_flow", "Sub Flow",
            "Sotto-flusso richiamabile tramite Flow Reference",
            "SUB", CONFIG, ConfigMode.FLOW,
            Arrays.asList(
                text("name", "Sub Flow Name", "e.g. common-subflow", true, "Nome univoco del sub-flow")
            )
        ));

        // ── HTTP Listener ────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "http_listener", "HTTP Listener",
            "Sorgente HTTP: espone un endpoint REST",
            "HTTP", HTTP, ConfigMode.FLOW,
            Arrays.asList(
                text ("configRef",       "Config Ref",       "HTTP_Listener_config", true,  "Riferimento alla HTTP Listener Config globale"),
                text ("path",            "Path",             "e.g. /orders/{id}",    true,  "Percorso endpoint (supporta URI template)"),
                combo("method",          "Allowed Methods",  new String[]{"GET","POST","PUT","PATCH","DELETE","GET,POST","ALL"}, false, "Metodi HTTP accettati"),
                text ("outputMimeType",  "Output MIME Type", "application/json",     false, "MIME type della risposta"),
                text ("displayName",     "Display Name",     "",                     false, "Nome nel canvas")
            )
        ));

        // ── HTTP Request ─────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "http_request", "HTTP Request",
            "Chiamata HTTP verso un servizio esterno",
            "REQ", HTTP, ConfigMode.FLOW,
            Arrays.asList(
                text    ("configRef",      "Config Ref",      "HTTP_Request_config",  true,  "Riferimento alla HTTP Request Config globale"),
                combo   ("method",         "Method",          new String[]{"GET","POST","PUT","PATCH","DELETE"}, true, "Metodo HTTP"),
                text    ("path",           "Path",            "e.g. /users/{id}",     true,  "Path relativo (aggiunto al base path)"),
                textarea("body",           "Body",            "e.g. payload",         false, "Espressione DataWeave per il body"),
                text    ("outputMimeType", "Output MIME Type","application/json",      false, "MIME type atteso in risposta"),
                checkbox("followRedirects","Follow Redirects","Segui redirect automaticamente"),
                text    ("targetValue",    "Target Value",    "#[payload]",           false, "Dove salvare il risultato"),
                text    ("displayName",    "Display Name",    "",                     false, "Nome nel canvas")
            )
        ));

        // ── Object Store — Retrieve ──────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "os_retrieve", "Object Store — Retrieve",
            "Recupera un valore dall'Object Store",
            "OS", DATA, ConfigMode.FLOW,
            Arrays.asList(
                text("objectStoreRef", "Object Store Ref", "e.g. appObjectStore", true,  "Nome dell'Object Store globale"),
                text("key",            "Key",              "e.g. #[vars.id]",     true,  "Chiave da cercare"),
                text("defaultValue",   "Default Value",   "",                     false, "Valore di default se assente"),
                text("targetVariable", "Target Variable", "e.g. osResult",        false, "Variabile Mule per il risultato"),
                text("targetValue",    "Target Value",    "#[payload]",           false, "Espressione target value"),
                text("displayName",    "Display Name",    "",                     false, "Nome nel canvas")
            )
        ));

        // ── Object Store — Store ─────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "os_store", "Object Store — Store",
            "Salva un valore nell'Object Store",
            "OS", DATA, ConfigMode.FLOW,
            Arrays.asList(
                text    ("objectStoreRef", "Object Store Ref", "e.g. appObjectStore", true,  "Nome dell'Object Store globale"),
                text    ("key",            "Key",              "e.g. #[vars.id]",     true,  "Chiave di salvataggio"),
                text    ("value",          "Value",            "e.g. #[payload]",     true,  "Valore da salvare"),
                checkbox("failIfPresent",  "Fail If Present",  "Fallisce se la chiave esiste già"),
                text    ("displayName",    "Display Name",     "",                    false, "Nome nel canvas")
            )
        ));

        // ── Object Store — Remove ────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "os_remove", "Object Store — Remove",
            "Rimuove una chiave dall'Object Store",
            "OS", DATA, ConfigMode.FLOW,
            Arrays.asList(
                text    ("objectStoreRef", "Object Store Ref", "e.g. appObjectStore", true,  "Nome dell'Object Store globale"),
                text    ("key",            "Key",              "e.g. #[vars.id]",     true,  "Chiave da eliminare"),
                checkbox("failIfAbsent",   "Fail If Absent",   "Fallisce se la chiave non esiste"),
                text    ("displayName",    "Display Name",     "",                    false, "Nome nel canvas")
            )
        ));

        // ── DB Select ────────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "db_select", "Database — Select",
            "Esegue una query SELECT",
            "DB", DATA, ConfigMode.FLOW,
            Arrays.asList(
                text    ("configRef",      "Config Ref",     "Database_Config",  true,  "Riferimento alla DB Config globale"),
                textarea("sql",            "SQL Query",      "SELECT * FROM …",  true,  "Query SQL (:paramName per i parametri)"),
                spinner ("fetchSize",      "Fetch Size",     "10",               "Numero di righe per fetch"),
                spinner ("maxRows",        "Max Rows",       "0",                "Limite righe (0 = illimitato)"),
                text    ("targetVariable", "Target Variable","e.g. dbResult",    false, "Variabile per il risultato"),
                text    ("displayName",    "Display Name",   "",                 false, "Nome nel canvas")
            )
        ));

        // ── DB Insert ────────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "db_insert", "Database — Insert",
            "Esegue un INSERT",
            "DB", DATA, ConfigMode.FLOW,
            Arrays.asList(
                text    ("configRef",      "Config Ref",   "Database_Config", true,  "Riferimento alla DB Config globale"),
                textarea("sql",            "SQL Statement","INSERT INTO …",   true,  "Statement SQL (:paramName per i parametri)"),
                text    ("targetVariable", "Target Var",  "",                 false, "Variabile per il risultato"),
                text    ("displayName",    "Display Name","",                 false, "Nome nel canvas")
            )
        ));

        // ── DB Update ────────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "db_update", "Database — Update",
            "Esegue un UPDATE",
            "DB", DATA, ConfigMode.FLOW,
            Arrays.asList(
                text    ("configRef",      "Config Ref",   "Database_Config",  true,  "Riferimento alla DB Config globale"),
                textarea("sql",            "SQL Statement","UPDATE … SET …",   true,  "Statement SQL (:paramName per i parametri)"),
                text    ("targetVariable", "Target Var",  "",                  false, "Variabile per il risultato"),
                text    ("displayName",    "Display Name","",                  false, "Nome nel canvas")
            )
        ));

        // ── DB Delete ────────────────────────────────────────────────────────
        REGISTRY.add(new ComponentDef(
            "db_delete", "Database — Delete",
            "Esegue un DELETE",
            "DB", DATA, ConfigMode.FLOW,
            Arrays.asList(
                text    ("configRef",      "Config Ref",   "Database_Config",  true,  "Riferimento alla DB Config globale"),
                textarea("sql",            "SQL Statement","DELETE FROM …",    true,  "Statement SQL (:paramName per i parametri)"),
                text    ("targetVariable", "Target Var",  "",                  false, "Variabile per il risultato"),
                text    ("displayName",    "Display Name","",                  false, "Nome nel canvas")
            )
        ));
    }

    public static List<ComponentDef> getAll() { return Collections.unmodifiableList(REGISTRY); }
    public static ComponentDef findById(String id) {
        return REGISTRY.stream().filter(c -> c.id.equals(id)).findFirst().orElse(null);
    }
}
