package edu.uark.ahnelson.roomwithaview2024

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private var tasks: List<Task> = listOf(),
    private val clickListener: (Task) -> Unit,
    private val deleteListener: (Task) -> Unit,
    private val switchListener: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize your views here with the correct IDs
        val taskName: TextView = itemView.findViewById(R.id.textView) // Updated ID
        val taskNotes: TextView = itemView.findViewById(R.id.noteTextView) // Updated ID
        val taskDueDate: TextView = itemView.findViewById(R.id.dueDateTextView) // Updated ID
        val taskSwitch: Switch = itemView.findViewById(R.id.switchComplete) // Updated ID
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton) // Updated ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Bind your views here
        holder.taskName.text = task.task // Assuming task.task contains the task name
        holder.taskNotes.text = task.notes // Assuming task.notes contains the task notes
        task.dueDate?.let {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(it))
            holder.taskDueDate.text = "Due Date: $formattedDate" // Format the due date as needed
        } ?: run {
            holder.taskDueDate.text = "Due Date: Not set" // Handle case where due date is null
        }

        holder.taskSwitch.isChecked = task.completed // Bind the switch to the completion status

        // Set background color based on completion status
        if (task.completed) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)) // Light gray
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black)) // Black
        }

        holder.itemView.setOnClickListener { clickListener(task) }

        holder.deleteButton.setOnClickListener { deleteListener(task) } // Setup delete button listener

        holder.taskSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Call switch listener with updated status
            switchListener(task, isChecked)

            // Update background color when switch is toggled
            if (isChecked) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)) // Light gray
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black)) // Black
            }
        }
    }



    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    fun updateTaskAt(position: Int, updatedTask: Task) {
        tasks = tasks.toMutableList().apply {
            this[position] = updatedTask
        }
        notifyItemChanged(position)
    }

    val currentList: List<Task>
        get() = tasks
}
