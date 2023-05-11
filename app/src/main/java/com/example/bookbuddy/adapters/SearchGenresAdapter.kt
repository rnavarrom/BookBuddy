package com.example.bookbuddy.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.Test.Genre
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.ui.navdrawer.BookCommentsFragmentDirections
import com.example.bookbuddy.ui.navdrawer.ContactsFragmentDirections
import com.example.bookbuddy.ui.navdrawer.ProfileSearchDialog
import com.example.bookbuddy.utils.currentPicture
import com.example.bookbuddy.utils.currentProfile
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext


class SearchGenresAdapter(var dialogFragment: DialogFragment, var dialog: ProfileSearchDialog.OnSearchCompleteListener?, var list: java.util.ArrayList<Genre>) :
    RecyclerView.Adapter<SearchGenresAdapter.viewholder>(), CoroutineScope {
    private var job: Job = Job()
    class viewholder(val view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tv_search_name)
    }

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val layout = LayoutInflater.from(parent.context)
        context = parent.context

        var vh = viewholder(layout.inflate(R.layout.cardview_profile_search, parent, false))
        return vh!!
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        holder.name.text = list[position].name

        holder.view.setOnClickListener {
            addGenreToFavourite(list[position].genreId)
        }
    }

    fun addGenreToFavourite(id: Int){
        currentProfile.genreId = id
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                crudApi.updateProfileToAPI(currentUser.userId, currentProfile)
            }
            corrutina.join()
        }
        dialog?.onSearchComplete(id)
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
