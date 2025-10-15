package com.example.btl.controller;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btl.R;
import com.example.btl.model.Task;
import com.example.btl.model.TaskViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;

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

        adapter = new TaskAdapter(this);
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

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = adapter.getTaskAt(position);

                if (direction == ItemTouchHelper.LEFT) { // Vuốt sang trái để hỏi XÓA

                    // Hiện hộp thoại xác nhận
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa công việc \"" + task.getTitle() + "\"?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                // Nếu người dùng đồng ý, thì mới thực hiện xóa
                                taskViewModel.delete(task);
                                Toast.makeText(MainActivity.this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> {
                                // Nếu người dùng hủy, ta cần báo cho adapter vẽ lại item
                                // để nó quay về trạng thái ban đầu.
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            })
                            .setOnCancelListener(dialog -> {
                                // Tương tự, nếu người dùng bấm ra ngoài để tắt dialog
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            })
                            .create()
                            .show();

                } else { // Vuốt sang phải để SỬA
                    Toast.makeText(MainActivity.this, "Chức năng sửa...", Toast.LENGTH_SHORT).show();
                    // Gọi notifyItemChanged để RecyclerView vẽ lại item
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Phần này giữ nguyên, không thay đổi
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.priority_high))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("Delete")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.green))
                        .addSwipeRightActionIcon(R.drawable.ic_edit)
                        .addSwipeRightLabel("Edit")
                        .setSwipeRightLabelColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }
}
