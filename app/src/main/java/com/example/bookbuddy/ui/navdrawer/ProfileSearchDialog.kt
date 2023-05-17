package com.example.bookbuddy.ui.navdrawer

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.adapters.SearchAuthorsAdapter
import com.example.bookbuddy.adapters.SearchGenresAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileSearchDialogBinding
import com.example.bookbuddy.models.Test.Genre
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileSearchDialog : DialogFragment(), CoroutineScope {
    lateinit var binding: FragmentProfileSearchDialogBinding
    private var job: Job = Job()
    //var searchResultList: ArrayList<Genre> = arrayListOf()
    private lateinit var adapter: SearchGenresAdapter

    var currentPage = 0
    private var position = 0
    var isLoading = false
    var genres: MutableList<Genre>? = null

    public var onGenreSearchCompleteListener: OnGenreSearchCompleteListener? = null
    public interface OnGenreSearchCompleteListener {
        fun onGenreSearchComplete(result: Int, name: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        if (parentFragment is OnGenreSearchCompleteListener) {
            onGenreSearchCompleteListener = parentFragment
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

                if(genres!!.isNotEmpty()){
                    position = 0
                    isLoading = false
                    binding.rvSearch.layoutManager = LinearLayoutManager(context)
                    adapter = SearchGenresAdapter(this, onGenreSearchCompleteListener, genres as java.util.ArrayList<Genre>)
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
                genres!!.addAll(crudApi.getSearchGenres(binding.searchThings.text.toString(), position) as MutableList<Genre>)
            }
            corrutina.join()
        }
        adapter.updateList(genres as ArrayList<Genre>)
        isLoading = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState)
    }

    private fun performSearch(searchValue: String) {
        // Aquí se realiza la búsqueda con el texto ingresado en el AutoCompleteTextView
        //Toast.makeText(requireContext(), "Realizando búsqueda: $searchValue", Toast.LENGTH_SHORT).show()

        genres = mutableListOf<Genre>()

        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                genres = crudApi.getSearchGenres(searchValue, position) as MutableList<Genre>
            }
            corrutina.join()
        }
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