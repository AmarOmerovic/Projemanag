package com.amaromerovic.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.utils.Constants
import com.amaromerovic.projemanag.databinding.ActivitySignUpBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.User
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        })


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

    private fun goBack() {
        val intent = Intent(this@SignUpActivity, StartActivity::class.java)
        intent.putExtra(Constants.DURATION_KEY, 0L)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }


    fun userRegisteredSuccess() {
        Toast.makeText(
            this@SignUpActivity,
            "You have successfully singed up.",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        auth.signOut()
        goBack()
    }

    private fun registerUser() {
        val name: String = binding.name.text.toString().trim { it <= ' ' }
        val email: String = binding.email.text.toString().trim { it <= ' ' }
        val password: String = binding.password.text.toString()

        showProgressDialog()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authUser = auth.currentUser
                    val user = User(authUser?.uid, name, email)
                    FirestoreHandler().registerUser(this@SignUpActivity, user)
                } else {
                    hideProgressDialog()
                    Toast.makeText(
                        this@SignUpActivity,
                        task.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}