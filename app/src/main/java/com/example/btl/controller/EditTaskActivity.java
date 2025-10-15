package com.example.btl.controller;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.example.btl.R;
import com.example.btl.model.Task;
import com.example.btl.model.TaskViewModel;
import java.text.DateFormat;
import java.util.Calendar;

public class EditTaskActivity extends AppCompatActivity {

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
    private TextView textViewDueTime; // Thêm TextView cho giờ
    private TaskViewModel taskViewModel;
    private Calendar selectedDate = Calendar.getInstance();
    private int currentTaskId = -1;
    private boolean currentTaskIsCompleted = false;
    private long originalDueDateMillis = -1; // Lưu lại due date gốc để hủy thông báo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        editTextTitle = findViewById(R.id.edit_text_task_title);
        editTextNotes = findViewById(R.id.edit_text_notes);
        radioGroupPriority = findViewById(R.id.radio_group_priority);
        radioGroupCategory = findViewById(R.id.radio_group_category);
        textViewDueDate = findViewById(R.id.text_view_due_date);
        textViewDueTime = findViewById(R.id.text_view_due_time); // Ánh xạ view
        Button buttonSave = findViewById(R.id.button_save_task);

        Toolbar toolbar = findViewById(R.id.toolbar_add_task);
        toolbar.setNavigationOnClickListener(v -> finish());

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Lấy dữ liệu và hiển thị
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            currentTaskId = intent.getIntExtra(EXTRA_ID, -1);
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextNotes.setText(intent.getStringExtra(EXTRA_NOTES));
            currentTaskIsCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false);

            // Set Priority
            int priority = intent.getIntExtra(EXTRA_PRIORITY, 2);
            if (priority == 1) radioGroupPriority.check(R.id.radio_priority_low);
            else if (priority == 3) radioGroupPriority.check(R.id.radio_priority_high);
            else radioGroupPriority.check(R.id.radio_priority_medium);

            // Set Category
            String category = intent.getStringExtra(EXTRA_CATEGORY);
            if ("Work".equals(category)) radioGroupCategory.check(R.id.radio_category_work);
            else if ("Wishlist".equals(category)) radioGroupCategory.check(R.id.radio_category_wishlist);
            else radioGroupCategory.check(R.id.radio_category_personal);

            // Set Due Date and Time
            originalDueDateMillis = intent.getLongExtra(EXTRA_DUE_DATE, -1);
            if (originalDueDateMillis != -1) {
                selectedDate.setTimeInMillis(originalDueDateMillis);
                updateDateAndTimeViews();
            }
        }

        textViewDueDate.setOnClickListener(v -> showDatePickerDialog());
        textViewDueTime.setOnClickListener(v -> showTimePickerDialog()); // Thêm sự kiện click
        buttonSave.setOnClickListener(v -> updateTask());
    }

    private void updateTask() {
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
        task.setId(currentTaskId);

        // --- CẬP NHẬT LOGIC THÔNG BÁO ---
        // 1. Hủy thông báo cũ
        if (originalDueDateMillis != -1) {
            cancelNotification((int) originalDueDateMillis);
        }
        // 2. Đặt lịch thông báo mới
        scheduleNotification(task, newDueDate);
        // --- KẾT THÚC CẬP NHẬT ---

        taskViewModel.update(task);

        Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        finish();
    }
c
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

    private void scheduleNotification(Task task, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("TASK_TITLE", task.getTitle());
        intent.putExtra("TASK_NOTES", task.getNotes());
        int notificationId = (int) timeInMillis;
        intent.putExtra("TASK_ID", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (timeInMillis > System.currentTimeMillis() && alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } catch (SecurityException e) {
                Toast.makeText(this, "Permission to set exact alarms is not granted.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void cancelNotification(int notificationId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}