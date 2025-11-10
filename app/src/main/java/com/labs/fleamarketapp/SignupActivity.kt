package com.labs.fleamarketapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.data.UserType
import com.labs.fleamarketapp.databinding.ActivitySignupBinding
import com.labs.fleamarketapp.viewmodel.UserViewModel

class SignupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignupBinding
    private val viewModel: UserViewModel by viewModels()
    
    private val userTypes = arrayOf("Buyer", "Seller")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUserTypeDropdown()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupUserTypeDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, userTypes)
        binding.userTypeAutoComplete.setAdapter(adapter)
    }
    
    private fun setupObservers() {
        viewModel.signupState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.signupButton.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.signupButton.isEnabled = true
                    // Navigate to MainActivity with user data
                    val intent = Intent(this@SignupActivity, MainActivity::class.java).apply {
                        putExtra("USER_DATA", state.data)
                    }
                    startActivity(intent)
                    finish()
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.signupButton.isEnabled = true
                    Toast.makeText(
                        this@SignupActivity,
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            val userTypeText = binding.userTypeAutoComplete.text.toString().trim()
            
            if (validateInput(name, email, phone, password, confirmPassword, userTypeText)) {
                val userType = if (userTypeText == "Seller") UserType.SELLER else UserType.BUYER
                viewModel.signup(email, password, name, userType, phone.ifEmpty { null })
            }
        }
        
        binding.loginLink.setOnClickListener {
            finish() // Go back to LoginActivity
        }
        
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateEmail(binding.emailEditText.text.toString().trim())
            }
        }
    }
    
    private fun validateInput(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        userType: String
    ): Boolean {
        var isValid = true
        
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Name is required"
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!validateEmail(email)) {
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }
        
        // Phone number is optional, but if provided, validate format
        if (phone.isNotEmpty() && !validatePhone(phone)) {
            binding.phoneInputLayout.error = "Invalid phone number format"
            isValid = false
        } else {
            binding.phoneInputLayout.error = null
        }
        
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordInputLayout.error = null
        }
        
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordInputLayout.error = null
        }
        
        if (userType.isEmpty()) {
            binding.userTypeInputLayout.error = "User type is required"
            isValid = false
        } else if (userType !in userTypes) {
            binding.userTypeInputLayout.error = "Please select a valid user type"
            isValid = false
        } else {
            binding.userTypeInputLayout.error = null
        }
        
        return isValid
    }
    
    private fun validatePhone(phone: String): Boolean {
        // Basic phone validation - removes common formatting characters and checks if remaining digits are valid length
        val cleanedPhone = phone.replace(Regex("[\\s\\-\\(\\)]"), "")
        // Should have 7-15 digits (international format)
        return cleanedPhone.matches(Regex("^[+]?[0-9]{7,15}$"))
    }
    
    private fun validateEmail(email: String): Boolean {
        return if (viewModel.isValidStrathmoreEmail(email)) {
            binding.emailErrorText.visibility = View.GONE
            binding.emailInputLayout.error = null
            true
        } else {
            binding.emailErrorText.visibility = View.VISIBLE
            binding.emailInputLayout.error = "Invalid Strathmore email"
            false
        }
    }
}

