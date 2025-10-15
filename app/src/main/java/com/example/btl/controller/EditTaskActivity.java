package com.example.btl.controller;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
import java.util.Date;

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
    private TaskViewModel taskViewModel;
    private Calendar selectedDate = Calendar.getInstance();
    private int currentTaskId = -1;
    private boolean currentTaskIsCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task); // Sử dụng layout mới

        editTextTitle = findViewById(R.id.edit_text_task_title);
        editTextNotes = findViewById(R.id.edit_text_notes);
        radioGroupPriority = findViewById(R.id.radio_group_priority);
        radioGroupCategory = findViewById(R.id.radio_group_category);
        textViewDueDate = findViewById(R.id.text_view_due_date);
        Button buttonSave = findViewById(R.id.button_save_task);

        Toolbar toolbar = findViewById(R.id.toolbar_add_task);
        toolbar.setNavigationOnClickListener(v -> finish());

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Lấy dữ liệu được truyền từ MainActivity và hiển thị lên UI
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            currentTaskId = intent.getIntExtra(EXTRA_ID, -1);
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextNotes.setText(intent.getStringExtra(EXTRA_NOTES));
            currentTaskIsCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false);

            // Set Priority RadioButton
            int priority = intent.getIntExtra(EXTRA_PRIORITY, 2);
            if (priority == 1) radioGroupPriority.check(R.id.radio_priority_low);
            else if (priority == 3) radioGroupPriority.check(R.id.radio_priority_high);
            else radioGroupPriority.check(R.id.radio_priority_medium);

            // Set Category RadioButton
            String category = intent.getStringExtra(EXTRA_CATEGORY);
            if (category.equals("Work")) radioGroupCategory.check(R.id.radio_category_work);
            else if (category.equals("Wishlist")) radioGroupCategory.check(R.id.radio_category_wishlist);
            else radioGroupCategory.check(R.id.radio_category_personal);

            // Set Due Date
            long dueDateMillis = intent.getLongExtra(EXTRA_DUE_DATE, -1);
            if(dueDateMillis != -1) {
                selectedDate.setTimeInMillis(dueDateMillis);
                String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDate.getTime());
                textViewDueDate.setText(formattedDate);
            }
        }

        textViewDueDate.setOnClickListener(v -> showDatePickerDialog());
        buttonSave.setOnClickListener(v -> updateTask());
    }

    private void updateTask() {
        String title = editTextTitle.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please insert a title", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy Priority
        int priority = 2;
        int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.radio_priority_low) priority = 1;
        else if (selectedPriorityId == R.id.radio_priority_high) priority = 3;

        // Lấy Category
        String category = "Personal";
        int selectedCategoryId = radioGroupCategory.getCheckedRadioButtonId();
        if (selectedCategoryId == R.id.radio_category_work) category = "Work";
        else if (selectedCategoryId == R.id.radio_category_wishlist) category = "Wishlist";

        long dueDate = selectedDate.getTimeInMillis();

        // Tạo đối tượng Task mới và gọi hàm update
        Task task = new Task(title, notes, priority, category, currentTaskIsCompleted, dueDate);
        task.setId(currentTaskId);
        taskViewModel.update(task);

        Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        finish();
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
}