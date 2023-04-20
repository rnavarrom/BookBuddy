package com.example.bookbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.models.SimpleBook

class SearchResultAdapter(val llista: ArrayList<SimpleBook>) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>(){
    class ViewHolder(val vista: View): RecyclerView.ViewHolder(vista){
        val imatge = vista.findViewById<ImageView>(R.id.book_cover)
        val text = vista.findViewById<TextView>(R.id.book_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        var vh : SearchResultAdapter.ViewHolder? = null
        vh = ViewHolder(layout.inflate(R.layout.cardview_book_and_rating, parent, false))
        return vh!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.setText(llista[position].rating.toString())
        Glide.with(holder.vista.context).load(llista[position].coverUrl).into(holder.imatge)

        holder.vista.setOnClickListener{

        }
    }
    override fun getItemCount(): Int = llista.size
}
