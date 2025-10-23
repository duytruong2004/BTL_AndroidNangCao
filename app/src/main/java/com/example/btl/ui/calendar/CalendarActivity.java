package com.example.btl.ui.calendar;

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

import com.example.btl.data.model.Task;
import com.example.btl.ui.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

        taskAdapter.setOnTaskToggleListener(this);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                loadTasksForDate(calendar);
            }
        });

        loadTasksForDate(Calendar.getInstance());
    }

    private void loadTasksForDate(Calendar calendar) {
        selectedDateText.setText(dateFormatter.format(calendar.getTime()));

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        long endOfDay = calendar.getTimeInMillis();

        taskViewModel.getTasksForDate(startOfDay, endOfDay).observe(this, tasks -> {
            taskAdapter.submitList(tasks);
        });
    }

    // --- CẬP NHẬT: onTaskToggled (ĐÃ SỬA LẠI LOGIC ĐÚNG) ---
    @Override
    public void onTaskToggled(Task task) {
        // 1. Lấy trạng thái mới
        boolean newCompletedState = !task.isCompleted();

        // 2. Tạo một đối tượng Task MỚI HOÀN TOÀN (Quan trọng)
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
    // --- KẾT THÚC CẬP NHẬT ---
}