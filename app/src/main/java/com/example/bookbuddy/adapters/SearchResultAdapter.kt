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
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.ui.navdrawer.SearchFragmentDirections
import com.example.bookbuddy.utils.navController


class SearchResultAdapter(var list: ArrayList<SimpleBook>) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>(){
    //, var fm: FragmentManager, var sf: Fragment
    class ViewHolder(val vista: View): RecyclerView.ViewHolder(vista){
        val imatge = vista.findViewById<ImageView>(R.id.book_cover)!!
        val text = vista.findViewById<TextView>(R.id.book_rating)!!
    }
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return ViewHolder(layout.inflate(R.layout.cardview_book_and_rating, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.text.text = list[position].rating.toString()
        Glide.with(holder.vista.context)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(list[position].cover)
            .into(holder.imatge)

        holder.vista.setOnClickListener{
            /*
            var newFragment = BookDisplayFragment()
            val transaction = fm.beginTransaction()
            transaction.show(newFragment)
            transaction.hide(sf)
            transaction.addToBackStack(null)
            transaction.commit()
             */
            val bundle = Bundle()
            bundle.putString("isbn", list[position].isbn)
            val action = SearchFragmentDirections.actionNavSearchToNavBookDisplay(bundle)
            navController.navigate(action)
               // val bundle = Bundle()
               // bundle.putString("isbn", llista[position].isbn)
               // navController.navigate(R.id.nav_book_display, bundle)
        }
    }
    override fun getItemCount(): Int = list.size
    fun updateList(newList: ArrayList<SimpleBook>){
        list = newList
        notifyDataSetChanged()
    }
}
