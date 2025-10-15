package com.example.btl.model;


import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

// Model: Repository để quản lý các nguồn dữ liệu
public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
    }

    public void insert(Task task) {
        new InsertTaskAsyncTask(taskDao).execute(task);
    }

    public void update(Task task) {
        new UpdateTaskAsyncTask(taskDao).execute(task);
    }

    public void delete(Task task) {
        new DeleteTaskAsyncTask(taskDao).execute(task);
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    public LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay) {
        return taskDao.getTasksForDate(startOfDay, endOfDay);
    }

    public LiveData<List<Task>> getTasksByCategory(String category) {
        return taskDao.getTasksByCategory(category);
    }

    private static class InsertTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        private InsertTaskAsyncTask(TaskDao taskDao) { this.taskDao = taskDao; }
        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.insert(tasks[0]);
            return null;
        }
    }
    // Tương tự cho Update và Delete
    private static class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        private UpdateTaskAsyncTask(TaskDao taskDao) { this.taskDao = taskDao; }
        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.update(tasks[0]);
            return null;
        }
    }
    private static class DeleteTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        private DeleteTaskAsyncTask(TaskDao taskDao) { this.taskDao = taskDao; }
        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.delete(tasks[0]);
            return null;
        }
    }
}
