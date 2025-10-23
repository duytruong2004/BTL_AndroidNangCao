package com.example.btl.data.repository; // <-- Package đã thay đổi

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import com.example.btl.data.local.TaskDao; // <-- Import đã thay đổi
import com.example.btl.data.local.TaskDatabase; // <-- Import đã thay đổi
import com.example.btl.data.model.Task; // <-- Import đã thay đổi
import com.example.btl.util.DateUtil; // <-- Import mới

import java.util.List;
// Xóa import java.util.Calendar (đã chuyển sang DateUtil)

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks_SortByPriority;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks_SortByPriority = taskDao.getAllTasks_SortByPriority();
    }

    public void insert(Task task) { new InsertTaskAsyncTask(taskDao).execute(task); }
    public void update(Task task) { new UpdateTaskAsyncTask(taskDao).execute(task); }
    public void delete(Task task) { new DeleteTaskAsyncTask(taskDao).execute(task); }

    public LiveData<List<Task>> getAllTasks_SortByPriority() { return allTasks_SortByPriority; }
    public LiveData<List<Task>> getTasksByCategory_SortByPriority(String category) { return taskDao.getTasksByCategory_SortByPriority(category); }
    public LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay) { return taskDao.getTasksForDate(startOfDay, endOfDay); }

    public LiveData<List<Task>> getAllTasksSortedByDateGroup() {
        long todayStart = DateUtil.getStartOfDayMillis(System.currentTimeMillis()); // <-- Dùng DateUtil
        long tomorrowStart = todayStart + (24 * 60 * 60 * 1000);
        return taskDao.getAllTasksSortedByDateGroup(todayStart, tomorrowStart);
    }

    public LiveData<List<Task>> getTasksByCategorySortedByDateGroup(String category) {
        long todayStart = DateUtil.getStartOfDayMillis(System.currentTimeMillis()); // <-- Dùng DateUtil
        long tomorrowStart = todayStart + (24 * 60 * 60 * 1000);
        return taskDao.getTasksByCategorySortedByDateGroup(category, todayStart, tomorrowStart);
    }

    // --- Xóa hàm getStartOfDayMillis (đã chuyển sang DateUtil) ---

    // --- Các AsyncTask giữ nguyên ---
    private static class InsertTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        private InsertTaskAsyncTask(TaskDao taskDao) { this.taskDao = taskDao; }
        @Override protected Void doInBackground(Task... tasks) { taskDao.insert(tasks[0]); return null; }
    }
    private static class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        private UpdateTaskAsyncTask(TaskDao taskDao) { this.taskDao = taskDao; }
        @Override protected Void doInBackground(Task... tasks) { taskDao.update(tasks[0]); return null; }
    }
    private static class DeleteTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        private DeleteTaskAsyncTask(TaskDao taskDao) { this.taskDao = taskDao; }
        @Override protected Void doInBackground(Task... tasks) { taskDao.delete(tasks[0]); return null; }
    }
}