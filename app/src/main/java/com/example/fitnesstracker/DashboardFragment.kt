package com.example.fitnesstracker

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.databinding.FragmentDashboardBinding
import com.example.fitnesstracker.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var recentAdapter: RecentWorkoutAdapter
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)

        binding.dashboardContent.visibility = View.INVISIBLE
        binding.progressDashboard.visibility = View.VISIBLE

        setupRecycler()
        loadDashboardData()
    }

    private fun setupRecycler() {
        recentAdapter = RecentWorkoutAdapter(mutableListOf())
        binding.rvRecentActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentActivities.adapter = recentAdapter
    }

    private fun loadDashboardData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        listener = db.collection("workouts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { result, error ->
                if (!isAdded || view == null) return@addSnapshotListener
                if (error != null || result == null) return@addSnapshotListener

                var totalCalories = 0
                var totalDuration = 0
                val recentList = mutableListOf<Workout>()

                // Prepare last 7 days map (ordered)
                val dailyCaloriesMap = linkedMapOf<String, Int>()
                val cal = Calendar.getInstance()
                cal.timeZone = TimeZone.getDefault()
                val formatter = java.text.SimpleDateFormat("EEE", Locale.getDefault())
                formatter.timeZone = TimeZone.getDefault()

                for (i in 6 downTo 0) {
                    val tempCal = cal.clone() as Calendar
                    tempCal.add(Calendar.DAY_OF_YEAR, -i)
                    val dayLabel = formatter.format(tempCal.time)
                    dailyCaloriesMap[dayLabel] = 0
                }

                val weekStartMillis = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }.timeInMillis

                for (doc in result.documents) {
                    val workout = doc.toObject(Workout::class.java) ?: continue
                    workout.id = doc.id

                    totalCalories += workout.calories
                    totalDuration += workout.duration

                    if (recentList.size < 3) recentList.add(workout)

                    val ts = workout.timestamp
                    if (ts >= weekStartMillis) {
                        val workoutCal = Calendar.getInstance().apply { timeInMillis = ts }
                        val dayLabel = formatter.format(workoutCal.time)
                        dailyCaloriesMap[dayLabel] = (dailyCaloriesMap[dayLabel] ?: 0) + workout.calories
                    }
                }

                // Update UI safely
                binding.tvCalories.text = totalCalories.toString()
                binding.tvDuration.text = totalDuration.toString()
                recentAdapter.updateList(recentList)
                showWeeklyBars(dailyCaloriesMap)

                binding.progressDashboard.visibility = View.GONE
                binding.dashboardContent.visibility = View.VISIBLE
            }
    }

    private fun showWeeklyBars(dailyCaloriesMap: LinkedHashMap<String, Int>) {
        if (!isAdded || view == null) return  // safety check

        val values = dailyCaloriesMap.values.toIntArray()
        val days = dailyCaloriesMap.keys.toList()

        val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
        binding.layoutWeeklyBars.removeAllViews()

        for (i in values.indices) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()  // remove Firestore listener to avoid crashes
    }
}
