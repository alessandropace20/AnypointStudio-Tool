package com.integrationtoolbox.datatools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for JSON formatting, validation (well-formedness and JSON Schema), and JSON→XML conversion.
 */
public class JsonService {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    // ──────────────────────────────────────────────────────────────────
    // Formatting
    // ──────────────────────────────────────────────────────────────────

    /** Pretty-prints a JSON string. Throws on parse errors. */
    public String format(String json) throws JsonProcessingException {
        JsonNode node = JSON_MAPPER.readTree(json);
        return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    // ──────────────────────────────────────────────────────────────────
    // Validation
    // ──────────────────────────────────────────────────────────────────

    /**
     * Validates JSON well-formedness.
     * @return empty list if valid; list of errors otherwise
     */
    public List<String> validate(String json) {
        List<String> errors = new ArrayList<>();
        try {
            JSON_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            String location = e.getLocation() != null
                    ? "Line " + e.getLocation().getLineNr() + ", Col " + e.getLocation().getColumnNr() + ": "
                    : "";
            errors.add(location + e.getOriginalMessage());
        }
        return errors;
    }

    /**
     * Validates JSON against a JSON Schema string (draft 2020-12, falls back to draft-07).
     * @return empty list if valid; list of violation messages otherwise
     */
    public List<String> validateAgainstSchema(String json, String schemaJson) {
        List<String> errors = new ArrayList<>();
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            com.networknt.schema.JsonSchema schema = factory.getSchema(schemaJson);
            JsonNode node = JSON_MAPPER.readTree(json);
            Set<ValidationMessage> messages = schema.validate(node);
            for (ValidationMessage msg : messages) {
                errors.add(msg.getMessage());
            }
        } catch (Exception e) {
            errors.add("Schema error: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
        return errors;
    }

    // ──────────────────────────────────────────────────────────────────
    // Conversion
    // ──────────────────────────────────────────────────────────────────

    /**
     * Converts JSON to XML.
     * @param rootElement XML root element name to wrap the output
     */
    public String toXml(String json, String rootElement) throws Exception {
        JsonNode node = JSON_MAPPER.readTree(json);
        XmlMapper xmlMapper = new XmlMapper();
        String inner = xmlMapper.writeValueAsString(node);
        // xmlMapper wraps in "ObjectNode" tag; we need to replace that with our rootElement
        String xmlString = inner.replaceFirst("^<[^>]+>", "<" + rootElement + ">")
                              .replaceFirst("</[^>]+>$", "</" + rootElement + ">");
        XmlService xmlService = new XmlService();
        try {
            return xmlService.format(xmlString);
        } catch (Exception e) {
            // Return unformatted if parsing fails (e.g., deeply nested arrays)
            return xmlString;
        }
    }

    public ObjectMapper getMapper() {
        return JSON_MAPPER;
    }
}
