package com.example.btl.controller;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.btl.R;
import com.example.btl.model.Task;
import com.example.btl.model.TaskViewModel;

// Controller: Activity để thêm công việc mới
public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextNotes;
    private RadioGroup radioGroupPriority;
    private RadioGroup radioGroupCategory;
    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editTextTitle = findViewById(R.id.edit_text_task_title);
        editTextNotes = findViewById(R.id.edit_text_notes);
        radioGroupPriority = findViewById(R.id.radio_group_priority);
        radioGroupCategory = findViewById(R.id.radio_group_category);
        Button buttonSave = findViewById(R.id.button_save_task);

        Toolbar toolbar = findViewById(R.id.toolbar_add_task);
        toolbar.setNavigationOnClickListener(v -> finish());

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        buttonSave.setOnClickListener(v -> saveTask());
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


        Task task = new Task(title, notes, priority, category, false);
        taskViewModel.insert(task);
        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
