package edu.uark.ahnelson.roomwithaview2024

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import edu.uark.ahnelson.roomwithaview2024.Util.NotificationUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/**
 * MainActivity manages the UI for displaying a list of tasks. It interacts with
 * the ViewModel and handles UI events such as clicking on a task, deleting tasks,
 * and toggling task completion.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var taskViewModel: TaskViewModel // ViewModel for managing task data
    private lateinit var taskAdapter: TaskAdapter // Adapter for displaying tasks in RecyclerView
    private lateinit var recyclerView: RecyclerView // RecyclerView to show the task list
    private val scope = CoroutineScope(Dispatchers.Main + Job()) // Coroutine scope for managing background operations

    private val REQUEST_CODE = 1 // Request code for task detail activity
    private val NOTIFICATION_REQUEST_CODE = 100 // Unique request code for notification permission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance() // Initialize Firebase Auth
        FirebaseFirestore.getInstance() // Initialize Firestore
        setContentView(R.layout.activity_main)

        enableEdgeToEdge() // Enables edge-to-edge display for devices with modern UI features

        // Initialize the ViewModel and repository
        val taskDao = TaskDatabase.getDatabase(applicationContext, scope).taskDao()
        val repository = TaskRepository(taskDao, scope)
        val viewModelFactory = TaskViewModelFactory(repository)
        taskViewModel = ViewModelProvider(this, viewModelFactory).get(TaskViewModel::class.java)

        // Create notification channel for sending notifications
        val notificationUtil = NotificationUtil()
        notificationUtil.createNotificationChannel(this)

        // Request notification permission if not already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_REQUEST_CODE)
        }

        // Apply window insets for immersive system UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up RecyclerView and adapter for task list
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create TaskAdapter with click, delete, and switch listeners
        taskAdapter = TaskAdapter(
            clickListener = { task -> onTaskClick(task) }, // Handles task clicks
            deleteListener = { task -> deleteTask(task) }, // Handles task deletion
            switchListener = { task, isChecked -> onSwitchToggle(task, isChecked) } // Handles task completion toggle
        )
        recyclerView.adapter = taskAdapter

        // Observe LiveData from the ViewModel for task updates
        taskViewModel.allTasks.observe(this, Observer { tasks ->
            tasks?.let { updateRecyclerView(it) }
        })

        // Floating Action Button to add new tasks
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this, NewTaskActivity::class.java)) // Opens activity to add a new task
        }
    }

    /**
     * Sends a notification for a specific task using NotificationUtil.
     */
    private fun sendNotification(task: Task) {
        val title = "Task Reminder: ${task.task}"
        val content = "Don't forget to complete: ${task.notes}"
        val clickIntent = Intent(this, MainActivity::class.java) // Intent to open MainActivity on click
        val notificationId = 1 // Unique notification ID

        val notificationUtil = NotificationUtil()
        notificationUtil.createClickableNotification(this, title, content, clickIntent, notificationId)
    }

    /**
     * Updates the RecyclerView with a list of tasks.
     */
    private fun updateRecyclerView(tasks: List<Task>) {
        taskAdapter.updateTasks(tasks) // Update adapter with new task list
    }

    /**
     * Handles task click events. Opens TaskDetailActivity to show task details.
     */
    private fun onTaskClick(task: Task) {
        val intent = Intent(this, TaskDetailActivity::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_name", task.task)
            putExtra("task_notes", task.notes)
            putExtra("task_due_date", task.dueDate)
            putExtra("task_completed", task.completed)
        }
        startActivityForResult(intent, REQUEST_CODE) // Start TaskDetailActivity and expect result
    }

    /**
     * Deletes a task using the ViewModel.
     */
    private fun deleteTask(task: Task) {
        taskViewModel.deleteTask(task) // Delete task from ViewModel
    }

    /**
     * Toggles the completion status of a task and updates it in the database.
     */
    private fun onSwitchToggle(task: Task, isChecked: Boolean) {
        val updatedTask = task.copy(completed = isChecked) // Update completion status
        taskViewModel.updateTask(updatedTask) // Update task in ViewModel
    }

    /**
     * Handles result from TaskDetailActivity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedTaskId = data?.getLongExtra("task_id", -1L) ?: -1L
            val updatedTaskName = data?.getStringExtra("task_name") ?: ""
            val updatedTaskNotes = data?.getStringExtra("task_notes") ?: ""
            val updatedDueDateMillis = data?.getLongExtra("task_due_date", -1L) ?: -1L
            val updatedTaskCompleted = data?.getBooleanExtra("task_completed", false) ?: false

            val taskIndex = taskAdapter.currentList.indexOfFirst { it.id == updatedTaskId }
            if (taskIndex != -1) {
                val updatedTask = Task(updatedTaskId, updatedTaskName, updatedTaskNotes, updatedDueDateMillis, updatedTaskCompleted)
                taskAdapter.updateTaskAt(taskIndex, updatedTask) // Update task in adapter
            }
        }
    }

    // Clean up the coroutine scope when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Cancel coroutines to avoid memory leaks
    }

    /**
     * Handles permission results for notifications.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    taskViewModel.allTasks.observe(this) { tasks ->
                        tasks.firstOrNull()?.let { firstTask ->
                            sendNotification(firstTask)
                        }
                    }
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
