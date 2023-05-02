package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class CommentAdapter(var list: java.util.ArrayList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.viewholder>(), CoroutineScope {
    private var job: Job = Job()
    class viewholder(val view: View) : RecyclerView.ViewHolder(view) {
        val profilePicture = view.findViewById<ImageView>(R.id.profile_imageView)
        val username = view.findViewById<TextView>(R.id.tv_name)
        val date = view.findViewById<TextView>(R.id.tv_date)
        val rating = view.findViewById<RatingBar>(R.id.book_rating_display)
        val comment = view.findViewById<TextView>(R.id.tv_comment)
        val dropmenu = view.findViewById<ImageButton>(R.id.drop_menu)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        var vh : CommentAdapter.viewholder? = null
        when (viewType) {
            1 -> vh = viewholder(layout.inflate(R.layout.cardview_comment_delete, parent, false))
            else -> vh = viewholder(layout.inflate(R.layout.cardview_comment, parent, false))
        }
        return vh!!
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        //holder.profilePicture.setImageResource()
        holder.username.text = list[position].user!!.name
        holder.date.text = list[position].fecha.toString()
        holder.rating.rating = list[position].rating.toFloat()
        holder.comment.text = list[position].comentText

        holder.profilePicture.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("userid", list[position].user!!.userId)
            bundle.putString("username", list[position].user!!.name)
            //bundle.putString("profilepicture", list[position].user!!.profilePicture.toString())
            navController.navigate(R.id.nav_profile, bundle)
        }

        holder.username.setOnClickListener{
            val bundle = Bundle()
            bundle.putInt("userid", list[position].user!!.userId)
            bundle.putString("username", list[position].user!!.name)
            //bundle.putString("profilepicture", list[position].user!!.profilePicture.toString())
            navController.navigate(R.id.nav_profile, bundle)
        }

        if (holder.dropmenu != null){
            holder.dropmenu.setOnClickListener {
                val popup = PopupMenu(context, holder.dropmenu)
                popup.getMenuInflater()
                    .inflate(com.example.bookbuddy.R.menu.comment_menu, popup.getMenu())
                popup.setOnDismissListener {
                    holder.dropmenu.setImageResource(R.drawable.ic_drop_down_menu)
                }
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.delete_comment -> {
                            runBlocking {
                                val crudApi = CrudApi()
                                val corroutine = launch {
                                    crudApi.deleteCommentToAPI(list[position].comentId!!)
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
}
