package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.ui.navdrawer.ProfileFragmentDirections
import com.example.bookbuddy.ui.navdrawer.profile.ProfileDialogDirections
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Adapter for displaying marked books of a user in a RecyclerView.
 * @param list The list of books to display.
 */
class ProfileBookMarkAdapter(var list: java.util.ArrayList<Readed>, private val isProfileFragment: Boolean) :
    RecyclerView.Adapter<ProfileBookMarkAdapter.ViewHolder>(), CoroutineScope {
    private var job: Job = Job()
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val bookCover = view.findViewById<ImageView>(R.id.book_cover)!!
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return ViewHolder(layout.inflate(R.layout.cardview_book_only_cover_multiple, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.view.context)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(list[position].book!!.cover)
            .into(holder.bookCover)

        holder.view.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("isbn", list[position].book!!.isbn)
            val action: NavDirections = if (isProfileFragment){
                ProfileFragmentDirections.actionNavProfileToNavBookDisplay(bundle)
            } else {
                ProfileDialogDirections.actionNavProfileDialogToNavBookDisplay(bundle)
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
