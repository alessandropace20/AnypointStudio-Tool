package com.integrationtoolbox.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model for a TIBCO BusinessWorks → MuleSoft equivalence entry.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TibcoMuleEntry {
    private String id;
    private String tibcoActivity;
    private String tibcoVersionContext;
    private String muleComponent;
    private String category;
    private String tibcoBehaviorExplanation;
    private String muleSuggestedApproach;
    private String codeExample;
    private String semanticDifferences;
    private String limitations;
    private List<String> tags = new ArrayList<>();
    private String documentationRef;
    private boolean approximateMapping;

    public TibcoMuleEntry() {}

    public TibcoMuleEntry(String tibcoActivity, String muleComponent, String category) {
        this.id = UUID.randomUUID().toString();
        this.tibcoActivity = tibcoActivity;
        this.muleComponent = muleComponent;
        this.category = category;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTibcoActivity() { return tibcoActivity; }
    public void setTibcoActivity(String v) { this.tibcoActivity = v; }
    public String getTibcoVersionContext() { return tibcoVersionContext; }
    public void setTibcoVersionContext(String v) { this.tibcoVersionContext = v; }
    public String getMuleComponent() { return muleComponent; }
    public void setMuleComponent(String v) { this.muleComponent = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getTibcoBehaviorExplanation() { return tibcoBehaviorExplanation; }
    public void setTibcoBehaviorExplanation(String v) { this.tibcoBehaviorExplanation = v; }
    public String getMuleSuggestedApproach() { return muleSuggestedApproach; }
    public void setMuleSuggestedApproach(String v) { this.muleSuggestedApproach = v; }
    public String getCodeExample() { return codeExample; }
    public void setCodeExample(String v) { this.codeExample = v; }
    public String getSemanticDifferences() { return semanticDifferences; }
    public void setSemanticDifferences(String v) { this.semanticDifferences = v; }
    public String getLimitations() { return limitations; }
    public void setLimitations(String v) { this.limitations = v; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> v) { this.tags = v; }
    public String getDocumentationRef() { return documentationRef; }
    public void setDocumentationRef(String v) { this.documentationRef = v; }
    public boolean isApproximateMapping() { return approximateMapping; }
    public void setApproximateMapping(boolean v) { this.approximateMapping = v; }

    @Override
    public String toString() { return tibcoActivity != null ? tibcoActivity : "(Untitled)"; }
}
