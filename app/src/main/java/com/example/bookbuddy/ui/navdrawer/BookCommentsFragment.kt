package com.example.bookbuddy.ui.navdrawer

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.CommentAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookCommentsBinding
import com.example.bookbuddy.databinding.FragmentSettingsBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class BookCommentsFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentBookCommentsBinding
    private var job: Job = Job()
    private var bookId: Int = 0
    lateinit var adapter: CommentAdapter


    var currentPage = 0
    private var position = 0
    var isLoading = false
    var comments: MutableList<Comment>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentBookCommentsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        bookId = requireArguments().getInt("book_id")
        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        launch {
            getCommentsBook(bookId, true)
            loadingEnded()
        }

        return binding.root
    }

    fun getCommentsBook(bookId: Int, addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                if (position == 0){
                    comments = setCardview(crudApi.getCommentsFromBook(bookId,position) as ArrayList<Comment>) as MutableList<Comment>?
                } else {
                    comments!!.addAll((setCardview(crudApi.getCommentsFromBook(bookId,position) as ArrayList<Comment>) as MutableList<Comment>?)!!)
                }

            }
            corrutina.join()
        }
        if (addAdapter){
            binding.rvComments.layoutManager = LinearLayoutManager(context)
            adapter = CommentAdapter(comments as ArrayList<Comment>)
            binding.rvComments.adapter = adapter
        } else {
            adapter.updateList(comments as ArrayList<Comment>)
        }
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE

        binding.addComment.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("book_id", bookId)
            navController.navigate(R.id.nav_write_comment, bundle)
        }

        binding.mainContent.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener() {
            position = 0
            currentPage = 0
            getCommentsBook(bookId, false)
            binding.mainContent.isRefreshing = false;
        });

        binding.rvComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    }

    private fun setCardview(coms: ArrayList<Comment>): ArrayList<Comment>{
        coms.forEach { c ->
            if (c.user!!.userId == 1){
                c.typeCardview = 1
            }
        }
        return coms
    }

    private fun loadMoreItems() {
        currentPage++
        binding.loadingComment.visibility = View.VISIBLE
        getCommentsBook(bookId, false)
        binding.loadingComment.visibility = View.GONE
        isLoading = false
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