package com.amaromerovic.projemanag.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivityProfileBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.model.User
import com.amaromerovic.projemanag.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var selectedImageURI: Uri? = null
    private var profileImageURL: String = ""
    private lateinit var userDetails: User

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
                    selectedImageURI = uri
                    this.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    Glide
                        .with(this)
                        .load(selectedImageURI)
                        .fitCenter()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(binding.circularImage)
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
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showRationalDialogForPermissions("Files and media")
            } else {
                readStoragePerm.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        binding.update.setOnClickListener {

            if (selectedImageURI != null) {
                uploadUserImage()
            } else {
                showProgressDialog()
                updateUserProfileData()
            }
        }

    }

    private fun updateUserProfileData() {
        val isNameValid = isInputEmpty(binding.nameInputLayout, binding.name)
        val isMobileValid = isInputEmpty(binding.mobileInputLayout, binding.mobile)

        if (!isNameValid && !isMobileValid) {

            val userHashMap = HashMap<String, Any>()

            if (userDetails.image != profileImageURL && profileImageURL.isNotEmpty()) {
                userHashMap[Constants.USER_IMAGE] = profileImageURL
            }

            if (userDetails.name != binding.name.text.toString()) {
                userHashMap[Constants.USER_NAME] = binding.name.text.toString()
            }

            if (userDetails.mobile != binding.mobile.text.toString()) {
                userHashMap[Constants.USER_MOBILE] = binding.mobile.text.toString()
            }

            FirestoreHandler().updateUserProfileData(this@ProfileActivity, userHashMap)
        } else {
            hideProgressDialog()
        }
    }

    private fun uploadUserImage() {
        showProgressDialog()
        if (selectedImageURI != null) {
            val storage = FirebaseStorage.getInstance()
                .reference.child(
                    "Projemanag_user_image" + System.currentTimeMillis() + "." + getFileExtension(
                        selectedImageURI
                    )
                )
            storage.putFile(selectedImageURI!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { url ->
                        profileImageURL = url.toString()
                        updateUserProfileData()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this@ProfileActivity, it.message, Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
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
        userDetails = user
        Glide
            .with(this)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.circularImage)

        binding.name.setText(user.name.toString())
        binding.email.setText(user.email.toString())
        if (user.mobile!!.isNotEmpty()) {
            binding.mobile.setText(user.mobile.toString())
        }
    }
}