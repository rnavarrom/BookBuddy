package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.ActualReading
import com.example.bookbuddy.ui.navdrawer.HomeFragment
import com.example.bookbuddy.ui.navdrawer.HomeFragmentDirections
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Adapter for displaying ActualReading books in a recycler view
 * @param list The list of search results to display.
 */
class HomeReadingBooksAdapter(var list: ArrayList<ActualReading>, val fragment: HomeFragment) :
    RecyclerView.Adapter<HomeReadingBooksAdapter.ViewHolder>(), ApiErrorListener {
    lateinit var layout: LayoutInflater
    lateinit var view: View
    private val api = CrudApi(this@HomeReadingBooksAdapter)

    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val imatge = vista.findViewById<ImageView>(R.id.actual_book_image)!!
        var pagesReaded = vista.findViewById<TextView>(R.id.pages_current)!!
        val pagesTotal = vista.findViewById<TextView>(R.id.pages_total)!!
        var percentage = vista.findViewById<TextView>(R.id.progress_text)!!
        var progressbar = vista.findViewById<ProgressBar>(R.id.progress_bar)!!
        val linearLayout = vista.findViewById<LinearLayout>(R.id.home_ll)!!
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layout = LayoutInflater.from(parent.context)
        context = parent.context
        view = parent
        return ViewHolder(layout.inflate(R.layout.cardview_books_reading, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pagesReaded.text = list[position].pagesReaded.toString()
        holder.pagesTotal.text = list[position].pages.toString()
        val percent = makePercentage(list[position].pagesReaded, list[position].pages)
        holder.percentage.text = percent.toString()
        holder.progressbar.progress = percent
        Glide.with(holder.vista.context)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(list[position].cover)
            .into(holder.imatge)

        holder.linearLayout.setOnClickListener {
            changeReaded(context, layout, position, holder)
        }
        holder.imatge.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("isbn", list[position].isbn)
            bundle.putParcelable("fragment", fragment)
            val action = HomeFragmentDirections.actionNavHomeToNavBookDisplay(bundle)
            navController.navigate(action)
        }
    }

    fun updateList(newList: ArrayList<ActualReading>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    /**
     * Function to change the read pages of a reading book
     * @param position The position of the item in the list.
     */
    private fun changeReaded(
        context: Context, layoutInf: LayoutInflater, position: Int,
        holder: ViewHolder
    ) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInf
        val dialogLayout = inflater.inflate(R.layout.dialog_readed_pages, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.dialog_readed_pages)
        val total = dialogLayout.findViewById<TextView>(R.id.dialog_total_pages)

        total.text = list[position].pages.toString()
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(total.text.length)
        editText.filters = filterArray
        builder.setView(dialogLayout)
        builder.setNegativeButton(context.getString(R.string.BT_Cancel)) { _, _ -> }
        builder.setPositiveButton(context.getString(R.string.BT_Accept)) { _, _ ->
            val valueint = Integer.parseInt(editText.text.toString())
            if (valueint == list[position].pages) {

                list[position].pagesReaded = Integer.parseInt(editText.text.toString())
                holder.pagesReaded.text = list[position].pagesReaded.toString()
                val percent =
                    makePercentage(Integer.parseInt(editText.text.toString()), list[position].pages)
                holder.progressbar.progress = percent
                holder.percentage.text = percent.toString()
                putBook(list[position].readedId, list[position].pagesReaded)
                getUser()
                reloadFragment(fragment)
            } else if (valueint < list[position].pages) {
                list[position].pagesReaded = Integer.parseInt(editText.text.toString())
                holder.pagesReaded.text = list[position].pagesReaded.toString()
                val percent =
                    makePercentage(Integer.parseInt(editText.text.toString()), list[position].pages)
                holder.progressbar.progress = percent
                holder.percentage.text = percent.toString()
                putBook(list[position].readedId, list[position].pagesReaded)
            }
        }
        builder.show()
    }

    private fun makePercentage(current: Int, total: Int): Int {
        return ((current * 100) / total)
    }

    private fun putBook(readedId: Int, pagesReaded: Int) {
        runBlocking {
            val coroutine = launch {
                api.updateReadedToAPI(readedId, pagesReaded)
            }
            coroutine.join()
        }
    }

    private fun reloadFragment(fragment: Fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragment.parentFragmentManager.beginTransaction().detach(fragment).commitNow()
            fragment.parentFragmentManager.beginTransaction().attach(fragment).commitNow()
        } else {
            fragment.parentFragmentManager.beginTransaction().detach(fragment).attach(fragment)
                .commit()
        }
    }

    private fun getUser() {
        runBlocking {
            val coroutine = launch {
                currentUser = api.getUserId(currentUser?.userId!!)
            }
            coroutine.join()
        }
    }

    override fun onApiError(connectionFailed: Boolean) {
        showSnackBar(context, view, Constants.ErrrorMessage)
    }
}