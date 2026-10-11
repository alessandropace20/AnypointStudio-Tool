package com.integrationtoolbox.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model for a DataWeave code snippet entry.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWeaveSnippet {
    private String id;
    private String title;
    private String category;
    private List<String> tags = new ArrayList<>();
    private String description;
    private String code;
    private String exampleInput;
    private String expectedOutput;
    private String compatibilityNotes;
    private String notes;
    private String sourceUrl;
    private boolean favorite;

    public DataWeaveSnippet() {}

    public DataWeaveSnippet(String title, String category, String description, String code) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.description = description;
        this.code = code;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getExampleInput() { return exampleInput; }
    public void setExampleInput(String exampleInput) { this.exampleInput = exampleInput; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public String getCompatibilityNotes() { return compatibilityNotes; }
    public void setCompatibilityNotes(String compatibilityNotes) { this.compatibilityNotes = compatibilityNotes; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    @Override
    public String toString() { return title != null ? title : "(Untitled)"; }
}
