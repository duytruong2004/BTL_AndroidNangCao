package com.example.btl.model;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.Calendar; // Thêm import Calendar
import java.util.List;

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks_SortByPriority;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks_SortByPriority = taskDao.getAllTasks_SortByPriority(); // Gọi hàm DAO cũ đã đổi tên
    }

    // --- Các hàm insert, update, delete giữ nguyên ---
    public void insert(Task task) { new InsertTaskAsyncTask(taskDao).execute(task); }
    public void update(Task task) { new UpdateTaskAsyncTask(taskDao).execute(task); }
    public void delete(Task task) { new DeleteTaskAsyncTask(taskDao).execute(task); }

    // --- Các hàm get cũ giữ nguyên (nếu cần) ---
    public LiveData<List<Task>> getAllTasks_SortByPriority() { return allTasks_SortByPriority; }
    public LiveData<List<Task>> getTasksByCategory_SortByPriority(String category) { return taskDao.getTasksByCategory_SortByPriority(category); }
    public LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay) { return taskDao.getTasksForDate(startOfDay, endOfDay); }


    // --- HÀM TRẢ VỀ DANH SÁCH ĐÃ SẮP XẾP THEO NHÓM NGÀY ---
    public LiveData<List<Task>> getAllTasksSortedByDateGroup() {
        long todayStart = getStartOfDayMillis(System.currentTimeMillis());
        long tomorrowStart = todayStart + (24 * 60 * 60 * 1000);
        return taskDao.getAllTasksSortedByDateGroup(todayStart, tomorrowStart); // Gọi hàm DAO mới
    }

    // --- HÀM TRẢ VỀ DANH SÁCH ĐÃ SẮP XẾP THEO NHÓM NGÀY VÀ LỌC ---
    public LiveData<List<Task>> getTasksByCategorySortedByDateGroup(String category) {
        long todayStart = getStartOfDayMillis(System.currentTimeMillis());
        long tomorrowStart = todayStart + (24 * 60 * 60 * 1000);
        return taskDao.getTasksByCategorySortedByDateGroup(category, todayStart, tomorrowStart); // Gọi hàm DAO mới
    }

    // --- Hàm tiện ích lấy start of day ---
    private long getStartOfDayMillis(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

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