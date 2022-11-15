package com.amaromerovic.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.adapter.TaskListAdapter
import com.amaromerovic.projemanag.databinding.ActivityTaskListBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.Card
import com.amaromerovic.projemanag.models.Task
import com.amaromerovic.projemanag.models.User
import com.amaromerovic.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var myBoardDetails: Board
    private lateinit var boardDocumentID: String
    lateinit var assignedMemberDetailList: ArrayList<User>

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

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentID = intent.getStringExtra(Constants.DOCUMENT_ID)!!
            showProgressDialog()
            FirestoreHandler().getBoardDetails(this, boardDocumentID)
        }

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(
                    this@TaskListActivity,
                    R.color.darkBlue
                ),
                ContextCompat.getColor(this@TaskListActivity, R.color.darkBlue),
                ContextCompat.getColor(this@TaskListActivity, R.color.darkBlue)
            )
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
            }
            showProgressDialog()
            FirestoreHandler().getBoardDetails(this@TaskListActivity, boardDocumentID)
        }
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        myBoardDetails.taskList.removeAt(myBoardDetails.taskList.size - 1)

        myBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, myBoardDetails)
    }

    fun boardMembersDetailsList(list: ArrayList<User>) {
        assignedMemberDetailList = list
        hideProgressDialog()

        val taskList = Task(resources.getString(R.string.add_list))
        myBoardDetails.taskList.add(taskList)

        binding.recyclerViewTaskList.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewTaskList.setHasFixedSize(false)
        val adapter = TaskListAdapter(this@TaskListActivity, myBoardDetails.taskList)
        binding.recyclerViewTaskList.adapter = adapter

    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, myBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, assignedMemberDetailList)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.members_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionMembers -> {
                val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, myBoardDetails)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        return true
    }

    fun boardDetails(board: Board) {
        hideProgressDialog()

        myBoardDetails = board
        binding.taskToolbarText.text = board.name



        showProgressDialog()
        FirestoreHandler().getAssignedMembers(this@TaskListActivity, myBoardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog()
        FirestoreHandler().getBoardDetails(this@TaskListActivity, myBoardDetails.documentID)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreHandler().getCurrentUserUID())
        myBoardDetails.taskList.add(0, task)
        myBoardDetails.taskList.removeAt(myBoardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, myBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy, model.cards)
        myBoardDetails.taskList[position] = task
        myBoardDetails.taskList.removeAt(myBoardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, myBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        myBoardDetails.taskList.removeAt(position)
        myBoardDetails.taskList.removeAt(myBoardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, myBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        myBoardDetails.taskList.removeAt(myBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreHandler().getCurrentUserUID())

        val card = Card(cardName, FirestoreHandler().getCurrentUserUID(), cardAssignedUsersList)

        val cardsList = myBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            myBoardDetails.taskList[position].title,
            myBoardDetails.taskList[position].createdBy,
            cardsList
        )

        myBoardDetails.taskList[position] = task

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, myBoardDetails)
    }

    override fun onResume() {
        super.onResume()
        if (::myBoardDetails.isInitialized) {
            showProgressDialog()
            FirestoreHandler().getBoardDetails(this@TaskListActivity, boardDocumentID)
        }
    }
}