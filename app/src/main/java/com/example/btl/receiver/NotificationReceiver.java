package com.example.btl.receiver; // <-- Package đã thay đổi

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
// THÊM IMPORT NÀY
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.btl.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("TASK_TITLE");
        String taskNotes = intent.getStringExtra("TASK_NOTES");
        int notificationId = intent.getIntExtra("TASK_ID", 0);

        createNotificationChannel(context);

        // --- CẬP NHẬT PHẦN TẠO NỘI DUNG THÔNG BÁO ---
        String contentTitle = "Nhắc nhở: " + taskTitle; // Giữ nguyên tiêu đề là tên Task
        String contentText = taskNotes; // Nội dung chính là ghi chú

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(contentTitle) // Tiêu đề là tên Task
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Tự động hủy khi nhấn vào

        // Kiểm tra xem có ghi chú không
        if (!TextUtils.isEmpty(contentText)) {
            // Nếu có ghi chú, đặt nó làm nội dung chính và cho phép mở rộng
            builder.setContentText(contentText);
            // Sử dụng BigTextStyle để hiển thị đầy đủ ghi chú dài
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        } else {
            // Nếu không có ghi chú, có thể để trống hoặc hiển thị một thông điệp mặc định
            builder.setContentText("Đã đến giờ thực hiện!"); // Ví dụ
        }
        // --- KẾT THÚC CẬP NHẬT ---

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    // Hàm createNotificationChannel (Không đổi)
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