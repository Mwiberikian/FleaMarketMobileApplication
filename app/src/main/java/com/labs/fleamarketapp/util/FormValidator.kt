package com.labs.fleamarketapp.util

import com.google.android.material.textfield.TextInputLayout

object FormValidator {

    fun requiredText(input: TextInputLayout, value: CharSequence?, message: String = "Required"): Boolean {
        val valid = !value.isNullOrBlank()
        input.error = if (valid) null else message
        return valid
    }

    fun dropdownSelection(input: TextInputLayout, value: String?, message: String = "Select an option"): Boolean {
        val valid = !value.isNullOrBlank()
        input.error = if (valid) null else message
        return valid
    }

    fun positiveNumber(input: TextInputLayout, value: Double?, message: String = "Enter valid amount"): Boolean {
        val valid = value != null && value > 0
        input.error = if (valid) null else message
        return valid
    }

    fun positiveInteger(input: TextInputLayout, value: Int?, message: String = "Enter valid number"): Boolean {
        val valid = value != null && value > 0
        input.error = if (valid) null else message
        return valid
    }
}

