package com.example.bookbuddy.adapters

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.utils.navController


class LibraryAdapter(var list: java.util.ArrayList<LibraryExtended>, var ubi: Location?) :
    RecyclerView.Adapter<LibraryAdapter.viewholder>() {

    private var selected: LibraryExtended? = null

    class viewholder(val view: View) : RecyclerView.ViewHolder(view) {
        val libraryName = view.findViewById<TextView>(R.id.lib_name)
        val libraryZip = view.findViewById<TextView>(R.id.lib_zip)
        val libraryDistance = view.findViewById<TextView>(R.id.lib_distance)
        val libraryCopies = view.findViewById<TextView>(R.id.lib_copies)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context

        if (viewType == 0){
            return viewholder(layout.inflate(R.layout.cardview_library, parent, false))
        } else{
            return viewholder(layout.inflate(R.layout.cardview_library_selected, parent, false))
        }
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        holder.libraryName.text = list[position].library.name
        holder.libraryZip.text = list[position].library.zipCode
        holder.libraryDistance.text = String.format("%.1f", list[position].distance) + " km"
        holder.libraryCopies.text = list[position].copies.toString() + " copies"

        holder.view.setOnClickListener {
            for (i in 0..list.size - 1) {
                if (i == position)
                    if (list[i].cardview == 0)
                        list[i].cardview = 1
                    else {
                        list[i].cardview = 0
                    }
                else
                    list[i].cardview = 0
            }
            selected = list[position]
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
