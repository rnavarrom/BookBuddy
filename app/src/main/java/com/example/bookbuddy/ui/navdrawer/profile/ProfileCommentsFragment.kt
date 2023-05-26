package com.example.bookbuddy.ui.navdrawer.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.ProfileCommentAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileCommentsBinding
import com.example.bookbuddy.models.UserComments.Comment
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ProfileCommentsFragment : Fragment(), CoroutineScope, ApiErrorListener {

    lateinit var binding: FragmentProfileCommentsBinding
    private var job: Job = Job()
    private var userId: Int = currentUser.userId
    private var isProfileFragment: Boolean = false
    lateinit var adapter: ProfileCommentAdapter
    private val api = CrudApi(this@ProfileCommentsFragment)

    private var position = 0
    private var lastPosition = -1
    private var comments: MutableList<Comment>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentProfileCommentsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        userId = requireArguments().getInt("userid")
        isProfileFragment = requireArguments().getBoolean("isfragment")
        binding.refresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getCommentsUser(userId, true)
        loadingEnded()

        return binding.root
    }

    private fun getCommentsUser(userId: Int, addAdapter: Boolean){
        runBlocking {            
            val corrutina = launch {
                if (position == 0){
                    val tempComments = api.getUserComments(userId,position) as MutableList<Comment>?
                    if(tempComments != null){
                        comments = tempComments
                    }
                } else {
                    val tempComments = api.getUserComments(userId,position)
                    if(tempComments != null){
                        comments!!.addAll( tempComments as MutableList<Comment>)
                    }
                }
            }
            corrutina.join()
        }
        if(comments == null){

        }else if (addAdapter){
            binding.rvComments.layoutManager = LinearLayoutManager(context)
            adapter = ProfileCommentAdapter(comments as ArrayList<Comment>, isProfileFragment)
            binding.rvComments.adapter = adapter
        } else {
            adapter.updateList(comments as ArrayList<Comment>)
        }
    }


    private fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.refresh.setOnRefreshListener {
            position = 0
            getCommentsUser(userId, false)
            binding.refresh.isRefreshing = false
        }

        binding.rvComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
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
        binding.loadingComment.visibility = View.VISIBLE
        getCommentsUser(userId, false)
        binding.loadingComment.visibility = View.GONE
    }

    override fun onApiError() {
        showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
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