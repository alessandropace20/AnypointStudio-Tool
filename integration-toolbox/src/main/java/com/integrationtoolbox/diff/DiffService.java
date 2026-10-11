package com.integrationtoolbox.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Structural diff service for comparing JSON or XML documents.
 * Reports added, removed, and changed values with path information.
 */
public class DiffService {

    public enum DiffType { ADDED, REMOVED, CHANGED }

    public static class DiffItem {
        private final DiffType type;
        private final String path;
        private final String leftValue;
        private final String rightValue;

        public DiffItem(DiffType type, String path, String leftValue, String rightValue) {
            this.type = type;
            this.path = path;
            this.leftValue = leftValue;
            this.rightValue = rightValue;
        }

        public DiffType getType() { return type; }
        public String getPath() { return path; }
        public String getLeftValue() { return leftValue; }
        public String getRightValue() { return rightValue; }

        @Override
        public String toString() {
            return switch (type) {
                case ADDED -> "[ADDED]   " + path + " = " + rightValue;
                case REMOVED -> "[REMOVED] " + path + " = " + leftValue;
                case CHANGED -> "[CHANGED] " + path + "\n          Left:  " + leftValue + "\n          Right: " + rightValue;
            };
        }
    }

    public static class DiffResult {
        private final List<DiffItem> items;

        public DiffResult(List<DiffItem> items) {
            this.items = Collections.unmodifiableList(items);
        }

        public List<DiffItem> getItems() { return items; }

        public long countAdded() { return items.stream().filter(i -> i.type == DiffType.ADDED).count(); }
        public long countRemoved() { return items.stream().filter(i -> i.type == DiffType.REMOVED).count(); }
        public long countChanged() { return items.stream().filter(i -> i.type == DiffType.CHANGED).count(); }
        public boolean areEqual() { return items.isEmpty(); }

        public String getSummary() {
            if (areEqual()) return "Documents are structurally identical.";
            return "Differences found: " + countAdded() + " added, " + countRemoved() + " removed, " + countChanged() + " changed.";
        }

        public String getFullReport() {
            if (areEqual()) return getSummary();
            StringBuilder sb = new StringBuilder(getSummary()).append("\n\n");
            for (DiffItem item : items) {
                sb.append(item).append("\n");
            }
            return sb.toString();
        }
    }

    private final ObjectMapper jsonMapper = new ObjectMapper();

    // ===== JSON DIFF =====

    /**
     * Compares two JSON documents structurally.
     * @param leftJson  left document JSON string
     * @param rightJson right document JSON string
     * @param ignoreArrayOrder if true, arrays are compared as sets (order-independent)
     * @return DiffResult with all differences
     */
    public DiffResult diffJson(String leftJson, String rightJson, boolean ignoreArrayOrder) throws Exception {
        JsonNode left = jsonMapper.readTree(leftJson);
        JsonNode right = jsonMapper.readTree(rightJson);
        List<DiffItem> items = new ArrayList<>();
        compareJsonNodes(left, right, "$", ignoreArrayOrder, items);
        return new DiffResult(items);
    }

    private void compareJsonNodes(JsonNode left, JsonNode right, String path, boolean ignoreArrayOrder, List<DiffItem> items) {
        if (left == null && right == null) return;
        if (left == null) {
            items.add(new DiffItem(DiffType.ADDED, path, null, nodeToString(right)));
            return;
        }
        if (right == null) {
            items.add(new DiffItem(DiffType.REMOVED, path, nodeToString(left), null));
            return;
        }

        // Different node types
        if (!left.getNodeType().equals(right.getNodeType())) {
            items.add(new DiffItem(DiffType.CHANGED, path, nodeToString(left), nodeToString(right)));
            return;
        }

        if (left.isObject()) {
            Set<String> leftKeys = new LinkedHashSet<>();
            left.fieldNames().forEachRemaining(leftKeys::add);
            Set<String> rightKeys = new LinkedHashSet<>();
            right.fieldNames().forEachRemaining(rightKeys::add);

            // Check removed
            for (String key : leftKeys) {
                if (!rightKeys.contains(key)) {
                    items.add(new DiffItem(DiffType.REMOVED, path + "." + key, nodeToString(left.get(key)), null));
                }
            }
            // Check added
            for (String key : rightKeys) {
                if (!leftKeys.contains(key)) {
                    items.add(new DiffItem(DiffType.ADDED, path + "." + key, null, nodeToString(right.get(key))));
                }
            }
            // Check common
            for (String key : leftKeys) {
                if (rightKeys.contains(key)) {
                    compareJsonNodes(left.get(key), right.get(key), path + "." + key, ignoreArrayOrder, items);
                }
            }
        } else if (left.isArray()) {
            if (ignoreArrayOrder) {
                compareArraysIgnoreOrder(left, right, path, ignoreArrayOrder, items);
            } else {
                int size = Math.max(left.size(), right.size());
                for (int i = 0; i < size; i++) {
                    JsonNode l = i < left.size() ? left.get(i) : null;
                    JsonNode r = i < right.size() ? right.get(i) : null;
                    compareJsonNodes(l, r, path + "[" + i + "]", ignoreArrayOrder, items);
                }
            }
        } else {
            // Scalar: compare values
            if (!left.equals(right)) {
                items.add(new DiffItem(DiffType.CHANGED, path, nodeToString(left), nodeToString(right)));
            }
        }
    }

    private void compareArraysIgnoreOrder(JsonNode left, JsonNode right, String path, boolean ignoreArrayOrder, List<DiffItem> items) {
        // Simple order-independent comparison: find matching items
        List<JsonNode> leftList = new ArrayList<>();
        left.forEach(leftList::add);
        List<JsonNode> rightList = new ArrayList<>();
        right.forEach(rightList::add);

        List<JsonNode> rightUnmatched = new ArrayList<>(rightList);
        List<JsonNode> leftUnmatched = new ArrayList<>();

        for (JsonNode leftItem : leftList) {
            boolean matched = rightUnmatched.removeIf(r -> r.equals(leftItem));
            if (!matched) leftUnmatched.add(leftItem);
        }

        for (int i = 0; i < leftUnmatched.size(); i++) {
            items.add(new DiffItem(DiffType.REMOVED, path + "[?]", nodeToString(leftUnmatched.get(i)), null));
        }
        for (int i = 0; i < rightUnmatched.size(); i++) {
            items.add(new DiffItem(DiffType.ADDED, path + "[?]", null, nodeToString(rightUnmatched.get(i))));
        }
    }

    private String nodeToString(JsonNode node) {
        if (node == null) return "null";
        if (node.isTextual()) return "\"" + node.asText() + "\"";
        return node.toString();
    }

    // ===== XML DIFF =====

    /**
     * Compares two XML documents structurally.
     * @param leftXml  left document XML string
     * @param rightXml right document XML string
     * @param normalizeWhitespace if true, text content is trimmed before comparison
     * @return DiffResult with all differences
     */
    public DiffResult diffXml(String leftXml, String rightXml, boolean normalizeWhitespace) throws Exception {
        Document leftDoc = parseXml(leftXml);
        Document rightDoc = parseXml(rightXml);
        List<DiffItem> items = new ArrayList<>();
        compareElements(leftDoc.getDocumentElement(), rightDoc.getDocumentElement(), "/", normalizeWhitespace, items);
        return new DiffResult(items);
    }

    private void compareElements(Element left, Element right, String path, boolean normalize, List<DiffItem> items) {
        if (left == null && right == null) return;
        if (left == null) {
            items.add(new DiffItem(DiffType.ADDED, path, null, "<" + right.getNodeName() + ">"));
            return;
        }
        if (right == null) {
            items.add(new DiffItem(DiffType.REMOVED, path, "<" + left.getNodeName() + ">", null));
            return;
        }

        // Compare element names
        if (!left.getNodeName().equals(right.getNodeName())) {
            items.add(new DiffItem(DiffType.CHANGED, path + " (element name)", left.getNodeName(), right.getNodeName()));
        }

        // Compare attributes
        NamedNodeMap leftAttrs = left.getAttributes();
        NamedNodeMap rightAttrs = right.getAttributes();
        Set<String> allAttrNames = new LinkedHashSet<>();
        for (int i = 0; i < leftAttrs.getLength(); i++) allAttrNames.add(leftAttrs.item(i).getNodeName());
        for (int i = 0; i < rightAttrs.getLength(); i++) allAttrNames.add(rightAttrs.item(i).getNodeName());

        for (String attrName : allAttrNames) {
            Node la = leftAttrs.getNamedItem(attrName);
            Node ra = rightAttrs.getNamedItem(attrName);
            String attrPath = path + "[@" + attrName + "]";
            if (la == null) {
                items.add(new DiffItem(DiffType.ADDED, attrPath, null, ra.getNodeValue()));
            } else if (ra == null) {
                items.add(new DiffItem(DiffType.REMOVED, attrPath, la.getNodeValue(), null));
            } else if (!la.getNodeValue().equals(ra.getNodeValue())) {
                items.add(new DiffItem(DiffType.CHANGED, attrPath, la.getNodeValue(), ra.getNodeValue()));
            }
        }

        // Compare text content (direct text nodes)
        String leftText = getDirectTextContent(left, normalize);
        String rightText = getDirectTextContent(right, normalize);
        if (!leftText.equals(rightText)) {
            items.add(new DiffItem(DiffType.CHANGED, path + "/text()", leftText, rightText));
        }

        // Compare child elements (in order per XML spec)
        List<Element> leftChildren = getChildElements(left);
        List<Element> rightChildren = getChildElements(right);

        int size = Math.max(leftChildren.size(), rightChildren.size());
        for (int i = 0; i < size; i++) {
            Element lc = i < leftChildren.size() ? leftChildren.get(i) : null;
            Element rc = i < rightChildren.size() ? rightChildren.get(i) : null;
            String childName = lc != null ? lc.getNodeName() : (rc != null ? rc.getNodeName() : "?");
            compareElements(lc, rc, path + "/" + childName + "[" + i + "]", normalize, items);
        }
    }

    private String getDirectTextContent(Element el, boolean normalize) {
        StringBuilder sb = new StringBuilder();
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                sb.append(child.getNodeValue());
            }
        }
        String text = sb.toString();
        return normalize ? text.trim() : text;
    }

    private List<Element> getChildElements(Element el) {
        List<Element> result = new ArrayList<>();
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                result.add((Element) children.item(i));
            }
        }
        return result;
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
