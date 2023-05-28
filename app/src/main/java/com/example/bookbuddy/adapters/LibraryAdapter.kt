package com.example.bookbuddy.adapters

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.LibraryExtended

/**
 * Adapter for displaying libraries of a book in a RecyclerView.
 * @param list The list of libraries to display.
 */
class LibraryAdapter(var list: java.util.ArrayList<LibraryExtended>, var location: Location?) :
    RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    private var selected: LibraryExtended? = null

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val libraryName = view.findViewById<TextView>(R.id.lib_name)!!
        val libraryZip = view.findViewById<TextView>(R.id.lib_zip)!!
        val libraryDistance = view.findViewById<TextView>(R.id.lib_distance)!!
        val libraryCopies = view.findViewById<TextView>(R.id.lib_copies)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return if (viewType == 0){
            ViewHolder(layout.inflate(R.layout.cardview_library, parent, false))
        } else{
            ViewHolder(layout.inflate(R.layout.cardview_library_selected, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.libraryName.text = list[position].library.name
        holder.libraryZip.text = list[position].library.zipCode

        if (list[position].distance != null){
            holder.libraryDistance.text = String.format("%.1f", list[position].distance) + " km"
        } else {
            holder.libraryDistance.visibility = View.GONE
        }

        holder.libraryCopies.text = list[position].copies.toString() + " copies"

        holder.view.setOnClickListener {
            for (i in 0 until list.size) {
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
