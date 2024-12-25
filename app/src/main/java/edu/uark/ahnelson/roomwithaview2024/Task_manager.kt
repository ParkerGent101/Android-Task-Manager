package edu.uark.ahnelson.roomwithaview2024

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

// Custom application class that extends Android's Application class
class Task_manager : Application() {

    // CoroutineScope for launching coroutines in this application
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy to initialize the database only when it's first accessed
    private val database by lazy {
        TaskDatabase.getDatabase(this, applicationScope)
    }

    // Using by lazy to initialize the repository only when it's first accessed
    val repository by lazy {
        TaskRepository(database.taskDao(), applicationScope) // Pass the DAO and scope to the repository
    }
}
