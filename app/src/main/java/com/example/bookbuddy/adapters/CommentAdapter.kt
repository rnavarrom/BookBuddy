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
import com.example.bookbuddy.Utils.Constants.Companion.profileRequestOptions
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.ui.navdrawer.BookCommentsFragmentDirections
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext


class CommentAdapter(var list: java.util.ArrayList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>(), CoroutineScope, ApiErrorListener {
    private var job: Job = Job()
    private val api = CrudApi( this@CommentAdapter)
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val profilePicture = view.findViewById<ImageView>(R.id.profile_imageView)!!
        val username = view.findViewById<TextView>(R.id.tv_name)!!
        val date = view.findViewById<TextView>(R.id.tv_date)!!
        val rating = view.findViewById<RatingBar>(R.id.book_rating_display)!!
        val comment = view.findViewById<TextView>(R.id.tv_comment)!!
        val dropmenu = view.findViewById<ImageButton>(R.id.drop_menu)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        val vh: ViewHolder = when (viewType) {
            1 -> ViewHolder(layout.inflate(R.layout.cardview_comment_delete, parent, false))
            else -> ViewHolder(layout.inflate(R.layout.cardview_comment, parent, false))
        }
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.profilePicture.setImageResource()

        if(list[position].user!!.haspicture){
            runBlocking {

                val corrutina = launch {
                    if (list[position].user!!.haspicture){
                        val commentPicture = api.getUserImage(list[position].user!!.userId)
                        val body = commentPicture //.body()
                        if (body != null) {
                            // Leer los bytes de la imagen
                            val bytes = body.bytes()

                            // Guardar los bytes en un archivo
                            val file = File(context.cacheDir, list[position].user!!.userId.toString() + "user.jpg")
                            withContext(Dispatchers.IO) {
                                val outputStream = FileOutputStream(file)
                                outputStream.write(bytes)
                                outputStream.close()
                            }
                            // Mostrar la imagen en un ImageView usando Glide
                            Glide.with(context)
                                .setDefaultRequestOptions(profileRequestOptions)
                                .load(BitmapFactory.decodeFile(file.absolutePath))
                                .into(holder.profilePicture)
                        }
                    }
                }
                corrutina.join()
            }
        }

        holder.username.text = list[position].user!!.name
        holder.date.text = list[position].fecha.toString()
        holder.rating.rating = list[position].rating.toFloat()
        holder.comment.text = list[position].comentText

        holder.profilePicture.setOnClickListener {
            goToUserProfile(list[position].user!!.userId, list[position].user!!.name)
        }

        holder.username.setOnClickListener{
            goToUserProfile(list[position].user!!.userId, list[position].user!!.name)
        }

        if (holder.dropmenu != null){
            holder.dropmenu.setOnClickListener {
                val popup = PopupMenu(context, holder.dropmenu)
                popup.menuInflater
                    .inflate(R.menu.comment_menu, popup.menu)
                popup.setOnDismissListener {
                    holder.dropmenu.setImageResource(R.drawable.ic_drop_down_menu)
                }
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.delete_comment -> {
                            runBlocking {
                                val corroutine = launch {
                                    api.deleteCommentToAPI(list[position].comentId!!)
                                }
                                corroutine.join()
                                list.removeAt(position)
                                notifyDataSetChanged()
                            }
                            true
                        }
                        else -> false
                    }
                }
                holder.dropmenu.setImageResource(R.drawable.ic_drop_up_menu)
                popup.show()
            }
        }
    }

    private fun goToUserProfile(userid: Int, username: String){
        val bundle = Bundle()
        bundle.putInt("userid", userid)
        bundle.putString("username", username)
        val action = BookCommentsFragmentDirections.actionNavReadCommentToNavProfileDialog(bundle)
        navController.navigate(action)
    }

    fun updateList(newList: ArrayList<Comment>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = list[position].typeCardview

    override fun getItemCount(): Int {
        return list.size
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onApiError() {
        Toast.makeText(context,"Aviso error", Toast.LENGTH_LONG).show()
    }
}
