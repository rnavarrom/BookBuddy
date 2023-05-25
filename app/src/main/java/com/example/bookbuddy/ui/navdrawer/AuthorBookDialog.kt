package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.AuthorBooksAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAuthorBookDialogBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AuthorBookDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAuthorBookDialogBinding
    private var job: Job = Job()
    lateinit var adapter: AuthorBooksAdapter
    private val api = CrudApi(this@AuthorBookDialog)
    private var position = 0
    private var lastPosition = -1
    var books: MutableList<Book>? = null

    private var authorId = 0
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAuthorBookDialogBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        val bundle = arguments?.getBundle("bundle")
        authorId = bundle?.getInt("authorid")!!
        name = bundle.getString("name")!!

        setToolBar(this, binding.toolbar, requireContext(), name)

        launch {
            getAuthorBooks(true)
            loadingEnded()
        }

        return binding.root
    }

    private fun getAuthorBooks(addAdapter: Boolean){
        var tempBooks : List<Book>?
        runBlocking {

            val corrutina = launch {
                if (position == 0){
                    tempBooks = api.getAuthorsBooks(authorId, position)
                    if(tempBooks !=  null){
                        books = tempBooks as MutableList<Book>?
                    }
                } else {
                    tempBooks = api.getAuthorsBooks(authorId, position)
                    if(tempBooks != null){
                        books!!.addAll( tempBooks as MutableList<Book>)
                    }
                }
                if (addAdapter){
                    binding.rvBooks.layoutManager = GridLayoutManager(context, 3)
                    adapter = AuthorBooksAdapter(books as ArrayList<Book>)
                    binding.rvBooks.adapter = adapter
                } else {
                    adapter.updateList(books as ArrayList<Book>)
                }
            }
            corrutina.join()
        }

    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.cl.visibility = View.VISIBLE

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getAuthorBooks(false)
            binding.mainContent.isRefreshing = false
        }

        binding.rvBooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        position = totalItemCount
                        if (lastPosition != totalItemCount){
                            loadMoreItems()
                        }
                        lastPosition = totalItemCount
                    }
                }
            }
        })
    }

    private fun loadMoreItems() {
        binding.loadingBooks.visibility = View.VISIBLE
        getAuthorBooks(false)
        binding.loadingBooks.visibility = View.GONE
    }

    override fun onApiError() {
        Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}