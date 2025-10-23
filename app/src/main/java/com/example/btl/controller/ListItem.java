package com.example.btl.controller; // Hoặc package bạn muốn

public interface ListItem {
    int TYPE_HEADER = 0;
    int TYPE_TASK = 1;

    int getItemType();
}