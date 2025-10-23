package com.example.btl.ui.viewmodel; // <-- Package đã thay đổi

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.btl.data.model.Task; // <-- Import đã thay đổi
import com.example.btl.data.repository.TaskRepository; // <-- Import đã thay đổi

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks_SortByPriority;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks_SortByPriority = repository.getAllTasks_SortByPriority();
    }

    public void insert(Task task) { repository.insert(task); }
    public void update(Task task) { repository.update(task); }
    public void delete(Task task) { repository.delete(task); }

    public LiveData<List<Task>> getAllTasks_SortByPriority() { return allTasks_SortByPriority; }
    public LiveData<List<Task>> getTasksByCategory_SortByPriority(String category) { return repository.getTasksByCategory_SortByPriority(category); }
    public LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay) { return repository.getTasksForDate(startOfDay, endOfDay); }

    public LiveData<List<Task>> getAllTasksSortedByDateGroup() {
        return repository.getAllTasksSortedByDateGroup();
    }

    public LiveData<List<Task>> getTasksByCategorySortedByDateGroup(String category) {
        return repository.getTasksByCategorySortedByDateGroup(category);
    }
}