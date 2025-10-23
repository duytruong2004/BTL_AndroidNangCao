package com.example.btl.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

// Model: Giao diện truy cập dữ liệu (Data Access Object) cho bảng Task
@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    // SỬA ĐỔI QUERY: Sắp xếp theo isCompleted (ASC - false lên trước)
    // rồi mới đến priority
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC")
    LiveData<List<Task>> getAllTasks();

    // SỬA ĐỔI QUERY: Tương tự, sắp xếp theo isCompleted trước
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY isCompleted ASC, priority DESC")
    LiveData<List<Task>> getTasksByCategory(String category);

    // SỬA ĐỔI QUERY: Tương tự, sắp xếp theo isCompleted trước
    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate < :endOfDay ORDER BY isCompleted ASC, priority DESC")
    LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay);
}