package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.models.Test.ActualReading
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.ui.navdrawer.HomeFragment
import com.example.bookbuddy.ui.navdrawer.HomeFragmentDirections
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.base.ApiErrorListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeReadingBooksAdapter(var llista: ArrayList<ActualReading>, fragment: HomeFragment) : //, context: Context, layoutInf: LayoutInflater
    RecyclerView.Adapter<HomeReadingBooksAdapter.ViewHolder>(), ApiErrorListener {
    lateinit var layout: LayoutInflater
    val fragment = fragment
    lateinit var view : View
    val api = CrudApi(this@HomeReadingBooksAdapter)

    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val imatge = vista.findViewById<ImageView>(R.id.actual_book_image)
        var pagesReaded = vista.findViewById<TextView>(R.id.pages_current)
        val pagesTotal = vista.findViewById<TextView>(R.id.pages_total)
        var percentage = vista.findViewById<TextView>(R.id.progress_text)
        var progressbar = vista.findViewById<ProgressBar>(R.id.progress_bar)
        val linearLayout = vista.findViewById<LinearLayout>(R.id.home_ll)
        //val dummyText = vista.findViewById<TextView>(R.id.NoBooksTV)
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layout = LayoutInflater.from(parent.context)
        context = parent.context
        view = parent
        return ViewHolder(layout.inflate(R.layout.cardview_books_reading, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*
        println("------------" + llista.size)
        if(llista.isEmpty()){
            holder.dummyText.visibility = View.VISIBLE
            println("Showing dummy")
        }

         */
        //println(llista[position].pages.toString())
        holder.pagesReaded.text = llista[position].pagesReaded.toString()
        holder.pagesTotal.text = llista[position].pages.toString()
        var percent = MakePercent(llista[position].pagesReaded, llista[position].pages)
        holder.percentage.text = percent.toString()
        holder.progressbar.progress = percent
        Glide.with(holder.vista.context)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(llista[position].cover)
            .into(holder.imatge)

        holder.linearLayout.setOnClickListener {
            ChangeReaded(context, layout, position, holder)
        }
        holder.imatge.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("isbn", llista[position].isbn)
            bundle.putSerializable("fragment", fragment)
            var action = HomeFragmentDirections.actionNavHomeToNavBookDisplay(bundle)
            navController.navigate(action)
        }
    }

    fun updateList(newList: ArrayList<ActualReading>){
        llista = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = llista.size

    fun ChangeReaded(
        context: Context,
        layoutInf: LayoutInflater,
        position: Int,
        holder: ViewHolder
    ) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInf
        //builder.setTitle("Book progress")
        val dialogLayout = inflater.inflate(R.layout.dialog_readed_pages, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.dialog_readed_pages)
        val total = dialogLayout.findViewById<TextView>(R.id.dialog_total_pages)
        total.text = llista[position].pages.toString()
        // editText.setText(llista[position].pagesReaded)
        builder.setView(dialogLayout)
        builder.setNegativeButton("Cancel") { dialogInterface, i -> }
        builder.setPositiveButton("Save") { dialogInterface, i ->
                var valueint = Integer.parseInt(editText.text.toString())
            if(valueint == llista[position].pages){
                llista[position].pagesReaded = Integer.parseInt(editText.text.toString())
                holder.pagesReaded.text = llista[position].pagesReaded.toString()
                var percent =
                    MakePercent(Integer.parseInt(editText.text.toString()), llista[position].pages)
                holder.progressbar.progress = percent
                holder.percentage.text = percent.toString()
                PutBook(llista[position].readedId, llista[position].pagesReaded)
                //RemoveBookReading(llista[position].readedId)
                getUser()
                reloadFragment(fragment)
            }else if ( valueint < llista[position].pages) {
                llista[position].pagesReaded = Integer.parseInt(editText.text.toString())
                holder.pagesReaded.text = llista[position].pagesReaded.toString()
                var percent =
                    MakePercent(Integer.parseInt(editText.text.toString()), llista[position].pages)
                holder.progressbar.progress = percent
                holder.percentage.text = percent.toString()
                PutBook(llista[position].readedId, llista[position].pagesReaded)
            }
        }
        builder.show()
    }

    fun MakePercent(current: Int, total: Int): Int {
        var percent: Int = ((current * 100) / total)
        return percent
    }

    fun PutBook(readedId: Int, pagesReaded: Int){
        var result : Boolean? = false
        runBlocking {
            val corrutina = launch {
                result = api.updateReadedToAPI(readedId, pagesReaded)
            }
            corrutina.join()
        }
        Toast.makeText(context, "Resultat: " + result, Toast.LENGTH_LONG).show()
    }
    fun reloadFragment(fragment: Fragment){
        Toast.makeText(context, "Reloading fragment", Toast.LENGTH_LONG).show()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragment.parentFragmentManager.beginTransaction().detach(fragment).commitNow()
            fragment.parentFragmentManager.beginTransaction().attach(fragment).commitNow()
        } else {
            fragment.parentFragmentManager.beginTransaction().detach(fragment).attach(fragment).commit()
        }
    }
    fun getUser(){
        runBlocking {
            val corrutina = launch {
                currentUser = api.getUserId(currentUser.userId)!!
            }
            corrutina.join()
        }
    }

    override fun onApiError() {
        //if (this::context.isInitialized){

        //}
        Tools.showSnackBar(context, view, Constants.ErrrorMessage)
    }
}