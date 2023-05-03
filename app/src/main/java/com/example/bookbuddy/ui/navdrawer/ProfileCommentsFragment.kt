package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.CommentAdapter
import com.example.bookbuddy.adapters.ProfileCommentAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookCommentsBinding
import com.example.bookbuddy.databinding.FragmentProfileCommentsBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ProfileCommentsFragment : Fragment(), CoroutineScope {

    lateinit var binding: FragmentProfileCommentsBinding
    private var job: Job = Job()
    private var userId: Int = currentUser.userId
    lateinit var adapter: ProfileCommentAdapter


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
        binding =  FragmentProfileCommentsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        userId = requireArguments().getInt("userid")
        binding.refresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        launch {
            getCommentsUser(userId, true)
            println("CHECKPOINT 3")
            loadingEnded()
            println("CHECKPOINT 4")
        }

        return binding.root
    }

    fun getCommentsUser(userId: Int, addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                if (position == 0){
                    comments = crudApi.getUserComments(userId,position) as MutableList<Comment>?
                } else {
                    comments!!.addAll(crudApi.getUserComments(userId,position) as MutableList<Comment>)
                }

            }
            corrutina.join()
        }
        if (addAdapter){
            binding.rvComments.layoutManager = LinearLayoutManager(context)
            adapter = ProfileCommentAdapter(comments as ArrayList<Comment>)
            binding.rvComments.adapter = adapter
        } else {
            adapter.updateList(comments as ArrayList<Comment>)
        }
    }


    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.refresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener() {
            position = 0
            currentPage = 0
            getCommentsUser(userId, false)
            binding.refresh.isRefreshing = false;
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
                        isLoading = true
                        loadMoreItems()
                    }
                }
            }
        })
    }

    private fun loadMoreItems() {
        currentPage++
        binding.loadingComment.visibility = View.VISIBLE
        getCommentsUser(userId, false)
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