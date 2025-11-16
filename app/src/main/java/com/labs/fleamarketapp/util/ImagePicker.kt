package com.labs.fleamarketapp.util

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

object ImagePicker {
    fun Fragment.registerMultiImagePicker(onResult: (List<Uri>) -> Unit): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            onResult(uris ?: emptyList())
        }
    }

    fun Fragment.registerImagePicker(onResult: (Uri?) -> Unit): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent(), onResult)
    }

    fun ComponentActivity.registerImagePicker(onResult: (Uri?) -> Unit): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent(), onResult)
    }
}

