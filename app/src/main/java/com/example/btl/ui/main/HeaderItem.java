package com.example.btl.ui.main; // <-- Package đã thay đổi

// Import com.example.btl.controller.ListItem đã được thay đổi
import com.example.btl.ui.main.ListItem;

public class HeaderItem implements ListItem {
    private String title;
    private boolean isExpanded = true;

    public HeaderItem(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    @Override
    public int getItemType() { return TYPE_HEADER; }
}