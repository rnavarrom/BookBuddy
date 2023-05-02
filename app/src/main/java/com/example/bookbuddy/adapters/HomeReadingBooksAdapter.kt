package com.example.bookbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.utils.dialogValue

class HomeReadingBooksAdapter(val llista: ArrayList<Pending>) : //, context: Context, layoutInf: LayoutInflater
    RecyclerView.Adapter<HomeReadingBooksAdapter.ViewHolder>() {
    lateinit var layout : LayoutInflater
    class ViewHolder(val vista: View) : RecyclerView.ViewHolder(vista) {
        val imatge = vista.findViewById<ImageView>(R.id.actual_book_image)
        var pagesReaded = vista.findViewById<TextView>(R.id.pages_current)
        val pagesTotal = vista.findViewById<TextView>(R.id.pages_total)
        var percentage = vista.findViewById<TextView>(R.id.progress_text)
        val linearLayout = vista.findViewById<LinearLayout>(R.id.home_ll)
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        layout = LayoutInflater.from(parent.context)
        context = parent.context
        var vh: ViewHolder? = null
        vh = ViewHolder(layout.inflate(R.layout.cardview_books_reading, parent, false))
        return vh!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pagesReaded.setText(dialogValue)
        holder.pagesTotal.setText(llista[position].pages.toString())
        //holder.percentage.setText(MakePercent(dialogValue, llista[position].pages))
        Glide.with(holder.vista.context).load(llista[position].cover).into(holder.imatge)

        holder.linearLayout.setOnClickListener {
            ChangeReaded(context, layout, position)
        }
    }

    override fun getItemCount(): Int = llista.size

    fun ChangeReaded(context: Context, layoutInf: LayoutInflater, position: Int) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInf
        //builder.setTitle("Book progress")
        val dialogLayout = inflater.inflate(R.layout.dialog_readed_pages, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.dialog_readed_pages)
        val total = dialogLayout.findViewById<TextView>(R.id.dialog_total_pages)
        total.text = llista[position].pages.toString()
        editText.setText(dialogValue)
        builder.setView(dialogLayout)
        builder.setNegativeButton("Cancel"){dialogInterface, i ->}
        builder.setPositiveButton("Save") { dialogInterface, i ->
            //llista[position].tempPages = editText.text.toString()
            dialogValue = editText.text.toString()
            //MakePercent()
        }
        builder.show()
    }
    fun MakePercent(pagesReaded: String, pagesTotal : Int  ) : Int {
        var current = Integer.parseInt(pagesReaded)
        var total = pagesTotal //Integer.parseInt(pagesTotal.text.toString())

        var percent : Int= ((current*100)/total)

        return percent
        //binding.progressText.text = percent.toString()
        //binding.progressBar.progress = percent
    }

}