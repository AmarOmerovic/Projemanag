package com.amaromerovic.projemanag.utils

import android.app.Activity
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


    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}