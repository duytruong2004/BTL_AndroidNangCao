package com.example.btl.controller; // Hoặc package bạn muốn

public class HeaderItem implements ListItem {
    private String title;
    private boolean isExpanded = true; // Mặc định mở rộng

    public HeaderItem(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    @Override
    public int getItemType() { return TYPE_HEADER; }
}