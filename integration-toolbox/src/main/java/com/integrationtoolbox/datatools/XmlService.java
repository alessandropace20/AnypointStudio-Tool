package com.integrationtoolbox.datatools;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import javax.xml.XMLConstants;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for XML formatting, validation (well-formedness and XSD), and XML→JSON conversion.
 * All parsers are configured with XXE protection.
 */
public class XmlService {

    // ──────────────────────────────────────────────────────────────────
    // Formatting
    // ──────────────────────────────────────────────────────────────────

    /** Pretty-prints an XML string. Throws on malformed input. */
    public String format(String xml) throws Exception {
        Document doc = parseSecure(xml);
        return serialize(doc, true);
    }

    // ──────────────────────────────────────────────────────────────────
    // Validation
    // ──────────────────────────────────────────────────────────────────

    /**
     * Validates XML well-formedness.
     * @return empty list if valid; otherwise list of error messages
     */
    public List<String> validate(String xml) {
        List<String> errors = new ArrayList<>();
        try {
            parseSecure(xml);
        } catch (Exception e) {
            errors.add(e.getMessage() != null ? e.getMessage() : e.toString());
        }
        return errors;
    }

    /**
     * Validates XML against an XSD schema string.
     * @return empty list if valid; list of messages otherwise
     */
    public List<String> validateAgainstXsd(String xml, String xsdContent) throws Exception {
        List<String> errors = new ArrayList<>();
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Schema schema = factory.newSchema(new StreamSource(new StringReader(xsdContent)));
        Validator validator = schema.newValidator();
        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try {
            validator.validate(new StreamSource(new StringReader(xml)));
        } catch (org.xml.sax.SAXParseException ex) {
            errors.add("Line " + ex.getLineNumber() + ", Col " + ex.getColumnNumber() + ": " + ex.getMessage());
        } catch (Exception ex) {
            errors.add(ex.getMessage() != null ? ex.getMessage() : ex.toString());
        }
        return errors;
    }

    // ──────────────────────────────────────────────────────────────────
    // Conversion
    // ──────────────────────────────────────────────────────────────────

    /**
     * Converts XML to JSON using Jackson XmlMapper.
     * @param prettyPrint format the JSON output
     */
    public String toJson(String xml, boolean prettyPrint) throws Exception {
        com.fasterxml.jackson.dataformat.xml.XmlMapper xmlMapper =
                new com.fasterxml.jackson.dataformat.xml.XmlMapper();
        com.fasterxml.jackson.databind.JsonNode node =
                xmlMapper.readTree(xml.getBytes(StandardCharsets.UTF_8));
        com.fasterxml.jackson.databind.ObjectMapper jsonMapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        if (prettyPrint) {
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        }
        return jsonMapper.writeValueAsString(node);
    }

    // ──────────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────────

    /** Parses XML with XXE protection. */
    public Document parseSecure(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Suppress default error output
        builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
            @Override public void warning(org.xml.sax.SAXParseException e) {}
            @Override public void error(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException { throw e; }
            @Override public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException { throw e; }
        });
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /** Serializes a DOM document to a string. */
    public String serialize(Document doc, boolean indent) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        if (indent) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
