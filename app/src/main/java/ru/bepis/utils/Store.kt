package ru.bepis.utils

import android.graphics.Bitmap
import android.net.Uri

object Store {
    var name: String? = null
    var description: String? = null
    var duration: String? = null
    var image: Bitmap? = null
    var musicIsPicked: Boolean = false
    var timeCodes: MutableList<String> = mutableListOf<String>()


    var audioUri: Uri? = null
    var audioFilename: String? = null
}