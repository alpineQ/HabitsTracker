package com.alpineQ.habitstracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import java.util.*

private const val TAG = "HabitFragment"
private const val ARG_HABIT_ID = "habit_id"
private const val REQUEST_DATE = "DialogDate"
private const val REQUEST_CONTACT = 0
private const val DATE_FORMAT = "dd MMMM, yyyy"


class HabitFragment : Fragment(), FragmentResultListener, DatePickerFragment.Callbacks {
    private lateinit var habit: Habit
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var dailyDoneCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var partnerButton: Button
    private val habitDetailViewModel: HabitDetailViewModel by lazy {
        ViewModelProvider(this).get(HabitDetailViewModel::class.java)
    }
    private lateinit var pickContactContract: ActivityResultContract<Uri, Uri?>
    private lateinit var pickContactCallback: ActivityResultCallback<Uri?>
    private lateinit var pickContactLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habit = Habit()
        val habitId: UUID = arguments?.getSerializable(ARG_HABIT_ID) as UUID
        habitDetailViewModel.loadHabit(habitId)
        pickContactContract = object : ActivityResultContract<Uri, Uri?>() {
            override fun createIntent(context: Context, input: Uri): Intent {
                Log.d(TAG, "createIntent() called")
                return Intent(Intent.ACTION_PICK, input)
            }
            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                Log.d(TAG, "parseResult() called")
                if(resultCode != Activity.RESULT_OK || intent == null)
                    return null
                return intent.data
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_habit, container, false)
        titleField = view.findViewById(R.id.habit_title) as EditText
        dateButton = view.findViewById(R.id.habit_date) as Button
        dailyDoneCheckBox = view.findViewById(R.id.habit_daily_done) as CheckBox
        reportButton = view.findViewById(R.id.share_progress_button) as Button
        partnerButton = view.findViewById(R.id.habit_partner_button) as Button
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
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getHabitReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.habit_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

//        partnerButton.apply {
//            setOnClickListener {
//                val pickContactIntent =
//                    Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
//                setOnClickListener {
//                    startActivityForResult(pickContactIntent, REQUEST_CONTACT)
//
//            }
//            }
//        }
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
        dateButton.text = DateFormat.format(DATE_FORMAT, habit.date)
        dailyDoneCheckBox.apply {
            isChecked = habit.dailyDone
            jumpDrawablesToCurrentState()
        }
    }

    private fun getHabitReport(): String {
        val solvedString = if (habit.dailyDone) {
            getString(R.string.progress_report_success)
        } else {
            getString(R.string.progress_report_failure)
        }
        val dateString = DateFormat.format(DATE_FORMAT, habit.date).toString()
        val partner = if (habit.partner.isBlank()) {
            getString(R.string.habit_report_no_partner)
        } else {
            getString(R.string.habit_report_partner, habit.partner)
        }
        return getString(R.string.habit_report,
            habit.title, dateString, solvedString, partner)
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
        Toast.makeText(context, "${habit.title} saved!", Toast.LENGTH_SHORT).show()
        habitDetailViewModel.saveHabit(habit)
    }

    override fun onDateSelected(date: Date) {
        habit.date = date
        updateUI()
    }
}