package com.amaromerovic.projemanag.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.databinding.ActivityCardDetailsBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.Card
import com.amaromerovic.projemanag.models.Task
import com.amaromerovic.projemanag.utils.Constants
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var boardDetails: Board
    private var taskListPosition = -1
    private var cardListPosition = -1
    private var labelColor = ""

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
            boardDetails.taskList[taskListPosition].cards[cardListPosition].assignedTo, labelColor
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
    }

}