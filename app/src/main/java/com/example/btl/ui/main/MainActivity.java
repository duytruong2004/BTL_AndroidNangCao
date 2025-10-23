package com.example.btl.ui.main;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btl.R;

import com.example.btl.data.model.Task;
import com.example.btl.ui.addedit.AddEditTaskActivity;
import com.example.btl.ui.calendar.CalendarActivity;
import com.example.btl.ui.viewmodel.TaskViewModel;
import com.example.btl.util.DateUtil;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

// --- CẬP NHẬT: Thêm OnTaskClickListener vào danh sách implements ---
public class MainActivity extends AppCompatActivity
        implements GroupedTaskAdapter.OnTaskToggleListener,
        GroupedTaskAdapter.OnHeaderClickListener,
        GroupedTaskAdapter.OnTaskClickListener { // <-- THÊM VÀO ĐÂY

    private TaskViewModel taskViewModel;
    private GroupedTaskAdapter adapter;
    private LiveData<List<Task>> currentLiveData = null;
    private Observer<List<Task>> taskObserver = null;
    private Map<String, Boolean> headerExpansionState = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        ChipGroup chipGroup = findViewById(R.id.chip_group_filter);
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(true);

        adapter = new GroupedTaskAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setOnTaskToggleListener(this);
        adapter.setOnHeaderClickListener(this);
        adapter.setOnTaskClickListener(this); // <-- CẬP NHẬT: Set listener mới

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        taskObserver = tasks -> {
            List<ListItem> groupedList = groupTasksByDate(tasks);
            adapter.submitList(groupedList);
        };

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });

        // ... (Code chipGroup và BottomAppBar không đổi) ...
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            removeObserver();
            if (checkedId == R.id.chip_all) {
                currentLiveData = taskViewModel.getAllTasksSortedByDateGroup();
            } else if (checkedId == R.id.chip_personal) {
                currentLiveData = taskViewModel.getTasksByCategorySortedByDateGroup("Personal");
            } else if (checkedId == R.id.chip_work) {
                currentLiveData = taskViewModel.getTasksByCategorySortedByDateGroup("Work");
            } else if (checkedId == R.id.chip_wishlist) {
                currentLiveData = taskViewModel.getTasksByCategorySortedByDateGroup("Wishlist");
            }
            if (currentLiveData != null) {
                currentLiveData.observe(this, taskObserver);
            }
        });

        currentLiveData = taskViewModel.getAllTasksSortedByDateGroup();
        currentLiveData.observe(this, taskObserver);

        ImageButton btnList = findViewById(R.id.btn_list);
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        ImageButton btnNotifications = findViewById(R.id.btn_notifications);
        btnList.setOnClickListener(v -> Toast.makeText(this, "List Clicked", Toast.LENGTH_SHORT).show());
        btnMenu.setOnClickListener(v -> Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show());
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
        btnNotifications.setOnClickListener(v -> Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show());


        // ... (Code ItemTouchHelper không đổi) ...
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Task task = adapter.getTaskObjectAt(position);
                if (task == null) {
                    adapter.notifyItemChanged(position);
                    return;
                }
                if (direction == ItemTouchHelper.LEFT) { // Xóa
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa công việc \"" + task.getTitle() + "\"?")
                            .setPositiveButton("Xóa", (dialog, which) -> taskViewModel.delete(task))
                            .setNegativeButton("Hủy", (dialog, which) -> adapter.notifyItemChanged(position))
                            .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                            .show();
                } else { // Sửa
                    Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                    intent.putExtra(AddEditTaskActivity.EXTRA_ID, task.getId());
                    intent.putExtra(AddEditTaskActivity.EXTRA_TITLE, task.getTitle());
                    intent.putExtra(AddEditTaskActivity.EXTRA_NOTES, task.getNotes());
                    intent.putExtra(AddEditTaskActivity.EXTRA_PRIORITY, task.getPriority());
                    intent.putExtra(AddEditTaskActivity.EXTRA_CATEGORY, task.getCategory());
                    intent.putExtra(AddEditTaskActivity.EXTRA_DUE_DATE, task.getDueDate());
                    intent.putExtra(AddEditTaskActivity.EXTRA_IS_COMPLETED, task.isCompleted());
                    startActivity(intent);
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof GroupedTaskAdapter.HeaderViewHolder) { return 0; }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (viewHolder instanceof GroupedTaskAdapter.TaskViewHolder) {
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
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Giữ lại phần sửa lỗi UI vuốt
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Hàm onTaskToggled (Đã sửa lỗi check/uncheck)
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

    // Hàm onHeaderClick (Không đổi)
    @Override
    public void onHeaderClick(HeaderItem headerItem, int position) {
        boolean isCurrentlyExpanded = headerExpansionState.getOrDefault(headerItem.getTitle(), true);
        headerExpansionState.put(headerItem.getTitle(), !isCurrentlyExpanded);
        if (currentLiveData != null && currentLiveData.getValue() != null) {
            List<ListItem> updatedGroupedList = groupTasksByDate(currentLiveData.getValue());
            adapter.submitList(updatedGroupedList);
        }
    }

    // --- CẬP NHẬT: Thêm hàm xử lý sự kiện click vào task ---
    @Override
    public void onTaskClick(Task task) {
        // Mở màn hình AddEditTaskActivity (giống hệt logic của vuốt "Sửa")
        Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_ID, task.getId());
        intent.putExtra(AddEditTaskActivity.EXTRA_TITLE, task.getTitle());
        intent.putExtra(AddEditTaskActivity.EXTRA_NOTES, task.getNotes());
        intent.putExtra(AddEditTaskActivity.EXTRA_PRIORITY, task.getPriority());
        intent.putExtra(AddEditTaskActivity.EXTRA_CATEGORY, task.getCategory());
        intent.putExtra(AddEditTaskActivity.EXTRA_DUE_DATE, task.getDueDate());
        intent.putExtra(AddEditTaskActivity.EXTRA_IS_COMPLETED, task.isCompleted());
        startActivity(intent);
    }
    // --- KẾT THÚC CẬP NHẬT ---


    // --- Các hàm còn lại (Không đổi) ---
    private void removeObserver() { if (currentLiveData != null && taskObserver != null) { currentLiveData.removeObserver(taskObserver); } }
    @Override protected void onDestroy() { super.onDestroy(); removeObserver(); }

    private List<ListItem> groupTasksByDate(List<Task> tasks) {
        List<ListItem> items = new ArrayList<>();
        if (tasks == null) return items;

        long todayStartMillis = DateUtil.getStartOfDayMillis(System.currentTimeMillis());
        long tomorrowStartMillis = todayStartMillis + (24 * 60 * 60 * 1000);

        String currentHeaderTitle = null;
        HeaderItem currentHeaderItem = null;

        for (Task task : tasks) {
            long dueDateMillis = task.getDueDate();
            String headerTitle;

            if (dueDateMillis <= 0) { headerTitle = "Không có ngày hạn"; }
            else if (dueDateMillis < todayStartMillis) { headerTitle = "Trước"; }
            else if (dueDateMillis < tomorrowStartMillis) { headerTitle = "Hôm nay"; }
            else { headerTitle = "Tương lai"; }

            boolean isExpanded = headerExpansionState.getOrDefault(headerTitle, true);

            if (!headerTitle.equals(currentHeaderTitle)) {
                currentHeaderTitle = headerTitle;
                currentHeaderItem = new HeaderItem(currentHeaderTitle);
                currentHeaderItem.setExpanded(isExpanded);
                items.add(currentHeaderItem);
            }

            if (currentHeaderItem != null && currentHeaderItem.isExpanded()) {
                items.add(new TaskItem(task));
            }
        }
        return items;
    }
}