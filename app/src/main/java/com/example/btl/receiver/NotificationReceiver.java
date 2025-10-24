package com.example.btl.receiver; // <-- Package đã thay đổi

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent; // <-- THÊM IMPORT NÀY
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.btl.R;
import com.example.btl.ui.main.MainActivity; // <-- THÊM IMPORT NÀY

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("TASK_TITLE");
        String taskNotes = intent.getStringExtra("TASK_NOTES");
        int notificationId = intent.getIntExtra("TASK_ID", 0);

        createNotificationChannel(context);

        // --- BẮT ĐẦU CẬP NHẬT: TẠO PENDINGINTENT CHO NOTIFICATION ---
        // 1. Tạo Intent để mở MainActivity
        Intent mainIntent = new Intent(context, MainActivity.class);
        // Đặt cờ (flag) để mở lại app nếu nó đã chạy hoặc khởi động nó
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 2. Tạo PendingIntent
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                notificationId, // Dùng chung notificationId cho PendingIntent
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        // --- KẾT THÚC CẬP NHẬT ---

        String contentTitle = "Nhắc nhở: " + taskTitle;
        String contentText = taskNotes;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(contentTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Tự động hủy khi nhấn vào
                .setContentIntent(contentPendingIntent); // <-- THÊM DÒNG NÀY ĐỂ GẮN SỰ KIỆN CLICK

        if (!TextUtils.isEmpty(contentText)) {
            builder.setContentText(contentText);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        } else {
            builder.setContentText("Đã đến giờ thực hiện!");
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminder Channel";
            String description = "Channel for task reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}