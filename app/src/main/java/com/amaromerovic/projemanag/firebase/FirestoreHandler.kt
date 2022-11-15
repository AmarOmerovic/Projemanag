package com.amaromerovic.projemanag.firebase

import android.app.Activity
import android.widget.Toast
import com.amaromerovic.projemanag.activities.*
import com.amaromerovic.projemanag.models.Board
import com.amaromerovic.projemanag.models.User
import com.amaromerovic.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreHandler {
    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        fireStore.collection(Constants.USERS_COLLECTION_KEY)
            .document(getCurrentUserUID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(
                    activity,
                    "Something went wrong adding the user to the collection",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun getCurrentUserUID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserUID = ""
        if (currentUser != null) {
            currentUserUID = currentUser.uid
        }
        return currentUserUID
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        fireStore.collection(Constants.USERS_COLLECTION_KEY)
            .document(getCurrentUserUID())
            .get()
            .addOnSuccessListener {
                val loggedInUser = it.toObject(User::class.java)

                when (activity) {
                    is SignInActivity -> {
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity -> {
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                    }

                    is ProfileActivity -> {
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }


            }
            .addOnFailureListener {
                when (activity) {
                    is SignInActivity -> activity.hideProgressDialog()
                    is MainActivity -> activity.hideProgressDialog()
                }
                Toast.makeText(
                    activity,
                    "Something went wrong getting the user from the collection",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun updateUserProfileData(activity: ProfileActivity, userHashMap: HashMap<String, Any>) {
        fireStore.collection(Constants.USERS_COLLECTION_KEY)
            .document(getCurrentUserUID())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Profile updated successfully.", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Profile update error!", Toast.LENGTH_LONG).show()

            }
    }


    fun createBoard(activity: CreateBoardActivity, board: Board) {
        fireStore.collection(Constants.BOARDS_COLLECTION_KEY)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully!", Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(
                    activity,
                    "There was a problem creating the board!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun getBoardList(activity: MainActivity) {
        fireStore.collection(Constants.BOARDS_COLLECTION_KEY)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserUID())
            .get()
            .addOnSuccessListener { document ->
                val boards = ArrayList<Board>()
                document.forEach {
                    val board = it.toObject(Board::class.java)
                    board.documentID = it.id
                    boards.add(board)
                }

                activity.populateBoardsListToUI(boards)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(
                    activity,
                    "There was a problem displaying the boards!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentID: String) {
        fireStore.collection(Constants.BOARDS_COLLECTION_KEY)
            .document(documentID)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)
                if (board != null) {
                    board.documentID = document.id
                    activity.boardDetails(board)
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(
                    activity,
                    "There was a problem getting the board details!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        fireStore.collection(Constants.BOARDS_COLLECTION_KEY)
            .document(board.documentID)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener {
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Toast.makeText(
                    activity,
                    "There was a problem updating the task!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun getAssignedMembers(activity: Activity, assignedTo: ArrayList<String>) {
        fireStore.collection(Constants.USERS_COLLECTION_KEY)
            .whereIn(Constants.USER_ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    val user = i.toObject(User::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }
                if (activity is MembersActivity) {
                    activity.setUpMembersList(usersList)
                } else if (activity is TaskListActivity) {
                    activity.boardMembersDetailsList(usersList)
                }
            }
            .addOnFailureListener {
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Toast.makeText(
                    activity,
                    "There was a problem loading the members!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        fireStore.collection(Constants.USERS_COLLECTION_KEY)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.size() > 0) {
                    val user = document.documents[0].toObject(User::class.java)
                    if (user != null) {
                        activity.memberDetails(user)
                    }
                } else {
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "No such member found!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error searching the member!", Toast.LENGTH_LONG).show()

            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedTo = HashMap<String, Any>()
        assignedTo[Constants.ASSIGNED_TO] = board.assignedTo
        fireStore.collection(Constants.BOARDS_COLLECTION_KEY)
            .document(board.documentID)
            .update(assignedTo)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                Toast.makeText(
                    activity,
                    "There was a problem assigning the user!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


}