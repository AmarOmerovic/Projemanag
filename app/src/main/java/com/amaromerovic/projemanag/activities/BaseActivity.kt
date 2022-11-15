package com.amaromerovic.projemanag.activities

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivityBaseBinding
import com.amaromerovic.projemanag.databinding.ProgressDialogBinding
import com.google.android.material.textfield.TextInputLayout

open class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding
    private var doubleBackToExitPressedOnce = false

    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    fun noInternetConnectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No internet connection!")
        builder.setMessage("Please check you internet connection before using the app. Bad or no connection at all can cause major problems.")
        builder.setIcon(R.drawable.alert)
        builder.setPositiveButton("I understand") { dialogInterface, _ ->
            dialogInterface.dismiss()
            finish()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
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


    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            finish()
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

    fun showRationalDialogForPermissions(context: Context, permissionName: String) {
        AlertDialog.Builder(context)
            .setTitle("Open Settings")
            .setMessage("It looks like you have turned off the required permission for this feature. It can be enabled under the Applications Settings/Permissions/$permissionName.")
            .setPositiveButton("Go to settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.show()
    }
}