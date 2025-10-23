package com.example.btl.ui.main;

import static com.example.btl.ui.main.ListItem.TYPE_HEADER;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl.R;
import com.example.btl.data.model.Task;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class GroupedTaskAdapter extends ListAdapter<ListItem, RecyclerView.ViewHolder> {

    private final Context context;
    private OnTaskToggleListener toggleListener;
    private OnHeaderClickListener headerClickListener;
    private OnTaskClickListener taskClickListener;

    public GroupedTaskAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    // DiffUtil (Không đổi)
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
                        oldTask.getDueDate() == newTask.getDueDate() &&
                        Objects.equals(oldTask.getNotes(), newTask.getNotes());
            } else if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
                return ((HeaderItem) oldItem).getTitle().equals(((HeaderItem) newItem).getTitle()) &&
                        ((HeaderItem) oldItem).isExpanded() == ((HeaderItem) newItem).isExpanded();
            }
            return false;
        }
    };

    // HeaderViewHolder (Không đổi)
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        // ... (Giữ nguyên)
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

    // TaskViewHolder (Cập nhật logic bind)
    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle; TextView subtaskInfo; ImageView priorityIcon;
        ImageView categoryIcon; TextView dueDate; CheckBox checkBoxCompleted;
        ImageView notesIcon;

        TaskViewHolder(View itemView) {
            super(itemView);
            // ... (findViewById không đổi)
            taskTitle = itemView.findViewById(R.id.text_view_task_title);
            subtaskInfo = itemView.findViewById(R.id.text_view_subtask_info);
            priorityIcon = itemView.findViewById(R.id.image_view_priority);
            categoryIcon = itemView.findViewById(R.id.image_view_category);
            dueDate = itemView.findViewById(R.id.text_view_item_due_date);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);
            notesIcon = itemView.findViewById(R.id.image_view_notes);

            // Listener Checkbox (Không đổi)
            checkBoxCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getBindingAdapterPosition();
                    if (toggleListener == null || position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    ListItem item = getItem(position);
                    if (item instanceof TaskItem) {
                        Task task = ((TaskItem) item).getTask();
                        if (task.isCompleted() != isChecked) {
                            toggleListener.onTaskToggled(task);
                        }
                    }
                }
            });

            // Listener Item Click (Không đổi)
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (taskClickListener != null && position != RecyclerView.NO_POSITION) {
                    ListItem item = getItem(position);
                    if (item instanceof TaskItem) {
                        taskClickListener.onTaskClick(((TaskItem) item).getTask());
                    }
                }
            });
        }

        void bind(TaskItem taskItem) {
            Task currentTask = taskItem.getTask();
            taskTitle.setText(currentTask.getTitle());

            // Logic dueDate (Không đổi)
            if (currentTask.getDueDate() > 0) {
                String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(currentTask.getDueDate()));
                dueDate.setText(formattedDate); dueDate.setVisibility(View.VISIBLE);
            } else { dueDate.setVisibility(View.GONE); }

            // Logic Checkbox (Không đổi)
            checkBoxCompleted.setChecked(currentTask.isCompleted());

            // Logic text 0/1 | 1/1 (Không đổi)
            if (currentTask.isCompleted()) {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                subtaskInfo.setText("1/1");
                itemView.setAlpha(0.5f);
            } else {
                taskTitle.setPaintFlags(taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                subtaskInfo.setText("0/1");
                itemView.setAlpha(1.0f);
            }

            // --- CẬP NHẬT: Luôn reset icon priority ---
            priorityIcon.setImageResource(R.drawable.ic_flag); // Luôn đặt lại icon gốc
            priorityIcon.clearColorFilter(); // Luôn xóa bộ lọc màu cũ

            switch (currentTask.getPriority()) {
                case 1:
                    priorityIcon.setVisibility(View.VISIBLE); // Đảm bảo hiện
                    priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_low));
                    break;
                case 2:
                    priorityIcon.setVisibility(View.VISIBLE); // Đảm bảo hiện
                    priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_medium));
                    break;
                case 3:
                    priorityIcon.setVisibility(View.VISIBLE); // Đảm bảo hiện
                    priorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_high));
                    break;
                default:
                    // Ngay cả khi không có priority, vẫn reset icon và ẩn đi
                    priorityIcon.setVisibility(View.INVISIBLE); // Ẩn đi
                    // priorityIcon.setImageResource(R.drawable.ic_flag); // (Không cần thiết nếu đã ẩn)
                    // priorityIcon.clearColorFilter(); // (Không cần thiết nếu đã ẩn)
                    break;
            }
            // --- KẾT THÚC CẬP NHẬT ---

            // Logic category (Không đổi)
            // Cân nhắc thêm clearColorFilter() và setImageResource(0) hoặc ẩn đi trong default nếu gặp lỗi tương tự
            if (currentTask.getCategory() != null && !currentTask.getCategory().isEmpty()) {
                categoryIcon.setVisibility(View.VISIBLE);
                switch (currentTask.getCategory()) {
                    case "Work": categoryIcon.setImageResource(R.drawable.ic_work); categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_work)); break;
                    case "Personal": categoryIcon.setImageResource(R.drawable.ic_personal); categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_personal)); break;
                    case "Wishlist": categoryIcon.setImageResource(R.drawable.ic_wishlist); categoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.category_wishlist)); break; // Sửa màu nếu cần
                    default: categoryIcon.setVisibility(View.INVISIBLE); break;
                }
            } else { categoryIcon.setVisibility(View.INVISIBLE); } // Đảm bảo ẩn nếu không có category

            // Logic notes icon (Không đổi)
            // Cân nhắc thêm setImageResource(0) hoặc ẩn đi trong else nếu gặp lỗi tương tự
            String notes = currentTask.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                notesIcon.setVisibility(View.VISIBLE);
                notesIcon.setImageResource(R.drawable.ic_menu); // Đảm bảo đúng icon
            } else {
                notesIcon.setVisibility(View.GONE); // Đảm bảo ẩn nếu không có notes
            }
        }
    }

    // --- Các hàm còn lại (Không đổi) ---
    @Override public int getItemViewType(int position) { return getItem(position).getItemType(); }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ... (Giữ nguyên)
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
        // ... (Giữ nguyên)
        ListItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) { ((HeaderViewHolder) holder).bind((HeaderItem) item); }
        else if (holder instanceof TaskViewHolder) { ((TaskViewHolder) holder).bind((TaskItem) item); }
    }

    public Task getTaskObjectAt(int position) {
        // ... (Giữ nguyên)
        ListItem item = getItem(position);
        if (item instanceof TaskItem) { return ((TaskItem) item).getTask(); }
        return null;
    }

    public interface OnTaskToggleListener { void onTaskToggled(Task task); }
    public void setOnTaskToggleListener(OnTaskToggleListener listener) { this.toggleListener = listener; }

    public interface OnHeaderClickListener { void onHeaderClick(HeaderItem headerItem, int position); }
    public void setOnHeaderClickListener(OnHeaderClickListener listener) { this.headerClickListener = listener; }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.taskClickListener = listener;
    }
}