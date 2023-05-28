package com.example.bookbuddy.ui.navdrawer

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.adapters.SearchResultAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentSearchBinding
import com.example.bookbuddy.models.SimpleBook
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
/**
 * Load the search fragment from navMenu
 */
class SearchFragment : Fragment(), ApiErrorListener{
    lateinit var binding: FragmentSearchBinding
    private lateinit var searchResultList: MutableList<SimpleBook>
    private lateinit var adapter: SearchResultAdapter
    private val api = CrudApi(this@SearchFragment)
    var searchValues = ArrayList<String>()
    private var position = 0
    private var lastPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        binding.advancedSearchButton.setOnClickListener {
            if (binding.advanced.visibility == View.VISIBLE) {
                binding.advanced.visibility = View.GONE
                binding.advancedSearchButton.setImageResource(R.drawable.ic_search_expand)
            } else {
                binding.advanced.visibility = View.VISIBLE
                binding.advancedSearchButton.setImageResource(R.drawable.ic_search_constrain)
            }
        }

        val searchList = ArrayList<EditText>()
        searchList.add(binding.SearchView)
        searchList.add(binding.etAuthor)
        searchList.add(binding.etGenre)

        for (et in searchList) {
            et.setOnKeyListener { view, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    binding.advanced.visibility = View.GONE
                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

                    val performSearch: Boolean

                    //Check is theres a value at last on one field
                    searchValues = ArrayList()
                    if (binding.SearchView.text.isNullOrBlank() && binding.etAuthor.text.isNullOrBlank() && binding.etGenre.text.isNullOrBlank()) {
                        binding.SearchView.setBackgroundResource(R.drawable.search_bg_error)
                        binding.etAuthor.setBackgroundResource(R.drawable.search_bg_error)
                        binding.etGenre.setBackgroundResource(R.drawable.search_bg_error)
                        performSearch = false
                    }else{
                        binding.SearchView.setBackgroundResource(R.drawable.search_bg)
                        binding.etAuthor.setBackgroundResource(R.drawable.search_bg)
                        binding.etGenre.setBackgroundResource(R.drawable.search_bg)
                        performSearch = true
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
                    if(performSearch){
                        val tempSearch = performSearch(searchValues)
                        if (tempSearch != null) {
                            searchResultList = tempSearch
                        }else{
                            searchResultList = arrayListOf()
                        }
                    }else{
                        showSnackBar(requireContext(), requireView(), getString(R.string.SB_NothingFound))
                        searchResultList = arrayListOf<SimpleBook>()
                    }
                    binding.SearchReciclerView.layoutManager = GridLayoutManager(context, 3)
                    adapter =
                        SearchResultAdapter(searchResultList as ArrayList<SimpleBook>)
                    binding.SearchReciclerView.adapter = adapter
                    true
                } else {
                    false
                }
            }
        }

        // Load more items when scrolling the recycler view
        binding.SearchReciclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        position = totalItemCount
                        if (lastPosition != totalItemCount){
                            loadMoreSearch(position, searchValues)
                        }
                        lastPosition = totalItemCount
                    }
                }
            }
        })
        return binding.root
    }

    private fun loadMoreSearch(position : Int, searchValues: ArrayList<String>) {
        runBlocking {
            val coroutine = launch {
                val tempSearchList =
                    api.getSimpleSearch(
                        position,
                        searchValues as List<String>
                    )
                if (tempSearchList != null)
                    searchResultList.addAll(tempSearchList as MutableList<SimpleBook>)
            }
            coroutine.join()
        }
        adapter.updateList(searchResultList as ArrayList<SimpleBook>)
    }

    private fun performSearch(searchValues: ArrayList<String>): ArrayList<SimpleBook>? {
        var searchResultList : ArrayList<SimpleBook>? = arrayListOf()
        position = 0
        lastPosition = -1
        runBlocking {
            val coroutine = launch {
                searchResultList = api.getSimpleSearch(position, searchValues as List<String>)
            }
            coroutine.join()
        }
        return searchResultList
    }

    override fun onApiError(connectionFailed: Boolean) {
        showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}




