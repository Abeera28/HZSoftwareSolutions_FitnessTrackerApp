package com.example.fitnesstracker.model

data class Workout(
    var id: String = "",          //  Firestore document ID
    var userId: String = "",      //  Firebase user UID
    var activityType: String = "",
    var workoutName: String = "",
    var duration: Int = 0,
    var steps: Int = 0,
    var calories: Int = 0,
    var timestamp: Long = 0L
)
