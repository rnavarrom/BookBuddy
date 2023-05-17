package com.example.bookbuddy.ui.navdrawer

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Transition
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.ProfileAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogProfileBinding
import com.example.bookbuddy.databinding.FragmentProfileBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.currentUser
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.CoroutineContext


class ProfileDialog : DialogFragment(), CoroutineScope {
    lateinit var binding: DialogProfileBinding
    private var job: Job = Job()

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
            DialogFragment.STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        );
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DialogProfileBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


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

    fun loadUser(){
        binding.tvUsername.text = username
        var profileImage: File? = null
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                followers = crudApi.getFollowerCount(profileUser!!)!!
                following = crudApi.getIsFollowing(currentUser.userId,profileUser!!)!!
                // TODO: LOAD IMAGE if ()
            }
            corrutina.join()
        }
        binding.tvFollowers.text = followers.toString() + " seguidores"
        followButton()

    }

    fun followButton(){

        if (following){
            binding.btFollow.tag = "Following"
            binding.btFollow.text = "Following"
        } else {
            binding.btFollow.tag = "Follow"
            binding.btFollow.text = "Follow"
        }

        binding.btFollow.setOnClickListener {
            if (binding.btFollow.tag.equals("Follow")){
                var followed = false
                runBlocking {
                    val crudApi = CrudApi()
                    val corrutina = launch {
                        followed = crudApi.addFollowToAPI(currentUser.userId, profileUser!!)
                    }
                    corrutina.join()
                }
                if (followed){
                    binding.btFollow.text = "Following"
                    binding.btFollow.tag = "Following"
                }
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Do you want to unfollow " + binding.tvUsername.text + "?")
                builder.setMessage("You will stop receibing notifications from this user")
                    .setPositiveButton("Unfollow",
                        DialogInterface.OnClickListener { dialog, id ->
                            runBlocking {
                                val crudApi = CrudApi()
                                val corrutina = launch {
                                    crudApi.deleteFollowAPI(currentUser.userId, profileUser!!)
                                }
                                corrutina.join()
                            }
                            binding.btFollow.text = "Follow"
                            binding.btFollow.tag = "Follow"
                        })
                    .setNegativeButton("Cancell",
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                builder.show()
            }
        }
    }

    fun loadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
    }

    fun loadTabLayout(){
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
}