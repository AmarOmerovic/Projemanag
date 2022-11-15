package com.amaromerovic.projemanag.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivityTaskListBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.taskListToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.taskListToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })

        val boardDocumentID: String? = intent.getStringExtra(Constants.DOCUMENT_ID)
        if (boardDocumentID?.isNotEmpty() == true) {
            showProgressDialog()
            FirestoreHandler().getBoardDetails(this, boardDocumentID)
        }

    }

    fun boardDetails(board: Board) {
        hideProgressDialog()
        binding.taskToolbarText.text = board.name
    }
}