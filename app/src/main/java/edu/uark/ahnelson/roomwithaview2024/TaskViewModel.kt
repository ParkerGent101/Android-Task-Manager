// Package declaration to define the namespace of the application
package edu.uark.ahnelson.roomwithaview2024

// Import statements for necessary Android and lifecycle classes
import android.util.Log
import androidx.lifecycle.LiveData // Class representing observable data holder
import androidx.lifecycle.ViewModel // Base class for ViewModels
import androidx.lifecycle.ViewModelProvider // Class for creating ViewModels
import androidx.lifecycle.asLiveData // Extension function to convert Flow to LiveData
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope


// ViewModel class for managing UI-related data related to tasks
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // Backing property for all tasks as LiveData
    private val _allTasks: LiveData<List<Task>> = repository.allTasks.asLiveData()

    // Publicly exposed LiveData for observing changes
    val allTasks: LiveData<List<Task>> get() = _allTasks

    // Method to update an existing task
    fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            try {
                repository.updateTask(updatedTask)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error updating task: ${e.message}", e)
            }
        }
    }

    // Method for inserting a new task
    fun insertTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.insertTask(task)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error adding task: ${e.message}", e)
            }
        }
    }

    // Method for deleting a specific task
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error deleting task: ${e.message}", e)
            }
        }
    }

    // Method to delete all tasks
    fun deleteAllTasks() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error deleting all tasks: ${e.message}", e)
            }
        }
    }

    // Method to refresh tasks (optional, since LiveData will auto-refresh)
    fun refreshTasks() {
        // No specific logic is needed unless you want to trigger something manually.
        // The LiveData will update automatically when the data changes in the repository.
    }
}

// Factory class for creating instances of TaskViewModel
class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    // Create method for ViewModel creation
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the modelClass is assignable to TaskViewModel
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") // Suppress unchecked cast warning
            return TaskViewModel(repository) as T // Create and return TaskViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class") // Throw exception if unknown class
    }
}
