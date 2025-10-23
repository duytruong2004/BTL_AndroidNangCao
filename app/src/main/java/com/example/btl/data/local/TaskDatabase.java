package com.example.btl.data.local; // <-- Package đã thay đổi

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.btl.data.model.Task; // <-- Import đã thay đổi

@Database(entities = {Task.class}, version = 2)
public abstract class TaskDatabase extends RoomDatabase {

    private static TaskDatabase instance;

    public abstract TaskDao taskDao();

    public static synchronized TaskDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            TaskDatabase.class, "task_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private TaskDao taskDao;

        private PopulateDbAsyncTask(TaskDatabase db) {
            taskDao = db.taskDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long currentTime = System.currentTimeMillis();
            taskDao.insert(new Task("Task 1", "Notes for task 1", 3, "Work", false, currentTime));
            taskDao.insert(new Task("Task 2", "Notes for task 2", 2, "Personal", false, currentTime + 86400000));
            taskDao.insert(new Task("Task 3", "Notes for task 3", 1, "Wishlist", false, currentTime + 172800000));
            return null;
        }
    }
}