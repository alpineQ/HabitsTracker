package com.alpineQ.habitstracker

import androidx.lifecycle.ViewModel

class HabitListViewModel : ViewModel() {
    private val habitRepository = HabitRepository.get()
    val habits = habitRepository.getHabits()
    val habitListLiveData = habitRepository.getHabits()
    fun addHabit(habit: Habit) {
        habitRepository.addHabit(habit)
    }
}