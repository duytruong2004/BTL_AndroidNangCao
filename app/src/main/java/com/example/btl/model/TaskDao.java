package com.example.btl.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

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

    // Query cũ (giữ lại nếu cần)
    @Query("SELECT * FROM tasks ORDER BY priority DESC")
    LiveData<List<Task>> getAllTasks_SortByPriority();

    // Query cũ (giữ lại nếu cần)
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY priority DESC")
    LiveData<List<Task>> getTasksByCategory_SortByPriority(String category);

    // Query cũ (giữ lại nếu cần)
    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate < :endOfDay ORDER BY isCompleted ASC, priority DESC")
    LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay);


    // --- QUERY ĐÃ SỬA LỖI SẮP XẾP NHÓM ---
    // Sắp xếp theo: Nhóm Ngày -> Hoàn thành -> Ưu tiên
    @Query("SELECT *, " +
            "CASE " +
            "  WHEN dueDate <= 0 THEN 3 " + // Nhóm "Không có ngày hạn" = 3
            "  WHEN dueDate < :todayStartMillis THEN 0 " + // Nhóm "Trước" = 0
            "  WHEN dueDate < :tomorrowStartMillis THEN 1 " + // Nhóm "Hôm nay" = 1
            "  ELSE 2 " + // Nhóm "Tương lai" = 2
            "END AS date_group " +
            "FROM tasks " +
            "ORDER BY date_group ASC, isCompleted ASC, priority DESC")
    LiveData<List<Task>> getAllTasksSortedByDateGroup(long todayStartMillis, long tomorrowStartMillis); //

    // --- QUERY ĐÃ SỬA LỖI SẮP XẾP NHÓM (LỌC THEO CATEGORY) ---
    // Sắp xếp theo: Nhóm Ngày -> Hoàn thành -> Ưu tiên
    @Query("SELECT *, " +
            "CASE " +
            "  WHEN dueDate <= 0 THEN 3 " +
            "  WHEN dueDate < :todayStartMillis THEN 0 " +
            "  WHEN dueDate < :tomorrowStartMillis THEN 1 " +
            "  ELSE 2 " +
            "END AS date_group " +
            "FROM tasks " +
            "WHERE category = :category " + // Thêm điều kiện lọc
            "ORDER BY date_group ASC, isCompleted ASC, priority DESC")
    LiveData<List<Task>> getTasksByCategorySortedByDateGroup(String category, long todayStartMillis, long tomorrowStartMillis); //
}