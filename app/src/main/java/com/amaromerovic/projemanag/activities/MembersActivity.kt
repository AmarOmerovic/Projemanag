package com.amaromerovic.projemanag.activities

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.adapter.MemberListAdapter
import com.amaromerovic.projemanag.databinding.ActivityMembersBinding
import com.amaromerovic.projemanag.databinding.MemberSearchDialogBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.User
import com.amaromerovic.projemanag.utils.Constants

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var boardDetails: Board
    private lateinit var assignedMembersList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.membersToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.membersToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })


        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
            } else {
                intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            }

            showProgressDialog()
            FirestoreHandler().getAssignedMembers(this@MembersActivity, boardDetails.assignedTo)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_member_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionAddMember -> {
                dialogSearchMember()
            }
        }
        return true
    }

    fun setUpMembersList(list: ArrayList<User>) {
        assignedMembersList = list

        hideProgressDialog()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        val adapter = MemberListAdapter(this@MembersActivity, list)
        binding.recyclerView.adapter = adapter
    }

    fun memberDetails(user: User) {
        user.id?.let { boardDetails.assignedTo.add(it) }
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this@MembersActivity)
        val dialogBinding: MemberSearchDialogBinding =
            MemberSearchDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.add.setOnClickListener {
            val isEmailValid =
                isInputEmpty(dialogBinding.emailSearchInputLayout, dialogBinding.emailSearchMember)

            if (!isEmailValid) {
                showProgressDialog()
                FirestoreHandler().getMemberDetails(
                    this@MembersActivity,
                    dialogBinding.emailSearchMember.text.toString()
                )
            }
        }

        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}