package com.amaromerovic.projemanag.activities

import android.os.Bundle
import android.widget.Toast
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.singUpToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.singUpToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        auth = FirebaseAuth.getInstance()

        binding.signUp.setOnClickListener {
            val isEmailValid = isInputEmpty(binding.emailInputLayout, binding.email)
            val isPasswordValid = isInputEmpty(binding.passwordInputLayout, binding.password)

            if (!isEmailValid && !isPasswordValid) {
                signInUser()
            }
        }
    }


    private fun signInUser() {
        val email: String = binding.email.text.toString().trim { it <= ' ' }
        val password: String = binding.password.text.toString()

        showProgressDialog()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@SignInActivity,
                        "You have successfully singed in!",
                        Toast.LENGTH_LONG
                    ).show()
                    auth.signOut()
                    finish()
                } else {
                    Toast.makeText(
                        this@SignInActivity,
                        task.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

}
