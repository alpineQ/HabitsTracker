package com.alpineQ.habitstracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = HabitListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.fragment_container,
                    fragment
                )
                .commit()
        }
    }
}