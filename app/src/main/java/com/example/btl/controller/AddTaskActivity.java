package com.example.btl.controller;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
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
import com.example.btl.model.Task;
import com.example.btl.model.TaskViewModel;
import java.text.DateFormat;
import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextNotes;
    private RadioGroup radioGroupPriority;
    private RadioGroup radioGroupCategory;
    private TaskViewModel taskViewModel;
    private TextView textViewDueDate;
    private TextView textViewDueTime;
    private Calendar selectedDate = Calendar.getInstance();
    private static final int NOTIFICATION_PERMISSION_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editTextTitle = findViewById(R.id.edit_text_task_title);
        editTextNotes = findViewById(R.id.edit_text_notes);
        radioGroupPriority = findViewById(R.id.radio_group_priority);
        radioGroupCategory = findViewById(R.id.radio_group_category);
        Button buttonSave = findViewById(R.id.button_save_task);
        textViewDueDate = findViewById(R.id.text_view_due_date);
        textViewDueTime = findViewById(R.id.text_view_due_time);

        textViewDueDate.setOnClickListener(v -> showDatePickerDialog());
        textViewDueTime.setOnClickListener(v -> showTimePickerDialog());

        Toolbar toolbar = findViewById(R.id.toolbar_add_task);
        toolbar.setNavigationOnClickListener(v -> finish());

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        buttonSave.setOnClickListener(v -> saveTask());
        requestNotificationPermission();
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDate.getTime());
                    textViewDueDate.setText(formattedDate);
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
                    String formattedTime = android.text.format.DateFormat.getTimeFormat(this).format(selectedDate.getTime());
                    textViewDueTime.setText(formattedTime);
                },
                selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                true // Sử dụng định dạng 24 giờ
        );
        timePickerDialog.show();
    }
  

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please insert a title", Toast.LENGTH_SHORT).show();
            return;
        }

        int priority = 2; // Default Medium
        int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.radio_priority_low) priority = 1;
        else if (selectedPriorityId == R.id.radio_priority_high) priority = 3;

        String category = "Personal"; // Default
        int selectedCategoryId = radioGroupCategory.getCheckedRadioButtonId();
        if (selectedCategoryId == R.id.radio_category_work) category = "Work";
        else if (selectedCategoryId == R.id.radio_category_wishlist) category = "Wishlist";

        long dueDate = selectedDate.getTimeInMillis();
        Task task = new Task(title, notes, priority, category, false, dueDate);

        // --- PHẦN ĐƯỢC THÊM VÀO ---
        // Đặt lịch thông báo trước khi lưu
        scheduleNotification(task, dueDate);
        // --- KẾT THÚC PHẦN THÊM VÀO ---

        taskViewModel.insert(task);
        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        finish();
    }

   
    private void scheduleNotification(Task task, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("TASK_TITLE", task.getTitle());
        intent.putExtra("TASK_NOTES", task.getNotes());
        // Sử dụng timestamp làm ID cho PendingIntent để đảm bảo nó là duy nhất
        int notificationId = (int) timeInMillis;
        intent.putExtra("TASK_ID", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Chỉ đặt lịch nếu thời gian là trong tương lai
        if (timeInMillis > System.currentTimeMillis() && alarmManager != null) {
            try {
                // Sử dụng setExactAndAllowWhileIdle để đảm bảo thông báo hiển thị đúng giờ
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } catch (SecurityException e) {
                // Xử lý trường hợp không có quyền đặt báo thức chính xác
                Toast.makeText(this, "Permission to set exact alarms is not granted.", Toast.LENGTH_LONG).show();
            }
        }
    }
    // --- KẾT THÚC PHẦN THÊM VÀO ---
}