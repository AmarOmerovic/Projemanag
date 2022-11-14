package com.amaromerovic.projemanag.firebase

import android.widget.Toast
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

    fun signInUser(activity: SignInActivity) {
        fireStore.collection(Constants.USERS_COLLECTION_KEY)
            .document(getCurrentUserUID())
            .get()
            .addOnSuccessListener {
                val loggedInUser = it.toObject(User::class.java)
                if (loggedInUser != null) {
                    activity.signInSuccess(loggedInUser)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    activity,
                    "Something went wrong getting the user from the collection",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}