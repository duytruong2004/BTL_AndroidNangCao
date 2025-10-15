package com.example.btl.model;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

// Model: ViewModel để cung cấp dữ liệu cho UI và tồn tại qua các thay đổi cấu hình
public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public void insert(Task task) {
        repository.insert(task);
    }
    public void update(Task task) {
        repository.update(task);
    }
    public void delete(Task task) {
        repository.delete(task);
    }
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    public LiveData<List<Task>> getTasksByCategory(String category) {
        return repository.getTasksByCategory(category);
    }
    public LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay) {
        return repository.getTasksForDate(startOfDay, endOfDay);
    }
}

