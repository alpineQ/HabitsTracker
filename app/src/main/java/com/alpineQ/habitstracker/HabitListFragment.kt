package com.alpineQ.habitstracker

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "HabitListFragment"
private const val POLL_WORK = "POLL_WORK"
private const val DATE_FORMAT = "dd MMMM, yyyy"

class HabitListFragment : Fragment() {
    interface Callbacks {
        fun onHabitSelected(habitID: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var habitRecyclerView: RecyclerView
    private lateinit var addFirstHabitButton: Button
    private lateinit var addNewHabitButton: FloatingActionButton
    private var adapter: HabitAdapter? = HabitAdapter(emptyList())
    private val habitListViewModel: HabitListViewModel by lazy {
        ViewModelProvider(this).get(HabitListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val workRequest = OneTimeWorkRequest
            .Builder(PollWorker::class.java)
            .build()

        context?.let { WorkManager.getInstance(it).enqueue(workRequest) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habit_list, container, false)
        habitRecyclerView = view.findViewById(R.id.habit_recycler_view) as RecyclerView
        addFirstHabitButton = view.findViewById(R.id.add_first_habit_button) as Button
        addFirstHabitButton.setOnClickListener {
            val habit = Habit()
            habitListViewModel.addHabit(habit)
            callbacks?.onHabitSelected(habit.id)
        }
        addNewHabitButton = view.findViewById(R.id.new_habit_fab) as FloatingActionButton
        addNewHabitButton.setOnClickListener {
            val habit = Habit()
            habitListViewModel.addHabit(habit)
            callbacks?.onHabitSelected(habit.id)
        }
        habitRecyclerView.layoutManager = LinearLayoutManager(context)
        habitRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        habitListViewModel.habitListLiveData.observe(viewLifecycleOwner,
            { habits -> habits?.let { updateUI(habits) } })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_habit_list, menu)
        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = PollWorker.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            R.id.menu_item_toggle_polling -> {
                val isPolling = PollWorker.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
                    PollWorker.setPolling(requireContext(), false)

                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                            .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .build()
                    WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest
                    )
                    PollWorker.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(habits: List<Habit>) {
        if (habits.isEmpty()) {
            addFirstHabitButton.visibility = View.VISIBLE
        }
        else {
            addFirstHabitButton.visibility = View.INVISIBLE
        }
    adapter = HabitAdapter(habits)
        habitRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): HabitListFragment {
            return HabitListFragment()
        }
    }

    private inner class HabitHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var habit: Habit
        private val titleTextView: TextView = itemView.findViewById(R.id.habit_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.habit_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.daily_done)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(habit: Habit) {
            this.habit = habit
            titleTextView.text = this.habit.title
            dateTextView.text = DateFormat.format(DATE_FORMAT, habit.date)
            solvedImageView.visibility = if (habit.dailyDone) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
            callbacks?.onHabitSelected(habit.id)
        }
    }

    private inner class HabitAdapter(var habits: List<Habit>) : RecyclerView.Adapter<HabitHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitHolder {
            val view = layoutInflater.inflate(R.layout.list_item_habit, parent, false)
            return HabitHolder(view)
        }

        override fun getItemCount() = habits.size
        override fun onBindViewHolder(holder: HabitHolder, position: Int) {
            val habit = habits[position]
            holder.bind(habit)
        }
    }
}