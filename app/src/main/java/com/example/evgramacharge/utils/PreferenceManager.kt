package com.example.evgramacharge.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ev_grama_prefs", Context.MODE_PRIVATE)
    private val context = context.applicationContext

    fun saveTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", isDarkMode).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", true) // Default to true for premium look
    }

    fun saveProfileImageUri(uri: String) {
        sharedPreferences.edit().putString("profile_image_uri", uri).apply()
    }

    fun getProfileImageUri(): String? {
        return sharedPreferences.getString("profile_image_uri", null)
    }

    // New method to save profile image by copying to internal storage
    fun saveProfileImage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val file = File(context.filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val path = file.absolutePath
            sharedPreferences.edit().putString("profile_image_path", path).apply()
            path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getProfileImagePath(): String? {
        return sharedPreferences.getString("profile_image_path", null)
    }

    fun deleteProfileImage() {
        val path = getProfileImagePath()
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            sharedPreferences.edit().remove("profile_image_path").apply()
        }
    }
}
