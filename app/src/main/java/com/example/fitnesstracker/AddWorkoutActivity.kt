package com.example.fitnesstracker

import android.os.Bundle
import android.widget.Toast
import android.view.View
import android.widget.AdapterView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fitnesstracker.databinding.ActivityAddWorkoutBinding
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.example.fitnesstracker.model.Workout
class AddWorkoutActivity : AppCompatActivity() {

    private var workoutId: String? = null
    private lateinit var binding: ActivityAddWorkoutBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workoutId = intent.getStringExtra("WORKOUT_ID")

        setupSpinner()
        binding.spinnerActivity.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val duration = binding.etDuration.text.toString().toIntOrNull() ?: return
                    autoCalculate(parent.getItemAtPosition(position).toString(), duration)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        binding.etDuration.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val duration = binding.etDuration.text.toString().toIntOrNull() ?: return@setOnFocusChangeListener
                val activity = binding.spinnerActivity.selectedItem.toString()
                autoCalculate(activity, duration)
            }
        }

        workoutId?.let { loadWorkout(it) }

        binding.btnSave.setOnClickListener {
            saveOrUpdateWorkout()
        }
    }
    private fun loadWorkout(id: String) {
        db.collection("workouts")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                val workout = doc.toObject(Workout::class.java) ?: return@addOnSuccessListener

                binding.etWorkoutName.setText(workout.workoutName)
                binding.etDuration.setText(workout.duration)
                binding.etSteps.setText(workout.steps.toString())
                binding.etCalories.setText(workout.calories.toString())

                val activities = listOf("Walking", "Running", "Cycling", "Gym", "Yoga")
                binding.spinnerActivity.setSelection(
                    activities.indexOf(workout.activityType)
                )
            }
    }


    private fun setupSpinner() {
        val activities = listOf("Walking", "Running", "Cycling", "Gym", "Yoga")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            activities
        )
        binding.spinnerActivity.adapter = adapter
    }

    private fun saveOrUpdateWorkout() {

        val workout = hashMapOf(
            "activityType" to binding.spinnerActivity.selectedItem.toString(),
            "workoutName" to binding.etWorkoutName.text.toString(),
            "duration" to binding.etDuration.text.toString(),
            "steps" to binding.etSteps.text.toString().toInt(),
            "calories" to binding.etCalories.text.toString().toInt(),
            "timestamp" to System.currentTimeMillis()
        )

        if (workoutId == null) {
            db.collection("workouts").add(workout)
        } else {
            db.collection("workouts").document(workoutId!!).update(workout as Map<String, Any>)
        }

        finish()
    }
    private fun autoCalculate(activity: String, durationMin: Int) {

        val stepsPerMin = when (activity) {
            "Walking" -> 100
            "Running" -> 160
            "Cycling" -> 60
            "Gym" -> 80
            "Yoga" -> 40
            else -> 0
        }

        val caloriesPerMin = when (activity) {
            "Walking" -> 4
            "Running" -> 10
            "Cycling" -> 8
            "Gym" -> 7
            "Yoga" -> 3
            else -> 0
        }

        binding.etSteps.setText((stepsPerMin * durationMin).toString())
        binding.etCalories.setText((caloriesPerMin * durationMin).toString())
    }

}
