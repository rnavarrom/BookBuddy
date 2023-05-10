package com.example.bookbuddy.ui.navdrawer

import android.app.appsearch.SearchResult
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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

        binding.SearchView.setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                // Realizar búsqueda
                //Toast.makeText(context, "", Toast.LENGTH_LONG).show()
                var searchValue = binding.SearchView.text.toString()
                searchResultList = performSearch(searchValue, requireContext())

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

        return binding.root
    }

}

private fun performSearch(searchValue: String, context: Context): ArrayList<SimpleBook> {
    // Aquí se realiza la búsqueda con el texto ingresado en el AutoCompleteTextView
    Toast.makeText(context, "Realizando búsqueda: $searchValue", Toast.LENGTH_SHORT).show()

    var searchResultList = arrayListOf<SimpleBook>()

    runBlocking {
        val crudApi = CrudApi()
        val corrutina = launch {
            searchResultList = crudApi.getSimpleSearch(searchValue)
        }
        corrutina.join()
    }
    return searchResultList
}


