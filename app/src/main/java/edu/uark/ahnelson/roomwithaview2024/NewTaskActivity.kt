package edu.uark.ahnelson.roomwithaview2024

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.uark.ahnelson.roomwithaview2024.Util.DatePickerFragment
import edu.uark.ahnelson.roomwithaview2024.Util.NotificationUtil
import java.util.Calendar
import androidx.activity.enableEdgeToEdge
import edu.uark.ahnelson.roomwithaview2024.Util.TimePickerFragment
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * NewTaskActivity handles the creation of new tasks, including setting
 * notifications for task due dates. It interacts with the ViewModel to
 * save tasks and manage notifications.
 */
class NewTaskActivity : AppCompatActivity() {

    private lateinit var editWordView: EditText // Input field for task name
    private lateinit var editNotesView: EditText // Input field for task notes
    private lateinit var etDateTime: Button // Button for selecting due date and time
    private var notificationTimeMillis: Long = 0 // Milliseconds for the scheduled notification
    private var notificationId: Int = 0 // Unique ID for notifications
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String> // Launcher for notification permission

    // Custom date formatter for mm/dd/yyyy format with 12-hour time and AM/PM
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())

    // ViewModel instance for managing task data
    val newTaskViewModel: NewTaskViewModel by viewModels {
        NewTaskViewModelFactory((application as Task_manager).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for immersive UI
        setContentView(R.layout.activity_new_task) // Set content view for this activity

        // Initialize views
        etDateTime = findViewById(R.id.etDateTime)
        editWordView = findViewById(R.id.edit_word)
        editNotesView = findViewById(R.id.edit_notes) // Initialize task notes view
        val calendar: Calendar = Calendar.getInstance() // Get current date and time
        val id = intent.getIntExtra(getString(R.string.EXTRA_ID), 0) // Retrieve notification ID from intent

        // Apply window insets for immersive system UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Display a toast message if a notification ID is provided
        if (id != 0) {
            Toast.makeText(this, "Notification $id clicked", Toast.LENGTH_SHORT).show()
        }

        // Set the current date and time on the button using SimpleDateFormat
        etDateTime.text = dateFormat.format(calendar.time)

        // Set click listener for date and time selection
        etDateTime.setOnClickListener {
            dateClicked(it)
        }

        // Set click listener for the schedule button
        findViewById<Button>(R.id.btnSchedule).setOnClickListener {
            if (checkNotificationPrivilege()) {
                scheduleNotification() // Schedule notification if permission is granted
            }
        }

        // Set click listener for the save button
        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val replyIntent = Intent() // Intent to hold the result
            if (TextUtils.isEmpty(editWordView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent) // No task name, return canceled result
            } else {
                // Get task name and notes from input fields
                val word = editWordView.text.toString()
                val notes = editNotesView.text.toString() // Get the task notes
                // Insert the task with the due date into the ViewModel
                newTaskViewModel.insert(Task(null, word, notes, notificationTimeMillis)) // Pass dueDate
                setResult(Activity.RESULT_OK) // Return OK result
            }
            finish() // Close the activity
        }

        // Initialize the permission launcher for notification permission
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                NotificationUtil().createNotificationChannel(this) // Create notification channel
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY" // Constant for reply intent extra
    }

    /**
     * Checks if the app has permission to send notifications.
     * If permission is not granted, it requests the permission.
     */
    private fun checkNotificationPrivilege(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationUtil().createNotificationChannel(this) // Create notification channel if permission granted
            true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) // Request permission
            }
            false // Permission not granted
        }
    }

    /**
     * Schedules a notification for the selected date and time.
     * It sets the notification only if the time is in the future.
     */
    private fun scheduleNotification() {
        if (notificationTimeMillis > Calendar.getInstance().timeInMillis) {
            Log.d("+new_task_activity", "Scheduling Notification") // Log notification scheduling
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager // Get AlarmManager service
            val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra(getString(R.string.EXTRA_ID), notificationId) // Pass the notification ID to the AlarmReceiver
            }
            val pendingAlarmIntent = PendingIntent.getBroadcast(
                this, notificationId, alarmIntent, PendingIntent.FLAG_IMMUTABLE // Create PendingIntent for alarm
            )
            alarmManager?.setWindow(AlarmManager.RTC_WAKEUP, notificationTimeMillis, 1000 * 10, pendingAlarmIntent) // Schedule the alarm
            notificationId++ // Increment notification ID for future notifications
        } else {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show() // Notify user to select future time
        }
    }

    /**
     * Displays the TimePickerFragment to set the time for the notification.
     * @param calendar The calendar object representing the selected date.
     */
    private fun dateSet(calendar: Calendar) {
        TimePickerFragment(calendar, this::timeSet).show(supportFragmentManager, "timePicker") // Show time picker
    }

    /**
     * Updates the selected time and formats it for display.
     * @param calendar The calendar object representing the selected time.
     */
    private fun timeSet(calendar: Calendar) {
        etDateTime.text = dateFormat.format(calendar.time) // Update the button text with formatted date
        notificationTimeMillis = calendar.timeInMillis // Store the notification time in milliseconds
    }

    /**
     * Displays the DatePickerFragment to set the date for the notification.
     * @param view The view that was clicked to initiate date selection.
     */
    private fun dateClicked(view: View) {
        Log.d("+new_task_activity", "Date Clicked: ${view.id}") // Log date click event
        DatePickerFragment(this::dateSet).show(supportFragmentManager, "datePicker") // Show date picker
    }
}
