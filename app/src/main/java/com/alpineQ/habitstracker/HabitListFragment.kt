package com.alpineQ.habitstracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "HabitListFragment"

class HabitListFragment : Fragment() {
    interface Callbacks {
        fun onHabitSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var habitRecyclerView: RecyclerView
    private var adapter: HabitAdapter? = null
    private val habitListViewModel: HabitListViewModel by lazy {
        ViewModelProvider(this).get(HabitListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total habits:${habitListViewModel.habits.size}")
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
        habitRecyclerView.layoutManager = LinearLayoutManager(context)
        updateUI()
        return view
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI() {
        val habits = habitListViewModel.habits
        adapter = HabitAdapter(habits)
        habitRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): HabitListFragment {
            return HabitListFragment()
        }
    }

    private inner class HabitHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
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
            dateTextView.text = this.habit.date.toString()
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