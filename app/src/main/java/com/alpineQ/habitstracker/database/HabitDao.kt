package com.alpineQ.habitstracker.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alpineQ.habitstracker.Habit
import java.util.*

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit")
    fun getHabits(): LiveData<List<Habit>>
    @Query("SELECT * FROM habit WHERE id=(:id)")
    fun getHabit(id: UUID): LiveData<Habit?>
    @Update
    fun updateHabit(habit: Habit)
    @Insert
    fun addHabit(habit: Habit)
}