package com.example.btl.controller;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btl.R;
// Thêm import cho Task
import com.example.btl.model.Task;
import com.example.btl.model.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// 1. Thêm "implements TaskAdapter.OnTaskToggleListener"
public class CalendarActivity extends AppCompatActivity implements TaskAdapter.OnTaskToggleListener {

    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;
    private TextView selectedDateText;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Toolbar toolbar = findViewById(R.id.toolbar_calendar);
        toolbar.setNavigationOnClickListener(v -> finish());

        selectedDateText = findViewById(R.id.text_view_selected_date);
        CalendarView calendarView = findViewById(R.id.calendarView);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_calendar_tasks);

        taskAdapter = new TaskAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // 2. Thiết lập listener cho adapter (GIỐNG NHƯ MAINACTIVITY)
        taskAdapter.setOnTaskToggleListener(this);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Xử lý sự kiện khi người dùng chọn một ngày mới
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                loadTasksForDate(calendar);
            }
        });

        // Tải công việc cho ngày hôm nay khi mới mở màn hình
        loadTasksForDate(Calendar.getInstance());
    }

    private void loadTasksForDate(Calendar calendar) {
        // Hiển thị ngày đã chọn
        selectedDateText.setText(dateFormatter.format(calendar.getTime()));

        // Thiết lập thời gian về đầu ngày (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        // Thiết lập thời gian về cuối ngày (23:59:59)
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        long endOfDay = calendar.getTimeInMillis();

        // Lấy và hiển thị các công việc trong ngày đó
        // (Chúng ta chỉ observe 1 lần để tránh lỗi khi observe nhiều LiveData)
        taskViewModel.getTasksForDate(startOfDay, endOfDay).observe(this, tasks -> {
            taskAdapter.submitList(tasks);
        });
    }

    // 3. Thêm phương thức onTaskToggled (GIỐNG HỆT MAINACTIVITY)
    @Override
    public void onTaskToggled(Task task) {
        // 1. Lấy trạng thái mới
        boolean newCompletedState = !task.isCompleted();

        // 2. Tạo một đối tượng Task MỚI HOÀN TOÀN
        Task taskToUpdate = new Task(
                task.getTitle(),
                task.getNotes(),
                task.getPriority(),
                task.getCategory(),
                newCompletedState, // Đặt trạng thái mới
                task.getDueDate()
        );

        // 3. Đặt ID cho task mới để Room biết update task nào
        taskToUpdate.setId(task.getId());

        // 4. Gửi task MỚI đi
        taskViewModel.update(taskToUpdate);
    }
}