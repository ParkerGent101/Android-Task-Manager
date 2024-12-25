package edu.uark.ahnelson.roomwithaview2024

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [Task::class], version = 2, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database" // Changed name for clarity
                ).addCallback(TaskDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class TaskDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.taskDao())
                }
            }
        }

        suspend fun populateDatabase(taskDao: TaskDao) {
            taskDao.deleteAll() // Clear the database

            // Get the current date and add days for due dates
            val calendar = Calendar.getInstance()

            // Example tasks for initial population with realistic due dates
            taskDao.insert(Task(task = "Clean House", notes = "These are notes", dueDate = calendar.apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis)) // Due in 7 days
            taskDao.insert(Task(task = "Take out Trash", notes = "Notes again", dueDate = calendar.apply { add(Calendar.DAY_OF_YEAR, 14) }.timeInMillis)) // Due in 14 days
        }

    }
}
