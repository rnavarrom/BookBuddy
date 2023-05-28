package com.example.bookbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.example.bookbuddy.R

/**
 * Adapter for displaying languages avaible in a Spinner Item.
 * @param list The list of languages to display.
 */
class LanguageSpinnerAdapter(context: Context, private val imageNames: Array<String>) :
    ArrayAdapter<String>(context, R.layout.spinner_item, imageNames) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.spinner_item, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.flag_item)
        val imageName = imageNames[position]
        val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        imageView.setImageResource(imageResId)
        return view
    }
}