package edu.uark.ahnelson.roomwithaview2024

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import edu.uark.ahnelson.roomwithaview2024.Util.DatePickerFragment
import edu.uark.ahnelson.roomwithaview2024.Util.NotificationUtil
import edu.uark.ahnelson.roomwithaview2024.Util.TimePickerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {
    // Define a CoroutineScope
    private val activityScope = CoroutineScope(SupervisorJob())

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskTitle: EditText
    private lateinit var taskNotes: EditText
    private lateinit var taskDueDate: EditText
    private lateinit var saveButton: Button
    private lateinit var setNotificationTimeButton: Button
    private lateinit var shareButton: Button  // Add share button to UI
    private var taskId: Long = -1L
    private lateinit var taskCompleted: CheckBox
    private var notificationTimeMillis: Long = 0 // Store the notification time
    private var notificationId: Int = 0 // Initialize notificationId here
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String> // Declare the permission launcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        // Initialize UI components
        taskTitle = findViewById(R.id.taskTitle)
        taskNotes = findViewById(R.id.taskNotes)
        taskDueDate = findViewById(R.id.taskDueDate)
        saveButton = findViewById(R.id.saveButton)
        setNotificationTimeButton = findViewById(R.id.btnSetNotificationTime)
        taskCompleted = findViewById(R.id.taskCompleted)
        val backButton = findViewById<Button>(R.id.btn_back)
        shareButton = findViewById(R.id.btn_share) // Reference the new share button

        // Initialize TaskRepository and TaskViewModel
        val taskDao = TaskDatabase.getDatabase(applicationContext, activityScope).taskDao()
        taskRepository = TaskRepository(taskDao, lifecycleScope)
        val factory = TaskViewModelFactory(taskRepository)
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)

        // Get the data from the Intent
        retrieveTaskDetailsFromIntent()

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                scheduleNotification()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Set onClick listeners
        saveButton.setOnClickListener { saveTaskChanges() }
        backButton.setOnClickListener { finish() }
        setNotificationTimeButton.setOnClickListener { dateClicked(it) }
        shareButton.setOnClickListener { shareTaskDetails() }  // Set share button listener
    }

    private fun retrieveTaskDetailsFromIntent() {
        taskId = intent.getLongExtra("task_id", -1L)
        val taskName = intent.getStringExtra("task_name")
        val taskNotesString = intent.getStringExtra("task_notes")
        val taskDueDateMillis = intent.getLongExtra("task_due_date", -1L)
        val taskCompletedStatus = intent.getBooleanExtra("task_completed", false)

        // Set task details into the UI elements
        taskTitle.setText(taskName ?: "")
        taskNotes.setText(taskNotesString ?: "")
        taskCompleted.isChecked = taskCompletedStatus

        // Display the due date if it's available
        val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()) // Include time format
        if (taskDueDateMillis != -1L) {
            taskDueDate.setText(dateFormat.format(Date(taskDueDateMillis)))
            notificationTimeMillis = taskDueDateMillis // Set initial notification time
        }

        // Generate a unique notification ID based on the taskId
        notificationId = taskId.hashCode()
    }

    private fun saveTaskChanges() {
        // Retrieve the updated details from the EditText fields
        val updatedTitle = taskTitle.text.toString()
        val updatedNotes = taskNotes.text.toString()
        val isCompleted = taskCompleted.isChecked

        // Parse the due date with time included
        val updatedDueDateMillis = parseDueDate(taskDueDate.text.toString())

        if (updatedDueDateMillis == -1L) {
            Toast.makeText(this, "Invalid due date. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Insert or update the task in the database
        if (taskId != -1L) {
            val updatedTask = Task(taskId, updatedTitle, updatedNotes, updatedDueDateMillis, isCompleted)
            taskViewModel.updateTask(updatedTask)
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()

            // Create an Intent to send back the updated task details
            val resultIntent = Intent().apply {
                putExtra("task_id", taskId)
                putExtra("task_name", updatedTitle)
                putExtra("task_notes", updatedNotes)
                putExtra("task_due_date", updatedDueDateMillis)
                putExtra("task_completed", isCompleted)
            }
            setResult(RESULT_OK, resultIntent) // Set result with updated data
        } else {
            val newTask = Task(null, updatedTitle, updatedNotes, updatedDueDateMillis, isCompleted)
            taskViewModel.insertTask(newTask)
            Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show()
        }

        // Check and schedule notification if a valid date is provided
        checkAndScheduleNotification()

        finish() // Close the activity
    }

    private fun checkAndScheduleNotification() {
        if (notificationTimeMillis > Calendar.getInstance().timeInMillis) {
            Log.d("+task_detail_activity", "Checking Notification Permission")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    scheduleNotification()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // Schedule notification for devices below API level 33 (Tiramisu)
                scheduleNotification()
            }
        } else {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNotification() {
        Log.d("+task_detail_activity", "Scheduling Notification")
        NotificationUtil().scheduleNotification(this, notificationTimeMillis, notificationId)
    }

    private fun dateClicked(view: View) {
        DatePickerFragment(this::dateSet).show(supportFragmentManager, "datePicker") // Show the date picker
    }

    private fun dateSet(calendar: Calendar) {
        TimePickerFragment(calendar, this::timeSet).show(supportFragmentManager, "timePicker") // Show the time picker
    }

    private fun timeSet(calendar: Calendar) {
        notificationTimeMillis = calendar.timeInMillis // Update notification time
        val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()) // Format to include time
        taskDueDate.setText(dateFormat.format(calendar.time)) // Display selected date and time
    }

    private fun shareTaskDetails() {
        val taskName = taskTitle.text.toString()
        val taskNotes = taskNotes.text.toString()

        // Construct the message
        val message = "Task: $taskName\nNotes: $taskNotes"

        // Create an Intent chooser with options for email or SMS
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Task Details")
            putExtra(Intent.EXTRA_TEXT, message)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Task via"))
    }

    private fun parseDueDate(dueDateString: String): Long {
        return try {
            // Define the expected format for the due date
            val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())

            // Parse the string into a Date object, and return its time in milliseconds
            dateFormat.parse(dueDateString)?.time ?: -1L
        } catch (e: Exception) {
            Log.e("TaskDetailActivity", "Error parsing date: $e")
            -1L
        }
    }

}
