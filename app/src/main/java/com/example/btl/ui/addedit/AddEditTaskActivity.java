package com.example.btl.ui.addedit; // <-- Package đã thay đổi

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.btl.R;
import com.example.btl.data.model.Task; // <-- Import đã thay đổi
import com.example.btl.ui.viewmodel.TaskViewModel; // <-- Import đã thay đổi
import com.example.btl.util.AlarmScheduler; // <-- Import mới
import com.example.btl.widget.TaskWidgetProvider;

import java.text.DateFormat;
import java.util.Calendar;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "com.example.btl.EXTRA_ID";
    public static final String EXTRA_TITLE = "com.example.btl.EXTRA_TITLE";
    public static final String EXTRA_NOTES = "com.example.btl.EXTRA_NOTES";
    public static final String EXTRA_PRIORITY = "com.example.btl.EXTRA_PRIORITY";
    public static final String EXTRA_CATEGORY = "com.example.btl.EXTRA_CATEGORY";
    public static final String EXTRA_DUE_DATE = "com.example.btl.EXTRA_DUE_DATE";
    public static final String EXTRA_IS_COMPLETED = "com.example.btl.EXTRA_IS_COMPLETED";

    private EditText editTextTitle;
    private EditText editTextNotes;
    private RadioGroup radioGroupPriority;
    private RadioGroup radioGroupCategory;
    private TextView textViewDueDate;
    private TextView textViewDueTime;
    private Button buttonSave;
    private Toolbar toolbar;

    private TaskViewModel taskViewModel;
    private Calendar selectedDate = Calendar.getInstance();
    private int currentTaskId = -1;
    private boolean currentTaskIsCompleted = false;
    private long originalDueDateMillis = -1;

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task); // <-- Dùng layout gộp

        editTextTitle = findViewById(R.id.edit_text_task_title);
        editTextNotes = findViewById(R.id.edit_text_notes);
        radioGroupPriority = findViewById(R.id.radio_group_priority);
        radioGroupCategory = findViewById(R.id.radio_group_category);
        textViewDueDate = findViewById(R.id.text_view_due_date);
        textViewDueTime = findViewById(R.id.text_view_due_time);
        buttonSave = findViewById(R.id.button_save_task);
        toolbar = findViewById(R.id.toolbar_add_task);

        toolbar.setNavigationOnClickListener(v -> finish());
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            // Chế độ Sửa
            toolbar.setTitle("Sửa Nghiệm Vụ");
            buttonSave.setText("CẬP NHẬT");

            currentTaskId = intent.getIntExtra(EXTRA_ID, -1);
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextNotes.setText(intent.getStringExtra(EXTRA_NOTES));
            currentTaskIsCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false);

            int priority = intent.getIntExtra(EXTRA_PRIORITY, 2);
            if (priority == 1) radioGroupPriority.check(R.id.radio_priority_low);
            else if (priority == 3) radioGroupPriority.check(R.id.radio_priority_high);
            else radioGroupPriority.check(R.id.radio_priority_medium);

            String category = intent.getStringExtra(EXTRA_CATEGORY);
            if ("Work".equals(category)) radioGroupCategory.check(R.id.radio_category_work);
            else if ("Wishlist".equals(category)) radioGroupCategory.check(R.id.radio_category_wishlist);
            else radioGroupCategory.check(R.id.radio_category_personal);

            originalDueDateMillis = intent.getLongExtra(EXTRA_DUE_DATE, -1);
            if (originalDueDateMillis != -1) {
                selectedDate.setTimeInMillis(originalDueDateMillis);
                updateDateAndTimeViews();
            }
        } else {
            // Chế độ Thêm
            toolbar.setTitle("Thêm Nghiệm Vụ");
            buttonSave.setText("LƯU NGHIỆM VỤ");
            requestNotificationPermission(); // Chỉ xin quyền khi thêm mới
        }

        textViewDueDate.setOnClickListener(v -> showDatePickerDialog());
        textViewDueTime.setOnClickListener(v -> showTimePickerDialog());
        buttonSave.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please insert a title", Toast.LENGTH_SHORT).show();
            return;
        }

        int priority = 2;
        int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.radio_priority_low) priority = 1;
        else if (selectedPriorityId == R.id.radio_priority_high) priority = 3;

        String category = "Personal";
        int selectedCategoryId = radioGroupCategory.getCheckedRadioButtonId();
        if (selectedCategoryId == R.id.radio_category_work) category = "Work";
        else if (selectedCategoryId == R.id.radio_category_wishlist) category = "Wishlist";

        long newDueDate = selectedDate.getTimeInMillis();
        Task task = new Task(title, notes, priority, category, currentTaskIsCompleted, newDueDate);

        if (currentTaskId != -1) {
            // Cập nhật Task
            task.setId(currentTaskId);

            // 1. Hủy thông báo cũ (nếu có)
            if (originalDueDateMillis != -1) {
                AlarmScheduler.cancelNotification(this, originalDueDateMillis); // <-- Dùng AlarmScheduler
            }
            // 2. Đặt lịch thông báo mới
            AlarmScheduler.scheduleNotification(this, task); // <-- Dùng AlarmScheduler

            taskViewModel.update(task);
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        } else {
            // Thêm Task mới
            AlarmScheduler.scheduleNotification(this, task); // <-- Dùng AlarmScheduler
            taskViewModel.insert(task);
            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, TaskWidgetProvider.class)
        );
        // Báo cho ListView của widget biết dữ liệu đã thay đổi
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        finish();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateAndTimeViews();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute);
                    selectedDate.set(Calendar.SECOND, 0);
                    updateDateAndTimeViews();
                },
                selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                true // 24-hour format
        );
        timePickerDialog.show();
    }

    private void updateDateAndTimeViews() {
        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDate.getTime());
        String formattedTime = android.text.format.DateFormat.getTimeFormat(this).format(selectedDate.getTime());
        textViewDueDate.setText(formattedDate);
        textViewDueTime.setText(formattedTime);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
}