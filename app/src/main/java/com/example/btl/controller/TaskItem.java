package com.example.btl.controller; // Hoặc package bạn muốn

import com.example.btl.model.Task;

public class TaskItem implements ListItem {
    private Task task;

    public TaskItem(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public int getItemType() {
        return TYPE_TASK;
    }
}