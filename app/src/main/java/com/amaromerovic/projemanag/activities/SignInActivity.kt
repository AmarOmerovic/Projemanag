package com.amaromerovic.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.activities.utils.Constants
import com.amaromerovic.projemanag.databinding.ActivitySignInBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.model.User
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@SignInActivity, StartActivity::class.java)
                intent.putExtra(Constants.DURATION_KEY, 0L)
                startActivity(intent)
                finish()
            }
        })

        auth = FirebaseAuth.getInstance()

        binding.signUp.setOnClickListener {
            val isEmailValid = isInputEmpty(binding.emailInputLayout, binding.email)
            val isPasswordValid = isInputEmpty(binding.passwordInputLayout, binding.password)

            if (!isEmailValid && !isPasswordValid) {
                signInUser()
            }
        }
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun signInUser() {
        val email: String = binding.email.text.toString().trim { it <= ' ' }
        val password: String = binding.password.text.toString()

        showProgressDialog()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirestoreHandler().signInUser(this@SignInActivity)
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
