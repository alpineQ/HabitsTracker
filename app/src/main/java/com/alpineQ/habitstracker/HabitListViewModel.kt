package com.alpineQ.habitstracker

import androidx.lifecycle.ViewModel

class HabitListViewModel : ViewModel() {
    // private val habitRepository = HabitRepository.get()
    // val habits = habitRepository.getHabits()
    val habits = mutableListOf<Habit>()
    init {
        for (i in 0 until 100) {
            val habit = Habit()
            habit.title = "Habit #$i"
            habit.dailyDone = i % 5 == 0
            habits += habit
        }
    }
}