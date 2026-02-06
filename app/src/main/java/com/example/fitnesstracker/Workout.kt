package com.example.fitnesstracker.model

data class Workout(
    var id: String = "",
    val activityType: String = "",
    val workoutName: String = "",
    val duration: Int = 0,
    val steps: Int = 0,
    val calories: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
