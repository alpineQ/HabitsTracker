package com.alpineQ.habitstracker

import android.app.Application

class HabitApplication : Application()
{
    override fun onCreate() {
        super.onCreate()
        HabitRepository.initialize(this)
    }
}