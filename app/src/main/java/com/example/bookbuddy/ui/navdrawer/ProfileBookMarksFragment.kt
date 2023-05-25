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
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.ProfileBookMarkAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileBookmarksBinding
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ProfileBookMarksFragment : Fragment(), CoroutineScope, ApiErrorListener {

    lateinit var binding: FragmentProfileBookmarksBinding
    private var job: Job = Job()
    private var userId: Int = currentUser.userId
    private var isProfileFragment: Boolean = false
    lateinit var adapter: ProfileBookMarkAdapter
    private val api = CrudApi(this@ProfileBookMarksFragment)

    private var position = 0
    private var lastPosition = -1
    private var readeds: MutableList<Readed>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBookmarksBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        userId = requireArguments().getInt("userid")
        isProfileFragment = requireArguments().getBoolean("isfragment")
        binding.refresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        launch {
            getCommentsUser(userId, true)
            loadingEnded()
        }
        return binding.root
    }

    private fun getCommentsUser(userId: Int, addAdapter: Boolean){
        runBlocking {            
            val corrutina = launch {
                if (position == 0){
                     val tempReadeds = api.getReadedsFromUser(userId,position)
                    if(tempReadeds != null){
                        readeds = tempReadeds  as MutableList<Readed>
                    }
                } else {
                    val tempReadeds = api.getReadedsFromUser(userId,position)
                    if(tempReadeds != null){
                        readeds!!.addAll( tempReadeds as MutableList<Readed>)
                    }
                }
            }
            corrutina.join()
        }
        if(readeds == null){

        }else if (addAdapter){
            val gridLayout = GridLayoutManager(context, 3)
            binding.rvBookmarks.layoutManager = gridLayout
            adapter = ProfileBookMarkAdapter(readeds as ArrayList<Readed>, isProfileFragment)
            binding.rvBookmarks.adapter = adapter
        } else {
            adapter.updateList(readeds as ArrayList<Readed>)
        }
    }


    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.refresh.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getCommentsUser(userId, false)
            binding.refresh.isRefreshing = false
        }

        binding.rvBookmarks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.loadingComment.visibility = View.VISIBLE
        getCommentsUser(userId, false)
        binding.loadingComment.visibility = View.GONE
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