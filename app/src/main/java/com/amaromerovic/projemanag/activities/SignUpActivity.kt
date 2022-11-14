package com.amaromerovic.projemanag.activities

import android.os.Bundle
import android.widget.Toast
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

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

        auth = Firebase.auth

        binding.signUp.setOnClickListener {
            val isNameValid = isInputEmpty(binding.nameInputLayout, binding.name)
            val isEmailValid = isInputEmpty(binding.emailInputLayout, binding.email)
            val isPasswordValid = isInputEmpty(binding.passwordInputLayout, binding.password)
            if (!isNameValid && !isEmailValid && !isPasswordValid) {
                registerUser()
            }

        }
    }

    private fun registerUser() {
        val name: String = binding.name.text.toString().trim { it <= ' ' }
        val email: String = binding.email.text.toString().trim { it <= ' ' }
        val password: String = binding.password.text.toString()

        showProgressDialog()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val regEmail = user?.email
                    Toast.makeText(
                        this@SignUpActivity,
                        "$name, you have successfully singed up the email address $regEmail",
                        Toast.LENGTH_LONG
                    ).show()
                    auth.signOut()
                    finish()
                } else {
                    Toast.makeText(
                        this@SignUpActivity,
                        task.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}