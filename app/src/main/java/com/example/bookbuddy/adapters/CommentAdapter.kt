package com.example.bookbuddy.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.example.bookbuddy.Utils.Constants.Companion.profileRequestOptions
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.UserComments.Comment
import com.example.bookbuddy.ui.navdrawer.bookdisplay.CommentsListDialogDirections
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

//Adapter for the comments fragment
class CommentAdapter(var list: java.util.ArrayList<Comment>, val activity: Activity, val title: String) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>(), CoroutineScope, ApiErrorListener {
    private var job: Job = Job()
    lateinit var view : View
    private val api = CrudApi( this@CommentAdapter)
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val profilePicture = view.findViewById<ImageView>(R.id.profile_imageView)!!
        val username = view.findViewById<TextView>(R.id.tv_name)!!
        val date = view.findViewById<TextView>(R.id.tv_date)!!
        val rating = view.findViewById<RatingBar>(R.id.book_rating_display)!!
        val comment = view.findViewById<TextView>(R.id.tv_comment)!!
        val share = view.findViewById<ImageView>(R.id.iv_share)!!
        val dropmenu = view.findViewById<ImageButton>(R.id.drop_menu)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view = parent
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
        val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateString = list[position].fecha.toString()
        val dateTime = LocalDateTime.parse(dateString, inputFormat)
        holder.date.text = dateTime.format(outputFormat)//list[position].fecha.toString()
        holder.rating.rating = list[position].rating.toFloat()
        holder.comment.text = list[position].comentText

        holder.profilePicture.setOnClickListener {
            goToUserProfile(list[position].user!!.userId, list[position].user!!.name)
        }

        holder.share.setOnClickListener {
            val title = title
            val rating = list[position].rating.toString()
            val review = context.getString(R.string.MSG_ShareReviewOf) + title + "\n" +
                    context.getString(R.string.MSG_ShareRating) +  rating +
                    context.getString(R.string.MSG_ShareStars) + "\n" +
                    context.getString(R.string.MSG_ShareText)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, review)
            }
            activity.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.MSG_ShareTitle)))
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
                                val coroutine = launch {
                                    api.deleteCommentToAPI(list[position].comentId!!)
                                }
                                coroutine.join()
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
        val action = CommentsListDialogDirections.actionNavReadCommentToNavProfileDialog(bundle)
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

    override fun onApiError(connectionFailed: Boolean) {
        Tools.showSnackBar(context, view, Constants.ErrrorMessage)
    }
}
