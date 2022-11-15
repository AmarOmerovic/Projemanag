package com.amaromerovic.projemanag.activities

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.adapter.MemberListAdapter
import com.amaromerovic.projemanag.databinding.ActivityMembersBinding
import com.amaromerovic.projemanag.databinding.MemberSearchDialogBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.User
import com.amaromerovic.projemanag.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

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
        user.id?.let {
            if (boardDetails.assignedTo.contains(it)) {
                hideProgressDialog()
                Toast.makeText(
                    this@MembersActivity,
                    "The user is already a member of this board!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                boardDetails.assignedTo.add(it)
                FirestoreHandler().assignMemberToBoard(this, boardDetails, user)
            }
        }
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        assignedMembersList.add(user)
        setUpMembersList(assignedMembersList)

        user.fcmToken?.let { SendNotificationToUser(boardDetails.name, it).startApiCall() }
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
                dialog.dismiss()
            }
        }

        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private inner class SendNotificationToUser(val boardName: String, val token: String) {

        fun startApiCall() {
            showProgressDialog()
            lifecycleScope.launch(Dispatchers.IO) {
                makeApiCall()
            }
            afterCallFinish()
        }

        private fun makeApiCall(): String {
            var result: String
            var connection: HttpURLConnection? = null

            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false

                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the Board $boardName by ${assignedMembersList[0].name}"
                )

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)


                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

                val httpResult: Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder: StringBuilder = StringBuilder()
                    var line: String?

                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }
            return result
        }


        private fun afterCallFinish() {
            hideProgressDialog()
        }
    }

}