package com.example.bookbuddy.ui.navdrawer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentScanBinding
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ScanFragment : Fragment(), ApiErrorListener {
    private lateinit var codeScanner: CodeScanner
    lateinit var binding: FragmentScanBinding

    private var isScannerEnabled = false
    private var isDialogOpen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentScanBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        return binding.root
    }

    fun bookExist(isbn: String): Boolean? {
        var exist : Boolean? = false
        runBlocking {
            var api = CrudApi(this@ScanFragment)
            var corroutine = launch {
                exist = api.getBookExist(isbn, "")
            }
            corroutine.join()
        }
        return exist
    }

    fun createRequest(isbn: String): Boolean? {
        var succes: Boolean? = false
        runBlocking {
            var api = CrudApi(this@ScanFragment)
            var corroutine = launch {
                succes = api.addRequestAPI(isbn, "")!!
            }
            corroutine.join()
        }
        return succes
    }

    fun startCamera(){
        val scannerView = binding.scannerView
        val activity = requireActivity()
        // TODO: FIX THIS
        if (!this::codeScanner.isInitialized){
            codeScanner = CodeScanner(activity, scannerView)
        }

        //codeScanner.formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                // Vibrate
                val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(200)
                }
                if (it.text.length == 13 && it.text.matches(Regex("\\d+"))){
                    codeScanner.releaseResources()
                    if (!bookExist(it.text)!!) {
                        var a: Boolean? = bookExist(it.text)

                        if (a == null) {

                        } else if (a == false) {  //!bookExist(it.text)!!
                            var created: Boolean? = createRequest(it.text)
                            if (created == true) {
                                showSnackBar(
                                    requireContext(),
                                    requireView(),
                                    "Added book for pending"
                                )
                            } else {
                                showSnackBar(
                                    requireContext(),
                                    requireView(),
                                    "Book already requested to add"
                                )
                            }
                            codeScanner.startPreview()
                        } else {
                            val bundle = Bundle()
                            bundle.putString("isbn", it.text)
                            var action =
                                ScanFragmentDirections.actionNavScanToNavBookDisplay(bundle)
                            navController.navigate(action)
                            isDialogOpen = true
                            isScannerEnabled = false
                            codeScanner.releaseResources()
                        }
                    }
                } else {
                    codeScanner.startPreview()
                    showSnackBar(requireContext(),requireView(),"This is not a ISBN. Press again to Scan")
                }

            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
        codeScanner.startPreview()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                isScannerEnabled = true
                startCamera()
            } else {
                showSnackBar(requireContext(), requireView(),"Camera access needed to scan codes")
                navController.popBackStack()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val currentDestination = navController.currentDestination
        val isDialogOpen = currentDestination?.id == R.id.nav_book_display

        if (allPermissionsGranted() && !isDialogOpen) {
            isScannerEnabled = true
            startCamera()
            codeScanner.startPreview()
        } else if (isDialogOpen){
            println("El idalogog esta abierto")
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onPause() {
        if (this::codeScanner.isInitialized){
            isScannerEnabled = false
            codeScanner.releaseResources()
        }
        super.onPause()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().applicationContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onApiError(errorMessage: String) {
        Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}