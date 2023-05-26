package com.example.bookbuddy.ui.navdrawer.bookdisplay

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogBookdisplayLibraryMapBinding
import com.example.bookbuddy.models.CleanResponse
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
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

class LibraryMapDialog : DialogFragment(), OnMapReadyCallback, CoroutineScope {
    lateinit var binding: DialogBookdisplayLibraryMapBinding

    private var job: Job = Job()

    //private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequestCode = 0
    private var permissionsGranted = false
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var method : String? = "walking"
    private var library: LibraryExtended? = null
    private lateinit var mMap: GoogleMap

    private val api = CrudApi()
    private var isGpsEnabled = false
    private var resp : CleanResponse? = null

    lateinit var bundle : Bundle
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
        binding =  DialogBookdisplayLibraryMapBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        setToolBar(this, binding.toolbar, requireContext(), "Map")

        bundle = arguments?.getBundle("bundle")!!
        if (bundle.containsKey("latitude") && bundle.containsKey("longitude") && bundle.containsKey("method")){
            latitude = bundle.getDouble("latitude")
            longitude = bundle.getDouble("longitude")
            method = bundle.getString("method")
        }

        if (bundle != null) {
            library = bundle.getSerializable("library") as? LibraryExtended
            if (library != null) {
                loadLibraryBasicInformation(library!!)
            }
        }
        requestPermissionsMap()

        if (library != null){
            val supportMapFragment = childFragmentManager.findFragmentById(R.id.googlemap) as SupportMapFragment?
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

        if (library != null) {
            println("DEF1")
            val supportMapFragment =
                childFragmentManager.findFragmentById(R.id.googlemap) as SupportMapFragment?
            supportMapFragment!!.getMapAsync(this)
            println("DEF2")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (permissionsGranted && latitude != null && longitude != null && isGpsEnabled){
            val start = LatLng(latitude!!, longitude!!)
            val end = LatLng(library!!.library.lat, library!!.library.lon)
            mMap = googleMap
            checkPermissionsTMP()
            val lib = LatLng(library!!.library.lat, library!!.library.lon)

            mMap.addMarker(
                MarkerOptions().position(lib).title(library!!.library.name)
            )

            launch {

                val startString =
                    start.longitude.toString() + ", " + start.latitude.toString()
                val endString =
                    end.longitude.toString() + ", " + end.latitude.toString()

                binding.tvLibraryName.setOnClickListener {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(lib, 17.0f),
                        1500, null
                    )
                }

                resp = if (method == "car")
                    api.getCarRoute(startString, endString)
                else
                    api.getWalkingRoute(startString, endString)
                if (resp != null) {
                    drawRoute(mMap, resp!!.coordinates)

                    val distance = resp!!.distance
                    val timeInSeconds = resp!!.duration

                    var distanceText: String?
                    var timeText: String?

                    if (distance < 1000) {
                        distanceText = "Distancia: " + distance.toString() + " m"
                    } else {
                        val distanceInKm = distance / 1000.0
                        distanceText = "Distancia: " + String.format("%.2f", distanceInKm) + " km"
                    }

                    val hours = (timeInSeconds / 3600).toInt()
                    val minutes = ((timeInSeconds % 3600) / 60).toInt()

                    val formattedHours = String.format("%02d", hours)
                    val formattedMinutes = String.format("%02d", minutes)

                    timeText = "Tiempo: ${formattedHours}:${formattedMinutes} " + "h"

                    binding.tvLibraryDistance.text = distanceText
                    binding.tvLibraryTime.text = timeText

                    val middleLocation = LatLng((latitude!!+library!!.library.lat)/2, (longitude!!+library!!.library.lon)/2)
                    val zoom: Float?
                    zoom = if (resp!!.distance < 1000.0)
                        15.0f
                    else if (resp!!.distance<= 5000.0)
                        14.0f
                    else if (resp!!.distance<= 10000.0)
                        13.0f
                    else if (resp!!.distance<= 15000.0)
                        12.0f
                    else
                        11.0f
                    loadingEnded()
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(middleLocation, zoom),
                        2500, null
                    )
                } else {
                    binding.tvLibraryTime.visibility = View.GONE
                    binding.tvLibraryDistance.visibility = View.GONE

                    loadingEnded()
                    showSnackBar(requireContext(), requireView(), "Could not trace a route")
                }
            }
        } else {
            mMap = googleMap
            val lib = LatLng(library!!.library.lat, library!!.library.lon)
            mMap.addMarker(
                MarkerOptions().position(lib).title(library!!.library.name)
            )

            launch {
                val zoom = 17.0f
                binding.tvLibraryName.setOnClickListener {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(lib, zoom),
                        1500, null
                    )
                }

                loadingEnded()
                mMap.animateCamera(

                    CameraUpdateFactory.newLatLngZoom(lib, zoom),
                    2500, null
                )
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
                loadFragment()
            }
        }
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
            loadFragment()
        } else {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                //Toast.makeText(requireContext(),"El permís ACCESS_FINE_LOCATION no està disponible",Toast.LENGTH_LONG).show()
                permissionsGranted = false
                loadFragment()
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    //Toast.makeText(requireContext(),"El permís ACCESS_COARSE_LOCATION no està disponible",Toast.LENGTH_LONG).show()
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
            loadFragment()
        }
    }

    private fun checkPermissionsTMP() {
        if (
            (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            permissionsGranted = true
            if (isGpsEnabled){
                mMap.isMyLocationEnabled = true
            }
        }
    }

    private fun loadLibraryBasicInformation(library: LibraryExtended){
        binding.tvLibraryName.text = library.library.name
        //binding.tvLibraryDistance.text = String.format("%.1f", library.distance) + " km"
    }

    private fun loadFragment(){
        if (!permissionsGranted || latitude == null || longitude == null){
            binding.tvLibraryTime.visibility = View.GONE
            binding.tvLibraryDistance.visibility = View.GONE
        }
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.googlemap) as SupportMapFragment?
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
        polyLineOptions.color(ContextCompat.getColor(requireContext(), R.color.primary_green))
        polyLineOptions.endCap(RoundCap())
        polyLineOptions.width(6.0f)

        map.addPolyline(polyLineOptions)
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
