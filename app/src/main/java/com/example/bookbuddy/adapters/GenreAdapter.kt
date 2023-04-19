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
import com.example.bookbuddy.utils.navController

class GenreAdapter(val list: java.util.ArrayList<Genre>) :
    RecyclerView.Adapter<GenreAdapter.viewholder>() {

    class viewholder(val view: View) : RecyclerView.ViewHolder(view) {
        val genre = view.findViewById<TextView>(R.id.genre_name)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return viewholder(layout.inflate(R.layout.cardview_genre, parent, false))
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        holder.genre.text = "· " + list[position].name
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
