package com.amaromerovic.projemanag.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivitySignUpBinding
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.singUpToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.singUpToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.signUp.setOnClickListener {
            checkIfItIsEmpty(binding.nameInputLayout, binding.name)
            checkIfItIsEmpty(binding.emailInputLayout, binding.email)
            checkIfItIsEmpty(binding.passwordInputLayout, binding.password)

        }
    }


    private fun checkIfItIsEmpty(textInputLayout : TextInputLayout, editText: AppCompatEditText) {
        if(editText.text!!.isEmpty()) {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error = "Field is empty!"
        } else {
            textInputLayout.isErrorEnabled = false
        }
    }
}