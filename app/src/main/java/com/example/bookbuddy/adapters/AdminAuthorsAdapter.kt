package com.example.bookbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.Author

//Adapter for the admin authors fragment
class AdminAuthorsAdapter(var list: ArrayList<Author>) : RecyclerView.Adapter<AdminAuthorsAdapter.ViewHolder>() {
    private var selected: Author? = null

    //Link values to the view
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById<TextView>(R.id.genre_name)
    }

    lateinit var context: Context
    //Load values to the view and select cardview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context

        return if (viewType == 0){
            ViewHolder(layout.inflate(R.layout.cardview_admin_genre, parent, false))
        } else{
            ViewHolder(layout.inflate(R.layout.cardview_admin_genre_selected, parent, false))
        }
    }
    //Bind each value from the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list[position].name
        holder.view.setOnClickListener {
            for (i in 0 until list.size) {
                if (i == position)
                    if (list[i].cardview == 0){
                        list[i].cardview = 1
                        selected = list[position]
                    }
                    else {
                        list[i].cardview = 0
                        selected = null
                    }
                else
                    list[i].cardview = 0
            }
            notifyDataSetChanged()
        }
    }
    fun updateList(newList: ArrayList<Author>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int) = list[position].cardview

    fun getSelected() = selected
}