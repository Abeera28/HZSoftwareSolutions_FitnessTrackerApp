package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnesstracker.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Sign-Up Button Click
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Save user data to Firestore
                        val user = hashMapOf(
                            "name" to name,
                            "email" to email
                        )
                        firestore.collection("users")
                            .document(auth.currentUser!!.uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created! Please login.", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Sign-Up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Redirect to Login Screen
        binding.tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
