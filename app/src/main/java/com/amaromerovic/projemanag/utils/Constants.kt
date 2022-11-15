package com.amaromerovic.projemanag.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.webkit.MimeTypeMap

object Constants {
    const val EMAIL = "email"
    const val DURATION_KEY = "Duration"
    const val USERS_COLLECTION_KEY = "Users"
    const val USER_NAME = "name"
    const val USER_IMAGE = "image"
    const val USER_MOBILE = "mobile"
    const val BOARDS_COLLECTION_KEY = "Boards"
    const val ASSIGNED_TO = "assignedTo"
    const val DOCUMENT_ID = "documentID"
    const val TASK_LIST = "taskList"
    const val BOARD_DETAIL = "board detail"
    const val USER_ID = "id"
    const val TASK_LIST_ITEM_POSITION = "task list item position"
    const val CARD_LIST_ITEM_POSITION = "card list item position"
    const val BOARD_MEMBERS_LIST: String = "Board members list"
    const val SELECT = "Select"
    const val UN_SELECT = "UnSelect"
    const val PROJEMANAG_PREFERENCES = "Projemanag_preferences"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION: String = "authorization"
    const val FCM_KEY: String = "key"
    const val FCM_SERVER_KEY: String = ""
    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"


    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}