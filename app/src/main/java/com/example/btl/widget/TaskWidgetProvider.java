package com.example.btl.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName; // <-- THÊM IMPORT
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.btl.R;
import com.example.btl.ui.addedit.AddEditTaskActivity;
import com.example.btl.ui.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskWidgetProvider extends AppWidgetProvider {

    // 1. ĐỊNH NGHĨA ACTION TÙY CHỈNH
    public static final String ACTION_UPDATE_WIDGET = "com.example.btl.ACTION_UPDATE_WIDGET";

    /**
     * Hàm helper để cập nhật một widget cụ thể
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_list_widget);

        // Cập nhật ngày tháng
        updateDateTime(views, context);

        // Gắn sự kiện click cho Header (Mở MainActivity)
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_header, mainPendingIntent);

        // Gắn sự kiện click cho nút Add (Mở AddEditTaskActivity)
        Intent addIntent = new Intent(context, AddEditTaskActivity.class);
        PendingIntent addPendingIntent = PendingIntent.getActivity(
                context, 1, addIntent, PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_button_add, addPendingIntent);

        // Kết nối ListView với Service
        Intent serviceIntent = new Intent(context, TaskWidgetService.class);
        views.setRemoteAdapter(R.id.widget_list_view, serviceIntent);

        // 2. SỬA LẠI EMPTY VIEW
        // Chỉ định view sẽ hiển thị khi R.id.widget_list_view rỗng
        views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view);

        // Xử lý click cho từng item trong ListView
        Intent clickIntentTemplate = new Intent(context, MainActivity.class);
        PendingIntent clickPendingIntentTemplate = PendingIntent.getActivity(
                context, 2, clickIntentTemplate, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntentTemplate);

        // Cập nhật widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Cập nhật văn bản ngày và giờ
     */
    private static void updateDateTime(RemoteViews views, Context context) {
        Date now = new Date();
        Locale locale;
        try {
            // Thử dùng Tiếng Việt
            locale = new Locale("vi", "VN");
        } catch (Exception e) {
            // Nếu lỗi, dùng ngôn ngữ mặc định của thiết bị
            locale = Locale.getDefault();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("d 'thg' M, E", locale);
        views.setTextViewText(R.id.widget_text_day, "Hôm nay");
        views.setTextViewText(R.id.widget_text_date, dateFormat.format(now));
    }

    /**
     * 3. Xử lý tất cả các broadcast
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent); // Rất quan trọng

        // Kiểm tra xem có phải là action tùy chỉnh của chúng ta không
        if (ACTION_UPDATE_WIDGET.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, TaskWidgetProvider.class)
            );
            // Thông báo cho tất cả widget cập nhật lại ListView của chúng
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        }
    }
}