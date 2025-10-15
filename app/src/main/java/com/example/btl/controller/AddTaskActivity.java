package com.example.btl.controller;


import android.app.DatePickerDialog;
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

// Controller: Activity để thêm công việc mới
public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextNotes;
    private RadioGroup radioGroupPriority;
    private RadioGroup radioGroupCategory;
    private TaskViewModel taskViewModel;
    private TextView textViewDueDate;
    private Calendar selectedDate = Calendar.getInstance();
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
        textViewDueDate.setOnClickListener(v -> showDatePickerDialog());


        Toolbar toolbar = findViewById(R.id.toolbar_add_task);
        toolbar.setNavigationOnClickListener(v -> finish());

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        buttonSave.setOnClickListener(v -> saveTask());
    }
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Hiển thị ngày đã chọn
                    String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDate.getTime());
                    textViewDueDate.setText(formattedDate);
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
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
        taskViewModel.insert(task);
        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
