package com.integrationtoolbox.datatools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Utility methods for JSON and XML formatting, validation and conversion.
 * This is a minimal implementation serving as a foundation for the full feature set.
 */
public class DataTools {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectWriter PRETTY_JSON_WRITER = JSON_MAPPER.writer(new DefaultPrettyPrinter());

    /**
     * Pretty‑print JSON string. Throws on parse errors.
     */
    public static String formatJson(String json) throws Exception {
        Object obj = JSON_MAPPER.readValue(json, Object.class);
        return PRETTY_JSON_WRITER.writeValueAsString(obj);
    }

    /**
     * Validate JSON against a provided JSON Schema (as a JSON string).
     * Placeholder implementation – returns true if parsing succeeds.
     */
    public static boolean validateJson(String json, String jsonSchema) {
        try {
            JSON_MAPPER.readTree(json);
            // Full schema validation can be added with network‑json-schema‑validator library.
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Pretty‑print XML string using JAXB marshaller.
     */
    public static String formatXml(String xml, Class<?> rootClass) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(rootClass);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(new StringReader(xml));
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        marshaller.marshal(obj, sw);
        return sw.toString();
    }

    // Additional methods for validation, XSD validation, conversion will be added in later phases.
}
