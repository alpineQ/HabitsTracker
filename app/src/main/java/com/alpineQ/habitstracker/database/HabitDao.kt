package com.alpineQ.habitstracker.database

import androidx.room.Dao
import androidx.room.Query
import com.alpineQ.habitstracker.Habit
import java.util.UUID

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit")
    fun getHabits(): List<Habit>
    @Query("SELECT * FROM habit WHERE id=(:id)")
    fun getHabit(id: UUID): Habit?
}