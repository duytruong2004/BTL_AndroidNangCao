package com.example.btl.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.btl.data.model.Task; // <-- Import package mới
import com.example.btl.receiver.NotificationReceiver; // <-- Import package mới

public class AlarmScheduler {

    /**
     * Đặt lịch thông báo cho một công việc
     */
    public static void scheduleNotification(Context context, Task task) {
        if (task.getDueDate() <= System.currentTimeMillis()) {
            return; // Không đặt lịch cho thời gian trong quá khứ
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("TASK_TITLE", task.getTitle());
        intent.putExtra("TASK_NOTES", task.getNotes());

        int notificationId = (int) task.getDueDate(); // Dùng due date làm ID
        intent.putExtra("TASK_ID", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.getDueDate(), pendingIntent);
            } catch (SecurityException e) {
                Toast.makeText(context, "Permission to set exact alarms is not granted.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Hủy một thông báo đã được đặt lịch
     */
    public static void cancelNotification(Context context, long dueDateMillis) {
        int notificationId = (int) dueDateMillis;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}