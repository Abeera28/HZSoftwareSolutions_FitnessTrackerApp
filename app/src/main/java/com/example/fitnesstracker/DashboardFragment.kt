package com.example.fitnesstracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import java.util.Calendar
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fitnesstracker.databinding.FragmentDashboardBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.model.Workout

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var recentAdapter: RecentWorkoutAdapter
    private lateinit var binding: FragmentDashboardBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)

        binding.dashboardContent.visibility = View.INVISIBLE
        binding.progressDashboard.visibility = View.VISIBLE

        setupRecyclerOnce()
        loadDashboardData()
    }

    private fun setupRecyclerOnce() {
        recentAdapter = RecentWorkoutAdapter(mutableListOf())
        binding.rvRecentActivities.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvRecentActivities.adapter = recentAdapter
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

                recentAdapter.updateList(recentList)
                loadWeeklyProgress()

                binding.progressDashboard.visibility = View.GONE
                binding.dashboardContent.visibility = View.VISIBLE

            }
    }

    private fun loadWeeklyProgress() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startTime = calendar.timeInMillis

        db.collection("workouts")
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .get()
            .addOnSuccessListener { docs ->

                val dailyCalories = IntArray(7)

                for (doc in docs) {
                    val ts = doc.getLong("timestamp") ?: continue
                    val calories = doc.getLong("calories")?.toInt() ?: 0

                    val dayIndex = getDayIndex(ts)
                    dailyCalories[dayIndex] += calories
                }

                showWeeklyBars(dailyCalories)
            }
    }
    private fun getDayIndex(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.DAY_OF_WEEK) - 1 // 0–6
    }

    private fun showWeeklyBars(values: IntArray) {

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1

        binding.layoutWeeklyBars.removeAllViews()

        for (i in 0..6) {
            val barView = layoutInflater.inflate(
                R.layout.item_weekly_bar,
                binding.layoutWeeklyBars,
                false
            )

            val tvPercent = barView.findViewById<TextView>(R.id.tvPercent)
            val bar = barView.findViewById<View>(R.id.bar)
            val tvDay = barView.findViewById<TextView>(R.id.tvDay)

            val percent = (values[i] * 100) / max

            tvPercent.text = "$percent%"
            tvDay.text = days[i]

            bar.layoutParams.height =
                (percent * 120 / 100).coerceAtLeast(8)

            binding.layoutWeeklyBars.addView(barView)
        }
    }


}

