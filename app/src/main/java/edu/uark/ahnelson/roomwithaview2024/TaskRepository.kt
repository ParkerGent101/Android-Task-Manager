package edu.uark.ahnelson.roomwithaview2024

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

// Repository for managing task data
class TaskRepository(private val taskDao: TaskDao, private val scope: CoroutineScope) {

    // Flow to observe all tasks
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks() // Assuming this method exists in your DAO

    // Method to insert a new task
    @WorkerThread
    fun insertTask(task: Task) {
        scope.launch {
            taskDao.insert(task) // Call the DAO's insert method
        }
    }

    // Method to update an existing task
    @WorkerThread
    fun updateTask(task: Task) {
        scope.launch {
            taskDao.update(task) // Call the DAO's update method
        }
    }

    // Method to delete a specific task
    @WorkerThread
    suspend fun deleteTask(task: Task) {
        Log.d("TaskRepository", "Attempting to delete task with ID: ${task.id}")
        scope.launch {
            try {
                taskDao.delete(task)
            } catch (e: Exception) {
                Log.e("TaskRepository", "Error deleting task with ID: ${task.id}, Error: ${e.message}", e)
            }
        }
    }

    // Method to delete all tasks
    @WorkerThread
    fun deleteAll() {
        scope.launch {
            taskDao.deleteAll() // Call the DAO's delete method
        }
    }

    // Add any additional repository methods here as needed
}
