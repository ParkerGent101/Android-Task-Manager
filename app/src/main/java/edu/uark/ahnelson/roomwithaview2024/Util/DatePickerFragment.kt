package edu.uark.ahnelson.roomwithaview2024.Util

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * A fragment that shows a date picker dialog to allow the user to select a date.
 * Once the date is selected, it returns the selected date via a callback function.
 *
 * @param dateSetCallback A lambda function that takes a Calendar object
 *                        and performs the required action when a date is selected.
 */
class DatePickerFragment(val dateSetCallback: (calendar: Calendar) -> Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    /**
     * Called when the dialog is first created. This sets up the date picker dialog with
     * the current date as the default selection.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the current date (today) to set as the default date in the picker
        val currentCalendar = Calendar.getInstance()
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)
        val day = currentCalendar.get(Calendar.DAY_OF_MONTH)

        // Create a new DatePickerDialog instance and return it
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    /**
     * This method is called when the user selects a date from the picker.
     * It updates the Calendar object with the selected date and invokes the callback.
     *
     * @param view The date picker view
     * @param year The selected year
     * @param month The selected month (0-based index)
     * @param day The selected day of the month
     */
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        // Create a Calendar object and set it to the selected date
        val selectedDate = Calendar.getInstance()
        selectedDate.set(year, month, day)

        // Invoke the callback function, passing the selected date
        dateSetCallback(selectedDate)
    }
}
