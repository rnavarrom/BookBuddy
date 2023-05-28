package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.RecommendedBooksAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentRecommendationsBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Load the recomendations fragment from navMenu
 */
class RecommendationsFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentRecommendationsBinding
    private var job: Job = Job()
    lateinit var adapter: RecommendedBooksAdapter
    private val api = CrudApi(this@RecommendationsFragment)
    private var position = 0
    private var lastPosition = -1
    var books: MutableList<Book>? = null

    private var isOnCreateViewExecuted = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecommendationsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary_green
            )
        )

        getUserRecommended(true)
        onLoadingEnded()
        isOnCreateViewExecuted = true
        return binding.root
    }

    private fun getUserRecommended(addAdapter: Boolean) {
        runBlocking {
            val coroutine = launch {
                if (position == 0) {
                    val tempBooks = api.getRecommendedBooks(
                        currentUser!!.userId,
                        position
                    ) as MutableList<Book>?
                    if (tempBooks != null) {
                        books = tempBooks
                    }
                } else {
                    val tempBooks = api.getRecommendedBooks(currentUser!!.userId, position)
                    if (tempBooks != null) {
                        books!!.addAll(tempBooks as MutableList<Book>)
                    }
                }
                if (books != null) {
                    if (addAdapter) {
                        binding.rvRecommended.layoutManager = GridLayoutManager(context, 3)
                        adapter = RecommendedBooksAdapter(books as ArrayList<Book>)
                        binding.rvRecommended.adapter = adapter
                    } else {
                        adapter.updateList(books as ArrayList<Book>)
                    }
                }
            }
            coroutine.join()
        }

    }

    private fun emptyBooks() {
        if (books == null || books!!.isEmpty()) {
            binding.emptyActivity.text = getString(R.string.MSG_NoRecommendedBooks)
            binding.emptyActivity.visibility = View.VISIBLE
        } else {
            binding.emptyActivity.visibility = View.GONE
        }
    }

    // Change visible layouts and add bindings
    private fun onLoadingEnded() {
        emptyBooks()
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getUserRecommended(false)
            emptyBooks()
            binding.mainContent.isRefreshing = false
        }

        // Load more items when scrolling the recycler view
        binding.rvRecommended.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        position = totalItemCount
                        if (lastPosition != totalItemCount) {
                            loadMoreItems()
                        }
                        lastPosition = totalItemCount
                    }
                }
            }
        })
    }

    private fun loadMoreItems() {
        binding.loadingRecommended.visibility = View.VISIBLE
        getUserRecommended(false)
        binding.loadingRecommended.visibility = View.GONE
    }

    override fun onApiError(connectionFailed: Boolean) {
        if (isOnCreateViewExecuted) {
            Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
        }
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