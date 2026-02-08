package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.databinding.FragmentAddBinding
import com.example.fitnesstracker.model.Workout
import com.google.firebase.auth.FirebaseAuth
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
        super.onViewCreated(view, savedInstanceState)

        // Setup adapter
        adapter = WorkoutAdapter(
            workoutList,
            onEdit = { workout ->
                val intent = Intent(requireContext(), AddWorkoutActivity::class.java)
                intent.putExtra("WORKOUT_ID", workout.id) // use correct doc ID
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

    override fun onResume() {
        super.onResume()
        loadWorkouts()
    }

    private fun deleteWorkout(docId: String) {
        db.collection("workouts")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d("ADD_FRAGMENT", "Workout deleted: $docId")
                loadWorkouts()
            }
            .addOnFailureListener { e ->
                Log.e("ADD_FRAGMENT", "Failed to delete workout: $docId", e)
            }
    }

    private fun loadWorkouts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("ADD_FRAGMENT", "User not logged in!")
            return
        }

        Log.d("ADD_FRAGMENT", "Fetching workouts for UID: $userId")

        db.collection("workouts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("ADD_FRAGMENT", "Firestore error", error)
                    return@addSnapshotListener
                }

                workoutList.clear()

                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val workout = doc.toObject(Workout::class.java)
                        if (workout != null) {
                            workout.id = doc.id
                            workoutList.add(workout)
                        }
                    }
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
    }

}
