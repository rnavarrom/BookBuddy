package com.example.bookbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.Language
import com.example.bookbuddy.models.Test.Author
import com.example.bookbuddy.models.Test.Genre
import com.example.bookbuddy.ui.navdrawer.ProfileAuthorDialog
import com.example.bookbuddy.ui.navdrawer.ProfileLanguageDialog
import com.example.bookbuddy.utils.currentProfile
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class SearchLanguagesAdapter(var dialogFragment: DialogFragment, var dialog: ProfileLanguageDialog.OnLanguageSearchCompleteListener?, var list: java.util.ArrayList<Language>) :
    RecyclerView.Adapter<SearchLanguagesAdapter.viewholder>(), CoroutineScope {
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
            addLanguageToFavourite(list[position].languageId, list[position].name)
        }
    }

    fun addLanguageToFavourite(id: Int, name: String){
        /*
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                crudApi.updateProfileGenreToAPI(currentProfile.profileId, id)
            }
            corrutina.join()
        }
        */
        dialog?.onLanguageSearchComplete(id, name)
        dialogFragment.dismiss()
    }

    fun updateList(newList: ArrayList<Language>){
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}
