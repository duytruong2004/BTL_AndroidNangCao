package com.example.btl.ui.main; // <-- Package đã thay đổi

public interface ListItem {
    int TYPE_HEADER = 0;
    int TYPE_TASK = 1;

    int getItemType();
}