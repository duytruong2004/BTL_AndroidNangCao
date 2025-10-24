package com.example.btl.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log; // <-- THÊM IMPORT NÀY
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.btl.R;
import com.example.btl.data.local.TaskDao;
import com.example.btl.data.local.TaskDatabase;
import com.example.btl.data.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "TaskWidgetFactory"; // <-- Thêm TAG để lọc log
    private Context context;
    private TaskDao taskDao;
    private List<Task> tasks = new ArrayList<>(); // <-- Khởi tạo ngay lập tức

    public TaskWidgetFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        // Hàm này được gọi khi factory được tạo
        try {
            taskDao = TaskDatabase.getInstance(context).taskDao();
            Log.d(TAG, "onCreate: TaskDao đã được khởi tạo.");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: LỖI khi khởi tạo TaskDao", e);
        }
    }

    @Override
    public void onDataSetChanged() {
        // Hàm này được gọi để làm mới dữ liệu
        // Nó chạy trên một background thread (luồng nền)
        Log.d(TAG, "onDataSetChanged: Đang làm mới dữ liệu widget...");
        if (taskDao == null) {
            Log.e(TAG, "onDataSetChanged: TaskDao bị null! Thử khởi tạo lại.");
            try {
                taskDao = TaskDatabase.getInstance(context).taskDao();
                if (taskDao == null) {
                    Log.e(TAG, "onDataSetChanged: Khởi tạo lại thất bại. DAO vẫn null.");
                    tasks.clear(); // Xóa dữ liệu cũ
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "onDataSetChanged: LỖI trong khi khởi tạo lại", e);
                tasks.clear();
                return;
            }
        }

        // Tải dữ liệu từ CSDL
        try {
            tasks.clear(); // Xóa dữ liệu cũ trước
            List<Task> newTasks = taskDao.getTasksForWidget();
            if (newTasks != null) {
                tasks.addAll(newTasks);
                Log.d(TAG, "onDataSetChanged: Đã tải " + tasks.size() + " tasks.");
            } else {
                Log.w(TAG, "onDataSetChanged: taskDao.getTasksForWidget() trả về null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "onDataSetChanged: LỖI khi truy vấn CSDL", e);
            tasks.clear(); // Đảm bảo danh sách rỗng nếu có lỗi
        }
    }

    @Override
    public void onDestroy() {
        tasks.clear();
        Log.d(TAG, "onDestroy: Factory đã bị hủy.");
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // Kiểm tra tính hợp lệ của vị trí
        if (position >= tasks.size() || tasks.get(position) == null) {
            Log.w(TAG, "getViewAt: Vị trí không hợp lệ " + position + " hoặc task bị null.");
            return null;
        }

        try {
            Task task = tasks.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
            views.setTextViewText(R.id.widget_item_text, task.getTitle());

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("TASK_ID_FROM_WIDGET", task.getId());
            views.setOnClickFillInIntent(R.id.widget_item_text, fillInIntent);

            return views;
        } catch (Exception e) {
            Log.e(TAG, "getViewAt: LỖI khi tạo view cho vị trí " + position, e);
            return null; // Trả về null nếu có lỗi
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null; // Trả về null để hệ thống dùng loading view mặc định
    }

    @Override
    public int getViewTypeCount() { return 1; }

    @Override
    public long getItemId(int position) {
        if (position >= tasks.size() || tasks.get(position) == null) {
            return position; // Giá trị dự phòng
        }
        return tasks.get(position).getId();
    }

    @Override
    public boolean hasStableIds() { return true; }
}