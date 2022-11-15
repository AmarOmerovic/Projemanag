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
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentID: String) {
        fireStore.collection(Constants.BOARDS_COLLECTION_KEY)
            .document(documentID)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)
                if (board != null) {
                    activity.boardDetails(board)
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }
}