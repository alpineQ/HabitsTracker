package com.alpineQ.habitstracker

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "HabitListFragment"
private const val DATE_FORMAT = "dd MMMM, yyyy"

class HabitListFragment : Fragment() {
    interface Callbacks {
        fun onHabitSelected(habitID: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var habitRecyclerView: RecyclerView
    private lateinit var addNewHabitButton: Button
    private var adapter: HabitAdapter? = HabitAdapter(emptyList())
    private val habitListViewModel: HabitListViewModel by lazy {
        ViewModelProvider(this).get(HabitListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        addNewHabitButton = view.findViewById(R.id.add_new_habit_button) as Button
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
            { habits ->
                habits?.let {
                    Log.i(TAG, "Got habits ${habits.size}")
                    updateUI(habits)
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_habit_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_habit -> {
                val habit = Habit()
                habitListViewModel.addHabit(habit)
                callbacks?.onHabitSelected(habit.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(habits: List<Habit>) {
        if (habits.isEmpty()) {
            addNewHabitButton.visibility = View.VISIBLE
        }
        else {
            addNewHabitButton.visibility = View.INVISIBLE
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