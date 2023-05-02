package com.example.bookbuddy.ui.navdrawer

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.ContentValues.TAG
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding

    var permission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentProfileBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        binding.profileImageView.setOnClickListener {
            comprobaPermisos()
        }
        return binding.root
    }

    fun imageChooser(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            // Hacer algo con la imagen seleccionada
            binding.profileImageView.setImageURI(imageUri)
            if (imageUri != null) {
                uploadImage(imageUri)
            }
        }
    }

    fun uploadImage(imageUri: Uri) {
        /*
        val contentResolver = requireContext().contentResolver
        val inputStream: InputStream =
            contentResolver.openInputStream(imageUri)!! // Abre un InputStream para leer la imagen
        val bitmap = BitmapFactory.decodeStream(inputStream) // Convierte el InputStream a un Bitmap
        val outputStream = ByteArrayOutputStream() // Crea un OutputStream para escribir el Bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Comprime el Bitmap en formato JPEG y escribe los bytes en el OutputStream
        val imageBytes = outputStream.toByteArray() // Convierte el OutputStream en un ByteArray
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        */
        var base64Image = ""
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .override(400, 400) // Aquí puedes especificar el tamaño deseado de la imagen
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()

                    // Convertir el arreglo de bytes a una cadena de texto codificada en base64
                    base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
                }


                override fun onLoadCleared(placeholder: Drawable?) {
                    // Este método se llama cuando la imagen ha sido eliminada de la memoria caché
                }
            })
        val crudApi = CrudApi()
        runBlocking {
            val ru = launch {
                crudApi.addImageToAPI(base64Image)
            }
            ru.join()
        }
    }


    fun comprobaPermisos(){
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
            permission = true
            imageChooser()
        }else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(requireContext(), "El permís READ EXTERNAL STORAGE no està disponible. S'ha de canviar als ajustaments", Toast.LENGTH_LONG).show()
                permission = false
            }else{
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission = true
                    imageChooser()

                } else {
                    Toast.makeText(requireContext(), "No s'han acceptat els permisos, per poder utilitzar la gravadora canvia-ho als ajustaments", Toast.LENGTH_LONG).show()
                    permission = false
                }
                return
            }
        }
    }



    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2

    }
}