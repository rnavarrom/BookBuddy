package com.example.bookbuddy.ui.navdrawer

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.ProfileAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileBinding
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.Tools.Companion.getPathFromUri
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext


class ProfileFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentProfileBinding
    private var job: Job = Job()

    private var profileUser: Int? = 0
    private var username: String? = ""
    private var profilepicture: String? = ""

    private var followers: Int = 0
    private var following: Boolean = false

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

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

        val bundle = arguments?.getBundle("bundle")
        profileUser = bundle?.getInt("userid", currentUser.userId)
        username = bundle?.getString("username",  currentUser.name)

        if (profileUser == null){
            profileUser = currentUser.userId
            username = currentUser.name
        }

        launch {
            println("CHECKPOINT 1")
            loadUser()
            println("CHECKPOINT 2")
            loadTabLayout()
            println("CHECKPOINT 3")
            loadingEnded()
        }

        binding.bSelectImage.setOnClickListener {
            comprobaPermisos()
        }

        binding.bContacts.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                // Pedir permisos de acceso a los contactos
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    REQUEST_READ_CONTACTS)
            } else {
                // Obtener los correos electrónicos de los contactos
                val emails = getEmailsFromContacts()
                // Guardar los correos electrónicos en una lista
                val emailList = ArrayList<String>()
                emailList.addAll(emails)
                runBlocking {
                    val crudApi = CrudApi()
                    val corrutina = launch {
                        //var a = crudApi.getEmailsContact(currentUser.userId, listOf("email1","email2"))
                        var addedContacts = crudApi.getEmailsContact(currentUser.userId, emailList)!!
                        var message = ""
                        if (addedContacts > 0){
                            message = "Se han agregado " + addedContacts + " contactos nuevos!"
                        } else {
                            message = "No se ha encontrado ningun contacto"
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                    corrutina.join()
                }
            }



        }

        return binding.root
    }

    private fun getEmailsFromContacts(): List<String> {
        val emails = ArrayList<String>()
        val contentResolver: ContentResolver = requireActivity().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            if (columnIndex >= 0) {
                do {
                    val email = cursor.getString(columnIndex)
                    if (email != null) {
                        emails.add(email)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        return emails
    }

    fun loadUser(){
        binding.tvUsername.text = username
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                followers = crudApi.getFollowerCount(profileUser!!)!!
                following = crudApi.getIsFollowing(1,profileUser!!)!!
            }
            corrutina.join()
        }
        binding.tvFollowers.text = followers.toString() + " seguidores"

        if (currentPicture != null){
            Glide.with(requireContext())
                .load(currentPicture)
                .into(binding.profileImageView)
        }

        followButton()

    }

    fun followButton(){

        if (following){
            binding.btFollow.tag = "Following"
            binding.btFollow.text = "Following"
        } else {
            binding.btFollow.tag = "Follow"
            binding.btFollow.text = "Follow"
        }

        binding.btFollow.setOnClickListener {
            if (binding.btFollow.tag.equals("Follow")){
                var followed = false
                runBlocking {
                    val crudApi = CrudApi()
                    val corrutina = launch {
                        followed = crudApi.addFollowToAPI(1, profileUser!!)
                    }
                    corrutina.join()
                }
                if (followed){
                    binding.btFollow.text = "Following"
                    binding.btFollow.tag = "Following"
                }
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Do you want to unfollow " + binding.tvUsername.text + "?")
                builder.setMessage("You will stop receibing notifications from this user")
                    .setPositiveButton("Unfollow",
                        DialogInterface.OnClickListener { dialog, id ->
                            runBlocking {
                                val crudApi = CrudApi()
                                val corrutina = launch {
                                    crudApi.deleteFollowAPI(1, profileUser!!)
                                }
                                corrutina.join()
                            }
                            binding.btFollow.text = "Follow"
                            binding.btFollow.tag = "Follow"
                        })
                    .setNegativeButton("Cancell",
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
                builder.show()
            }
        }
    }

    fun loadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
        /*
        if (currentPicture.isSuccessful){
            val body = currentPicture.body()
            if (body != null) {
                // Leer los bytes de la imagen
                val bytes = body.bytes()

                // Guardar los bytes en un archivo
                val file = File(requireContext().cacheDir, currentUser.userId.toString() + "user.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.write(bytes)
                outputStream.close()

                // Mostrar la imagen en un ImageView usando Glide
                Glide.with(requireContext())
                    .load(file)
                    .into(binding.ivPreviewImage)
            }
        }
        */
    }

    fun loadTabLayout(){
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        tabLayout.addTab(tabLayout.newTab().setText("COMMENTS"))
        tabLayout.addTab(tabLayout.newTab().setText("READS"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = ProfileAdapter(activity?.applicationContext, childFragmentManager,
            tabLayout.tabCount, profileUser!!, true
        )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
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
            //binding.ivPreviewImage.setImageURI(imageUri)
            if (imageUri != null) {
                uploadImage(imageUri)
            }
        }
    }

    fun uploadImage(imageUri: Uri) {
        val contentResolver = requireContext().contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        val targetWidth = 320
        val targetHeight = 320
        val scaleFactor = Math.min(
            bitmap.width.toDouble() / targetWidth,
            bitmap.height.toDouble() / targetHeight
        )
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width / scaleFactor).toInt(),
            (bitmap.height / scaleFactor).toInt(),
            false
        )
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()

        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        val image = MultipartBody.Part.createFormData("image", currentUser.userId.toString() + "user.jpg", requestFile)

        val crudApi = CrudApi()
        runBlocking {
            val ru = launch {
                val response = crudApi.uploadImageToAPI(image)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Leer los bytes de la imagen
                        val bytes = body.bytes()
                        //requireContext().cacheDir.deleteRecursively()
                        val file = File(requireContext().cacheDir, currentUser.userId.toString() + "user.jpg")

                        val outputStream = FileOutputStream(file)
                        outputStream.write(bytes)
                        outputStream.close()

                        currentPicture = file

                        Glide.with(requireContext())
                            .load(BitmapFactory.decodeFile(file.absolutePath))
                            .into(binding.profileImageView)

                        Tools.setNavigationProfile(requireContext(), file, null)
                    }
                } else {
                    // Manejar la respuesta de error
                    // ...
                }
            }
            ru.join()
        }

        //
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
            REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso de acceso a los contactos concedido
                    // Obtener los correos electrónicos de los contactos
                    val emails = getEmailsFromContacts()
                    // Guardar los correos electrónicos en una lista
                    val emailList = ArrayList<String>()
                    emailList.addAll(emails)
                }
                return
            }
        }
    }

    /*
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
    */

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2
        private const val REQUEST_READ_CONTACTS = 3
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