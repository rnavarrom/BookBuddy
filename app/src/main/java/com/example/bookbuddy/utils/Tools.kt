package com.example.bookbuddy.utils

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.utils.Constants.Companion.profileRequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer


class Tools {
    companion object {

        // Check if email is valid
        fun isEmailValid(email: String): Boolean {
            val regex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            return regex.matches(email)
        }

        // Check if password is valid
        fun isPasswordValid(password: String): Boolean {
            val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
            return regex.matches(password)
        }

        // Generates a valid password, used in the function recover password
        fun generateRandomPassword(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            while (true) {
                val password = (1..length)
                    .map { allowedChars.random() }
                    .joinToString("")

                if (isPasswordValid(password)) {
                    return password
                }
            }
        }

        // Check if text has no special characters
        fun isValidText(text: String): Boolean {
            val regex = Regex("[^a-zA-Z0-9]")
            return !regex.containsMatchIn(text)
        }

        // Shows the content of a editext password
        fun tooglePasswordVisible(editText: EditText){
            if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        // Sets a toolbar on dialog fragments
        fun setToolBar(dialogFragment: DialogFragment, toolbar: Toolbar, context: Context, title: String){
            toolbar.title = title
            toolbar.setNavigationIcon(R.drawable.ic_back_arrow)
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_green))
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
            toolbar.setNavigationOnClickListener(View.OnClickListener { dialogFragment.dismiss() })
        }

        // Clears all cache of images except your profile pic
        fun clearCache(context: Context){
            val cacheDir = context.cacheDir
            val files = cacheDir.listFiles()

            if (files != null) {
                for (file in files) {
                    if (file != currentPicture) {
                        file.delete()
                    }
                }
            }
        }

        // Updates the username and the profile pic on the navigation drawer menu
        fun setNavigationProfile(context: Context, image: File?, username: String?){
            var hView: View = navView.getHeaderView(0)
            if (username != null){
                var profileName: TextView = hView.findViewById(R.id.profile_name)
                profileName.text = username
            }

            if (image != null){
                if (currentUser!!.haspicture){
                    var profileImg: ShapeableImageView = hView.findViewById(R.id.profile_imageView)

                    Glide.with(context)
                        .setDefaultRequestOptions(profileRequestOptions)
                        .load(BitmapFactory.decodeFile(image.absolutePath))
                        .into(profileImg)
                }
            }
        }

        // Given arespones of the bytes of an image, generates a file and set it to the currentPicture
        fun responseToFile(context: Context, response: ResponseBody? ){
            val body = response
            val bytes = body!!.bytes()
            context.cacheDir.deleteRecursively()
            val file = File(context.cacheDir, currentUser!!.userId.toString() + "user.jpg")
            val outputStream = FileOutputStream(file)
            outputStream.write(bytes)
            outputStream.close()
            currentPicture = file
        }



        fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                if (cursor != null) cursor.close()
            }
            return null
        }

        // Shows information on the bottom part of the application
        fun showSnackBar(context: Context, view: View, text: String){
            val snackbar = Snackbar.make(view, text,
                Snackbar.LENGTH_LONG)
            snackbar.setAction("OK"){
                snackbar.dismiss()
            }
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_green))
            val textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.WHITE)
            textView.textSize = 14f
            snackbar.show()
        }

        private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        fun CharSequence.unaccent(): String {
            val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
            return REGEX_UNACCENT.replace(temp, "")
        }
    }
}

