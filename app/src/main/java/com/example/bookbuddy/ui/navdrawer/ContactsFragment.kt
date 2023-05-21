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
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.CommentAdapter
import com.example.bookbuddy.adapters.ContactAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentContactsBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ContactsFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentContactsBinding
    private var job: Job = Job()
    lateinit var adapter: ContactAdapter


    var currentPage = 0
    private var position = 0
    var isLoading = false
    var follows: MutableList<UserItem>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentContactsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        launch {
            getUserFollows(true)
            loadingEnded()
        }

        return binding.root
    }

    fun getUserFollows(addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi(this@ContactsFragment)
            val corrutina = launch {
                if (position == 0){
                    var tempFollows = crudApi.getFollowersProfile(currentUser.userId, position, "") as MutableList<UserItem>?
                    if (tempFollows != null){
                        follows = tempFollows
                    }
                } else {
                    var tempFollows = crudApi.getFollowersProfile(currentUser.userId, position, "")
                    if(tempFollows != null){
                        follows!!.addAll(tempFollows as MutableList<UserItem>)
                    }
                }
                if(follows == null){

                }else if (addAdapter!!){
                    binding.rvContacts.layoutManager = LinearLayoutManager(context)
                    adapter = ContactAdapter(follows as ArrayList<UserItem>)
                    binding.rvContacts.adapter = adapter
                } else {
                    adapter.updateList(follows as ArrayList<UserItem>)
                }
            }
            corrutina.join()
        }

    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.mainContent.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener() {
            position = 0
            currentPage = 0
            getUserFollows(false)
            binding.mainContent.isRefreshing = false;
        });

        binding.rvContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun loadMoreItems() {
        currentPage++
        binding.loadingContacts.visibility = View.VISIBLE
        getUserFollows(false)
        binding.loadingContacts.visibility = View.GONE
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

    override fun onApiError(errorMessage: String) {
        Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}