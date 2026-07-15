package com.uce.floracare.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uce.floracare.data.local.dao.PlantDao
import com.uce.floracare.data.local.dao.TaskDao
import com.uce.floracare.data.local.entity.PlantEntity
import com.uce.floracare.data.local.entity.TaskEntity

@Database(entities = [PlantEntity::class, TaskEntity::class], version = 7, exportSchema = false)
abstract class FloraCareDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: FloraCareDatabase? = null

        fun getDatabase(context: Context): FloraCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FloraCareDatabase::class.java,
                    "floracare_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
