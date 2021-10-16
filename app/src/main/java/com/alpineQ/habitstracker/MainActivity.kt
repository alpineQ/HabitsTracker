package com.alpineQ.habitstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), HabitListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = HabitListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }
    override fun onHabitSelected(habitID: UUID) {
        val fragment = HabitFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}