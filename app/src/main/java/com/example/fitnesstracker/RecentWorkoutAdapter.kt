package com.example.fitnesstracker

import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.model.Workout
import android.view.ViewGroup
import android.view.View
class RecentWorkoutAdapter(
    private val list: MutableList<Workout>
) : RecyclerView.Adapter<RecentWorkoutAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvWorkoutName)
        val info: TextView = view.findViewById(R.id.tvWorkoutInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_workout, parent, false)
        return ViewHolder(view)
    }
    fun updateList(newList: List<Workout>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = list[position]
        holder.name.text = workout.workoutName
        holder.info.text =
            "${workout.duration} min • ${workout.calories} kcal"
    }

    override fun getItemCount() = list.size
}
