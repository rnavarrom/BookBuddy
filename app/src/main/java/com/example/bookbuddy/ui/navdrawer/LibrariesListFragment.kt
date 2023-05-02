package com.example.bookbuddy.ui.navdrawer

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.CommentAdapter
import com.example.bookbuddy.adapters.LibraryAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentLibrariesListBinding
import com.example.bookbuddy.databinding.FragmentRecommendationsBinding
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.navController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class LibrariesListFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentLibrariesListBinding
    private var job: Job = Job()
    private var isbn: String = ""
    lateinit var adapter: LibraryAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val locationRequestCode = 0
    var permissionsGranted = false
    var ubi: Location? = null

    private var position = 0
    var isLoading = false
    var libraries: MutableList<LibraryExtended>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentLibrariesListBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        isbn = requireArguments().getString("isbn").toString()

        requestPermissionsMap()

        return binding.root
    }

    fun loadFragment(){
        launch {
            getLibrariesBook(isbn, true)
            loadingEnded()
        }
    }

    fun getLibrariesBook(isbn: String, addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                if (position == 0){
                    libraries = crudApi.getBookLibrariesExtended(isbn, ubi!!.latitude, ubi!!.longitude) as MutableList<LibraryExtended>?
                } else {
                    // TODO: this
                    //libraries!!.addAll((setCardview(crudApi.getCommentsFromBook(bookId,position) as ArrayList<Comment>) as MutableList<Comment>?)!!)
                }

            }
            corrutina.join()
        }
        if (addAdapter){
            binding.rvLibraries.layoutManager = LinearLayoutManager(context)
            adapter = LibraryAdapter(libraries as ArrayList<LibraryExtended>, ubi)
            binding.rvLibraries.adapter = adapter
        } else {
            adapter.updateList(libraries as ArrayList<LibraryExtended>)
        }
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.mainContent.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener() {
            position = 0
            getLibrariesBook(isbn, false)
            binding.mainContent.isRefreshing = false;
        });

        binding.rvLibraries.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        /*
        currentPage++
        binding.loadingComment.visibility = View.VISIBLE
        getCommentsBook(bookId, false)
        binding.loadingComment.visibility = View.GONE
        isLoading = false
        */
    }

    fun requestPermissionsMap() {
        if (
            (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            permissionsGranted = true

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    ubi = location
                    loadFragment()
                }

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    "El permís ACCESS_FINE_LOCATION no està disponible",
                    Toast.LENGTH_LONG
                ).show()
                permissionsGranted = false
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    Toast.makeText(
                        requireContext(),
                        "El permís ACCESS_COARSE_LOCATION no està disponible",
                        Toast.LENGTH_LONG
                    ).show()
                    permissionsGranted = false
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        locationRequestCode
                    )
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (locationRequestCode == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                checkPermissions()
            } else {
                permissionsGranted = false
            }
        }
    }

    fun checkPermissions() {
        if (
            (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            permissionsGranted = true

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    ubi = location
                    loadFragment()
                }
        }
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