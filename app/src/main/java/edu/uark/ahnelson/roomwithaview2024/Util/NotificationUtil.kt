package edu.uark.ahnelson.roomwithaview2024.Util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.uark.ahnelson.roomwithaview2024.AlarmReceiver
import edu.uark.ahnelson.roomwithaview2024.R

class NotificationUtil {
    companion object {
        const val CHANNEL_ID = "TaskListNotificationChannel"
    }

    /**
     * Creates a notification channel for Android 8.0 (API 26) and above.
     * This channel is required for notifications to be displayed.
     *
     * @param context The context in which the channel is created, usually the application or activity context.
     */
    fun createNotificationChannel(context: Context) {
        // Check if the Android version supports notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name) // Name of the notification channel
            val descriptionText = context.getString(R.string.channel_description) // Description of the channel
            val importance = NotificationManager.IMPORTANCE_DEFAULT // Importance level of the notifications
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Get the NotificationManager service to create the channel
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates and shows a notification that the user can click to trigger an action.
     *
     * @param context The context in which the notification is created.
     * @param title The title text for the notification.
     * @param content The content text (or body) of the notification.
     * @param clickIntent The intent that will be triggered when the notification is clicked.
     * @param id A unique ID for the notification, used to identify or update the notification.
     *
     * Output: Displays a clickable notification to the user.
     */
    fun createClickableNotification(context: Context, title: String, content: String, clickIntent: Intent, id: Int) {
        // Build a TaskStackBuilder to add the click intent and ensure proper navigation backstack
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(clickIntent)

        // Get a PendingIntent that triggers the provided clickIntent
        val pendingClickIntent = stackBuilder.getPendingIntent(id, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Icon displayed in the notification
            .setContentTitle(title) // Title of the notification
            .setContentText(content) // Body text of the notification
            .setPriority(NotificationCompat.PRIORITY_MAX) // Sets priority for heads-up notifications
            .setContentIntent(pendingClickIntent) // Intent triggered when notification is clicked
            .setAutoCancel(true) // Auto-dismiss the notification after it is clicked

        // Check for notification permissions before showing the notification
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(context)) {
                notify(id, builder.build()) // Show the notification
            }
        }
    }

    /**
     * Schedules a notification for a specific time in the future using AlarmManager.
     *
     * @param context The context in which the notification is scheduled.
     * @param notificationTimeMillis The time in milliseconds when the notification should be triggered.
     * @param notificationId A unique ID for the notification.
     *
     * Output: Schedules a notification at the specified future time.
     */
    fun scheduleNotification(context: Context, notificationTimeMillis: Long, notificationId: Int) {
        // Ensure the notification time is in the future
        if (notificationTimeMillis > System.currentTimeMillis()) {
            // Check if exact alarms are allowed for Android 12+ (API level 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!canScheduleExactAlarms(context)) {
                    // Open settings to request permission for scheduling exact alarms
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                    return // Exit if permission is not granted
                }
            }

            // Set up the alarm manager to trigger a broadcast for the notification
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(context.getString(R.string.EXTRA_ID), notificationId) // Pass the notification ID in the intent
            }
            val pendingAlarmIntent = PendingIntent.getBroadcast(
                context, notificationId, alarmIntent, PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm to trigger at the specified time
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTimeMillis, pendingAlarmIntent)
            } catch (e: SecurityException) {
                // Handle the case where exact alarm permissions are not granted
                Toast.makeText(context, "Permission to schedule exact alarms is not granted.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Show a message if the selected time is not in the future
            Toast.makeText(context, "Please select a future date and time", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Helper function to check if the app has permission to schedule exact alarms.
     *
     * @param context The context in which to check the permission.
     * @return Boolean True if the app can schedule exact alarms, false otherwise.
     */
    private fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms() // Check if exact alarms are allowed
        } else {
            true // Exact alarms are not restricted in versions below Android 12 (API 31)
        }
    }
}
