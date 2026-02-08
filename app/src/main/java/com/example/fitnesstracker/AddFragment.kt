package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.databinding.FragmentAddBinding
import com.example.fitnesstracker.model.Workout
import com.google.firebase.firestore.FirebaseFirestore

class AddFragment : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private val db = FirebaseFirestore.getInstance()
    private val workoutList = mutableListOf<Workout>()
    private lateinit var adapter: WorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = WorkoutAdapter(
            workoutList,
            onEdit = { workout ->
                val intent = Intent(requireContext(), AddWorkoutActivity::class.java)
                intent.putExtra("WORKOUT_ID", workout.id)
                startActivity(intent)
            },
            onDelete = { workout ->
                deleteWorkout(workout.id)
            }
        )

        binding.recyclerWorkouts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWorkouts.adapter = adapter

        binding.btnAddWorkout.setOnClickListener {
            startActivity(Intent(requireContext(), AddWorkoutActivity::class.java))
        }
    }
    private fun deleteWorkout(id: String) {
        db.collection("workouts")
            .document(id)
            .delete()
            .addOnSuccessListener {
                loadWorkouts()
            }
    }


    override fun onResume() {
        super.onResume()
        loadWorkouts()
    }

    private fun loadWorkouts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("workouts")
            .whereEqualTo("userId", userId) // Show only current user's workouts
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->
                workoutList.clear()
                for (doc in snapshot.documents) {
                    val workout = doc.toObject(Workout::class.java)
                    workout?.id = doc.id
                    workout?.let { workoutList.add(it) }
                }
                adapter.notifyDataSetChanged()

                if (workoutList.isEmpty()) {
                    binding.tvNoWorkout.visibility = View.VISIBLE
                    binding.recyclerWorkouts.visibility = View.GONE
                } else {
                    binding.tvNoWorkout.visibility = View.GONE
                    binding.recyclerWorkouts.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                // Optional: handle error
                binding.tvNoWorkout.visibility = View.VISIBLE
                binding.recyclerWorkouts.visibility = View.GONE
            }
    }


}