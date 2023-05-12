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
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileSearchAuthorDialogBinding
import com.example.bookbuddy.models.Test.Author
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileAuthorDialog : DialogFragment(), CoroutineScope {
    lateinit var binding: FragmentProfileSearchAuthorDialogBinding
    private var job: Job = Job()
    //var searchResultList: ArrayList<Genre> = arrayListOf()
    private lateinit var adapter: SearchAuthorsAdapter

    var currentPage = 0
    private var position = 0
    var isLoading = false
    var authors: MutableList<Author>? = null

    public var onAuthorSearchCompleteListener: OnAuthorSearchCompleteListener? = null
    public interface OnAuthorSearchCompleteListener {
        fun onAuthorSearchComplete(result: Int, name: String)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().invalidateOptionsMenu()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        if (parentFragment is OnAuthorSearchCompleteListener) {
            onAuthorSearchCompleteListener = parentFragment
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
        binding =  FragmentProfileSearchAuthorDialogBinding.inflate(layoutInflater, container, false)

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

                if(authors!!.isNotEmpty()){
                    position = 0
                    isLoading = false
                    binding.rvSearch.layoutManager = LinearLayoutManager(context)
                    adapter = SearchAuthorsAdapter(this, onAuthorSearchCompleteListener, authors as java.util.ArrayList<Author>)
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
                authors!!.addAll(crudApi.getSearchAuthors(binding.searchThings.text.toString(), position) as MutableList<Author>)
            }
            corrutina.join()
        }
        adapter.updateList(authors as ArrayList<Author>)
        isLoading = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState)
    }

    private fun performSearch(searchValue: String) {
        // Aquí se realiza la búsqueda con el texto ingresado en el AutoCompleteTextView
        //Toast.makeText(requireContext(), "Realizando búsqueda: $searchValue", Toast.LENGTH_SHORT).show()

        authors = mutableListOf<Author>()

        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                authors = crudApi.getSearchAuthors(searchValue, position) as MutableList<Author>
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