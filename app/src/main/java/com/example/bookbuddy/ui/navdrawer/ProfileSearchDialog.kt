package com.example.bookbuddy.ui.navdrawer

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.SearchGenresAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileSearchDialogBinding
import com.example.bookbuddy.models.Test.Genre
import com.example.bookbuddy.models.UserItem
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileSearchDialog : DialogFragment(), CoroutineScope {
    lateinit var binding: FragmentProfileSearchDialogBinding
    private var job: Job = Job()
    lateinit var searchResultList: ArrayList<Genre>
    private lateinit var adapter: SearchGenresAdapter

    var currentPage = 0
    private var position = 0
    var isLoading = false
    var genres: MutableList<Genre>? = null

    public var onSearchCompleteListener: OnSearchCompleteListener? = null
    public interface OnSearchCompleteListener {
        fun onSearchComplete(result: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        if (parentFragment is OnSearchCompleteListener) {
            onSearchCompleteListener = parentFragment
        } else {
            throw IllegalArgumentException("Parent fragment must implement OnSearchCompleteListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentProfileSearchDialogBinding.inflate(layoutInflater, container, false)

        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        position = totalItemCount
                        println("LOADING MORE")
                        isLoading = true
                        loadMoreItems()
                    }
                }
            }
        })

        // Inflate the layout for this fragment
        binding.searchThings.setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                // Realizar búsqueda
                //Toast.makeText(context, "", Toast.LENGTH_LONG).show()
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

                var searchValue = binding.searchThings.text.toString()
                performSearch(searchValue)

                genres =  performSearch(searchValue) as MutableList<Genre>

                if(genres!!.isNotEmpty()){
                    binding.rvSearch.layoutManager = LinearLayoutManager(context)
                    adapter = SearchGenresAdapter(this, onSearchCompleteListener, genres as java.util.ArrayList<Genre>)
                    binding.rvSearch.adapter = adapter
                }
                true
            } else {
                false
            }
        }

        return binding.root
    }

    private fun loadMoreItems() {
        currentPage++
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                searchResultList = crudApi.getSearchGenres() as ArrayList<Genre>
            }
            corrutina.join()
        }
        isLoading = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState)
    }

    private fun performSearch(searchValue: String) : ArrayList<Genre> {
        // Aquí se realiza la búsqueda con el texto ingresado en el AutoCompleteTextView
        //Toast.makeText(requireContext(), "Realizando búsqueda: $searchValue", Toast.LENGTH_SHORT).show()

        var searchResultList = arrayListOf<Genre>()

        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                searchResultList = crudApi.getSearchGenres(searchValue) as ArrayList<Genre>
            }
            corrutina.join()
        }
        return searchResultList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}