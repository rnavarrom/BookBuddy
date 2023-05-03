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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.example.bookbuddy.R
import com.example.bookbuddy.databinding.FragmentScanBinding
import com.example.bookbuddy.utils.navController
import com.example.bookbuddy.utils.navView


class ScanFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    lateinit var binding: FragmentScanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentScanBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        /*
        if (true){
            val bundle = Bundle()
            bundle.putString("isbn", "9788408004097")
            navController.navigate(R.id.nav_book_display, bundle)
        }*/
        return binding.root
    }

    fun startCamera(){
        val scannerView = binding.scannerView
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
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
                //Toast.makeText(activity, it.text, Toast.LENGTH_LONG).show()
                if (it.text.length == 13 && it.text.matches(Regex("\\d+"))){
                    /*
                    val navOptions = NavOptions.Builder()
                        //.setLaunchSingleTop(true)
                        .setPopUpTo(R.id.scanner_view, true)
                        .build()

                    val bundle = Bundle()
                    bundle.putString("isbn", it.text)
                    navController.navigate(R.id.nav_book_display, bundle, navOptions)
                    */
                    /*
                    val bundle = Bundle()
                    bundle.putString("isbn", it.text)
                    var f = BookDisplayFragment()
                    f.arguments = bundle
                    val ft = childFragmentManager.beginTransaction()
                    ft.replace(R.id.my_frame_layout, f)
                    ft.addToBackStack("chat_fragment")
                    ft.commit()
                    */
                    val bundle = Bundle()
                    bundle.putString("isbn", it.text)
                    var action = ScanFragmentDirections.actionNavScanToNavBookDisplay(bundle)
                    navController.navigate(action)
                } else {
                    Toast.makeText(requireContext(), "This is not a ISBN. Press again to Scan", Toast.LENGTH_SHORT).show()
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
                startCamera()
            } else {
                Toast.makeText(requireActivity().applicationContext,
                    "You must give permissions to scan a QR.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onPause() {
        if (allPermissionsGranted()){
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
}