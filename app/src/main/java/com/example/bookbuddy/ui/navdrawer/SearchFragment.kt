package com.example.bookbuddy.ui.navdrawer

import android.app.appsearch.SearchResult
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.SearchResultAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentSearchBinding
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.models.UserItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding
    lateinit var searchResultList: ArrayList<SimpleBook>
    private lateinit var adapter: SearchResultAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        //val searchTextView = findViewById<AutoCompleteTextView>(R.id.search_text_view)
/*
        binding.authorSearch.setOnClickListener {
            if (binding.authorSearch.tag == "selected"){
                binding.authorSearch.setBackgroundColor(requireContext().getColor(R.color.primary_green))
                binding.authorSearch.tag = null
                binding.SearchView.hint = "Book Search"
            } else {
                binding.authorSearch.tag = "selected"
                binding.genreSearch.tag = null
                binding.authorSearch.setBackgroundColor(requireContext().getColor(R.color.primary_green_20))
                binding.genreSearch.setBackgroundColor(requireContext().getColor(R.color.primary_green))
                binding.SearchView.hint = "Author Book Search"
            }

        }

        binding.genreSearch.setOnClickListener {
            if (binding.genreSearch.tag == "selected"){
                binding.genreSearch.setBackgroundColor(requireContext().getColor(R.color.primary_green))
                binding.genreSearch.tag = null
                binding.SearchView.hint = "Book Search"
            } else {
                binding.genreSearch.tag = "selected"
                binding.authorSearch.tag = null
                binding.genreSearch.setBackgroundColor(requireContext().getColor(R.color.primary_green_20))
                binding.authorSearch.setBackgroundColor(requireContext().getColor(R.color.primary_green))
                binding.SearchView.hint = "Genre Book Search"
            }
        }

 */
        binding.advancedSearchButton.setOnClickListener {
            if (binding.advanced.visibility == View.VISIBLE) {
                binding.advanced.visibility = View.GONE
                binding.advancedSearchButton.setImageResource(R.drawable.ic_search_expand)
            } else {
                binding.advanced.visibility = View.VISIBLE
                binding.advancedSearchButton.setImageResource(R.drawable.ic_search_constrain)
            }
        }
/*
        binding.SearchView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // Este código se ejecutará cuando el AutoCompleteTextView obtenga el foco.
                binding.advanced.visibility = View.VISIBLE

            } else {
                // Este código se ejecutará cuando el AutoCompleteTextView pierda el foco.
                binding.advanced.visibility = View.GONE
            }
        }

 */

        var searchList = ArrayList<EditText>()
        searchList.add(binding.SearchView)
        searchList.add(binding.etAuthor)
        searchList.add(binding.etGenre)

        for (et in searchList) {
            et.setOnKeyListener { view, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    // Realizar búsqueda
                    //Toast.makeText(context, "", Toast.LENGTH_LONG).show()
                    binding.advanced.visibility = View.GONE
                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

                    var searchValues = ArrayList<String>()

                    if (binding.SearchView.text.isNullOrBlank() && binding.etAuthor.text.isNullOrBlank() && binding.etGenre.text.isNullOrBlank()) {
                        binding.SearchView.setBackgroundResource(R.drawable.custom_progress_bg_error)
                        binding.etAuthor.setBackgroundResource(R.drawable.custom_progress_bg_error)
                        binding.etGenre.setBackgroundResource(R.drawable.custom_progress_bg_error)
                    }
                    if (!binding.SearchView.text.isNullOrBlank()) {
                        searchValues.add(binding.SearchView.text.toString())
                    } else {
                        searchValues.add("")
                    }
                    if (!binding.etAuthor.text.isNullOrBlank()) {
                        searchValues.add(binding.etAuthor.text.toString())
                    } else {
                        searchValues.add("")
                    }
                    if (!binding.etGenre.text.isNullOrBlank()) {
                        searchValues.add(binding.etGenre.text.toString())
                    } else {
                        searchValues.add("")
                    }


                    searchResultList = performSearch(searchValues)

                    if (!searchResultList.isEmpty()) {
                        Toast.makeText(context, "Nothing found!", Toast.LENGTH_LONG).show()
                    }
                    binding.SearchReciclerView.setLayoutManager(GridLayoutManager(context, 3))
                    adapter =
                        SearchResultAdapter(searchResultList) //, requireActivity().supportFragmentManager, this
                    binding.SearchReciclerView.adapter = adapter

                    true
                } else {
                    false
                }
            }

        }
        return binding.root
    }

    private fun performSearch(searchValues: ArrayList<String>): ArrayList<SimpleBook> {
        var searchResultList = arrayListOf<SimpleBook>()

        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                searchResultList = crudApi.getSimpleSearch(0, searchValues as List<String>)
            }
            corrutina.join()
        }
        return searchResultList
    }
}




