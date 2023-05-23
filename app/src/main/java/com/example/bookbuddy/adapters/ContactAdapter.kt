package com.example.bookbuddy.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.ui.navdrawer.BookCommentsFragmentDirections
import com.example.bookbuddy.ui.navdrawer.ContactsFragmentDirections
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext


class ContactAdapter(var list: java.util.ArrayList<UserItem>) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>(), CoroutineScope, ApiErrorListener {
    private var job: Job = Job()
    lateinit var view : View
    val api = CrudApi(this@ContactAdapter)
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val profilePicture = view.findViewById<ShapeableImageView>(R.id.profile_imageView)
        val username = view.findViewById<TextView>(R.id.tv_name)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        view = parent
        return ViewHolder(layout.inflate(R.layout.cardview_contact, parent, false))!!         
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.username.text = list[position].name

        holder.view.setOnClickListener {
            goToUserProfile(list[position].userId, list[position].name)
        }

        if(list[position].haspicture){
            runBlocking {
                val corrutina = launch {
                    if (list[position].haspicture){
                        var commentPicture = api.getUserImage(list[position].userId)
                        val body = commentPicture //.body()
                        if (body != null) {
                            // Leer los bytes de la imagen
                            val bytes = body.bytes()

                            // Guardar los bytes en un archivo
                            val file = File(context.cacheDir, list[position].userId.toString() + "user.jpg")
                            val outputStream = FileOutputStream(file)
                            outputStream.write(bytes)
                            outputStream.close()

                            // Mostrar la imagen en un ImageView usando Glide
                            Glide.with(context)
                                .load(BitmapFactory.decodeFile(file.absolutePath))
                                .into(holder.profilePicture)
                        }
                    }
                }
                corrutina.join()
            }
        }
    }

    fun goToUserProfile(userid: Int, username: String){
        val bundle = Bundle()
        bundle.putInt("userid", userid)
        bundle.putString("username", username)
        var action = ContactsFragmentDirections.actionNavContactsToNavProfileDialog(bundle)
        navController.navigate(action)
    }

    fun updateList(newList: ArrayList<UserItem>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onApiError() {
        Tools.showSnackBar(context, view, Constants.ErrrorMessage)
        Toast.makeText(context,"Aviso error", Toast.LENGTH_LONG).show()
    }
}
