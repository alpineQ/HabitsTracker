package com.alpineQ.habitstracker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.alpineQ.habitstracker.database.HabitDatabase
import com.alpineQ.habitstracker.database.migration_1_2
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "habit-database"

class HabitRepository private constructor(context: Context) {
    private val database : HabitDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            HabitDatabase::class.java,
            DATABASE_NAME
        ).addMigrations(migration_1_2)
            .build()
    private val habitDao = database.habitDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir
    fun getHabits(): LiveData<List<Habit>> = habitDao.getHabits()
    fun getHabit(id: UUID): LiveData<Habit?> = habitDao.getHabit(id)
    fun updateHabit(habit: Habit) {
        executor.execute {
            habitDao.updateHabit(habit)
        }
    }
    fun addHabit(habit: Habit) {
        executor.execute {
            habitDao.addHabit(habit)
        }
    }
    fun getPhotoFile(habit: Habit): File = File(filesDir, habit.photoFileName)
    companion object {
        private var INSTANCE: HabitRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = HabitRepository(context)
            }
        }
        fun get(): HabitRepository {
            return INSTANCE ?: throw IllegalStateException("HabitRepository must be initialized")
        }
    }
}