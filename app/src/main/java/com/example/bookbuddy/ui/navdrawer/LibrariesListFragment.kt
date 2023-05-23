package com.example.bookbuddy.ui.navdrawer

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.LibraryAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentLibrariesListBinding
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.navController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class LibrariesListFragment : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentLibrariesListBinding
    private var job: Job = Job()
    private var isbn: String = ""
    lateinit var adapter: LibraryAdapter
    val api = CrudApi(this@LibrariesListFragment)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequestCode = 0
    private var permissionsGranted = false
    private var location: Location? = null

    private var position = 0
    private var lastPosition = -1
    var libraries: MutableList<LibraryExtended>? = null
    private var gpsCar: Boolean = true
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
        binding =  FragmentLibrariesListBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        setToolBar(this, binding.toolbar, requireContext(), getString(R.string.TB_LibraryList))

        val bundle = arguments?.getBundle("bundle")
        isbn = bundle?.getString("isbn")!!

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        requestPermissionsMap()

        return binding.root
    }

    private fun loadFragment(){
        launch {
            if (!permissionsGranted || location == null){
                binding.gpscar.visibility = View.INVISIBLE
                binding.gpswalk.visibility = View.INVISIBLE
            }
            getLibrariesBook(isbn, true)
            loadingEnded()
        }
    }

    private fun getLibrariesBook(isbn: String, addAdapter: Boolean){
        var tmpLibraries : MutableList<LibraryExtended>?
        runBlocking {            
            val corrutina = launch {
                if (position == 0){
                    if (permissionsGranted){
                        if (location != null){
                                tmpLibraries = api.getBookLibrariesExtended(isbn, location!!.latitude, location!!.longitude) as MutableList<LibraryExtended>?
                            libraries = if (tmpLibraries == null){
                                mutableListOf()
                            } else {
                                tmpLibraries
                            }
                        } else {
                            showSnackBar(requireContext(), requireView(), getString(R.string.SB_EnableGPS))
                            tmpLibraries = api.getBookLibraries(isbn) as MutableList<LibraryExtended>?
                            if(tmpLibraries != null){
                                libraries = tmpLibraries
                            }
                        }
                    } else {
                        tmpLibraries = api.getBookLibraries(isbn) as MutableList<LibraryExtended>?
                        if(tmpLibraries != null){
                            libraries = tmpLibraries
                        }
                    }

                } else {
                    // TODO: this
                    //libraries!!.addAll((setCardview(api.getCommentsFromBook(bookId,position) as ArrayList<Comment>) as MutableList<Comment>?)!!)
                }

            }
            corrutina.join()
        }
        if (addAdapter){
            binding.rvLibraries.layoutManager = LinearLayoutManager(context)
            adapter = LibraryAdapter(libraries as ArrayList<LibraryExtended>, location)
            binding.rvLibraries.adapter = adapter
        } else {
            adapter.updateList(libraries as ArrayList<LibraryExtended>)
        }
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE
        binding.gpssearch.setOnClickListener {
            val selectedLibrary = adapter.getSelected()
            if (selectedLibrary != null){
                val bundle = Bundle()
                if (permissionsGranted){
                    if (location != null){
                        bundle.putDouble("latitude", location!!.latitude)
                        bundle.putDouble("longitude", location!!.longitude)
                        bundle.putString("method", if (gpsCar) "car" else "walking" )
                    }
                }

                bundle.putSerializable("library", selectedLibrary)

                val action = LibrariesListFragmentDirections.actionNavLibrariesListToNavLibraryMap(bundle)
                navController.navigate(action)
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_SelectLibrary))
            }
        }

        binding.gpscar.setOnClickListener{
            gpsCar = true
            binding.gpscar.background = getDrawable(requireContext(), R.drawable.bg_button_selected)
            binding.gpswalk.background = getDrawable(requireContext(), R.drawable.bg_button_standby)
        }

        binding.gpswalk.setOnClickListener{
            gpsCar = false
            binding.gpscar.background = getDrawable(requireContext(), R.drawable.bg_button_standby)
            binding.gpswalk.background = getDrawable(requireContext(), R.drawable.bg_button_selected)
        }

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getLibrariesBook(isbn, false)
            binding.mainContent.isRefreshing = false
        }

        binding.rvLibraries.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        /*
        currentPage++
        binding.loadingComment.visibility = View.VISIBLE
        getCommentsBook(bookId, false)
        binding.loadingComment.visibility = View.GONE
        isLoading = false
        */
    }

    private fun requestPermissionsMap() {
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
                    this.location = location
                    loadFragment()
                }

        } else {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                //showSnackBar(requireContext(), requireView(), "Location needed to show closest libraries")
                permissionsGranted = false
                loadFragment()
                //showSnackBar(requireContext(), requireView(), "Location needed to show closest libraries")
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    //showSnackBar(requireContext(), requireView(), "Location needed to show closest libraries")
                    permissionsGranted = false
                    loadFragment()
                } else {
                    requestPermissions(
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
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_LocationNeeded))
                permissionsGranted = false
                loadFragment()
            }
        }
    }

    private fun checkPermissions() {
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
                    this.location = location
                    loadFragment() }
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

    override fun onApiError() {
        showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}