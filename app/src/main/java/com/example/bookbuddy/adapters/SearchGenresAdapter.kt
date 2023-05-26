package com.example.bookbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.Test.Genre
import com.example.bookbuddy.ui.navdrawer.profile.ProfileSearchDialog
import com.example.bookbuddy.utils.currentProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


class SearchGenresAdapter(private var dialogFragment: DialogFragment, var dialog: ProfileSearchDialog.OnGenreSearchCompleteListener?, var list: java.util.ArrayList<Genre>) :
    RecyclerView.Adapter<SearchGenresAdapter.ViewHolder>(), CoroutineScope {
    private var job: Job = Job()
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tv_search_name)!!
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context
        return ViewHolder(layout.inflate(R.layout.cardview_profile_search, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list[position].name

        holder.view.setOnClickListener {
            addGenreToFavourite(list[position].genreId, list[position].name)
        }
    }

    private fun addGenreToFavourite(id: Int, name: String){
        currentProfile.genreId = id
        dialog?.onGenreSearchComplete(id, name)
        dialogFragment.dismiss()
    }

    fun updateList(newList: ArrayList<Genre>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}
