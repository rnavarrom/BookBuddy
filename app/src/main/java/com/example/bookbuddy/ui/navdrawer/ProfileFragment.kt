package com.example.bookbuddy.ui.navdrawer

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.*
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.ProfileAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileBinding
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.currentPicture
import com.example.bookbuddy.utils.currentProfile
import com.example.bookbuddy.utils.currentUser
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext
import com.bumptech.glide.request.transition.Transition
import com.example.bookbuddy.adapters.LanguageSpinnerAdapter
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import java.util.*
import kotlin.collections.ArrayList


class ProfileFragment : Fragment(), CoroutineScope, ProfileSearchDialog.OnGenreSearchCompleteListener, ProfileAuthorDialog.OnAuthorSearchCompleteListener {
    lateinit var binding: FragmentProfileBinding
    private var job: Job = Job()

    private var profileUser: Int? = 0
    private var username: String? = ""
    private var profilepicture: String? = ""

    private var followers: Int = 0
    private var following: Boolean = false

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    private lateinit var gMenu: Menu

    private lateinit var settings: MenuItem
    private lateinit var accept: MenuItem
    private lateinit var cancel: MenuItem

    private lateinit var tmpUri: Uri
    private var tmpGenreId: Int = 0
    private var tmpAuthorId: Int = 0

    var permission = false
    private var menuItemsVisibility = mutableMapOf("settings" to true, "accept" to false, "cancel" to false)
    private lateinit var menuItems: ArrayList<MenuItem>

    fun setLocal(activity: Activity, langCode: String){
        var locale: Locale = Locale(langCode)
        Locale.setDefault(locale)
        var resources = activity.resources
        var config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun getCurrentLanguageCode(code: String): String {
        var finalCode: String = code
        if (finalCode == "null"){
            finalCode = requireActivity().applicationContext.resources.configuration.locales.get(0).language.toString()
        }
        when (finalCode){
            "en" -> {
                return "american_flag"
            }
            "ca" -> {
                return "catalan_flag"
            }
            "es" -> {
                return "spanish_flag"
            }
            else -> {
                return "american_flag"
            }
        }
    }

    private fun saveLanguageCode(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("language_code", languageCode)
        editor.apply()
    }

    fun getStoredLanguage(): String {
        var sharedPreferences = requireActivity().applicationContext.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        var code = sharedPreferences.getString("language_code", "") ?: ""
        if (code.isNullOrEmpty()){
            code = requireActivity().applicationContext.resources.configuration.locales.get(0).language.toString()
        }
        return code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        gMenu = menu
        settings = gMenu.findItem(R.id.action_settings)
        accept = gMenu.findItem(R.id.action_accept)
        cancel = gMenu.findItem(R.id.action_cancel)
        menuItems = arrayListOf(settings, accept, cancel)

        menuItems.forEach {
            it.isVisible = menuItemsVisibility.get(it.title.toString())!!
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> {
                // Navigate to settings screen.
                actionSettings()
                true
            }
            R.id.action_accept -> {
                // Save profile changes.
                actionAccept()
                true
            }
            R.id.action_cancel -> {
                // Cancel profile changes.
                actionCancel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun actionSettings(){
        menuItems.forEach {
            it.isVisible = !menuItemsVisibility.get(it.title.toString())!!
            var title = it.title.toString()
            menuItemsVisibility[title] = it.isVisible
        }

        binding.profileImageView.visibility = View.INVISIBLE
        binding.editProfileImageView.visibility = View.VISIBLE


        Glide.with(this)
            .load(binding.profileImageView.drawable)
            .error(R.drawable.defaultpic)
            .into(binding.editProfileImageView)
        //binding.editProfileImageView.setImageDrawable()

        binding.editProfileImageView.setOnClickListener {
            checkPermissions()
        }

        binding.tvUsername.visibility = View.INVISIBLE
        binding.etUsername.visibility = View.VISIBLE
        binding.etUsername.setText(binding.tvUsername.text.toString())

        binding.et1PrefferredGenre.visibility = View.INVISIBLE
        binding.et2PrefferredGenre.visibility = View.VISIBLE
        if (!binding.et1PrefferredGenre.visibility.equals("Not selected")){
            binding.et2PrefferredGenre.setText(binding.et1PrefferredGenre.text.toString())
        }

        binding.et1PrefferredAuthor.visibility = View.INVISIBLE
        binding.et2PrefferredAuthor.visibility = View.VISIBLE
        if (!binding.et1PrefferredAuthor.visibility.equals("Not selected")){
            binding.et2PrefferredAuthor.setText(binding.et1PrefferredAuthor.text.toString())
        }
    }

    fun actionAccept(){
        menuItems.forEach {
            it.isVisible = !menuItemsVisibility.get(it.title.toString())!!
            var title = it.title.toString()
            menuItemsVisibility[title] = it.isVisible
        }

        updateUserName()
        // TODO: FIX lateinit property tmpUri has not been initialized
        if (this::tmpUri.isInitialized){
            uploadImage(tmpUri)
        }
        updateGenre()
        updateAuthor()
        binding.profileImageView.visibility = View.VISIBLE
        binding.editProfileImageView.visibility = View.INVISIBLE

        binding.tvUsername.visibility = View.VISIBLE
        binding.etUsername.visibility = View.INVISIBLE

        binding.et1PrefferredGenre.visibility = View.VISIBLE
        binding.et2PrefferredGenre.visibility = View.INVISIBLE

        binding.et1PrefferredAuthor.visibility = View.VISIBLE
        binding.et2PrefferredAuthor.visibility = View.INVISIBLE
    }

    fun updateGenre(){
        if (binding.et1PrefferredGenre.text.toString() != binding.et2PrefferredGenre.text.toString()){
            runBlocking {
                val crudApi = CrudApi()
                val corrutina = launch {
                    crudApi.updateProfileGenreToAPI(currentProfile.profileId, tmpGenreId)
                }
                corrutina.join()
            }
            currentProfile.genre!!.name = binding.et2PrefferredGenre.text.toString()
            binding.et1PrefferredGenre.setText(binding.et2PrefferredGenre.text.toString())
        }
    }

    fun updateAuthor(){
        if (binding.et1PrefferredAuthor.text.toString() != binding.et2PrefferredAuthor.text.toString()){
            runBlocking {
                val crudApi = CrudApi()
                val corrutina = launch {
                    crudApi.updateProfileAuthorToAPI(currentProfile.profileId, tmpAuthorId)
                }
                corrutina.join()
            }
            currentProfile.author!!.name = binding.et2PrefferredAuthor.text.toString()
            binding.et1PrefferredAuthor.setText(binding.et2PrefferredAuthor.text.toString())
        }
    }

    fun updateUserName(){
        if (binding.etUsername.text.toString().isNotEmpty()){
            if (!binding.tvUsername.text.toString().equals(binding.etUsername.text.toString())){
                var userName = binding.etUsername.text.toString().trim()
                runBlocking {
                    var crudApi = CrudApi()
                    var corroutine = launch {
                        if (!crudApi.getUserExists(userName, "")!!){
                            crudApi.updateUserName(currentUser.userId, userName)
                            Tools.setNavigationProfile(requireContext(), null, userName)
                            binding.tvUsername.setText(binding.etUsername.text.toString())
                        }
                    }
                    corroutine.join()
                }
            }
        }
    }

    fun actionCancel(){
        menuItems.forEach {
            it.isVisible = !menuItemsVisibility.get(it.title.toString())!!
            var title = it.title.toString()
            menuItemsVisibility[title] = it.isVisible
        }

        binding.profileImageView.visibility = View.VISIBLE
        binding.editProfileImageView.visibility = View.INVISIBLE

        binding.tvUsername.visibility = View.VISIBLE
        binding.etUsername.visibility = View.INVISIBLE

        binding.et1PrefferredGenre.visibility = View.VISIBLE
        binding.et2PrefferredGenre.visibility = View.INVISIBLE

        binding.et1PrefferredAuthor.visibility = View.VISIBLE
        binding.et2PrefferredAuthor.visibility = View.INVISIBLE
    }

    override fun onGenreSearchComplete(result: Int, genreName: String) {
        binding.et2PrefferredGenre.setText(genreName)
        tmpGenreId = result
    }

    override fun onAuthorSearchComplete(result: Int, authorName: String) {
        binding.et2PrefferredAuthor.setText(authorName)
        tmpAuthorId = result
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentProfileBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        var currentLanguageCode = getStoredLanguage()
        var curr = getCurrentLanguageCode(currentLanguageCode)
        val languages = arrayOf("american_flag","catalan_flag","spanish_flag")
        val adapter = LanguageSpinnerAdapter(requireContext(), languages)
        binding.languageSpinner.adapter = adapter
        var position = languages.indexOf(curr)
        binding.languageSpinner.setSelection(position)
        var lastSelectedPosition = position

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != lastSelectedPosition) {
                    val selectedImageName = parent.getItemAtPosition(position).toString()
                    println(selectedImageName)
                    when (selectedImageName){
                        "american_flag" -> {
                            setLocal(requireActivity(), "en")
                            saveLanguageCode(requireActivity().applicationContext,"en")
                        }
                        "catalan_flag" -> {
                            setLocal(requireActivity(), "ca")
                            saveLanguageCode(requireActivity().applicationContext,"ca")
                        }
                        else -> {
                            setLocal(requireActivity(), "es")
                            saveLanguageCode(requireActivity().applicationContext,"es")
                        }
                    }
                    recreate(requireActivity())
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acciones a realizar cuando no se selecciona ningún elemento
            }
        }

        if (profileUser == null){
            profileUser = currentUser.userId
            username = currentUser.name
        }

        launch {
            loadUser()
            loadTabLayout()
            loadingEnded()
        }

        binding.bContacts.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                // Pedir permisos de acceso a los contactos
                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),
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
                        showSnackBar(requireContext(), requireView(),message)
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
        binding.tvUsername.text = currentUser.name
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                followers = crudApi.getFollowerCount(currentUser.userId)!!
            }
            corrutina.join()
        }
        binding.tvFollowers.text = followers.toString() + " seguidores"

        if (currentProfile.genre != null){
            binding.et1PrefferredGenre.setText(currentProfile.genre!!.name)
        }

        if (currentProfile.author != null){
            binding.et1PrefferredAuthor.setText(currentProfile.author!!.name)
        }

        if (currentPicture != null){
            Glide.with(requireContext())
                .load(currentPicture)
                .error(R.drawable.defaultpic)
                .into(binding.profileImageView)
        }
    }

    fun loadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE

        binding.et2PrefferredGenre.setOnClickListener {
            val bundle = Bundle()
            val dialog: ProfileSearchDialog = ProfileSearchDialog()
            dialog.onGenreSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "Date Picker")
        }

        binding.et2PrefferredAuthor.setOnClickListener {
            val bundle = Bundle()
            val dialog: ProfileAuthorDialog = ProfileAuthorDialog()
            dialog.onAuthorSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "Date Picker")
        }
    }

    fun loadTabLayout(){
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        tabLayout.addTab(tabLayout.newTab().setText("COMMENTS"))
        tabLayout.addTab(tabLayout.newTab().setText("READS"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = ProfileAdapter(activity?.applicationContext, childFragmentManager,
            tabLayout.tabCount, currentUser.userId, true
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
            tmpUri = imageUri!!
            // Hacer algo con la imagen seleccionada
            //binding.ivPreviewImage.setImageURI(imageUri)
            if (imageUri != null) {
                //uploadImage(imageUri)
                binding.editProfileImageView.setImageURI(tmpUri)
            }
        }
    }


    fun uploadImage(imageUri: Uri) {
        /*
        val contentResolver = requireContext().contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        */
        var bitmap: Bitmap? = null
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    // Asigna el bitmap a la variable definida fuera del callback
                    bitmap = resource
                    val outputStream = ByteArrayOutputStream()
                    val targetWidth = 320
                    val targetHeight = 320
                    val scaleFactor = Math.min(
                        bitmap!!.width.toDouble() / targetWidth,
                        bitmap!!.height.toDouble() / targetHeight
                    )
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap!!,
                        (bitmap!!.width / scaleFactor).toInt(),
                        (bitmap!!.height / scaleFactor).toInt(),
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
                                        .error(R.drawable.errorimage)
                                        .into(binding.profileImageView)

                                    Tools.setNavigationProfile(requireContext(), file, null)
                                }
                            } else {
                                // TODO: NOSE
                                // Manejar la respuesta de error
                                // ...
                            }
                        }
                        ru.join()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Opcionalmente, puedes hacer algo aquí cuando se borra la carga
                }
            })



        //
    }


    fun checkPermissions(){
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
            imageChooser()
        }else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showSnackBar(requireContext(), requireView(),"Galery acces not available")
            }else{
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageChooser()
                } else {
                    showSnackBar(requireContext(), requireView(),"Galery access is required to pick an image")
                }
                return
            }
            REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val emails = getEmailsFromContacts()
                    val emailList = ArrayList<String>()
                    emailList.addAll(emails)
                } else {
                    showSnackBar(requireContext(), requireView(),"Contacts access is required to import contacts")
                }
                return
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val REQUEST_READ_EXTERNAL_STORAGE = 2
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