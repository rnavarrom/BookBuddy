package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.UserComments.Comment
import com.example.bookbuddy.ui.navdrawer.ProfileFragmentDirections
import com.example.bookbuddy.ui.navdrawer.profile.ProfileDialogDirections
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Adapter for displaying comments of a user in a RecyclerView.
 * @param list The list of comments to display.
 */
class ProfileCommentAdapter(var list: java.util.ArrayList<Comment>, private val isProfileFragment: Boolean) :
    RecyclerView.Adapter<ProfileCommentAdapter.ViewHolder>(), CoroutineScope {
    private var job: Job = Job()
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle = view.findViewById<TextView>(R.id.tv_book_title)!!
        val date = view.findViewById<TextView>(R.id.tv_date)!!
        val rating = view.findViewById<RatingBar>(R.id.book_rating_display)!!
        val comment = view.findViewById<TextView>(R.id.tv_comment)!!
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return ViewHolder(layout.inflate(R.layout.cardview_comment_profile, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bookTitle.text = list[position].book!!.title
        holder.date.text = list[position].fecha.toString()
        holder.rating.rating = list[position].rating.toFloat()
        holder.comment.text = list[position].comentText

        holder.view.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("isbn", list[position].book!!.isbn)
            var action: NavDirections? = null
            action = if (isProfileFragment){
                ProfileFragmentDirections.actionNavProfileToNavBookDisplay(bundle)
            } else {
                ProfileDialogDirections.actionNavProfileDialogToNavBookDisplay(bundle)
            }
            navController.navigate(action)
        }
    }

    fun updateList(newList: ArrayList<Comment>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = list[position].typeCardview

    override fun getItemCount(): Int {
        return list.size
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}
