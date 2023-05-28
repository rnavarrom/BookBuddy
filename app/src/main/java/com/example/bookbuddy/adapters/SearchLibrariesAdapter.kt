package com.example.bookbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.ui.navdrawer.profile.ProfileLibraryDialog

/**
 * Adapter for displaying search results of libraries in a RecyclerView.
 * @param dialogFragment The fragment to close when search completed
 * @param dialog Dialog where execute function on search end
 * @param list The list of search results to display.
 */
class SearchLibrariesAdapter(
    private var dialogFragment: DialogFragment,
    var dialog: ProfileLibraryDialog.OnLibrarySearchCompleteListener?,
    var list: java.util.ArrayList<LibraryExtended>
) :
    RecyclerView.Adapter<SearchLibrariesAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tv_search_name)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
        return ViewHolder(layout.inflate(R.layout.cardview_profile_search, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list[position].library.name

        holder.view.setOnClickListener {
            addLibraryToFavourite(
                list[position].library.libraryId,
                list[position].library.name,
                list[position].library.zipCode
            )
        }
    }

    private fun addLibraryToFavourite(id: Int, name: String, zipCode: String) {
        dialog?.onLibrarySearchComplete(id, name, zipCode)
        dialogFragment.dismiss()
    }

    fun updateList(newList: ArrayList<LibraryExtended>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
