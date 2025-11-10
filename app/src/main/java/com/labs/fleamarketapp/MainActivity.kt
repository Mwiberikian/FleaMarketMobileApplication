package com.labs.fleamarketapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.labs.fleamarketapp.databinding.ActivityMainBinding
import com.labs.fleamarketapp.data.User
import com.labs.fleamarketapp.viewmodel.UserViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize user data if passed from LoginActivity
        initializeUserData()
        
        setupBottomNavigation()
    }
    
    private fun initializeUserData() {
        val user = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("USER_DATA", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<User>("USER_DATA")
        }
        user?.let {
            // Initialize the ViewModel with user data
            userViewModel.setUser(it)
}
    }
    
    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as? NavHostFragment
        
        navHostFragment?.let {
            val navController = it.navController
            binding.bottomNavigation.setupWithNavController(navController)
    }
}
}
