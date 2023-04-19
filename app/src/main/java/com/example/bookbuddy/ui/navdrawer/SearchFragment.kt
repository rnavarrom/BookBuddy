package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentSearchBinding.inflate(layoutInflater, container, false)

        binding.SearchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.SearchView.setOnKeyListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Toast.makeText(context, "Searching....", Toast.LENGTH_LONG).show()
                true
            } else {
                false // Indica que el evento no ha sido manejado
            }
        }

        binding.SearchReciclerView.setLayoutManager(GridLayoutManager(context, 3))

        return binding.root
    }
}