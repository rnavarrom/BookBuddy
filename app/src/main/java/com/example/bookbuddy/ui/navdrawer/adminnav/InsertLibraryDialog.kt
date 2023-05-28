package com.example.bookbuddy.ui.navdrawer.adminnav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogInsertLibraryBinding
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Dialog to insert o edit a library
 */
class InsertLibraryDialog : DialogFragment(), CoroutineScope, OnMapReadyCallback, ApiErrorListener {
    lateinit var binding: DialogInsertLibraryBinding
    private var job: Job = Job()

    private var mode = "insert"
    private lateinit var library: Library

    private lateinit var mMap: GoogleMap
    private var currentMarker: Marker? = null

    var onAdminDialogClose: OnAdminDialogClose? = null
    private val api = CrudApi(this@InsertLibraryDialog)
    interface OnAdminDialogClose {
        fun onAdminDialogClose()
    }

    // Set fullscreen dialog style
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
        binding =  DialogInsertLibraryBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)


        val bundle = arguments?.getBundle("bundle")
        val toolbarMessage: String
        val fragment = bundle!!.getParcelable("fragment") as? AdminFragment?
        if (fragment != null){
            onAdminDialogClose = fragment
        }

        if (bundle.containsKey("library")){
            mode = "edit"
            library = bundle.getParcelable("library")!!
        }

        if (mode == "edit"){
            toolbarMessage = getString(R.string.EditLibrary)
            binding.etId.text = library.libraryId.toString()
            binding.etName.setText(library.name)
            binding.etZip.setText(library.zipCode)
            binding.etLat.setText(library.lat.toString())
            binding.etLon.setText(library.lon.toString())
        } else {
            toolbarMessage = getString(R.string.InsertLibrary)
            binding.tvId.visibility = View.GONE
            binding.etId.visibility = View.GONE
        }

        setToolBar(this, binding.toolbar, requireContext(), toolbarMessage)

        binding.btnAccept.setOnClickListener {
            editLibrary()

        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.googlemap) as SupportMapFragment?
        supportMapFragment!!.getMapAsync(this)

        return binding.root
    }

    private fun editLibrary(){
        var result = false

        val name = binding.etName.text.toString().trim()
        val zip = binding.etZip.text.toString()
        val lat = binding.etLat.text.toString().toDoubleOrNull()
        val lon = binding.etLon.text.toString().toDoubleOrNull()
        println(binding.etLon.text.toString())
        if (name.isNullOrEmpty()){
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_CantEmptyName))
            return
        }
        if (zip.isNullOrEmpty()){
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_EmptyZip))
            return
        } else {
            if (zip.length < 5){
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_ZipLenght))
                return
            }
        }
        if (lat == null){
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_Latitude))
            return
        }
        if (lon == null){
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_Longitude))
            return
        }

        runBlocking {
            val coroutine = launch {
                result = if (mode == "edit"){
                    api.updateLibrary(library.libraryId,name, lat, lon, zip)!!
                } else {
                    api.insertLibrary(name, lat, lon, zip)!!
                }
            }
            coroutine.join()
        }

        if (result) {
            onAdminDialogClose!!.onAdminDialogClose()
            showSnackBar(requireActivity().applicationContext, requireParentFragment().requireView(), getString(
                            R.string.SB_LibraryEdited))
            dismiss()
        } else {
            showSnackBar(requireContext(), requireView(), getString(
                R.string.SB_DuplicateLibraryName))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (mode == "edit"){
            val lib = LatLng(library.lat, library.lon)

            currentMarker = mMap.addMarker(
                MarkerOptions().position(lib).title(library.name)
            )

            val zoom = 17.0f
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(lib, zoom),
                2500, null
            )
        }

        mMap.setOnMapClickListener { latLng ->
            currentMarker?.remove()

            currentMarker = mMap.addMarker(MarkerOptions().position(latLng))
            binding.etLat.setText(latLng.latitude.toString())
            binding.etLon.setText(latLng.longitude.toString())
        }

        binding.btnGoto.setOnClickListener {
            val lat = binding.etLat.text.toString().toDoubleOrNull()
            val lon = binding.etLon.text.toString().toDoubleOrNull()

            if (lat != null && lon != null){
                val lib = LatLng(lat, lon)

                currentMarker?.remove()

                currentMarker = mMap.addMarker(MarkerOptions().title(binding.etName.text.toString()).position(lib))
                val zoom = 17.0f
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(lib, zoom),
                    2500, null
                )
            }
        }

    }


    override fun onApiError(connectionFailed: Boolean) {
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