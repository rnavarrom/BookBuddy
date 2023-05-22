package com.example.bookbuddy.ui.navdrawer.adminnav

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentInsertLibraryDialogBinding
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.ui.navdrawer.HomeFragment
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class InsertLibraryDialog : DialogFragment(), CoroutineScope, OnMapReadyCallback, ApiErrorListener {
    lateinit var binding: FragmentInsertLibraryDialogBinding
    private var job: Job = Job()

    private var mode = "insert"
    private lateinit var library: Library

    lateinit var mMap: GoogleMap
    private var currentMarker: Marker? = null

    public var onAdminDialogClose: OnAdminDialogClose? = null

    public interface OnAdminDialogClose {
        fun onAdminDialogClose()
    }

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
        binding =  FragmentInsertLibraryDialogBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


        val bundle = arguments?.getBundle("bundle")
        var toolbarMessage = ""
        val fragment = bundle!!.getSerializable("fragment") as? AdminFragment?
        if (fragment != null){
            onAdminDialogClose = fragment
        }

        if (bundle.containsKey("library")){
            mode = "edit"
            library = bundle.getSerializable("library") as Library
        }

        if (mode == "edit"){
            toolbarMessage = "Edit library"
            binding.etId.text = library.libraryId.toString()
            binding.etName.setText(library.name)
            binding.etZip.setText(library.zipCode)
            binding.etLat.setText(library.lat.toString())
            binding.etLon.setText(library.lon.toString())
        } else {
            toolbarMessage = "Insert library"
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

    fun editLibrary(){
        var result = false

        var name = binding.etName.text.toString().trim()
        var zip = binding.etZip.text.toString()
        var lat = binding.etLat.text.toString().toDoubleOrNull()
        var lon = binding.etLon.text.toString().toDoubleOrNull()
        println(binding.etLon.text.toString())
        if (name.isNullOrEmpty()){
            showSnackBar(requireContext(), requireView(), "Name cannot be empty")
            return
        }
        if (zip.isNullOrEmpty()){
            showSnackBar(requireContext(), requireView(), "Zip cannot be empty")
            return
        } else {
            if (zip.length < 5){
                showSnackBar(requireContext(), requireView(), "Zip of 5 chars")
                return
            }
        }
        if (lat == null){
            showSnackBar(requireContext(), requireView(), "Latitude cannot be empty")
            return
        }
        if (lon == null){
            showSnackBar(requireContext(), requireView(), "Latitude cannot be empty")
            return
        }

        runBlocking {
            var api = CrudApi(this@InsertLibraryDialog)
            var coroutine = launch {
                if (mode == "edit"){
                    result = api.updateLibrary(library.libraryId,name, lat, lon, zip)!!
                } else {
                    result = api.insertLibrary(name, lat, lon, zip)!!
                }
            }
            coroutine.join()
        }

        if (result) {
            onAdminDialogClose!!.onAdminDialogClose()
            showSnackBar(requireActivity().applicationContext, requireParentFragment().requireView(), "Library Edited")
            dismiss()
        } else {
            showSnackBar(requireContext(), requireView(), "Duplicated library name")
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

            var zoom: Float= 17.0f
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
            var lat = binding.etLat.text.toString().toDoubleOrNull()
            var lon = binding.etLon.text.toString().toDoubleOrNull()

            if (lat != null && lon != null){
                val lib = LatLng(lat, lon)

                currentMarker?.remove()

                currentMarker = mMap.addMarker(MarkerOptions().title(binding.etName.text.toString()).position(lib))
                var zoom: Float= 17.0f
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(lib, zoom),
                    2500, null
                )
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

    override fun onApiError() {
        showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}