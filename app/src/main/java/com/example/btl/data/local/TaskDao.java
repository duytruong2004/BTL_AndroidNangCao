package com.example.btl.data.local; // <-- Package đã thay đổi

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.btl.data.model.Task; // <-- Import đã thay đổi

import java.util.List;

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

    @Query("SELECT * FROM tasks ORDER BY priority DESC")
    LiveData<List<Task>> getAllTasks_SortByPriority();

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY priority DESC")
    LiveData<List<Task>> getTasksByCategory_SortByPriority(String category);

    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate < :endOfDay ORDER BY isCompleted ASC, priority DESC")
    LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay);


    @Query("SELECT *, " +
            "CASE " +
            "  WHEN dueDate <= 0 THEN 3 " +
            "  WHEN dueDate < :todayStartMillis THEN 0 " +
            "  WHEN dueDate < :tomorrowStartMillis THEN 1 " +
            "  ELSE 2 " +
            "END AS date_group " +
            "FROM tasks " +
            "ORDER BY date_group ASC, isCompleted ASC, priority DESC")
    LiveData<List<Task>> getAllTasksSortedByDateGroup(long todayStartMillis, long tomorrowStartMillis);

    @Query("SELECT *, " +
            "CASE " +
            "  WHEN dueDate <= 0 THEN 3 " +
            "  WHEN dueDate < :todayStartMillis THEN 0 " +
            "  WHEN dueDate < :tomorrowStartMillis THEN 1 " +
            "  ELSE 2 " +
            "END AS date_group " +
            "FROM tasks " +
            "WHERE category = :category " +
            "ORDER BY date_group ASC, isCompleted ASC, priority DESC")
    LiveData<List<Task>> getTasksByCategorySortedByDateGroup(String category, long todayStartMillis, long tomorrowStartMillis);
}