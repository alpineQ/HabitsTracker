package com.alpineQ.habitstracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class HabitDetailViewModel : ViewModel() {
    private val habitRepository = HabitRepository.get()
    private val habitIdLiveData = MutableLiveData<UUID>()
    var habitLiveData: LiveData<Habit?> =
        Transformations.switchMap(habitIdLiveData) { habitId ->
            habitRepository.getHabit(habitId)
        }
    fun loadHabit(habitId: UUID) {
        habitIdLiveData.value = habitId
    }
    fun saveHabit(habit: Habit) {
        habitRepository.updateHabit(habit)
    }
    fun getPhotoFile(habit: Habit): File {
        return habitRepository.getPhotoFile(habit)
    }
}
