package com.amaromerovic.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.amaromerovic.projemanag.databinding.ActivityStartBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.utils.Constants

class StartActivity : BaseActivity() {
    private lateinit var binding: ActivityStartBinding
    private var isReady = false
    private var duration = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        duration = intent.getLongExtra(Constants.DURATION_KEY, 2000L)
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        Handler.createAsync(Looper.getMainLooper()).postDelayed({
            val currentUserUID = FirestoreHandler().getCurrentUserUID()
            if (currentUserUID.isNotEmpty()) {
                startActivity(Intent(this@StartActivity, MainActivity::class.java))
                finish()
            }
            isReady = true
        }, duration)
        setContentView(binding.root)


        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isReady) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            })


        if (!Constants.isNetworkAvailable(this@StartActivity)) {
            noInternetConnectionDialog()
        }

        binding.signUp.setOnClickListener {
            val intent = Intent(this@StartActivity, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                com.amaromerovic.projemanag.R.anim.slide_in_right,
                com.amaromerovic.projemanag.R.anim.slide_out_left
            )
            finish()
        }

        binding.signIn.setOnClickListener {
            val intent = Intent(this@StartActivity, SignInActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                com.amaromerovic.projemanag.R.anim.slide_in_right,
                com.amaromerovic.projemanag.R.anim.slide_out_left
            )
            finish()
        }
    }
}