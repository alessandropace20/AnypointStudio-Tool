package com.integrationtoolbox.library;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing the local DataWeave snippet library.
 * Stores data at ~/.integrationtoolbox/dw-snippets.json
 */
public class SnippetLibraryService {
    private static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + ".integrationtoolbox";
    private static final String STORAGE_FILE = STORAGE_DIR + File.separator + "dw-snippets.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<DataWeaveSnippet> snippets = new ArrayList<>();

    public SnippetLibraryService() {
        load();
    }

    // ──────────────────────────────────────────────────────────────────
    // Persistence
    // ──────────────────────────────────────────────────────────────────

    /** Loads snippets from disk; seeds with starters if file doesn't exist. */
    public void load() {
        File file = new File(STORAGE_FILE);
        if (file.exists()) {
            try {
                snippets = MAPPER.readValue(file, new TypeReference<List<DataWeaveSnippet>>() {});
            } catch (IOException e) {
                snippets = new ArrayList<>(starterSnippets());
            }
        } else {
            snippets = new ArrayList<>(starterSnippets());
            save();
        }
    }

    /** Persists snippets to disk. */
    public void save() {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(STORAGE_FILE), snippets);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save snippet library: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // CRUD
    // ──────────────────────────────────────────────────────────────────

    public List<DataWeaveSnippet> getAll() {
        return Collections.unmodifiableList(snippets);
    }

    public Optional<DataWeaveSnippet> findById(String id) {
        return snippets.stream().filter(s -> id.equals(s.getId())).findFirst();
    }

    public void addSnippet(DataWeaveSnippet snippet) {
        if (snippet.getId() == null || snippet.getId().isBlank()) {
            snippet.setId(UUID.randomUUID().toString());
        }
        snippets.add(snippet);
        save();
    }

    public void updateSnippet(DataWeaveSnippet updated) {
        for (int i = 0; i < snippets.size(); i++) {
            if (updated.getId().equals(snippets.get(i).getId())) {
                snippets.set(i, updated);
                save();
                return;
            }
        }
        throw new NoSuchElementException("Snippet not found: " + updated.getId());
    }

    public void deleteSnippet(String id) {
        boolean removed = snippets.removeIf(s -> id.equals(s.getId()));
        if (!removed) throw new NoSuchElementException("Snippet not found: " + id);
        save();
    }

    public DataWeaveSnippet duplicate(String id) {
        DataWeaveSnippet original = findById(id)
                .orElseThrow(() -> new NoSuchElementException("Snippet not found: " + id));
        DataWeaveSnippet copy = new DataWeaveSnippet();
        copy.setId(UUID.randomUUID().toString());
        copy.setTitle(original.getTitle() + " (copy)");
        copy.setCategory(original.getCategory());
        copy.setTags(new ArrayList<>(original.getTags() != null ? original.getTags() : List.of()));
        copy.setDescription(original.getDescription());
        copy.setCode(original.getCode());
        copy.setExampleInput(original.getExampleInput());
        copy.setExpectedOutput(original.getExpectedOutput());
        copy.setCompatibilityNotes(original.getCompatibilityNotes());
        copy.setNotes(original.getNotes());
        copy.setSourceUrl(original.getSourceUrl());
        copy.setFavorite(false);
        snippets.add(copy);
        save();
        return copy;
    }

    // ──────────────────────────────────────────────────────────────────
    // Search & filter
    // ──────────────────────────────────────────────────────────────────

    /** Searches by title, description, category, tags, and code text (case-insensitive). */
    public List<DataWeaveSnippet> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String q = query.toLowerCase(Locale.ROOT);
        return snippets.stream()
                .filter(s -> containsIgnoreCase(s.getTitle(), q)
                        || containsIgnoreCase(s.getDescription(), q)
                        || containsIgnoreCase(s.getCategory(), q)
                        || containsIgnoreCase(s.getCode(), q)
                        || (s.getTags() != null && s.getTags().stream().anyMatch(t -> containsIgnoreCase(t, q))))
                .collect(Collectors.toList());
    }

    public List<DataWeaveSnippet> getByCategory(String category) {
        return snippets.stream()
                .filter(s -> category.equalsIgnoreCase(s.getCategory()))
                .collect(Collectors.toList());
    }

    public List<DataWeaveSnippet> getFavorites() {
        return snippets.stream().filter(DataWeaveSnippet::isFavorite).collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return snippets.stream()
                .map(DataWeaveSnippet::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────
    // Import / Export
    // ──────────────────────────────────────────────────────────────────

    public void exportToFile(String path) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(path), snippets);
    }

    /**
     * Imports snippets from a JSON file. Skips duplicates (by ID).
     * @return number of snippets imported
     */
    public int importFromFile(String path) throws IOException {
        List<DataWeaveSnippet> imported = MAPPER.readValue(new File(path), new TypeReference<List<DataWeaveSnippet>>() {});
        Set<String> existingIds = snippets.stream().map(DataWeaveSnippet::getId).collect(Collectors.toSet());
        int count = 0;
        for (DataWeaveSnippet s : imported) {
            if (s.getId() == null) s.setId(UUID.randomUUID().toString());
            if (!existingIds.contains(s.getId())) {
                snippets.add(s);
                existingIds.add(s.getId());
                count++;
            }
        }
        if (count > 0) save();
        return count;
    }

    // ──────────────────────────────────────────────────────────────────
    // Starter data
    // ──────────────────────────────────────────────────────────────────

    private List<DataWeaveSnippet> starterSnippets() {
        List<DataWeaveSnippet> list = new ArrayList<>();

        DataWeaveSnippet s1 = new DataWeaveSnippet("Map Array of Objects", "Arrays and collections",
                "Transform each element in an array using map.",
                "%dw 2.0\noutput application/json\n---\npayload map (item, index) -> {\n  index: index,\n  name: item.name,\n  upperName: upper(item.name)\n}");
        s1.setExampleInput("[{\"name\": \"alice\"}, {\"name\": \"bob\"}]");
        s1.setExpectedOutput("[{\"index\":0,\"name\":\"alice\",\"upperName\":\"ALICE\"},{\"index\":1,\"name\":\"bob\",\"upperName\":\"BOB\"}]");
        s1.setCompatibilityNotes("DataWeave 2.0 (Mule 4+)");
        s1.setTags(List.of("map", "array", "transform"));
        list.add(s1);

        DataWeaveSnippet s2 = new DataWeaveSnippet("Filter Array", "Arrays and collections",
                "Filter array elements based on a condition.",
                "%dw 2.0\noutput application/json\n---\npayload filter (item) -> item.age > 18");
        s2.setExampleInput("[{\"name\":\"Alice\",\"age\":25},{\"name\":\"Bob\",\"age\":15}]");
        s2.setExpectedOutput("[{\"name\":\"Alice\",\"age\":25}]");
        s2.setCompatibilityNotes("DataWeave 2.0");
        s2.setTags(List.of("filter", "array", "condition"));
        list.add(s2);

        DataWeaveSnippet s3 = new DataWeaveSnippet("Reduce to Sum", "Reduce and aggregation",
                "Sum all values in an array using reduce.",
                "%dw 2.0\noutput application/json\n---\npayload reduce (item, acc = 0) -> acc + item");
        s3.setExampleInput("[1, 2, 3, 4, 5]");
        s3.setExpectedOutput("15");
        s3.setCompatibilityNotes("DataWeave 2.0");
        s3.setTags(List.of("reduce", "sum", "aggregation"));
        list.add(s3);

        DataWeaveSnippet s4 = new DataWeaveSnippet("Group By Category", "Arrays and collections",
                "Group an array of objects by a field value.",
                "%dw 2.0\noutput application/json\n---\npayload groupBy (item) -> item.category");
        s4.setExampleInput("[{\"name\":\"A\",\"category\":\"X\"},{\"name\":\"B\",\"category\":\"Y\"},{\"name\":\"C\",\"category\":\"X\"}]");
        s4.setExpectedOutput("{\"X\":[{\"name\":\"A\",\"category\":\"X\"},{\"name\":\"C\",\"category\":\"X\"}],\"Y\":[{\"name\":\"B\",\"category\":\"Y\"}]}");
        s4.setCompatibilityNotes("DataWeave 2.0");
        s4.setTags(List.of("groupBy", "group", "collection"));
        list.add(s4);

        DataWeaveSnippet s5 = new DataWeaveSnippet("pluck Object Keys", "Objects",
                "Convert object entries to array of key-value pairs using pluck.",
                "%dw 2.0\noutput application/json\n---\npayload pluck (value, key) -> {key: key, value: value}");
        s5.setExampleInput("{\"a\":1,\"b\":2,\"c\":3}");
        s5.setExpectedOutput("[{\"key\":\"a\",\"value\":1},{\"key\":\"b\",\"value\":2},{\"key\":\"c\",\"value\":3}]");
        s5.setCompatibilityNotes("DataWeave 2.0");
        s5.setTags(List.of("pluck", "object", "keys"));
        list.add(s5);

        DataWeaveSnippet s6 = new DataWeaveSnippet("Format Date", "Dates and timestamps",
                "Parse and format a date string to a different pattern.",
                "%dw 2.0\noutput application/json\n---\n{\n  formatted: payload.date as Date {format: \"yyyy-MM-dd\"} as String {format: \"dd/MM/yyyy\"}\n}");
        s6.setExampleInput("{\"date\": \"2024-01-15\"}");
        s6.setExpectedOutput("{\"formatted\":\"15/01/2024\"}");
        s6.setCompatibilityNotes("DataWeave 2.0");
        s6.setTags(List.of("date", "format", "datetime"));
        list.add(s6);

        DataWeaveSnippet s7 = new DataWeaveSnippet("Null-safe Default", "Null handling and defaults",
                "Use default operator to provide fallback values for null fields.",
                "%dw 2.0\noutput application/json\n---\n{\n  name: payload.name default \"Unknown\",\n  age: payload.age default 0\n}");
        s7.setExampleInput("{\"name\": null}");
        s7.setExpectedOutput("{\"name\":\"Unknown\",\"age\":0}");
        s7.setCompatibilityNotes("DataWeave 2.0");
        s7.setTags(List.of("null", "default", "safe"));
        list.add(s7);

        DataWeaveSnippet s8 = new DataWeaveSnippet("String Interpolation", "Strings",
                "Build strings dynamically using interpolation syntax.",
                "%dw 2.0\noutput application/json\n---\n{\n  greeting: \"Hello, $(payload.name)! You are $(payload.age) years old.\"\n}");
        s8.setExampleInput("{\"name\":\"Alice\",\"age\":30}");
        s8.setExpectedOutput("{\"greeting\":\"Hello, Alice! You are 30 years old.\"}");
        s8.setCompatibilityNotes("DataWeave 2.0");
        s8.setTags(List.of("string", "interpolation", "template"));
        list.add(s8);

        DataWeaveSnippet s9 = new DataWeaveSnippet("XML Namespace Handling", "XML and namespaces",
                "Work with XML namespaces in DataWeave.",
                "%dw 2.0\noutput application/xml\nns ns0 http://example.com/ns\n---\n{\n  ns0#root: {\n    ns0#child: payload.value\n  }\n}");
        s9.setCompatibilityNotes("DataWeave 2.0");
        s9.setTags(List.of("xml", "namespace", "ns"));
        list.add(s9);

        DataWeaveSnippet s10 = new DataWeaveSnippet("Standard Error Payload", "Error payloads",
                "Create a consistent error response payload.",
                "%dw 2.0\noutput application/json\n---\n{\n  timestamp: now() as String,\n  status: vars.httpStatus default 500,\n  error: error.description default \"An unexpected error occurred\",\n  path: attributes.requestPath default \"/\"\n}");
        s10.setCompatibilityNotes("DataWeave 2.0, Mule 4");
        s10.setTags(List.of("error", "exception", "payload", "response"));
        list.add(s10);

        return list;
    }

    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(query);
    }
}
