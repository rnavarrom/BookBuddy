package com.example.bookbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.models.Genre
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.navController

class CommentAdapter(var list: java.util.ArrayList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.viewholder>() {

    class viewholder(val view: View) : RecyclerView.ViewHolder(view) {
        val profilePicture = view.findViewById<ImageView>(R.id.profile_imageView)
        val username = view.findViewById<TextView>(R.id.tv_name)
        val date = view.findViewById<TextView>(R.id.tv_date)
        val comment = view.findViewById<TextView>(R.id.tv_comment)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return viewholder(layout.inflate(R.layout.cardview_comment, parent, false))
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        //holder.profilePicture.setImageResource()
        holder.username.text = list[position].user!!.name
        holder.date.text = list[position].fecha.toString()
        holder.comment.text = list[position].comentText
    }

    fun updateList(newList: ArrayList<Comment>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
