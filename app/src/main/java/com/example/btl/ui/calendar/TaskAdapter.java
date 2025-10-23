package com.example.btl.ui.calendar;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton; // Quan trọng: import này cần có
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl.R;
import com.example.btl.data.model.Task;
import java.util.Objects;
import java.text.DateFormat;
import java.util.Date;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    private final Context context;
    private OnTaskToggleListener toggleListener;

    public TaskAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    // DiffUtil (Không đổi)
    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    Objects.equals(oldItem.getCategory(), newItem.getCategory()) &&
                    oldItem.isCompleted() == newItem.isCompleted() &&
                    Objects.equals(oldItem.getNotes(), newItem.getNotes());
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = getItem(position);
        if (currentTask != null) {
            holder.taskTitle.setText(currentTask.getTitle());

            // Logic dueDate (Không đổi)
            if (currentTask.getDueDate() > 0) {
                String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(currentTask.getDueDate()));
                holder.dueDate.setText(formattedDate);
                holder.dueDate.setVisibility(View.VISIBLE);
            } else {
                holder.dueDate.setVisibility(View.GONE);
            }

            // Logic Checkbox (Đã sửa ở lần trước, giờ an toàn)
            holder.checkBoxCompleted.setChecked(currentTask.isCompleted());

            // Logic text 0/1 | 1/1 (Không đổi)
            if (currentTask.isCompleted()) {
                holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.subtaskInfo.setText("1/1");
                holder.itemView.setAlpha(0.5f);
            } else {
                holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.subtaskInfo.setText("0/1");
                holder.itemView.setAlpha(1.0f);
            }

            // --- CẬP NHẬT PHẦN PRIORITY ICON ---
            holder.priorityIcon.setVisibility(View.VISIBLE);
            // THÊM DÒNG NÀY ĐỂ SỬA LỖI ICON CHỒNG CHÉO:
            holder.priorityIcon.setImageResource(R.drawable.ic_flag);

            // Sau đó mới set màu
            switch (currentTask.getPriority()) {
                case 1: holder.priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_low)); break;
                case 2: holder.priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_medium)); break;
                case 3: holder.priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_high)); break;
                default:
                    holder.priorityIcon.setVisibility(View.INVISIBLE);
                    break;
            }
            // --- KẾT THÚC CẬP NHẬT ---

            // Logic category (Không đổi)
            if (currentTask.getCategory() != null && !currentTask.getCategory().isEmpty()) {
                holder.categoryIcon.setVisibility(View.VISIBLE);
                switch (currentTask.getCategory()) {
                    case "Work":
                        holder.categoryIcon.setImageResource(R.drawable.ic_work);
                        holder.categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_work));
                        break;
                    case "Personal":
                        holder.categoryIcon.setImageResource(R.drawable.ic_personal);
                        holder.categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_personal));
                        break;
                    case "Wishlist":
                        holder.categoryIcon.setImageResource(R.drawable.ic_wishlist);
                        holder.categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.purple_500));
                        break;
                    default:
                        holder.categoryIcon.setVisibility(View.INVISIBLE);
                        break;
                }
            } else {
                holder.categoryIcon.setVisibility(View.INVISIBLE);
            }

            // Logic notes icon (Không đổi)
            String notes = currentTask.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                holder.notesIcon.setVisibility(View.VISIBLE);
            } else {
                holder.notesIcon.setVisibility(View.GONE);
            }
        }
    }
    public Task getTaskAt(int position) {
        return getItem(position);
    }

    // ViewHolder (Đã sửa listener ở lần trước)
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle;
        private final TextView subtaskInfo;
        private final ImageView priorityIcon;
        private final ImageView categoryIcon;
        private final TextView dueDate;
        private final CheckBox checkBoxCompleted;
        private final ImageView notesIcon;

        private TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.text_view_task_title);
            subtaskInfo = itemView.findViewById(R.id.text_view_subtask_info);
            priorityIcon = itemView.findViewById(R.id.image_view_priority);
            categoryIcon = itemView.findViewById(R.id.image_view_category);
            dueDate = itemView.findViewById(R.id.text_view_item_due_date);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);
            notesIcon = itemView.findViewById(R.id.image_view_notes);

            // Dùng setOnCheckedChangeListener (Đã sửa ở lần trước)
            checkBoxCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getAdapterPosition();

                    if (toggleListener == null || position == RecyclerView.NO_POSITION) {
                        return;
                    }

                    Task task = getItem(position);

                    // Guard (bảo vệ)
                    if (task != null && task.isCompleted() != isChecked) {
                        toggleListener.onTaskToggled(task);
                    }
                }
            });
        }
    }

    // Interface và Setter (Không đổi)
    public interface OnTaskToggleListener {
        void onTaskToggled(Task task);
    }

    public void setOnTaskToggleListener(OnTaskToggleListener listener) {
        this.toggleListener = listener;
    }
}