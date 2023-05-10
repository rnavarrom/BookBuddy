package com.example.bookbuddy.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.Normalizer


class Tools {
    companion object {
        fun isEmailValid(email: String): Boolean {
            val regex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            return regex.matches(email)
        }

        fun isPasswordValid(password: String): Boolean {
            val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
            return regex.matches(password)
        }

        fun isValidText(text: String): Boolean{
            val regex = Regex("[a-zA-Z0-9._-]")
            return regex.matches(text)
        }

        fun isNameAviable(userName: String): Boolean {
            var response = false
            runBlocking {
                val crudApi = CrudApi()
                val corrutina = launch {
                    response = crudApi.getUserExists(userName)
                }
                corrutina.join()
            }
            return response
        }

        fun isEmailAviable(email: String): Boolean{
            var response = false
            runBlocking {
                val crudApi = CrudApi()
                val corrutina = launch {
                    response = crudApi.getEmailExists(email)
                }
                corrutina.join()
            }
            return response
        }

        fun tooglePasswordVisible(editText: EditText){
            if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                //eyeToggle.setImageResource(R.drawable.ic_eye_visible)
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                //eyeToggle.setImageResource(R.drawable.ic_eye_hidden)
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        fun setToolBar(dialogFragment: DialogFragment, toolbar: Toolbar, activity: AppCompatActivity, title: String){
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            toolbar.setBackgroundColor(ContextCompat.getColor(dialogFragment.requireContext(), R.color.primary_green))
            toolbar.setTitleTextColor(ContextCompat.getColor(dialogFragment.requireContext(), R.color.white))
            toolbar.setNavigationOnClickListener(View.OnClickListener { dialogFragment.dismiss() })
            toolbar.title = title
        }

        fun getPathFromUri(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.getLastPathSegment() else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
                return uri.getPath()
            }
            return null
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
                cursor = context.getContentResolver().query(
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


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.getAuthority()
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.getAuthority()
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.getAuthority()
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.getAuthority()
        }

        private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        fun CharSequence.unaccent(): String {
            val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
            return REGEX_UNACCENT.replace(temp, "")
        }
    }
}

