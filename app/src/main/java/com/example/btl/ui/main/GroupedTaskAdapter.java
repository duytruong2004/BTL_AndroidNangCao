package com.example.btl.ui.main; // <-- Package đã thay đổi

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl.R;
import com.example.btl.data.model.Task; // <-- Import đã thay đổi

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class GroupedTaskAdapter extends ListAdapter<ListItem, RecyclerView.ViewHolder> {

    private final Context context;
    private OnTaskToggleListener toggleListener;
    private OnHeaderClickListener headerClickListener;

    private static final int TYPE_HEADER = ListItem.TYPE_HEADER;
    private static final int TYPE_TASK = ListItem.TYPE_TASK;

    public GroupedTaskAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    private static final DiffUtil.ItemCallback<ListItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem.getItemType() != newItem.getItemType()) return false;
            if (oldItem instanceof TaskItem && newItem instanceof TaskItem) {
                return ((TaskItem) oldItem).getTask().getId() == ((TaskItem) newItem).getTask().getId();
            } else if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
                return ((HeaderItem) oldItem).getTitle().equals(((HeaderItem) newItem).getTitle());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem instanceof TaskItem && newItem instanceof TaskItem) {
                Task oldTask = ((TaskItem) oldItem).getTask();
                Task newTask = ((TaskItem) newItem).getTask();
                return oldTask.getTitle().equals(newTask.getTitle()) &&
                        oldTask.getPriority() == newTask.getPriority() &&
                        Objects.equals(oldTask.getCategory(), newTask.getCategory()) &&
                        oldTask.isCompleted() == newTask.isCompleted() &&
                        oldTask.getDueDate() == newTask.getDueDate();
            } else if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
                return ((HeaderItem) oldItem).getTitle().equals(((HeaderItem) newItem).getTitle()) &&
                        ((HeaderItem) oldItem).isExpanded() == ((HeaderItem) newItem).isExpanded(); // Kiểm tra cả isExpanded
            }
            return false;
        }
    };

    // --- ViewHolder cho Header ---
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        ImageView expandArrow;
        LinearLayout headerLayout;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.text_view_header_title);
            expandArrow = itemView.findViewById(R.id.image_view_expand_arrow);
            headerLayout = itemView.findViewById(R.id.header_layout);

            headerLayout.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (headerClickListener != null && position != RecyclerView.NO_POSITION) {
                    ListItem item = getItem(position);
                    if (item instanceof HeaderItem) {
                        headerClickListener.onHeaderClick((HeaderItem) item, position);
                    }
                }
            });
        }

        void bind(HeaderItem headerItem) {
            headerTitle.setText(headerItem.getTitle());
            expandArrow.setImageResource(
                    headerItem.isExpanded() ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down
            );
        }
    }

    // --- ViewHolder cho Task ---
    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle; TextView subtaskInfo; ImageView priorityIcon;
        ImageView categoryIcon; TextView dueDate; CheckBox checkBoxCompleted;
        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.text_view_task_title);
            subtaskInfo = itemView.findViewById(R.id.text_view_subtask_info);
            priorityIcon = itemView.findViewById(R.id.image_view_priority);
            categoryIcon = itemView.findViewById(R.id.image_view_category);
            dueDate = itemView.findViewById(R.id.text_view_item_due_date);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);

            checkBoxCompleted.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (toggleListener != null && position != RecyclerView.NO_POSITION) {
                    ListItem item = getItem(position);
                    if (item instanceof TaskItem) {
                        toggleListener.onTaskToggled(((TaskItem) item).getTask());
                    }
                }
            });
        }
        void bind(TaskItem taskItem) {
            Task currentTask = taskItem.getTask();
            taskTitle.setText(currentTask.getTitle());
            if (currentTask.getDueDate() > 0) {
                String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(currentTask.getDueDate()));
                dueDate.setText(formattedDate); dueDate.setVisibility(View.VISIBLE);
            } else { dueDate.setVisibility(View.GONE); }

            checkBoxCompleted.setChecked(currentTask.isCompleted());

            if (currentTask.isCompleted()) {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                subtaskInfo.setText("1/1"); itemView.setAlpha(0.5f);
            } else {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                subtaskInfo.setText("0/1"); itemView.setAlpha(1.0f);
            }

            priorityIcon.setVisibility(View.VISIBLE);
            switch (currentTask.getPriority()) {
                // Đã sửa: 1=Low, 2=Medium, 3=High (theo code AddEdit)
                case 1: priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_low)); break;
                case 2: priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_medium)); break;
                case 3: priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_high)); break;
                default: priorityIcon.setVisibility(View.INVISIBLE); break;
            }

            if (currentTask.getCategory() != null && !currentTask.getCategory().isEmpty()) {
                categoryIcon.setVisibility(View.VISIBLE);
                switch (currentTask.getCategory()) {
                    case "Work": categoryIcon.setImageResource(R.drawable.ic_work); categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_work)); break;
                    case "Personal": categoryIcon.setImageResource(R.drawable.ic_personal); categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_personal)); break;
                    case "Wishlist": categoryIcon.setImageResource(R.drawable.ic_wishlist); categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_wishlist)); break;
                    default: categoryIcon.setVisibility(View.INVISIBLE); break;
                }
            } else { categoryIcon.setVisibility(View.INVISIBLE); }
        }
    }

    // --- Các hàm Adapter cơ bản ---
    @Override public int getItemViewType(int position) { return getItem(position).getItemType(); }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.list_item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) { ((HeaderViewHolder) holder).bind((HeaderItem) item); }
        else if (holder instanceof TaskViewHolder) { ((TaskViewHolder) holder).bind((TaskItem) item); }
    }

    // --- Các hàm tiện ích và Listener ---
    public Task getTaskObjectAt(int position) {
        ListItem item = getItem(position);
        if (item instanceof TaskItem) { return ((TaskItem) item).getTask(); }
        return null;
    }

    public interface OnTaskToggleListener { void onTaskToggled(Task task); }
    public void setOnTaskToggleListener(OnTaskToggleListener listener) { this.toggleListener = listener; }

    public interface OnHeaderClickListener { void onHeaderClick(HeaderItem headerItem, int position); }
    public void setOnHeaderClickListener(OnHeaderClickListener listener) { this.headerClickListener = listener; }
}