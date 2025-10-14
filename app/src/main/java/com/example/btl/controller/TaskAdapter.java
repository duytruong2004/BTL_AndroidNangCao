package com.example.btl.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

// Nâng cấp lên ListAdapter để tương thích với ViewModel và LiveData
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    private final Context context;

    public TaskAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    // DiffUtil giúp RecyclerView nhận biết item nào đã thay đổi, thêm, hoặc xóa
    // một cách hiệu quả.
    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    Objects.equals(oldItem.getCategory(), newItem.getCategory());
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
        // Sử dụng getItem(position) thay vì truy cập trực tiếp vào danh sách
        Task currentTask = getItem(position);
        if (currentTask != null) {
            holder.taskTitle.setText(currentTask.getTitle());
            holder.subtaskInfo.setText("0/1"); // Placeholder for subtask info

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
                        holder.categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_wishlist));
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

    // ViewHolder giữ nguyên
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle;
        private final TextView subtaskInfo;
        private final ImageView priorityIcon;
        private final ImageView categoryIcon;

        private TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.text_view_task_title);
            subtaskInfo = itemView.findViewById(R.id.text_view_subtask_info);
            priorityIcon = itemView.findViewById(R.id.image_view_priority);
            categoryIcon = itemView.findViewById(R.id.image_view_category);
        }
    }
}

