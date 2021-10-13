package com.alpineQ.habitstracker

import android.content.Context
import androidx.room.Room
import com.alpineQ.habitstracker.database.HabitDatabase
import java.util.*

private const val DATABASE_NAME = "habit-database"

class HabitRepository private constructor(context: Context) {
    private val database : HabitDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            HabitDatabase::class.java,
            DATABASE_NAME
        ).build()
    private val habitDao = database.habitDao()
    fun getHabits(): List<Habit> =
        habitDao.getHabits()
    fun getHabit(id: UUID): Habit? =
        habitDao.getHabit(id)
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