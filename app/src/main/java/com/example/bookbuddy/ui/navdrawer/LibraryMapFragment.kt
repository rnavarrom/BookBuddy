package com.example.bookbuddy.ui.navdrawer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentLibraryMapBinding
import com.example.bookbuddy.databinding.FragmentRecommendationsBinding
import com.example.bookbuddy.models.CleanResponse
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LibraryMapFragment : DialogFragment(), OnMapReadyCallback, CoroutineScope {
    lateinit var binding: FragmentLibraryMapBinding

    private var job: Job = Job()

    //private lateinit var fusedLocationClient: FusedLocationProviderClient
    val locationRequestCode = 0
    var permissionsGranted = false
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private var library: LibraryExtended? = null
    lateinit var mMap: GoogleMap

    val crudAPI = CrudApi()

    var resp : CleanResponse? = null
    var method : String? = "walking"

    lateinit var bundle : Bundle
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
        binding =  FragmentLibraryMapBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setToolBar(this, binding.toolbar, (activity as AppCompatActivity?)!!, "Map")

        bundle = arguments?.getBundle("bundle")!!
        latitude = bundle.getDouble("latitude")
        longitude = bundle.getDouble("longitude")
        method = bundle.getString("method")

        println(latitude)
        println(longitude)
        println(method)

        if (bundle != null) {
            library = bundle.getSerializable("library") as? LibraryExtended
            if (library != null) {
                loadLibraryBasicInformation(library!!)
                println("CRASH")
            }
        }

        requestPermissionsMap()

        if (library != null && permissionsGranted){
            val supportMapFragment =
                childFragmentManager.findFragmentById(R.id.googlemap) as SupportMapFragment?
            supportMapFragment!!.getMapAsync(this)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            library = bundle.getSerializable("library") as? LibraryExtended
            if (library != null) {
                loadLibraryBasicInformation(library!!)
                println("CRASH")
            }
        }

        if (library != null && permissionsGranted) {
            val supportMapFragment =
                childFragmentManager.findFragmentById(R.id.googlemap) as SupportMapFragment?
            supportMapFragment!!.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        print("ENTRO")
        val start = LatLng(latitude, longitude)
        val end = LatLng(library!!.library.lat, library!!.library.lon)
        mMap = googleMap
        checkPermissionsTMP()
        val lib = LatLng(library!!.library.lat, library!!.library.lon)

        mMap!!.addMarker(
            MarkerOptions().position(lib).title(library!!.library.name)
        )

        launch {

            val startString =
                start.longitude.toString() + ", " + start.latitude.toString()
            val endString =
                end.longitude.toString() + ", " + end.latitude.toString()

            if (method == "car")
                resp = crudAPI.getCarRoute(startString, endString)
            else
                resp = crudAPI.getWalkingRoute(startString, endString)
            if (resp != null) {
                drawRoute(mMap, resp!!.coordinates)

                binding.tvLibraryDistance.text = "Distància:"+ resp!!.distance.toString()+" metres"
                binding.tvLibraryTime.text = "Temps: "+resp!!.duration.toString()+" segons"

                binding.tvLibraryName.setOnClickListener {
                    mMap!!.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(lib, 17.0f),
                        1500, null
                    )
                }

                val puntmig = LatLng((latitude+library!!.library.lat)/2, (longitude+library!!.library.lon)/2)
                var zoom : Float? = null
                if (resp!!.distance < 1000.0)
                    zoom = 15.0f
                else if (resp!!.distance<= 5000.0)
                    zoom = 14.0f
                else if (resp!!.distance<= 10000.0)
                    zoom = 13.0f
                else if (resp!!.distance<= 15000.0)
                    zoom = 12.0f
                else
                    zoom = 11.0f
                print("NOCRASH3")
                loadingEnded()
                mMap!!.animateCamera(

                    CameraUpdateFactory.newLatLngZoom(puntmig, zoom),
                    2500, null
                )
            }
        }
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
            loadFragment()
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
            loadFragment()
        }
    }

    fun checkPermissionsTMP() {
        if (
            (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            permissionsGranted = true
            mMap.isMyLocationEnabled = true
        }
    }

    fun loadLibraryBasicInformation(library: LibraryExtended){
        binding.tvLibraryName.text = library.library.name
        binding.tvLibraryDistance.text = String.format("%.1f", library.distance) + " km"
    }

    fun loadFragment(){
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.googlemap) as SupportMapFragment?
        println("CRASHE")
        supportMapFragment!!.getMapAsync(this)
        //loadingEnded()
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE
    }

    private fun drawRoute(map: GoogleMap, coordinates: List<List<Double>>?) {
        val polyLineOptions = PolylineOptions()
        coordinates!!.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        polyLineOptions.color(Color.BLUE)
        polyLineOptions.endCap(RoundCap())
        polyLineOptions.width(6.0f)

        val poly = map.addPolyline(polyLineOptions)
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
