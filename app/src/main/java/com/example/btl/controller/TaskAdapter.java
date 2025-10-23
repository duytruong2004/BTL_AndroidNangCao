package com.example.btl.controller;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl.R;
import com.example.btl.model.Task;
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
                    oldItem.isCompleted() == newItem.isCompleted();
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

            // Cập nhật ngày
            if (currentTask.getDueDate() > 0) {
                String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(currentTask.getDueDate()));
                holder.dueDate.setText(formattedDate);
                holder.dueDate.setVisibility(View.VISIBLE);
            } else {
                holder.dueDate.setVisibility(View.GONE);
            }

            // --- PHẦN CẬP NHẬT GIAO DIỆN HOÀN THÀNH ---
            holder.checkBoxCompleted.setChecked(currentTask.isCompleted());

            if (currentTask.isCompleted()) {
                // Thêm gạch ngang
                holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                // Cập nhật text "1/1"
                holder.subtaskInfo.setText("1/1");
                // Làm mờ toàn bộ item
                holder.itemView.setAlpha(0.5f);
            } else {
                // Xóa gạch ngang
                holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                // Cập nhật text "0/1"
                holder.subtaskInfo.setText("0/1");
                // Hiển thị rõ item
                holder.itemView.setAlpha(1.0f);
            }
            // --- KẾT THÚC CẬP NHẬT ---


            holder.priorityIcon.setVisibility(View.VISIBLE);
            switch (currentTask.getPriority()) {
                case 1: // High
                    holder.priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_high));
                    break;
                case 2: // Medium
                    holder.priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_medium));
                    break;
                case 3: // Low
                    holder.priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_low));
                    break;
                default:
                    holder.priorityIcon.setVisibility(View.INVISIBLE);
                    break;
            }

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
        }
    }
    public Task getTaskAt(int position) {
        return getItem(position);
    }

    // ViewHolder
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle;
        private final TextView subtaskInfo;
        private final ImageView priorityIcon;
        private final ImageView categoryIcon;
        private final TextView dueDate;
        private final CheckBox checkBoxCompleted;

        private TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.text_view_task_title);
            subtaskInfo = itemView.findViewById(R.id.text_view_subtask_info);
            priorityIcon = itemView.findViewById(R.id.image_view_priority);
            categoryIcon = itemView.findViewById(R.id.image_view_category);
            dueDate = itemView.findViewById(R.id.text_view_item_due_date);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);

            // Thiết lập listener
            checkBoxCompleted.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (toggleListener != null && position != RecyclerView.NO_POSITION) {
                    Task task = getItem(position);
                    toggleListener.onTaskToggled(task);
                }
            });
        }
    }

    // Interface và Setter (giữ nguyên)
    public interface OnTaskToggleListener {
        void onTaskToggled(Task task);
    }

    public void setOnTaskToggleListener(OnTaskToggleListener listener) {
        this.toggleListener = listener;
    }
}