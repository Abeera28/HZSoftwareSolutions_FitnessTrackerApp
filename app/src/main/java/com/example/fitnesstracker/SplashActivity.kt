package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnesstracker.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            //  User already logged in → auto move after 3 seconds
            binding.getStartedBtn.isEnabled = false
            binding.getStartedBtn.alpha = 0.6f // visually show it's disabled

            binding.root.postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 2000)

        } else {
            //  User NOT logged in → wait for user click
            binding.getStartedBtn.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}

