package com.amaromerovic.projemanag.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.amaromerovic.projemanag.R
import com.amaromerovic.projemanag.adapter.TaskListAdapter
import com.amaromerovic.projemanag.databinding.ActivityTaskListBinding
import com.amaromerovic.projemanag.firebase.FirestoreHandler
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.Card
import com.amaromerovic.projemanag.models.Task
import com.amaromerovic.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var boardDetails: Board

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

        boardDetails = board
        binding.taskToolbarText.text = board.name

        val taskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)
//        board.taskList.add(taskList)


        binding.recyclerViewTaskList.layoutManager =
            GridLayoutManager(this@TaskListActivity, 2, GridLayoutManager.HORIZONTAL, false)
        binding.recyclerViewTaskList.setHasFixedSize(false)
        val adapter = TaskListAdapter(this@TaskListActivity, board.taskList)
        binding.recyclerViewTaskList.adapter = adapter
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog()
        FirestoreHandler().getBoardDetails(this@TaskListActivity, boardDetails.documentID)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreHandler().getCurrentUserUID())
        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, boardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, boardDetails)
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, boardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreHandler().getCurrentUserUID())

        val card = Card(cardName, FirestoreHandler().getCurrentUserUID(), cardAssignedUsersList)

        val cardsList = boardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardsList
        )

        boardDetails.taskList[position] = task

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(this@TaskListActivity, boardDetails)
    }
}