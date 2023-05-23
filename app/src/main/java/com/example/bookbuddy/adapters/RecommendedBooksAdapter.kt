package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.ui.navdrawer.ContactsFragmentDirections
import com.example.bookbuddy.ui.navdrawer.RecommendationsFragmentDirections
import com.example.bookbuddy.utils.navController
import org.w3c.dom.Text

class RecommendedBooksAdapter(var list: ArrayList<Book>) : RecyclerView.Adapter<RecommendedBooksAdapter.ViewHolder>() {
    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val imatge = vista.findViewById<ImageView>(R.id.book_cover)
        val bookrating = vista.findViewById<TextView>(R.id.book_rating)
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        var vh: ViewHolder? = null
        vh = ViewHolder(layout.inflate(R.layout.cardview_book_and_rating, parent, false))
        return vh!!
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.vista.context)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(list[position].cover)
            .into(holder.imatge)

        holder.bookrating.text = list[position].rating.toString()
        holder.vista.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("isbn", list[position].isbn)
            var action = RecommendationsFragmentDirections.actionNavRecommendationsToNavBookDisplay(bundle)
            navController.navigate(action)
        }
    }

    fun updateList(newList: ArrayList<Book>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}