package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ProfilePhotoManager {
    fun savePhotoFromUri(context: Context, sourceUri: Uri, userId: String): String? {
        return try {
            val profileDir = File(context.filesDir, "profile_photos").apply { mkdirs() }
            val destFile = File(profileDir, "profile_${userId.ifBlank { "user" }}_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e("ProfilePhotoManager", "Failed to save photo from URI", e)
            null
        }
    }

    fun savePhotoFromBitmap(context: Context, bitmap: Bitmap, userId: String): String? {
        return try {
            val profileDir = File(context.filesDir, "profile_photos").apply { mkdirs() }
            val destFile = File(profileDir, "profile_${userId.ifBlank { "user" }}_${System.currentTimeMillis()}.jpg")

            FileOutputStream(destFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e("ProfilePhotoManager", "Failed to save photo from Bitmap", e)
            null
        }
    }
}
