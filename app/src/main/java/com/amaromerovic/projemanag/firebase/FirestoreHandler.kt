package com.amaromerovic.projemanag.firebase

import android.app.Activity
import android.widget.Toast
import com.amaromerovic.projemanag.activities.MainActivity
import com.amaromerovic.projemanag.activities.ProfileActivity
import com.amaromerovic.projemanag.activities.SignInActivity
import com.amaromerovic.projemanag.activities.SignUpActivity
import com.amaromerovic.projemanag.activities.utils.Constants
import com.amaromerovic.projemanag.model.User
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

    fun loadUserData(activity: Activity) {
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
                            activity.updateNavigationUserDetails(loggedInUser)
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
}