package com.alpineQ.habitstracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import java.util.Date

@Entity
data class Habit(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var dailyDone: Boolean = false,
                 var partner: String = "") {
    val photoFileName
        get() = "IMG_$id.jpg"
}