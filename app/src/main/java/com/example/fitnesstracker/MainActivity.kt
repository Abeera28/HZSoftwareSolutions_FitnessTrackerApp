package com.example.fitnesstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnesstracker.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  Default Fragment (Dashboard)
        replaceFragment(DashboardFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard

        // Show Username in Toolbar
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val username = document.getString("name") ?: "User"
                binding.tvUsername.text = "Hello $username 👋"
            }
            .addOnFailureListener {
                binding.tvUsername.text = "Hello User 👋"
            }

        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> replaceFragment(DashboardFragment())
                R.id.nav_add -> replaceFragment(AddFragment())
                R.id.nav_history -> replaceFragment(HistoryFragment())
            }

            popOutSelectedIcon(item.itemId)
            true
        }
    }

    private fun popOutSelectedIcon(selectedItemId: Int) {
        val menu = binding.bottomNavigation.menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val iconView = binding.bottomNavigation.findViewById<View>(item.itemId)
            if (item.itemId == selectedItemId) {
                iconView?.animate()?.scaleX(1.3f)?.scaleY(1.3f)?.setDuration(200)?.start()
            } else {
                iconView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(200)?.start()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
