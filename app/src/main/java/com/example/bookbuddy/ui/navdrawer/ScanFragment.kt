package com.example.bookbuddy.ui.navdrawer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentScanBinding
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ScanFragment : Fragment(), ApiErrorListener {
    private lateinit var codeScanner: CodeScanner
    lateinit var binding: FragmentScanBinding

    private var isScannerEnabled = false
    private var isDialogOpen = false
    private val api = CrudApi(this@ScanFragment)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentScanBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted() && !isDialogOpen) {
            isScannerEnabled = true
            startCamera()
            codeScanner.startPreview()
        } else if (!isDialogOpen){
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        /*
        val currentDestination = navController.currentDestination
        val isDialogOpen = currentDestination?.id == R.id.nav_book_display
        if (allPermissionsGranted() && !isDialogOpen) {
            isScannerEnabled = true
            startCamera()
            codeScanner.startPreview()
        } else if (isDialogOpen){
            //TODO : ????
            //println("El idalogog esta abierto")
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }*/
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                isScannerEnabled = true
                startCamera()
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_CammerAccessNedded))
                navController.popBackStack()
            }
        }
    }
    override fun onPause() {
        if (this::codeScanner.isInitialized){
            isScannerEnabled = false
            codeScanner.releaseResources()
        }
        super.onPause()
    }

    private fun bookExist(isbn: String): Boolean? {
        var exist : Boolean? = false
        runBlocking {
            val coroutine = launch {
                exist = api.getBookExist(isbn)
            }
            coroutine.join()
        }
        return exist
    }

    private fun createRequest(isbn: String): Boolean? {
        var succes: Boolean? = false
        runBlocking {
            val coroutine = launch {
                succes = api.addRequestAPI(isbn)!!
            }
            coroutine.join()
        }
        return succes
    }

    private fun startCamera(){
        val scannerView = binding.scannerView
        val activity = requireActivity()
        if (!this::codeScanner.isInitialized){
            codeScanner = CodeScanner(activity, scannerView)
        }
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                // Vibrate
                val isbnLength = 13
                val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                if (it.text.length == isbnLength && it.text.matches(Regex("\\d+"))){
                    codeScanner.releaseResources()
                    val bookExists: Boolean? = bookExist(it.text)
                    if (bookExists == null) {
                        showSnackBar(requireContext(), requireView(), getString(R.string.SB_ScanFailed))
                    } else if (!bookExists) {  //!bookExist(it.text)!!
                        val created: Boolean? = createRequest(it.text)
                        if (created == true) {
                            showSnackBar(requireContext(), requireView(), getString(R.string.SB_AddedBookPending))
                        } else {
                            showSnackBar(requireContext(), requireView(), getString(R.string.SB_BookAlreadyRequested))
                        }
                        codeScanner.startPreview()
                    } else {
                        val bundle = Bundle()
                        bundle.putString("isbn", it.text)
                        val action = ScanFragmentDirections.actionNavScanToNavBookDisplay(bundle)
                        navController.navigate(action)
                        isDialogOpen = true
                        isScannerEnabled = false
                        codeScanner.releaseResources()
                    }
                } else {
                    codeScanner.startPreview()
                    showSnackBar(requireContext(),requireView(),getString(R.string.SB_TryAgainScan))
                }
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
        codeScanner.startPreview()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().applicationContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onApiError(connectionFailed: Boolean) {
        showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}