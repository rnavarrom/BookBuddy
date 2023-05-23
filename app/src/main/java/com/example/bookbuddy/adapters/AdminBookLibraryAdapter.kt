package com.example.bookbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.LibraryExtended

class AdminBookLibraryAdapter(var list: ArrayList<LibraryExtended>) : RecyclerView.Adapter<AdminBookLibraryAdapter.ViewHolder>() {
    private var selected: LibraryExtended? = null

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.library_name)!!
        val zip = view.findViewById<TextView>(R.id.libraryzip)!!
        val copies = view.findViewById<TextView>(R.id.librarycopies)!!
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context

        return if (viewType == 0){
            ViewHolder(layout.inflate(R.layout.cardview_admin_library, parent, false))
        } else{
            ViewHolder(layout.inflate(R.layout.cardview_admin_library_selected, parent, false))
        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list[position].library.name
        holder.zip.text = list[position].library.zipCode
        holder.copies.text = list[position].copies.toString() + "copies"
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

    fun updateList(newList: ArrayList<LibraryExtended>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int) = list[position].cardview

    fun getSelected() = selected
}