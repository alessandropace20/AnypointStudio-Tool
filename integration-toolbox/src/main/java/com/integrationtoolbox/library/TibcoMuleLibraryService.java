package com.integrationtoolbox.library;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing the local TIBCO BusinessWorks → MuleSoft equivalence library.
 * Stores data at ~/.integrationtoolbox/tibco-mule-library.json
 */
public class TibcoMuleLibraryService {
    private static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + ".integrationtoolbox";
    private static final String STORAGE_FILE = STORAGE_DIR + File.separator + "tibco-mule-library.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<TibcoMuleEntry> entries = new ArrayList<>();

    public TibcoMuleLibraryService() {
        load();
    }

    // ──────────────────────────────────────────────────────────────────
    // Persistence
    // ──────────────────────────────────────────────────────────────────

    public void load() {
        File file = new File(STORAGE_FILE);
        if (file.exists()) {
            try {
                entries = MAPPER.readValue(file, new TypeReference<List<TibcoMuleEntry>>() {});
            } catch (IOException e) {
                entries = new ArrayList<>(starterEntries());
            }
        } else {
            entries = new ArrayList<>(starterEntries());
            save();
        }
    }

    public void save() {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(STORAGE_FILE), entries);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save TIBCO→Mule library: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // CRUD
    // ──────────────────────────────────────────────────────────────────

    public List<TibcoMuleEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    public Optional<TibcoMuleEntry> findById(String id) {
        return entries.stream().filter(e -> id.equals(e.getId())).findFirst();
    }

    public void addEntry(TibcoMuleEntry entry) {
        if (entry.getId() == null || entry.getId().isBlank()) {
            entry.setId(UUID.randomUUID().toString());
        }
        entries.add(entry);
        save();
    }

    public void updateEntry(TibcoMuleEntry updated) {
        for (int i = 0; i < entries.size(); i++) {
            if (updated.getId().equals(entries.get(i).getId())) {
                entries.set(i, updated);
                save();
                return;
            }
        }
        throw new NoSuchElementException("Entry not found: " + updated.getId());
    }

    public void deleteEntry(String id) {
        boolean removed = entries.removeIf(e -> id.equals(e.getId()));
        if (!removed) throw new NoSuchElementException("Entry not found: " + id);
        save();
    }

    public TibcoMuleEntry duplicate(String id) {
        TibcoMuleEntry original = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Entry not found: " + id));
        TibcoMuleEntry copy = new TibcoMuleEntry();
        copy.setId(UUID.randomUUID().toString());
        copy.setTibcoActivity(original.getTibcoActivity() + " (copy)");
        copy.setTibcoVersionContext(original.getTibcoVersionContext());
        copy.setMuleComponent(original.getMuleComponent());
        copy.setCategory(original.getCategory());
        copy.setTibcoBehaviorExplanation(original.getTibcoBehaviorExplanation());
        copy.setMuleSuggestedApproach(original.getMuleSuggestedApproach());
        copy.setCodeExample(original.getCodeExample());
        copy.setSemanticDifferences(original.getSemanticDifferences());
        copy.setLimitations(original.getLimitations());
        copy.setTags(new ArrayList<>(original.getTags() != null ? original.getTags() : List.of()));
        copy.setDocumentationRef(original.getDocumentationRef());
        copy.setApproximateMapping(original.isApproximateMapping());
        entries.add(copy);
        save();
        return copy;
    }

    // ──────────────────────────────────────────────────────────────────
    // Search & filter
    // ──────────────────────────────────────────────────────────────────

    /** Searches by TIBCO activity, MuleSoft component, category, tags, descriptions, free text. */
    public List<TibcoMuleEntry> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String q = query.toLowerCase(Locale.ROOT);
        return entries.stream()
                .filter(e -> containsIgnoreCase(e.getTibcoActivity(), q)
                        || containsIgnoreCase(e.getMuleComponent(), q)
                        || containsIgnoreCase(e.getCategory(), q)
                        || containsIgnoreCase(e.getTibcoBehaviorExplanation(), q)
                        || containsIgnoreCase(e.getMuleSuggestedApproach(), q)
                        || (e.getTags() != null && e.getTags().stream().anyMatch(t -> containsIgnoreCase(t, q))))
                .collect(Collectors.toList());
    }

    public List<TibcoMuleEntry> getByCategory(String category) {
        return entries.stream()
                .filter(e -> category.equalsIgnoreCase(e.getCategory()))
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return entries.stream()
                .map(TibcoMuleEntry::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────
    // Import / Export
    // ──────────────────────────────────────────────────────────────────

    public void exportToFile(String path) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(path), entries);
    }

    /** Returns number of entries actually imported (skips duplicate IDs). */
    public int importFromFile(String path) throws IOException {
        List<TibcoMuleEntry> imported = MAPPER.readValue(new File(path), new TypeReference<List<TibcoMuleEntry>>() {});
        Set<String> existingIds = entries.stream().map(TibcoMuleEntry::getId).collect(Collectors.toSet());
        int count = 0;
        for (TibcoMuleEntry e : imported) {
            if (e.getId() == null) e.setId(UUID.randomUUID().toString());
            if (!existingIds.contains(e.getId())) {
                entries.add(e);
                existingIds.add(e.getId());
                count++;
            }
        }
        if (count > 0) save();
        return count;
    }

    // ──────────────────────────────────────────────────────────────────
    // Starter data
    // ──────────────────────────────────────────────────────────────────

    private List<TibcoMuleEntry> starterEntries() {
        List<TibcoMuleEntry> list = new ArrayList<>();

        TibcoMuleEntry e1 = new TibcoMuleEntry("HTTP Receiver (Process Service)", "HTTP Listener", "HTTP and REST");
        e1.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e1.setTibcoBehaviorExplanation("Receives an HTTP request and starts a process instance. Configured with host, port, and path.");
        e1.setMuleSuggestedApproach("Use an HTTP Listener connector in a Mule 4 flow. Configure host, port, and path in the HTTP Listener Config.");
        e1.setCodeExample("<http:listener config-ref=\"HTTP_Listener_config\" path=\"/api/v1/resource\" doc:name=\"Listener\"/>");
        e1.setSemanticDifferences("Both trigger a flow/process on incoming HTTP. Mule uses reactive execution, TIBCO uses thread-per-process.");
        e1.setApproximateMapping(false);
        e1.setTags(List.of("HTTP", "listener", "receiver", "trigger", "REST"));
        list.add(e1);

        TibcoMuleEntry e2 = new TibcoMuleEntry("HTTP Request (Invoke REST API)", "HTTP Request", "HTTP and REST");
        e2.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e2.setTibcoBehaviorExplanation("Sends an outbound HTTP/HTTPS request to a remote endpoint.");
        e2.setMuleSuggestedApproach("Use the HTTP Request connector. Configure method, URL, headers, and query parameters on the operation.");
        e2.setCodeExample("<http:request method=\"GET\" config-ref=\"HTTP_Request_config\" path=\"/resource\" doc:name=\"Request\"/>");
        e2.setSemanticDifferences("Functionally equivalent. Mule supports reactive non-blocking I/O; TIBCO uses a thread model.");
        e2.setApproximateMapping(false);
        e2.setTags(List.of("HTTP", "request", "REST", "outbound", "invoke"));
        list.add(e2);

        TibcoMuleEntry e3 = new TibcoMuleEntry("Log Activity", "Logger", "Configuration and properties");
        e3.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e3.setTibcoBehaviorExplanation("Writes a message to the engine log at a configurable level (Info, Warning, Error, Debug).");
        e3.setMuleSuggestedApproach("Use the Logger component. Set level (INFO, WARN, ERROR, DEBUG) and message using DataWeave expression.");
        e3.setCodeExample("<logger level=\"INFO\" message=\"#[payload]\" doc:name=\"Logger\"/>");
        e3.setSemanticDifferences("Direct equivalent. Mule Logger uses DataWeave for dynamic messages; TIBCO uses XSLT/JNDI syntax.");
        e3.setApproximateMapping(false);
        e3.setTags(List.of("log", "logger", "debug", "info"));
        list.add(e3);

        TibcoMuleEntry e4 = new TibcoMuleEntry("Mapper (XSLT Transform)", "Transform Message (DataWeave)", "Mapping and transformations");
        e4.setTibcoVersionContext("TIBCO BW 5.x");
        e4.setTibcoBehaviorExplanation("Uses XSLT or BW mapper to transform an XML document from one schema to another.");
        e4.setMuleSuggestedApproach("Use Transform Message with DataWeave. DataWeave is more expressive than XSLT for JSON and supports multiple output formats.");
        e4.setCodeExample("%dw 2.0\noutput application/json\n---\n{name: payload.CustomerName, id: payload.CustomerId}");
        e4.setSemanticDifferences("XSLT is XML-centric; DataWeave handles JSON, XML, CSV, and more natively. Semantics are similar but syntax differs substantially.");
        e4.setApproximateMapping(true);
        e4.setTags(List.of("mapper", "transform", "XSLT", "DataWeave", "mapping"));
        list.add(e4);

        TibcoMuleEntry e5 = new TibcoMuleEntry("SOAP Invoke (Web Service Client)", "Web Service Consumer", "SOAP and XML");
        e5.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e5.setTibcoBehaviorExplanation("Invokes a SOAP web service operation using a WSDL definition.");
        e5.setMuleSuggestedApproach("Use the Web Service Consumer connector. Import the WSDL and select the operation. Map inputs/outputs with DataWeave.");
        e5.setCodeExample("<wsc:consume config-ref=\"Web_Service_Consumer_Config\" operation=\"GetCustomer\" doc:name=\"Consume\"/>");
        e5.setSemanticDifferences("Functionally equivalent. Mule auto-generates request/response types from WSDL.");
        e5.setApproximateMapping(false);
        e5.setTags(List.of("SOAP", "web service", "WSDL", "invoke", "consumer"));
        list.add(e5);

        TibcoMuleEntry e6 = new TibcoMuleEntry("Read File", "File Read", "File operations");
        e6.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e6.setTibcoBehaviorExplanation("Reads a file from the filesystem into the process data.");
        e6.setMuleSuggestedApproach("Use the File connector Read operation. Configure directory and file name. Content is available in payload; attributes include filename and metadata.");
        e6.setCodeExample("<file:read config-ref=\"File_Config\" path=\"/data/input.xml\" doc:name=\"Read\"/>");
        e6.setSemanticDifferences("Mule File connector returns a MuleMessage with attributes; TIBCO reads directly into a process variable.");
        e6.setApproximateMapping(false);
        e6.setTags(List.of("file", "read", "filesystem", "input"));
        list.add(e6);

        TibcoMuleEntry e7 = new TibcoMuleEntry("JMS Queue Get / Subscribe", "JMS Subscriber / Consumer", "Messaging");
        e7.setTibcoVersionContext("TIBCO BW 5.x (with TIBCO EMS or ActiveMQ)");
        e7.setTibcoBehaviorExplanation("Subscribes to or polls a JMS queue/topic and receives messages.");
        e7.setMuleSuggestedApproach("Use the JMS Connector listener (push) or consume (pull) operations. Configure connection factory, destination, and acknowledgement mode.");
        e7.setCodeExample("<jms:listener config-ref=\"JMS_Config\" destination=\"myQueue\" doc:name=\"On New Message\"/>");
        e7.setSemanticDifferences("Both support JMS. Mule 4 JMS connector supports JMS 1.1 and 2.0. Acknowledgement and transaction semantics should be verified per provider.");
        e7.setApproximateMapping(false);
        e7.setTags(List.of("JMS", "queue", "messaging", "subscribe", "consumer"));
        list.add(e7);

        TibcoMuleEntry e8 = new TibcoMuleEntry("Wait Activity (Sleep)", "Scheduler / async:thread-sleep", "Conditions and routing");
        e8.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e8.setTibcoBehaviorExplanation("Pauses process execution for a specified duration.");
        e8.setMuleSuggestedApproach("Avoid blocking waits in Mule. Use a Scheduler for periodic tasks. For delays, consider async:thread-sleep (non-reactive) or redesign with async flows.");
        e8.setCodeExample("// Scheduler:\n<scheduler doc:name=\"Scheduler\">\n  <scheduling-strategy><fixed-frequency frequency=\"5\" timeUnit=\"SECONDS\"/></scheduling-strategy>\n</scheduler>");
        e8.setSemanticDifferences("TIBCO Wait blocks a thread; Mule's reactive model discourages blocking. The Scheduler is only for periodic triggering, not in-flow delays.");
        e8.setApproximateMapping(true);
        e8.setLimitations("No direct in-flow sleep equivalent in Mule 4 without custom code. Using Thread.sleep() blocks reactive threads.");
        e8.setTags(List.of("wait", "sleep", "delay", "scheduler", "timer"));
        list.add(e8);

        TibcoMuleEntry e9 = new TibcoMuleEntry("Exception Catch (Fault Handler)", "On Error Propagate / On Error Continue", "Error handling");
        e9.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e9.setTibcoBehaviorExplanation("Catches faults of specific types thrown during process execution and handles them.");
        e9.setMuleSuggestedApproach("Use error-handler with on-error-propagate or on-error-continue inside a try scope or flow. Match error types using errorType attribute.");
        e9.setCodeExample("<error-handler>\n  <on-error-propagate type=\"HTTP:CONNECTIVITY\">\n    <logger level=\"ERROR\" message=\"#[error.description]\"/>\n  </on-error-propagate>\n</error-handler>");
        e9.setSemanticDifferences("TIBCO uses typed fault handlers; Mule uses typed error scopes. on-error-propagate re-throws after handling; on-error-continue swallows.");
        e9.setApproximateMapping(false);
        e9.setTags(List.of("error", "exception", "fault", "catch", "handler"));
        list.add(e9);

        TibcoMuleEntry e10 = new TibcoMuleEntry("For Each Group (Iterate Activity)", "For Each Scope", "Loops and collections");
        e10.setTibcoVersionContext("TIBCO BW 5.x / 6.x");
        e10.setTibcoBehaviorExplanation("Iterates over a collection, executing the contained activities for each item.");
        e10.setMuleSuggestedApproach("Use the For Each scope. Set collection attribute to the array expression. Each iteration receives one element as payload.");
        e10.setCodeExample("<foreach collection=\"#[payload]\" doc:name=\"For Each\">\n  <logger level=\"INFO\" message=\"#[payload]\"/>\n</foreach>");
        e10.setSemanticDifferences("Semantically equivalent. Mule For Each processes items sequentially by default; batch processing is available for large datasets via Batch Job.");
        e10.setApproximateMapping(false);
        e10.setTags(List.of("foreach", "iterate", "loop", "collection", "batch"));
        list.add(e10);

        return list;
    }

    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(query);
    }
}
