package com.example.fitnesstracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fitnesstracker.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import java.util.Calendar



class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDashboardData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("fitnessLogs")
            .get()
            .addOnSuccessListener { documents ->

                if (documents.isEmpty) {
                    _binding?.tvEmpty?.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                var totalCalories = 0
                var todayCalories = 0
                val todayStart = getTodayStartMillis()

                for (doc in documents) {
                    val calories = doc.getLong("calories")?.toInt() ?: 0
                    val date = doc.getLong("date") ?: 0L

                    totalCalories += calories
                    if (date >= todayStart) todayCalories += calories
                }

                _binding?.let {
                    it.tvTotalCalories.text = "$totalCalories kcal"
                    it.tvTotalActivities.text = documents.size().toString()
                    it.tvTodayCalories.text = "$todayCalories kcal"
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

