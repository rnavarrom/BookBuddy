package com.example.bookbuddy.ui.navdrawer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.ProfileAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogProfileBinding
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext


class ProfileDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: DialogProfileBinding
    private var job: Job = Job()
    val api = CrudApi(this@ProfileDialog)
    private var profileUser: Int? = 0
    private var username: String? = ""

    private var followers: Int = 0
    private var following: Boolean = false

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    var permission = false

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
        binding =  DialogProfileBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)


        setToolBar(this, binding.toolbar, requireContext(), "Anon Profile")

        val bundle = arguments?.getBundle("bundle")
        profileUser = bundle?.getInt("userid", currentUser.userId)
        username = bundle?.getString("username",  currentUser.name)

        if (profileUser == null){
            profileUser = currentUser.userId
            username = currentUser.name
        }

        launch {
            println("CHECKPOINT 1")
            loadUser()
            println("CHECKPOINT 2")
            loadTabLayout()
            println("CHECKPOINT 3")
            loadingEnded()
        }
        return binding.root
    }

    private fun loadUser(){
        binding.tvUsername.text = username
        var profileImage: File? = null
        runBlocking {            
            val corrutina = launch {
                val tempFollowers = api.getFollowerCount(profileUser!!)
                if(tempFollowers != null){
                    followers = tempFollowers
                }
                val tempFollowing = api.getIsFollowing(currentUser.userId,profileUser!!)
                if(tempFollowing != null){
                    following = tempFollowing
                }
                // TODO: LOAD IMAGE if ()
            }
            corrutina.join()
        }
        binding.tvFollowers.text = followers.toString() + " seguidores"
        followButton()

    }

    private fun followButton(){

        if (following){
            binding.btFollow.tag = "Following"
            binding.btFollow.text = "Following"
        } else {
            binding.btFollow.tag = "Follow"
            binding.btFollow.text = "Follow"
        }

        binding.btFollow.setOnClickListener {
            if (binding.btFollow.tag.equals("Follow")){
                var followed : Boolean? = false
                runBlocking {                    
                    val corrutina = launch {
                        followed = api.addFollowToAPI(currentUser.userId, profileUser!!)
                    }
                    corrutina.join()
                }
                if(followed != null){
                if(followed == true){
                    binding.btFollow.text = "Following"
                    binding.btFollow.tag = "Following"
                }}
            } else {
                var result : Boolean? = false
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Do you want to unfollow " + binding.tvUsername.text + "?")
                builder.setMessage("You will stop receibing notifications from this user")
                    .setPositiveButton("Unfollow") { _, _ ->
                        runBlocking {
                            val corrutina = launch {
                                var result = api.deleteFollowAPI(currentUser.userId, profileUser!!)
                            }
                            corrutina.join()
                        }
                        binding.btFollow.text = "Follow"
                        binding.btFollow.tag = "Follow"
                    }/*
                    .setNegativeButton("Cancell"
                    ) { _, _ ->
                        // User cancelled the dialog
                    }*/
                builder.show()
            }
        }
    }

    fun loadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
    }

    private fun loadTabLayout(){
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        tabLayout.addTab(tabLayout.newTab().setText("COMMENTS"))
        tabLayout.addTab(tabLayout.newTab().setText("READS"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = ProfileAdapter(activity?.applicationContext, childFragmentManager,
            tabLayout.tabCount, profileUser!!, false
        )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
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

    override fun onApiError() {
        Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}