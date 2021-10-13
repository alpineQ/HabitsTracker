package com.alpineQ.habitstracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alpineQ.habitstracker.Habit

@Database(entities = [ Habit::class ], version=1)
@TypeConverters(HabitTypeConverters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
