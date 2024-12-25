// Package declaration to define the namespace of the application
package edu.uark.ahnelson.roomwithaview2024

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Factory class for creating instances of NewTaskViewModel
class NewTaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    // Method for creating a ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the ViewModel class is assignable to NewTaskViewModel
        if (modelClass.isAssignableFrom(NewTaskViewModel::class.java)) {
            // Suppress unchecked cast warning since we are sure of the type
            @Suppress("UNCHECKED_CAST")
            return NewTaskViewModel(repository) as T // Create and return a NewTaskViewModel instance
        }
        // If the model class is not recognized, throw an exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// NewTaskViewModel class that extends ViewModel
class NewTaskViewModel(private val repository: TaskRepository) : ViewModel() {

    fun insert(task: Task) = viewModelScope.launch {
        // Call the repository's insert method to save the task to the database
        repository.insertTask(task)
    }
}
