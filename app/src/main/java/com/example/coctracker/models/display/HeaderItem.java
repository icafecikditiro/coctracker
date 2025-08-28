package com.example.coctracker.models.display;

// Represents a section title, like "Buildings" or "Heroes".
public class HeaderItem implements ListItem {
    private String title;

    public HeaderItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getItemType() {
        return TYPE_HEADER;
    }
}
