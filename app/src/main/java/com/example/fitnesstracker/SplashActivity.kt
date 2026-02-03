package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import kotlin.jvm.java
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnesstracker.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.fitnesstracker.MainActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in → go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // User not logged in → go to LoginActivity after delay (e.g., 2 seconds)
            binding.root.postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 2000)
        }
    }
}
