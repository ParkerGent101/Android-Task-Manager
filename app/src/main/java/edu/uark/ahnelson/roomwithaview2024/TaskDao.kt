// Package declaration to define the namespace of the application
package edu.uark.ahnelson.roomwithaview2024

// Import statements for Room annotations and coroutine libraries
import androidx.room.Dao // Annotation to define a Data Access Object (DAO)
import androidx.room.Insert // Annotation to define insert operations
import androidx.room.OnConflictStrategy // For defining conflict resolution strategies during inserts
import androidx.room.Query // Annotation to define query operations
import androidx.room.Update // Annotation to define update operations
import androidx.room.Delete // Annotation to define delete operations
import kotlinx.coroutines.flow.Flow // Importing Flow for asynchronous data streams

// DAO interface for accessing Task data in the Room database
@Dao // Marks the interface as a DAO for Room
interface TaskDao {

    // Query to retrieve all tasks from the task_table, ordered by due date (or any other column if needed)
    @Query("SELECT * FROM task_table ORDER BY due_date ASC") // Change "due_date" to your column name
    fun getAllTasks(): Flow<List<Task>> // Updated method name to match the repository

    // Insert operation for adding a new task to the database
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Defines how to handle conflicts (ignore if a conflict occurs)
    suspend fun insert(task: Task) // A suspend function to perform the insert operation asynchronously

    // Update operation for updating an existing task in the database
    @Update // Automatically generates the necessary SQL to update a task
    suspend fun update(task: Task) // A suspend function to perform the update operation asynchronously

    // Delete operation for removing a specific task from the database
    @Delete // Automatically generates the necessary SQL to delete a task
    suspend fun delete(task: Task) // A suspend function to perform the delete operation asynchronously

    // Query to delete all tasks from the task_table
    @Query("DELETE FROM task_table")
    suspend fun deleteAll() // A suspend function to perform the delete operation asynchronously
}
