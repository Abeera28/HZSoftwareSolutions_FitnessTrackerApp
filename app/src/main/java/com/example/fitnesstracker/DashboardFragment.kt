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

        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("workouts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { result, error ->

                if (error != null) return@addSnapshotListener
                if (result == null) return@addSnapshotListener

                var totalCalories = 0
                var totalDuration = 0
                val recentList = mutableListOf<Workout>()
                val dailyCalories = IntArray(7)

                val now = Calendar.getInstance()
                val weekStart = Calendar.getInstance()
                weekStart.add(Calendar.DAY_OF_YEAR, -6)

                for (doc in result.documents) {

                    val workout = doc.toObject(Workout::class.java) ?: continue
                    workout.id = doc.id

                    totalCalories += workout.calories
                    totalDuration += workout.duration

                    if (recentList.size < 3) {
                        recentList.add(workout)
                    }

                    val ts = workout.timestamp
                    if (ts >= weekStart.timeInMillis) {
                        val dayIndex = getDayIndex(ts)
                        dailyCalories[dayIndex] += workout.calories
                    }
                }

                binding.tvCalories.text = totalCalories.toString()
                binding.tvDuration.text = totalDuration.toString()

                recentAdapter.updateList(recentList)
                showWeeklyBars(dailyCalories)

                binding.progressDashboard.visibility = View.GONE
                binding.dashboardContent.visibility = View.VISIBLE
            }
    }



    private fun getDayIndex(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp

        val today = Calendar.getInstance()

        val diff = ((today.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diff in 0..6 -> 6 - diff // last 7 days mapping
            else -> -1
        }
    }


    private fun showWeeklyBars(values: IntArray) {

        val days = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6) // start from 6 days ago

        val formatter = java.text.SimpleDateFormat("EEE")

        for (i in 0..6) {
            days.add(formatter.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

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
            tvPercent.setTextColor(android.graphics.Color.WHITE)

            tvPercent.text = "$percent%"
            tvDay.text = days[i]
            tvDay.setTextColor(android.graphics.Color.WHITE)

            bar.layoutParams.height = (percent * 140 / 100).coerceAtLeast(10)

            binding.layoutWeeklyBars.addView(barView)
        }
    }
}