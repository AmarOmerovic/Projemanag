package com.amaromerovic.projemanag.utils

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap

object Constants {
    const val DURATION_KEY = "Duration"
    const val USERS_COLLECTION_KEY = "Users"
    const val USER_NAME = "name"
    const val USER_IMAGE = "image"
    const val USER_MOBILE = "mobile"
    const val BOARDS_COLLECTION_KEY = "Boards"
    const val ASSIGNED_TO = "assignedTo"


    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}