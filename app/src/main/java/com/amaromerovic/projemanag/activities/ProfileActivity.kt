package com.amaromerovic.projemanag.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivityProfileBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.model.User
import com.bumptech.glide.Glide

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding

    private val readStoragePerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openStorage()
            }
        }

    private val getImageFromStorage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                val uri = intent?.data
                if (uri != null) {
                    this.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Glide
                        .with(this)
                        .load(uri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(binding.circularImage);
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirestoreHandler().loadUserData(this@ProfileActivity)

        setSupportActionBar(binding.myProfileToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.myProfileToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })

        binding.circularImage.setOnClickListener {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showRationalDialogForPermissions("Files and media")
            } else {
                readStoragePerm.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        binding.update.setOnClickListener {
            val isNameValid = isInputEmpty(binding.nameInputLayout, binding.name)
            val isPasswordValid = isInputEmpty(binding.mobileInputLayout, binding.mobile)

            if (!isNameValid && !isPasswordValid) {

            }
        }

    }

    private fun showRationalDialogForPermissions(permissionName: String) {
        AlertDialog.Builder(this@ProfileActivity)
            .setTitle("Open Settings")
            .setMessage("It looks like you have turned off the required permission for this feature. It can be enabled under the Applications Settings/Permissions/$permissionName.")
            .setPositiveButton("Go to settings") { _, _ ->
                try {
                    goToSettings()
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.show()
    }

    private fun goToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun openStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        getImageFromStorage.launch(intent)
    }


    fun setUserDataInUI(user: User) {
//        Glide
//            .with(this)
//            .load(user.image)
//            .fitCenter()
//            .placeholder(R.drawable.ic_user_place_holder)
//            .into(binding.circularImage);

        binding.name.setText(user.name.toString())
        binding.email.setText(user.email.toString())
        if (user.mobile!!.isNotEmpty()) {
            binding.mobile.setText(user.mobile.toString())
        }
    }
}