package edu.uark.ahnelson.roomwithaview2024.Util

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * A fragment that displays a time picker dialog to allow the user to select a time.
 * Once the time is selected, it returns the selected time via a callback function.
 *
 * @param calendar The Calendar object used to store the selected time.
 * @param timeSetCallback A lambda function that takes a Calendar object as input
 *                        and performs the required action when the time is selected.
 */
class TimePickerFragment(val calendar: Calendar, val timeSetCallback: (calendar: Calendar) -> Unit) :
    DialogFragment(), TimePickerDialog.OnTimeSetListener {

    /**
     * Called when the dialog is first created. It sets up the time picker dialog with
     * the current time as the default selection.
     *
     * @param savedInstanceState Bundle to save the state of the dialog (if necessary).
     * @return Dialog The TimePickerDialog instance created for selecting time.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY) // Current hour of the day
        val minute = c.get(Calendar.MINUTE) // Current minute

        // Create a new instance of TimePickerDialog with the current time and return it
        return TimePickerDialog(
            activity,  // The context in which the dialog is to be displayed
            this,      // The listener for when the time is set
            hour,      // The default hour
            minute,    // The default minute
            DateFormat.is24HourFormat(activity) // Determines whether to use 24-hour format
        )
    }

    /**
     * This method is called when the user selects a time from the picker.
     * It updates the provided Calendar object with the selected time and invokes the callback.
     *
     * @param view The time picker view.
     * @param hourOfDay The selected hour (in 24-hour format).
     * @param minute The selected minute.
     *
     * Output: Updates the Calendar object with the selected time and triggers the callback.
     */
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Update the Calendar object with the selected time
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay) // Set the selected hour
        calendar.set(Calendar.MINUTE, minute) // Set the selected minute

        // Invoke the callback function with the updated Calendar object
        timeSetCallback(calendar)
    }
}
