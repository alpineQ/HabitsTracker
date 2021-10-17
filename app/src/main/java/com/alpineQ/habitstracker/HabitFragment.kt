package com.alpineQ.habitstracker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import java.util.*

private const val TAG = "HabitFragment"
private const val ARG_HABIT_ID = "habit_id"
private const val REQUEST_DATE = "DialogDate"

class HabitFragment : Fragment(), FragmentResultListener, DatePickerFragment.Callbacks {
    private lateinit var habit: Habit
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var dailyDoneCheckBox: CheckBox
    private val habitDetailViewModel: HabitDetailViewModel by lazy {
        ViewModelProvider(this).get(HabitDetailViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habit = Habit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_habit, container, false)
        titleField = view.findViewById(R.id.habit_title) as EditText
        dateButton = view.findViewById(R.id.habit_date) as Button
        dailyDoneCheckBox = view.findViewById(R.id.habit_daily_done) as CheckBox
        childFragmentManager.setFragmentResultListener(REQUEST_DATE, viewLifecycleOwner, this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        habitDetailViewModel.habitLiveData.observe(
            viewLifecycleOwner,
            { habit ->
                habit?.let {
                    this.habit = habit
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Это пространство оставлено пустым специально
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                habit.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                    // И это
            }
        }
        titleField.addTextChangedListener(titleWatcher)
        dailyDoneCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                habit.dailyDone = isChecked
            }
        }
        dateButton.setOnClickListener {
            DatePickerFragment
                .newInstance(habit.date, REQUEST_DATE)
                .show(childFragmentManager, REQUEST_DATE)
        }

    }

    override fun onFragmentResult(requestCode: String, result: Bundle) {
        when(requestCode) {
            REQUEST_DATE -> {
                Log.d(TAG, "received result for $requestCode")
                habit.date = DatePickerFragment.getSelectedDate(result)
                updateUI()
            }
        }

    }


    private fun updateUI() {
        titleField.setText(habit.title)
        dateButton.text = habit.date.toString()
        dailyDoneCheckBox.apply {
            isChecked = habit.dailyDone
            jumpDrawablesToCurrentState()
        }
    }

    companion object {
        fun newInstance(habitId: UUID): HabitFragment {
            val args = Bundle().apply {
                putSerializable(ARG_HABIT_ID, habitId)
            }
            return HabitFragment().apply {
                arguments = args
            }
        }
    }

    override fun onStop() {
        super.onStop()
        habitDetailViewModel.saveHabit(habit)
    }

    override fun onDateSelected(date: Date) {
        habit.date = date
        updateUI()
    }
}