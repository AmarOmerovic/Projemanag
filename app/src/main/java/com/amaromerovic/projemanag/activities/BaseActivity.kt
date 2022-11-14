package com.amaromerovic.projemanag.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivityBaseBinding
import com.amaromerovic.projemanag.databinding.ProgressDialogBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding
    private var doubleBackToExitPressedOnce = false

    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    fun showProgressDialog() {
        progressDialog = Dialog(this)
        val dialogBinding = ProgressDialogBinding.inflate(layoutInflater)
        progressDialog.setContentView(dialogBinding.root)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.getOnBackPressedDispatcher().onBackPressed()
            return
        }

        doubleBackToExitPressedOnce = true
        Toast.makeText(
            this@BaseActivity,
            "Please click once again to exit the Application.",
            Toast.LENGTH_LONG
        ).show()

        Handler.createAsync(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    fun showErrorSnackBar(string: String) {
        val snackBar = Snackbar.make(binding.root, string, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this@BaseActivity, R.color.snackbar_error_color))
        snackBar.show()
    }

    fun isInputEmpty(
        textInputLayout: TextInputLayout,
        editText: AppCompatEditText
    ): Boolean {
        return if (editText.text!!.isEmpty()) {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error = "Field can not be empty!"
            true
        } else {
            textInputLayout.isErrorEnabled = false
            false
        }
    }
}