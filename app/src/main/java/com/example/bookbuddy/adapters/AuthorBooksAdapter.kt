package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.ui.navdrawer.bookdisplay.AuthorBookDialogDirections
import com.example.bookbuddy.utils.navController
//adapter for the admi author books fragment
class AuthorBooksAdapter(var list: ArrayList<Book>) : RecyclerView.Adapter<AuthorBooksAdapter.ViewHolder>() {
    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val bookCover = vista.findViewById<ImageView>(R.id.book_cover)!!
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return ViewHolder(layout.inflate(R.layout.cardview_book_only_cover_multiple, parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.vista.context)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(list[position].cover)
            .into(holder.bookCover)

        holder.vista.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("isbn", list[position].isbn)
            val action = AuthorBookDialogDirections.actionNavAuthorBookDialogToNavBookDisplay(bundle)
            navController.navigate(action)
        }
    }

    fun updateList(newList: ArrayList<Book>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}