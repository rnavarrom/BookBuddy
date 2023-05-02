package com.example.bookbuddy.adapters

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.models.Genre
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.navController

class LibraryAdapter(var list: java.util.ArrayList<LibraryExtended>, var ubi: Location?) :
    RecyclerView.Adapter<LibraryAdapter.viewholder>() {

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
        return viewholder(layout.inflate(R.layout.cardview_library, parent, false))
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        holder.libraryName.text = list[position].library.name
        holder.libraryZip.text = list[position].library.zipCode
        holder.libraryDistance.text = String.format("%.1f", list[position].distance) + " km"
        holder.libraryCopies.text = list[position].copies.toString() + " copies"

        holder.view.setOnClickListener {
            /*println("CLICKED")
            println(ubi!!.latitude)
            println(ubi!!.longitude)
            println(list[position].library.lat)
            println(list[position].library.lon)*/

            val bundle = Bundle()
            bundle.putDouble("latitude", ubi!!.latitude)
            bundle.putDouble("longitude", ubi!!.longitude)
            bundle.putSerializable("library", list[position])
            navController.navigate(R.id.nav_library_map, bundle)

            // TODO: Go to map fragment
        }
    }

    fun updateList(newList: ArrayList<LibraryExtended>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
