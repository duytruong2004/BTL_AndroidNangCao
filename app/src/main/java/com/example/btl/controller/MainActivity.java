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
// Thêm import cho Observer và LiveData
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
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

// Thêm import cho List
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskToggleListener {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;
    // Biến để lưu trữ LiveData đang được theo dõi
    private LiveData<List<Task>> currentLiveData = null;
    // Biến để lưu trữ Observer hiện tại
    private Observer<List<Task>> taskObserver = null;


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
        adapter.setOnTaskToggleListener(this);

        // --- ViewModel ---
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // --- Khởi tạo Observer ---
        // Observer này sẽ được dùng lại cho tất cả các LiveData
        taskObserver = new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                adapter.submitList(tasks);
            }
        };

        // --- FAB thêm mới ---
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // --- ChipGroup lọc dữ liệu (ĐÃ SỬA LỖI) ---
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // 1. Xóa observer cũ nếu có
            removeObserver();

            // 2. Lấy LiveData mới dựa trên chip được chọn
            if (checkedId == R.id.chip_all) {
                currentLiveData = taskViewModel.getAllTasks();
            } else if (checkedId == R.id.chip_personal) {
                currentLiveData = taskViewModel.getTasksByCategory("Personal");
            } else if (checkedId == R.id.chip_work) {
                currentLiveData = taskViewModel.getTasksByCategory("Work");
            } else if (checkedId == R.id.chip_wishlist) {
                currentLiveData = taskViewModel.getTasksByCategory("Wishlist");
            }

            // 3. Thêm observer mới vào LiveData mới
            if (currentLiveData != null) {
                currentLiveData.observe(this, taskObserver);
            }
        });

        // --- Hiển thị danh sách ban đầu ("Tất Cả") ---
        currentLiveData = taskViewModel.getAllTasks();
        currentLiveData.observe(this, taskObserver);


        // --- Các nút trong BottomAppBar ---
        // (Giữ nguyên code xử lý các nút này)
        ImageButton btnList = findViewById(R.id.btn_list);
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnNotifications = findViewById(R.id.btn_notifications);

        btnList.setOnClickListener(v ->
                Toast.makeText(this, "List Clicked", Toast.LENGTH_SHORT).show());
        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show());
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show());


        // --- ItemTouchHelper (Vuốt để Xóa/Sửa) ---
        // (Giữ nguyên code xử lý vuốt)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            // ... (code onMove, onSwiped, onChildDraw giữ nguyên)
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = adapter.getTaskAt(position);

                if (direction == ItemTouchHelper.LEFT) { // Vuốt sang trái để hỏi XÓA
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa công việc \"" + task.getTitle() + "\"?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                taskViewModel.delete(task);
                                Toast.makeText(MainActivity.this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> {
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            })
                            .setOnCancelListener(dialog -> {
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            })
                            .create()
                            .show();

                } else { // Vuốt sang phải để SỬA
                    Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                    intent.putExtra(EditTaskActivity.EXTRA_ID, task.getId());
                    intent.putExtra(EditTaskActivity.EXTRA_TITLE, task.getTitle());
                    intent.putExtra(EditTaskActivity.EXTRA_NOTES, task.getNotes());
                    intent.putExtra(EditTaskActivity.EXTRA_PRIORITY, task.getPriority());
                    intent.putExtra(EditTaskActivity.EXTRA_CATEGORY, task.getCategory());
                    intent.putExtra(EditTaskActivity.EXTRA_DUE_DATE, task.getDueDate());
                    intent.putExtra(EditTaskActivity.EXTRA_IS_COMPLETED, task.isCompleted());
                    startActivity(intent);

                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.priority_high))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("Xóa")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.green))
                        .addSwipeRightActionIcon(R.drawable.ic_edit)
                        .addSwipeRightLabel("Sửa")
                        .setSwipeRightLabelColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    // --- Phương thức xử lý toggle checkbox ---
    // (Giữ nguyên code này)
    @Override
    public void onTaskToggled(Task task) {
        boolean newCompletedState = !task.isCompleted();
        Task taskToUpdate = new Task(
                task.getTitle(),
                task.getNotes(),
                task.getPriority(),
                task.getCategory(),
                newCompletedState,
                task.getDueDate()
        );
        taskToUpdate.setId(task.getId());
        taskViewModel.update(taskToUpdate);
    }

    // --- Phương thức helper để xóa observer cũ ---
    private void removeObserver() {
        if (currentLiveData != null && taskObserver != null) {
            currentLiveData.removeObserver(taskObserver);
        }
    }

    // --- Ghi đè onDestroy để đảm bảo observer được xóa khi Activity bị hủy ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeObserver(); // Xóa observer khi activity bị hủy
    }
}