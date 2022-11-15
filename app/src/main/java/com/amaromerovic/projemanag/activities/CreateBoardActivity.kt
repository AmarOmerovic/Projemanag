package com.amaromerovic.projemanag.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivityCreateBoardBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private var userName: String = ""
    private var boardImageURL: String = ""
    private var selectedFileURI: Uri? = null

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
                    selectedFileURI = uri
                    this.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    Glide
                        .with(this)
                        .load(uri)
                        .fitCenter()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(binding.circularImage)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra(Constants.USER_NAME).toString()

        setSupportActionBar(binding.createBoardToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.createBoardToolbar.setNavigationOnClickListener {
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
                showRationalDialogForPermissions(this@CreateBoardActivity, "Files and media")
            } else {
                readStoragePerm.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }


        binding.create.setOnClickListener {

            if (selectedFileURI != null) {
                uploadBoardImage()
            } else {
                showProgressDialog()
                createBoard()
            }

        }
    }

    private fun createBoard() {
        val isBoardNameValid = isInputEmpty(binding.boardNameInputLayout, binding.boardName)
        if (!isBoardNameValid) {
            val assignedUsersArrayList: ArrayList<String> = ArrayList()
            assignedUsersArrayList.add(getCurrentUserID())

            val board = Board(
                binding.boardName.text.toString(),
                boardImageURL,
                userName,
                assignedUsersArrayList
            )
            FirestoreHandler().createBoard(this@CreateBoardActivity, board)
        } else {
            hideProgressDialog()
        }
    }

    private fun uploadBoardImage() {
        showProgressDialog()
        val storage = FirebaseStorage.getInstance()
            .reference
            .child(
                "Projemanag_board_image" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this@CreateBoardActivity,
                    selectedFileURI
                )
            )
        storage.putFile(selectedFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    boardImageURL = uri.toString()
                    createBoard()
                }
            }.addOnFailureListener {
                Toast.makeText(this@CreateBoardActivity, it.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }

    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    private fun openStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        getImageFromStorage.launch(intent)
    }
}