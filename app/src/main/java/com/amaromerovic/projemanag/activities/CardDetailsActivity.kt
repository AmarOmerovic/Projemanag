package com.amaromerovic.projemanag.activities

import android.app.DatePickerDialog
import android.content.res.Resources
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.adapter.CardMemberListAdapter
import com.amaromerovic.projemanag.databinding.ActivityCardDetailsBinding
import com.amaromerovic.projemanag.dialogs.MembersListDialog
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.*
import com.amaromerovic.projemanag.utils.Constants
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import java.text.SimpleDateFormat
import java.util.*


class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var boardDetails: Board
    private var taskListPosition = -1
    private var cardListPosition = -1
    private var labelColor = ""
    private lateinit var assignedMembersDetailsList: ArrayList<User>
    private var selectedDueDateMilliseconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()

        setSupportActionBar(binding.cardDetailSToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.cardDetailSToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })

        labelColor = boardDetails.taskList[taskListPosition].cards[cardListPosition].labelColor
        if (labelColor.isNotEmpty()) {
            binding.selectLabelColor.text = ""
            binding.selectLabelColor.setBackgroundColor(Color.parseColor(labelColor))
        }

        binding.title.text = boardDetails.taskList[taskListPosition].cards[cardListPosition].name
        binding.nameCardDetails.setText(boardDetails.taskList[taskListPosition].cards[cardListPosition].name)



        binding.update.setOnClickListener {
            val isValidCardName =
                isInputEmpty(binding.nameLayoutCardDetails, binding.nameCardDetails)

            if (!isValidCardName) {
                updateCardDetails()
            }
        }

        binding.selectLabelColor.setOnClickListener {
            MaterialColorPickerDialog
                .Builder(this)
                .setTitle("Select Label Color")
                .setDefaultColor(R.color.teal_700)
                .setColors(resources.getStringArray(R.array.themeColorHex))
                .setColorShape(ColorShape.SQAURE)
                .setColorListener { color, colorHex ->
                    labelColor = colorHex
                    binding.selectLabelColor.text = ""
                    binding.selectLabelColor.setBackgroundColor(color)
                }
                .show()
        }


        binding.selectMembers.setOnClickListener {
            membersListDialog()
        }

        selectedDueDateMilliseconds =
            boardDetails.taskList[taskListPosition].cards[cardListPosition].dueDate

        if (selectedDueDateMilliseconds > 0) {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val selectedDate = sdf.format(Date(selectedDueDateMilliseconds))

            binding.selectDueDate.text = selectedDate
        }

        binding.selectDueDate.setOnClickListener {
            showDatePicker()
        }

        setUpSelectedMembersList()
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = boardDetails.taskList[taskListPosition].cards
        cardsList.removeAt(cardListPosition)

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[taskListPosition].cards = cardsList

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@CardDetailsActivity, boardDetails)

    }

    private fun updateCardDetails() {

        val card = Card(
            binding.nameCardDetails.text.toString(),
            boardDetails.taskList[taskListPosition].cards[cardListPosition].createdBy,
            boardDetails.taskList[taskListPosition].cards[cardListPosition].assignedTo,
            labelColor,
            selectedDueDateMilliseconds
        )

        boardDetails.taskList[taskListPosition].cards[cardListPosition] = card

        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun alertDialogForDeleteList(cardName: String) {
        val builder = AlertDialog.Builder(this@CardDetailsActivity)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $cardName?")
        builder.setIcon(R.drawable.alert)
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            deleteCard()
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteCard -> alertDialogForDeleteList(boardDetails.taskList[taskListPosition].cards[cardListPosition].name)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
            } else {
                intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            }
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            cardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            taskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {

            assignedMembersDetailsList = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST, User::class.java)!!
            } else {
                intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
            }
        }
    }

    private fun membersListDialog() {
        val cardAssignedMembersList =
            boardDetails.taskList[taskListPosition].cards[cardListPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in assignedMembersDetailsList.indices) {
                for (j in cardAssignedMembersList) {
                    if (assignedMembersDetailsList[i].id == j) {
                        assignedMembersDetailsList[i].selected = true
                    }
                }
            }
        } else {
            for (i in assignedMembersDetailsList.indices) {
                assignedMembersDetailsList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            assignedMembersDetailsList,
            "Select Member"
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!boardDetails.taskList[taskListPosition].cards[cardListPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        user.id?.let {
                            boardDetails.taskList[taskListPosition].cards[cardListPosition].assignedTo.add(
                                it
                            )
                        }
                    }
                    setUpSelectedMembersList()
                } else if (action == Constants.UN_SELECT) {
                    boardDetails.taskList[taskListPosition].cards[cardListPosition].assignedTo.remove(
                        user.id
                    )

                    for (i in assignedMembersDetailsList.indices) {
                        if (assignedMembersDetailsList[i].id == user.id) {
                            assignedMembersDetailsList[i].selected = false
                        }
                    }

                    setUpSelectedMembersList()
                }
            }
        }
        listDialog.show()

        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(listDialog.window!!.attributes)
        layoutParams.width = (width * 1f).toInt()
        listDialog.window!!.attributes = layoutParams
    }

    private fun setUpSelectedMembersList() {
        val cardAssignedMembersList =
            boardDetails.taskList[taskListPosition].cards[cardListPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in assignedMembersDetailsList.indices) {
            for (j in cardAssignedMembersList) {
                if (assignedMembersDetailsList[i].id == j) {
                    val selectedMember = assignedMembersDetailsList[i].id?.let {
                        assignedMembersDetailsList[i].image?.let { it1 ->
                            SelectedMembers(
                                it,
                                it1
                            )
                        }
                    }

                    if (selectedMember != null) {
                        selectedMembersList.add(selectedMember)
                    }
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            binding.selectMembers.visibility = View.GONE
            binding.selectedMembersList.visibility = View.VISIBLE

            binding.selectedMembersList.layoutManager = GridLayoutManager(this, 6)
            val adapter = CardMemberListAdapter(this, selectedMembersList, true)
            binding.selectedMembersList.setHasFixedSize(true)
            binding.selectedMembersList.adapter = adapter

            adapter.onCardMemberClick(object : CardMemberListAdapter.OnCardMemberClickListener {
                override fun onCardMemberClick() {
                    membersListDialog()
                }
            })
        } else {
            binding.selectMembers.visibility = View.VISIBLE
            binding.selectedMembersList.visibility = View.GONE
        }

    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()


        val dateSetListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                selectedDueDateMilliseconds = calendar.timeInMillis

                binding.selectDueDate.text =
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                        calendar.time
                    ).toString()
            }

        DatePickerDialog(
            this@CardDetailsActivity,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


}