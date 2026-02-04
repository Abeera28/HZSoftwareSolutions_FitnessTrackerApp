package com.example.fitnesstracker

import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.databinding.ItemWorkoutBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.fitnesstracker.model.Workout

class WorkoutAdapter(
    private val list: List<Workout>,
    private val onEdit: (Workout) -> Unit,
    private val onDelete: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = list[position]

        holder.binding.tvTitle.text =
            "${workout.activityType} - ${workout.workoutName}"

        holder.binding.tvDetails.text =
            "Duration: ${workout.duration} | Steps: ${workout.steps} | Calories: ${workout.calories}"

        holder.binding.btnEdit.setOnClickListener {
            onEdit(workout)
        }

        holder.binding.btnDelete.setOnClickListener {
            onDelete(workout)
        }
    }

    override fun getItemCount() = list.size
}

