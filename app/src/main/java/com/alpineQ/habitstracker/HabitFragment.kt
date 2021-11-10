package com.alpineQ.habitstracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.util.*

private const val TAG = "HabitFragment"
private const val ARG_HABIT_ID = "habit_id"
private const val REQUEST_DATE = "DialogDate"
private const val DATE_FORMAT = "dd MMMM, yyyy"


class HabitFragment : Fragment(), FragmentResultListener, DatePickerFragment.Callbacks {
    private lateinit var habit: Habit
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var dailyDoneCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var partnerButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private val habitDetailViewModel: HabitDetailViewModel by lazy {
        ViewModelProvider(this).get(HabitDetailViewModel::class.java)
    }
    private lateinit var pickContactContract: ActivityResultContract<Uri, Uri?>
    private lateinit var pickContactCallback: ActivityResultCallback<Uri?>
    private lateinit var pickContactLauncher: ActivityResultLauncher<Uri>

    private lateinit var takePhotoContract: ActivityResultContract<Uri, Uri?>
    private lateinit var takePhotoCallback: ActivityResultCallback<Uri?>
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habit = Habit()
        val habitId: UUID = arguments?.getSerializable(ARG_HABIT_ID) as UUID
        habitDetailViewModel.loadHabit(habitId)
        pickContactContract = object : ActivityResultContract<Uri, Uri?>() {
            override fun createIntent(context: Context, input: Uri): Intent {
                return Intent(Intent.ACTION_PICK, input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                if (resultCode != Activity.RESULT_OK || intent == null)
                    return null
                return intent.data
            }
        }

        pickContactCallback = ActivityResultCallback<Uri?> { contactUri: Uri? ->
            Log.d(TAG, "onActivityResult() called with result: $contactUri")
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            val cursor = contactUri?.let {
                requireActivity().contentResolver.query(it, queryFields, null, null, null)
            }
            cursor?.use {
                if (it.count == 0) {
                    return@ActivityResultCallback
                }
                it.moveToFirst()
                val suspect = it.getString(0)
                habit.partner = suspect
                habitDetailViewModel.saveHabit(habit)
                partnerButton.text = suspect
            }
        }
        pickContactLauncher = registerForActivityResult(pickContactContract, pickContactCallback)


        takePhotoContract = object : ActivityResultContract<Uri, Uri?>() {
            override fun createIntent(context: Context, input: Uri): Intent {
                return Intent(MediaStore.ACTION_IMAGE_CAPTURE, input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                if (resultCode != Activity.RESULT_OK || intent == null)
                    return null
                return intent.data
            }
        }

        pickContactCallback = ActivityResultCallback<Uri?> { photoUri: Uri? ->
            Log.d(TAG, "onActivityResult() called with result: $photoUri")
        }
        takePhotoLauncher = registerForActivityResult(takePhotoContract, takePhotoCallback)

//        val packageManager: PackageManager = requireActivity().packageManager
//        val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage,
//            PackageManager.MATCH_DEFAULT_ONLY)
//        if (resolvedActivity == null) {
//            isEnabled = true
//        }
//        setOnClickListener {
//            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//
//            val cameraActivities: List<ResolveInfo> =
//                packageManager.queryIntentActivities(captureImage,
//                    PackageManager.MATCH_DEFAULT_ONLY)
//            for (cameraActivity in cameraActivities) {
//                requireActivity().grantUriPermission(
//                    cameraActivity.activityInfo.packageName,
//                    photoUri,
//                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                )
//            }

        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
        val view = inflater.inflate(R.layout.fragment_habit, container, false)
        titleField = view.findViewById(R.id.habit_title) as EditText
        dateButton = view.findViewById(R.id.habit_date) as Button
        dailyDoneCheckBox = view.findViewById(R.id.habit_daily_done) as CheckBox
        reportButton = view.findViewById(R.id.share_progress_button) as Button
        partnerButton = view.findViewById(R.id.habit_partner_button) as Button
        photoButton = view.findViewById(R.id.habit_camera) as ImageButton
        photoView = view.findViewById(R.id.habit_photo) as ImageView
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
                    photoFile = habitDetailViewModel.getPhotoFile(habit)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.alpineQ.habitstracker.fileprovider",
                        photoFile)
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
            ) {}

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                habit.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {}
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
                    getString(R.string.habit_report_subject)
                )
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        partnerButton.apply {
            setOnClickListener {
                pickContactLauncher.launch(ContactsContract.Contacts.CONTENT_URI)
            }
        }
        photoButton.apply {
        }
    }

    override fun onFragmentResult(requestCode: String, result: Bundle) {
        when (requestCode) {
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
        if (habit.partner.isNotEmpty()) {
            partnerButton.text = habit.partner
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
        return getString(
            R.string.habit_report,
            habit.title, dateString, solvedString, partner
        )
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