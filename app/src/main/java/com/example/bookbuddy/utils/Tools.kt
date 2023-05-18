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
import com.example.bookbuddy.api.CrudApi
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.Normalizer
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream


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

        fun setToolBar(dialogFragment: DialogFragment, toolbar: Toolbar, context: Context, title: String){
            toolbar.title = title
            toolbar.setNavigationIcon(R.drawable.ic_back_arrow) // Establece un icono para la navegación hacia atrás
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_green)) // Establece el color de fondo de la Toolbar
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white)) // Establece el color del texto del título
            toolbar.setNavigationOnClickListener(View.OnClickListener { dialogFragment.dismiss() })
            /*
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            toolbar.setBackgroundColor(ContextCompat.getColor(dialogFragment.requireContext(), R.color.primary_green))
            toolbar.setTitleTextColor(ContextCompat.getColor(dialogFragment.requireContext(), R.color.white))
            toolbar.setNavigationOnClickListener(View.OnClickListener { dialogFragment.dismiss() })
            toolbar.title = title
            */
        }

        //fun setNavigationProfile(context: Context, image: Bitmap?, username: String?){
        fun setNavigationProfile(context: Context, image: File?, username: String?){
            var hView: View = navView.getHeaderView(0)
            if (username != null){
                var profileName: TextView = hView.findViewById(R.id.profile_name)
                profileName.text = username
            }

            if (image != null){
                if (currentUser.haspicture){
                    var profileImg: ShapeableImageView = hView.findViewById(R.id.profile_imageView)
                    //profileImg.setImageResource(R.drawable.ic_menu_profile)

                    Glide.with(context)
                        .load(BitmapFactory.decodeFile(image.absolutePath))
                        .error(R.drawable.errorimage)
                        .into(profileImg)
                    //profileImg.setImageURI(Uri.fromFile(image))

                    // Mostrar la imagen en un ImageView usando Glide
                    /*
                    profileImg.setImageDrawable(null)
                    println("ESTOOOOOOOOOOOOOOOOOOOOOOO")
                    Glide.with(context)
                        .load(image)
                        .into(profileImg)

                     */
                }
            }
        }

        fun responseToFile(context: Context, response: Response<ResponseBody>){
            val body = response.body()
            // Leer los bytes de la imagen
            val bytes = body!!.bytes()
            context.cacheDir.deleteRecursively()
            val file = File(context.cacheDir, currentUser.userId.toString() + "user.jpg")

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


        fun showSnackBar(context: Context, view: View, text: String){
            //Snackbar(view)
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

