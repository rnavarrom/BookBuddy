package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.ui.navdrawer.BookDisplayFragment
import com.example.bookbuddy.ui.navdrawer.NavDrawerActivity
import com.example.bookbuddy.utils.navController

class SimpleBookAdapter(val list: java.util.ArrayList<SimpleBook>) :
    RecyclerView.Adapter<SimpleBookAdapter.viewholder>() {
    class viewholder(val view: View) : RecyclerView.ViewHolder(view) {
        val cover = view.findViewById<ImageView>(R.id.book_cover)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return viewholder(layout.inflate(R.layout.cardview_simplebook, parent, false))
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        Glide.with(context)
            .load(list[position].coverUrl)
            .into(holder.cover)
        holder.view.setOnClickListener {
            navController.navigate(R.id.nav_book_display)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}