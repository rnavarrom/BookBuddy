package com.example.bookbuddy.ui.navdrawer

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
import com.example.bookbuddy.adapters.ContactAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentContactsBinding
import com.example.bookbuddy.models.UserItem
import com.example.bookbuddy.ui.navdrawer.profile.ProfileDialog
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ContactsFragment : Fragment(), CoroutineScope, ProfileDialog.OnProfileDialogClose, ApiErrorListener, java.io.Serializable {
    lateinit var binding: FragmentContactsBinding
    private var job: Job = Job()
    lateinit var adapter: ContactAdapter

    private val api = CrudApi(this@ContactsFragment)

    private var position = 0
    private var lastPosition = -1
    private var follows: MutableList<UserItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentContactsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getUserFollows(true)
        loadingEnded()

        return binding.root
    }

    private fun emptyContacts(){
        if (follows == null || follows!!.isEmpty()){
            binding.emptyActivity.text = "No contacts"
            binding.emptyActivity.visibility = View.VISIBLE
        } else {
            binding.emptyActivity.visibility = View.GONE
        }
    }

    private fun getUserFollows(addAdapter: Boolean){
        runBlocking {

            val corrutina = launch {
                if (position == 0){
                    val tempFollows = api.getFollowersProfile(currentUser.userId, position) as MutableList<UserItem>?
                    if (tempFollows != null){
                        follows = tempFollows
                    }
                } else {
                    val tempFollows = api.getFollowersProfile(currentUser.userId, position)
                    if(tempFollows != null){
                        follows!!.addAll(tempFollows as MutableList<UserItem>)
                    }
                }
                if(follows != null) {
                    if (addAdapter) {
                        binding.rvContacts.layoutManager = LinearLayoutManager(context)
                        adapter = ContactAdapter(follows as ArrayList<UserItem>, this@ContactsFragment)
                        binding.rvContacts.adapter = adapter
                    } else {
                        adapter.updateList(follows as ArrayList<UserItem>)
                    }
                }
            }
            corrutina.join()
        }
    }

    fun loadingEnded(){
        emptyContacts()
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getUserFollows(false)
            emptyContacts()
            binding.mainContent.isRefreshing = false
        }

        binding.rvContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.loadingContacts.visibility = View.VISIBLE
        getUserFollows(false)
        binding.loadingContacts.visibility = View.GONE
    }

    override fun onApiError() {
        Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }

    override fun onProfileDialogClose() {
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!, true)
        navController.navigate(id)
    }

    override fun onDestroy() {
        Tools.clearCache(requireContext())
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