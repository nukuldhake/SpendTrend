package com.example.spend_trend.data
import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
@TypeConverters(com.example.spend_trend.data.Converters::class)  // we'll add this in next step if needed
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendtrend_database"
                )
                    .fallbackToDestructiveMigration() // only for dev - removes old data on version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}