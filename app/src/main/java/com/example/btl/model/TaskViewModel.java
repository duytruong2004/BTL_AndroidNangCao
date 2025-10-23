package com.example.btl.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks_SortByPriority;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks_SortByPriority = repository.getAllTasks_SortByPriority();
    }

    // --- Các hàm insert, update, delete giữ nguyên ---
    public void insert(Task task) { repository.insert(task); }
    public void update(Task task) { repository.update(task); }
    public void delete(Task task) { repository.delete(task); }

    // --- Các hàm get cũ giữ nguyên (nếu cần) ---
    public LiveData<List<Task>> getAllTasks_SortByPriority() { return allTasks_SortByPriority; }
    public LiveData<List<Task>> getTasksByCategory_SortByPriority(String category) { return repository.getTasksByCategory_SortByPriority(category); }
    public LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay) { return repository.getTasksForDate(startOfDay, endOfDay); }


    // --- HÀM TRẢ VỀ DANH SÁCH ĐÃ SẮP XẾP THEO NHÓM NGÀY ---
    public LiveData<List<Task>> getAllTasksSortedByDateGroup() {
        return repository.getAllTasksSortedByDateGroup(); // Gọi hàm Repository mới
    }

    // --- HÀM TRẢ VỀ DANH SÁCH ĐÃ SẮP XẾP THEO NHÓM NGÀY VÀ LỌC ---
    public LiveData<List<Task>> getTasksByCategorySortedByDateGroup(String category) {
        return repository.getTasksByCategorySortedByDateGroup(category); // Gọi hàm Repository mới
    }
}