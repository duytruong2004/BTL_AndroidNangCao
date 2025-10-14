package com.example.btl.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btl.R;
import com.example.btl.model.TaskViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Ánh xạ view ---
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        ChipGroup chipGroup = findViewById(R.id.chip_group_filter);
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);

        // --- Cấu hình RecyclerView ---
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final TaskAdapter adapter = new TaskAdapter(this);
        recyclerView.setAdapter(adapter);

        // --- ViewModel ---
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, adapter::submitList);

        // --- FAB thêm mới ---
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // --- ChipGroup lọc dữ liệu ---
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                taskViewModel.getAllTasks().observe(this, adapter::submitList);
            } else if (checkedId == R.id.chip_personal) {
                taskViewModel.getTasksByCategory("Personal").observe(this, adapter::submitList);
            } else if (checkedId == R.id.chip_work) {
                taskViewModel.getTasksByCategory("Work").observe(this, adapter::submitList);
            } else if (checkedId == R.id.chip_wishlist) {
                taskViewModel.getTasksByCategory("Wishlist").observe(this, adapter::submitList);
            }
        });

        // --- Các nút trong BottomAppBar ---
        ImageButton btnList = findViewById(R.id.btn_list);
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnNotifications = findViewById(R.id.btn_notifications);

        btnList.setOnClickListener(v ->
                Toast.makeText(this, "List Clicked", Toast.LENGTH_SHORT).show());

        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show());

        btnCalendar.setOnClickListener(v ->
                Toast.makeText(this, "Calendar Clicked", Toast.LENGTH_SHORT).show());

        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show());
    }
}
