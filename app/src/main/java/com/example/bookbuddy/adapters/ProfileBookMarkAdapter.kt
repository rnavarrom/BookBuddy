package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.ui.navdrawer.ProfileDialogDirections
import com.example.bookbuddy.ui.navdrawer.ProfileFragmentDirections
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileBookMarkAdapter(var list: java.util.ArrayList<Readed>, val isProfileFragment: Boolean) :
    RecyclerView.Adapter<ProfileBookMarkAdapter.viewholder>(), CoroutineScope {
    private var job: Job = Job()
    class viewholder(val view: View) : RecyclerView.ViewHolder(view) {
        val bookCover = view.findViewById<ImageView>(R.id.book_cover)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        var vh : ProfileBookMarkAdapter.viewholder? = null
        vh = viewholder(layout.inflate(R.layout.cardview_book_only_cover_multiple, parent, false))
        return vh!!
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        Glide.with(holder.view.context).load(list[position].book!!.cover).into(holder.bookCover)

        holder.view.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("isbn", list[position].book!!.isbn)
            var action: NavDirections? = null
            if (isProfileFragment){
                action = ProfileFragmentDirections.actionNavProfileToNavBookDisplay(bundle)
            } else {
                action = ProfileDialogDirections.actionNavProfileDialogToNavBookDisplay(bundle)
            }
            navController.navigate(action)
        }
    }

    fun updateList(newList: ArrayList<Readed>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}
