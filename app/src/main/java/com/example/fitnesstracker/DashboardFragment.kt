package com.example.fitnesstracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fitnesstracker.databinding.FragmentDashboardBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.model.Workout

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var binding: FragmentDashboardBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)

        loadDashboardData()
    }

    private fun loadDashboardData() {
        db.collection("workouts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                var totalCalories = 0
                var totalDuration = 0
                val recentList = mutableListOf<Workout>()

                for (doc in result) {
                    val workout = doc.toObject(Workout::class.java)

                    totalCalories += workout.calories
                    totalDuration += workout.duration

                    if (recentList.size < 3) {
                        recentList.add(workout)
                    }
                }

                binding.tvCalories.text = totalCalories.toString()
                binding.tvDuration.text = totalDuration.toString()

                setupRecentRecycler(recentList)
            }
    }

    private fun setupRecentRecycler(list: List<Workout>) {
        binding.rvRecentActivities.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvRecentActivities.adapter =
            RecentWorkoutAdapter(list)
    }
}

