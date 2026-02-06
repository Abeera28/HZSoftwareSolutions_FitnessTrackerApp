package com.example.fitnesstracker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnesstracker.databinding.ActivityAddWorkoutBinding
import com.example.fitnesstracker.model.Workout
import com.google.firebase.firestore.FirebaseFirestore

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWorkoutBinding
    private val db = FirebaseFirestore.getInstance()
    private var workoutId: String? = null

    // Flags to track manual edits
    private var userEditingSteps = false
    private var userEditingCalories = false

    // Activity → Workout mapping
    private val workoutOptions = mapOf(
        "Walking" to listOf("Morning Walk", "Evening Walk"),
        "Running" to listOf("Jogging", "Sprint"),
        "Cycling" to listOf("Road Cycling", "Stationary"),
        "Gym" to listOf("Chest Day", "Leg Day", "Cardio"),
        "Yoga" to listOf("Power Yoga", "Stretching")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workoutId = intent.getStringExtra("WORKOUT_ID")

        setupActivitySpinner()
        setupTextWatchers()
        setupDurationWatcher()

        // Activity spinner listener
        binding.spinnerActivity.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val activity = parent?.getItemAtPosition(position).toString()
                    updateWorkoutSpinner(activity)

                    val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
                    userEditingSteps = false
                    userEditingCalories = false
                    autoCalculate(activity, duration)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        workoutId?.let { loadWorkout(it) }

        binding.btnSave.setOnClickListener {
            saveOrUpdateWorkout()
        }
    }

    // -------------------- Duration TextWatcher --------------------
    private fun setupDurationWatcher() {
        binding.etDuration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val duration = s.toString().toIntOrNull() ?: return
                val activity = binding.spinnerActivity.selectedItem.toString()

                userEditingSteps = false
                userEditingCalories = false

                autoCalculate(activity, duration)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // -------------------- Load Workout (Edit Mode) --------------------
    private fun loadWorkout(id: String) {
        db.collection("workouts").document(id).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val workout = doc.toObject(Workout::class.java) ?: return@addOnSuccessListener

                binding.etDuration.setText(workout.duration.toString())
                binding.etSteps.setText("${workout.steps} steps")
                binding.etCalories.setText("${workout.calories} kcal")

                val activities = listOf("Select Activity", "Walking", "Running", "Cycling", "Gym", "Yoga")
                binding.spinnerActivity.setSelection(activities.indexOf(workout.activityType))

                updateWorkoutSpinner(workout.activityType)
                val workoutList = workoutOptions[workout.activityType] ?: emptyList()
                binding.spinnerWorkout.setSelection(workoutList.indexOf(workout.workoutName))
            }
    }

    // -------------------- Activity Spinner --------------------
    private fun setupActivitySpinner() {
        val activities = listOf("Select Activity", "Walking", "Running", "Cycling", "Gym", "Yoga")

        val adapter = ArrayAdapter(this, R.layout.spinner_item, activities)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerActivity.adapter = adapter
    }

    // -------------------- Workout Spinner --------------------
    private fun updateWorkoutSpinner(activity: String) {
        val workouts = workoutOptions[activity] ?: emptyList()
        val adapter = ArrayAdapter(this, R.layout.spinner_item, workouts)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerWorkout.adapter = adapter
    }

    // -------------------- Steps & Calories Watchers --------------------
    private fun setupTextWatchers() {

        binding.etSteps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userEditingSteps = true
            }
            override fun afterTextChanged(s: Editable?) {
                binding.etSteps.removeTextChangedListener(this)
                val number = s.toString().replace("[^0-9]".toRegex(), "")
                if (number.isNotEmpty()) {
                    binding.etSteps.setText("$number steps")
                    binding.etSteps.setSelection(number.length)
                } else binding.etSteps.setText("")
                binding.etSteps.addTextChangedListener(this)
            }
        })

        binding.etCalories.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userEditingCalories = true
            }
            override fun afterTextChanged(s: Editable?) {
                binding.etCalories.removeTextChangedListener(this)
                val number = s.toString().replace("[^0-9]".toRegex(), "")
                if (number.isNotEmpty()) {
                    binding.etCalories.setText("$number kcal")
                    binding.etCalories.setSelection(number.length)
                } else binding.etCalories.setText("")
                binding.etCalories.addTextChangedListener(this)
            }
        })
    }

    // -------------------- Save / Update Workout --------------------
    private fun saveOrUpdateWorkout() {

        val activity = binding.spinnerActivity.selectedItem.toString()
        val workoutName = binding.spinnerWorkout.selectedItem?.toString() ?: ""
        val durationStr = binding.etDuration.text.toString().trim()

        if (activity == "Select Activity") {
            Toast.makeText(this, "Please select an activity", Toast.LENGTH_SHORT).show()
            return
        }
        if (workoutName.isEmpty()) {
            Toast.makeText(this, "Please select a workout", Toast.LENGTH_SHORT).show()
            return
        }
        if (durationStr.isEmpty()) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = durationStr.toInt()
        val steps = binding.etSteps.text.toString().replace("[^0-9]".toRegex(), "").toInt()
        val calories = binding.etCalories.text.toString().replace("[^0-9]".toRegex(), "").toInt()

        val workoutData = hashMapOf(
            "activityType" to activity,
            "workoutName" to workoutName,
            "duration" to duration,
            "steps" to steps,
            "calories" to calories,
            "timestamp" to System.currentTimeMillis()
        )

        if (workoutId == null) {
            db.collection("workouts").add(workoutData)
        } else {
            db.collection("workouts").document(workoutId!!).update(workoutData as Map<String, Any>)
        }

        Toast.makeText(this, "Workout saved successfully 💪", Toast.LENGTH_SHORT).show()
        finish()
    }

    // -------------------- Auto Calculation --------------------
    private fun autoCalculate(activity: String?, durationMin: Int) {

        if (activity == null || activity == "Select Activity") return

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

        val totalSteps = stepsPerMin * durationMin
        val totalCalories = caloriesPerMin * durationMin

        if (!userEditingSteps) binding.etSteps.setText("$totalSteps steps")
        if (!userEditingCalories) binding.etCalories.setText("$totalCalories kcal")
    }
}
