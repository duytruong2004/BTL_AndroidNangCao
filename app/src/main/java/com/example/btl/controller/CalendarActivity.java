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
import com.example.btl.model.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;
    private TextView selectedDateText;
    // Sử dụng SimpleDateFormat thay cho DateTimeFormatter
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

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Xử lý sự kiện khi người dùng chọn một ngày mới
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Sử dụng java.util.Calendar, có sẵn trong Android
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
        taskViewModel.getTasksForDate(startOfDay, endOfDay).observe(this, tasks -> {
            taskAdapter.submitList(tasks);
        });
    }
}