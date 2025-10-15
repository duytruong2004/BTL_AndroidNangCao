package com.example.btl.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Model: Lớp Entity đại diện cho một công việc trong cơ sở dữ liệu
@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String notes;
    private int priority; // 1: Low, 2: Medium, 3: High
    private String category; // e.g., "Personal", "Work"
    private boolean isCompleted;
    private long dueDate;

    public Task(String title, String notes, int priority, String category, boolean isCompleted,long dueDate) {
        this.title = title;
        this.notes = notes;
        this.priority = priority;
        this.category = category;
        this.isCompleted = isCompleted;
        this.dueDate = dueDate;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getNotes() { return notes; }
    public int getPriority() { return priority; }
    public String getCategory() { return category; }
    public boolean isCompleted() { return isCompleted; }
    public long getDueDate() { return dueDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }


}
