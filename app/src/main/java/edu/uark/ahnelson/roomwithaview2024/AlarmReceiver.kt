package edu.uark.ahnelson.roomwithaview2024

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import edu.uark.ahnelson.roomwithaview2024.Util.NotificationUtil

/**
 * AlarmReceiver is a BroadcastReceiver that triggers when the alarm goes off.
 * It receives the broadcast and creates a notification to remind the user about a task.
 */
class AlarmReceiver : BroadcastReceiver() {

    /**
     * This method is called when the alarm is received (triggered). It retrieves the
     * notification ID and then generates a notification to remind the user about the task.
     *
     * @param context The application context.
     * @param intent The intent passed by the AlarmManager when the alarm goes off.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve the notification ID from the intent's extras
        val notificationId = intent.getIntExtra(context.getString(R.string.EXTRA_ID), -1)

        // If the notification ID is valid, proceed to create the notification
        if (notificationId != -1) {
            // Set the notification's title and content
            val title = "Task Reminder" // Title of the notification
            val content = "It's time to complete your task!" // Message displayed in the notification

            // Create an intent to open TaskDetailActivity when the notification is clicked
            val clickIntent = Intent(context, TaskDetailActivity::class.java)

            // Create and show the notification using the NotificationUtil
            NotificationUtil().createClickableNotification(context, title, content, clickIntent, notificationId)
        }
    }
}
