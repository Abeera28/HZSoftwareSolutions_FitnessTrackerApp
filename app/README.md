🏃‍♂️ FITNESS TRACKER APP 🏋️‍♀️

A modern Android fitness tracker app that allows users to log activities, monitor workouts, and visualize their weekly fitness progress.
Designed with a minimal yet visually appealing UI for quick insights and easy navigation.

📑 TABLE OF CONTENTS

✨ Features

🏛️ Architecture

☁️ Firebase Integration

💻 Installation

🏃‍♀️ Usage


✨ FEATURES
📝 Activity Logging

Users can select an activity (Walking, Running, Cycling, Gym, Yoga) and corresponding workout type from dropdown menus.

⏱️ Workout Details

Input workout duration.

Steps and calories are auto-calculated based on activity type and duration.

📊 Dashboard

Summary Cards: Quick overview of total calories burned and total duration.

Weekly Progress: Visual bar chart showing daily calories burned over the past 7 days.

Recent Activities: Shows the last three workouts with duration and calories.

🌟 Smooth UX

Loading states implemented to avoid UI blinking or black screens while fetching data.

👤 User-Specific Data

Data is stored per user and dynamically fetched on login.

🏛️ ARCHITECTURE

MVVM Pattern for structured and maintainable code

View Binding for safer and more readable UI code

RecyclerView for recent activities

LinearLayout / ScrollView for flexible layouts

Firebase Firestore for cloud data storage and real-time updates

☁️ FIREBASE INTEGRATION

Stores user-specific workouts with fields:

{
"activityType": "Walking",
"workoutName": "Morning Walk",
"duration": 30,
"steps": 3000,
"calories": 120,
"timestamp": 1675429200000
}


Firestore queries are optimized for dashboard summaries and weekly analytics.

Weekly progress is automatically calculated from the last 7 days based on timestamps.

💻 INSTALLATION

Clone the repository:

git clone https://github.com/yourusername/FitnessTrackerApp.git


Open in Android Studio

Configure Firebase project and add google-services.json in app/ folder

Build and run on an emulator or physical device

🏃‍♀️ USAGE

Launch the app

Navigate to Add Activity / Workout

Select activity and workout from dropdowns

Enter duration, steps/calories will auto-calculate

Save workout → appears on your dashboard

View:

Total calories burned

Total duration

Weekly progress chart

Recent activities