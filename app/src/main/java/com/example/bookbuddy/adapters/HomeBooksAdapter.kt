package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.ui.navdrawer.HomeFragment
import com.example.bookbuddy.ui.navdrawer.HomeFragmentDirections
import com.example.bookbuddy.ui.navdrawer.SearchFragmentDirections
import com.example.bookbuddy.utils.navController

class HomeBooksAdapter(var llista: ArrayList<Pending>, fragment: HomeFragment) : RecyclerView.Adapter<HomeBooksAdapter.ViewHolder>() {
    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val imatge = vista.findViewById<ImageView>(R.id.book_cover)
       // val text = vista.findViewById<TextView>(R.id.book_rating)
    }
    val fragment = fragment
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        var vh: ViewHolder? = null
        vh = ViewHolder(layout.inflate(R.layout.cardview_book_only_cover, parent, false))
        return vh!!
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //holder.text.setText(llista[position].rating.toString())
        Glide.with(holder.vista.context)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(llista[position].cover)
            .into(holder.imatge)

        holder.vista.setOnClickListener {
            //bundle.putString("isbn", llista[position].isbn)
            //navController.navigate(R.id.nav_book_display, bundle)
            val bundle = Bundle()
            bundle.putString("isbn", llista[position].isbn)
            bundle.putSerializable("fragment", fragment)
            var action = HomeFragmentDirections.actionNavHomeToNavBookDisplay(bundle)
            navController.navigate(action)
        }
    }

    fun updateList(newList: ArrayList<Pending>){
        llista = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = llista.size
}