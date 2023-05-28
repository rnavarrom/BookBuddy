package com.example.bookbuddy.ui.navdrawer

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.bookbuddy.R
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.utils.Constants.Companion.profileRequestOptions
import com.example.bookbuddy.utils.Sha
import com.example.bookbuddy.adapters.LanguageSpinnerAdapter
import com.example.bookbuddy.adapters.ProfileAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileBinding
import com.example.bookbuddy.ui.navdrawer.profile.ProfileAuthorDialog
import com.example.bookbuddy.ui.navdrawer.profile.ProfileSearchDialog
import com.example.bookbuddy.utils.*
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.coroutines.CoroutineContext
/**
 * Profile fragment from the navMenu
 */
class ProfileFragment : Fragment(), CoroutineScope, ProfileSearchDialog.OnGenreSearchCompleteListener, ProfileAuthorDialog.OnAuthorSearchCompleteListener, ApiErrorListener {
    lateinit var binding: FragmentProfileBinding
    private var job: Job = Job()
    private val api = CrudApi(this@ProfileFragment)
    private var profileUser: Int? = 0
    private var username: String? = ""
    private var followers: Int = 0
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var gMenu: Menu
    private lateinit var settings: MenuItem
    private lateinit var accept: MenuItem
    private lateinit var cancel: MenuItem
    private lateinit var tmpUri: Uri
    private var tmpGenreId: Int = 0
    private var tmpAuthorId: Int = 0
    private var menuItemsVisibility = mutableMapOf("settings" to true, "accept" to false, "cancel" to false)
    private lateinit var menuItems: ArrayList<MenuItem>

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        gMenu = menu
        settings = gMenu.findItem(R.id.action_settings)
        accept = gMenu.findItem(R.id.action_accept)
        cancel = gMenu.findItem(R.id.action_cancel)
        menuItems = arrayListOf(settings, accept, cancel)

        menuItems.forEach {
            it.isVisible = menuItemsVisibility[it.title.toString()]!!
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentProfileBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val currentLanguageCode = getStoredLanguage()
        val curr = getCurrentLanguageCode(currentLanguageCode)
        val languages = arrayOf("american_flag","catalan_flag")
        val adapter = LanguageSpinnerAdapter(requireContext(), languages)
        binding.languageSpinner.adapter = adapter
        var position = languages.indexOf(curr)
        binding.languageSpinner.setSelection(position)
        var lastSelectedPosition = position

        //Change and store the active language on the app
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != lastSelectedPosition) {
                    val selectedImageName = parent.getItemAtPosition(position).toString()
                    when (selectedImageName){
                        "american_flag" -> {
                            setLocal(requireActivity(), "en")
                            saveLanguageCode(requireActivity().applicationContext,"en")
                        }
                        "catalan_flag" -> {
                            setLocal(requireActivity(), "ca")
                            saveLanguageCode(requireActivity().applicationContext,"ca")
                        }
                    }
                    recreate(requireActivity())
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        if (profileUser == null){
            profileUser = currentUser!!.userId
            username = currentUser!!.name
        }

        loadUser()
        loadTabLayout()
        onLoadingEnded()

        binding.bContacts.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),
                    REQUEST_READ_CONTACTS)
            } else {
                val emails = getEmailsFromContacts()
                val emailList = ArrayList<String>()
                emailList.addAll(emails)
                runBlocking {
                    val coroutine = launch {
                        var addedContacts : Int? = api.getEmailsContact(currentUser!!.userId, emailList)
                        var message = ""
                        if(addedContacts != null){

                        if (addedContacts > 0){
                            message = getString(R.string.MSG_Added) + addedContacts + getString(R.string.MSG_NewContacts)
                        } else {
                            message = getString(R.string.MSG_NoContactsFound)
                        }
                        }
                        showSnackBar(requireContext(), requireView(),message)
                    }
                    coroutine.join()
                }
            }
        }
        //Change password
        binding.bPassword.setOnClickListener {
            val etPassword1 = EditText(requireContext())
            val etPassword2 = EditText(requireContext())
            etPassword1.hint = getString(R.string.LAY_HintPassword)
            etPassword2.hint = getString(R.string.LAY_HintPassword2)
            etPassword1.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            etPassword2.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

            val layout = LinearLayout(requireContext())
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(etPassword1)
            layout.addView(etPassword2)

            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle(getString(R.string.BT_ChangePassword))
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton(getString(R.string.BT_Accept), DialogInterface.OnClickListener { dialog, id ->
                    val password1 = etPassword1.text.toString()
                    val password2 = etPassword2.text.toString()

                    if (password1.isBlank() || password2.isBlank()) {
                        showSnackBar(requireContext(), requireView(), getString(R.string.MSG_PasswordBlank))
                    } else if (password1 != password2){
                        showSnackBar(requireContext(), requireView(), getString(R.string.MSG_PasswordMatch))
                    } else {
                        var result: Boolean? = null
                        var passwordSha = Sha.calculateSHA(password1)
                        runBlocking {
                            launch {
                                result = api.updateUserPasswordId(currentUser!!.userId, passwordSha)
                            }
                        }
                        if (result != null && result as Boolean){
                            showSnackBar(requireContext(), requireView(), getString(R.string.MSG_PasswordChanged))
                        } else {
                            showSnackBar(requireContext(), requireView(), getString(R.string.MSG_NewPassword))
                        }
                    }
                })
                .setNegativeButton(getString(R.string.BT_Cancel), DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                })

            val alert = dialogBuilder.create()
            alert.show()
        }

        return binding.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            tmpUri = imageUri!!
            Glide.with(requireContext())
                .setDefaultRequestOptions(profileRequestOptions)
                .load(tmpUri)
                .into(binding.editProfileImageView)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
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
                    showSnackBar(requireContext(), requireView(),getString(R.string.MSG_GaleryAccesRequired))
                }
                return
            }
            REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val emails = getEmailsFromContacts()
                    val emailList = ArrayList<String>()
                    emailList.addAll(emails)
                } else {
                    showSnackBar(requireContext(), requireView(),getString(R.string.MSG_ContactsAccesRequired))
                }
                return
            }
        }
    }

    fun setLocal(activity: Activity, langCode: String){
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    /**
     * Get the language code from the selected value
     * @param code the selected value
     */
    private fun getCurrentLanguageCode(code: String): String {
        var finalCode: String = code
        if (finalCode == "null"){
            finalCode = requireActivity().applicationContext.resources.configuration.locales.get(0).language.toString()
        }
        return when (finalCode){
            "en" -> {
                "american_flag"
            }
            "ca" -> {
                "catalan_flag"
            }
            else -> {
                "american_flag"
            }
        }
    }
    /**
     * Save the language code on sharedPreferences
     * @param languageCode the language code to be stored
     */
    private fun saveLanguageCode(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("language_code", languageCode)
        editor.apply()
    }
    /**
     * Get the language code stored on sharedPreferences
     * return the stored language code
     */
    private fun getStoredLanguage(): String {
        val sharedPreferences = requireActivity().applicationContext.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        var code = sharedPreferences.getString("language_code", "") ?: ""
        if (code.isEmpty()){
            code = requireActivity().applicationContext.resources.configuration.locales.get(0).language.toString()
        }
        return code
    }

    private fun actionSettings(){
        menuItems.forEach {
            it.isVisible = !menuItemsVisibility[it.title.toString()]!!
            var title = it.title.toString()
            menuItemsVisibility[title] = it.isVisible
        }

        binding.profileImageView.visibility = View.INVISIBLE
        binding.editProfileImageView.visibility = View.VISIBLE


        Glide.with(this)
            .setDefaultRequestOptions(profileRequestOptions)
            .load(binding.profileImageView.drawable)
            .into(binding.editProfileImageView)

        binding.editProfileImageView.setOnClickListener {
            checkPermissions()
        }

        binding.tvUsername.visibility = View.INVISIBLE
        binding.etUsername.visibility = View.VISIBLE
        binding.etUsername.setText(binding.tvUsername.text.toString())

        binding.et1PrefferredGenre.visibility = View.INVISIBLE
        binding.et2PrefferredGenre.visibility = View.VISIBLE
        if (!binding.et1PrefferredGenre.visibility.equals(getString(R.string.MSG_NotSelected))){
            binding.et2PrefferredGenre.setText(binding.et1PrefferredGenre.text.toString())
        }

        binding.et1PrefferredAuthor.visibility = View.INVISIBLE
        binding.et2PrefferredAuthor.visibility = View.VISIBLE
        if (!binding.et1PrefferredAuthor.visibility.equals(getString(R.string.MSG_NotSelected))){
            binding.et2PrefferredAuthor.setText(binding.et1PrefferredAuthor.text.toString())
        }
    }

    private fun actionAccept(){
        menuItems.forEach {
            it.isVisible = !menuItemsVisibility[it.title.toString()]!!
            val title = it.title.toString()
            menuItemsVisibility[title] = it.isVisible
        }

        updateUserName()
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

    private fun updateGenre(){
        var result: Boolean? = false
        if (binding.et1PrefferredGenre.text.toString() != binding.et2PrefferredGenre.text.toString()){
            runBlocking {
                val coroutine = launch {
                    result = api.updateProfileGenreToAPI(currentProfile.profileId, tmpGenreId)
                }
                coroutine.join()
            }
            if (result != null) {
                currentProfile.genre!!.name = binding.et2PrefferredGenre.text.toString()
                binding.et1PrefferredGenre.text = binding.et2PrefferredGenre.text.toString()
            }
        }
    }

    private fun updateAuthor(){
        var result: Boolean? = false
        if (binding.et1PrefferredAuthor.text.toString() != binding.et2PrefferredAuthor.text.toString()){
            runBlocking {
                val coroutine = launch {
                    result = api.updateProfileAuthorToAPI(currentProfile.profileId, tmpAuthorId)
                }
                coroutine.join()
            }
            if (result != null){
                currentProfile.author!!.name = binding.et2PrefferredAuthor.text.toString()
                binding.et1PrefferredAuthor.text = binding.et2PrefferredAuthor.text.toString()
            }
        }
    }

    private fun updateUserName(){
        if (binding.etUsername.text.toString().isNotEmpty()){
            if (binding.tvUsername.text.toString() != binding.etUsername.text.toString()){
                val userName = binding.etUsername.text.toString().trim()
                runBlocking {
                    val coroutine = launch {
                        if (!api.getUserExists(userName)!!){
                            api.updateUserName(currentUser!!.userId, userName)
                            Tools.setNavigationProfile(requireContext(), null, userName)
                            binding.tvUsername.text = binding.etUsername.text.toString()
                        }
                    }
                    coroutine.join()
                }
            }
        }
    }

    private fun actionCancel(){
        menuItems.forEach {
            it.isVisible = !menuItemsVisibility[it.title.toString()]!!
            val title = it.title.toString()
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

    private fun loadUser(){
        binding.tvUsername.text = currentUser!!.name
        runBlocking {
            val coroutine = launch {
                val tempFollowers = api.getFollowerCount(currentUser!!.userId)
                if(tempFollowers != null){
                    followers = tempFollowers
                }
            }
            coroutine.join()
        }
        binding.tvFollowers.text = followers.toString() + getString(R.string.MSG_Followers)

        if (currentProfile.genre != null && !currentProfile.genre!!.name.isNullOrBlank()){
            binding.et1PrefferredGenre.text = currentProfile.genre!!.name
        }

        if (currentProfile.author != null && !currentProfile.author!!.name.isNullOrBlank()){
            binding.et1PrefferredAuthor.text = currentProfile.author!!.name
        }

        if (currentPicture != null){
            binding.profileImageView.visibility = View.VISIBLE
            binding.editProfileImageView.visibility = View.INVISIBLE
            Glide.with(requireContext())
                .setDefaultRequestOptions(profileRequestOptions)
                .load(BitmapFactory.decodeFile(currentPicture!!.absolutePath))
                .into(binding.profileImageView)
        }
    }

    // Change visible layouts and add bindings
    private fun onLoadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE

        binding.et2PrefferredGenre.setOnClickListener {
            val dialog = ProfileSearchDialog()
            dialog.onGenreSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "")
        }

        binding.et2PrefferredAuthor.setOnClickListener {
            val dialog = ProfileAuthorDialog()
            dialog.onAuthorSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "")
        }
    }

    private fun loadTabLayout(){
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.MSG_Comments)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.MSG_Reads)))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = ProfileAdapter(activity?.applicationContext, childFragmentManager,
            tabLayout.tabCount, currentUser!!.userId, true
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

    private fun imageChooser(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    /**
     * Upload the profile image to the server
     * @param imageUri The url from the image
     */
    private fun uploadImage(imageUri: Uri) {
        var bitmap: Bitmap?
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
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
                    val image = MultipartBody.Part.createFormData("image", currentUser!!.userId.toString() + "user.jpg", requestFile)


                    runBlocking {
                        val ru = launch {
                            val response = api.uploadImageToAPI(false, image)
                            if (response != null) {
                                val response2 = api.updateProfilePic(currentUser!!.userId)
                                currentUser!!.haspicture = true
                                val body = response
                                val bytes = body.bytes()
                                val file = File(requireContext().cacheDir, currentUser!!.userId.toString() + "user.jpg")

                                withContext(Dispatchers.IO) {
                                    val outputStream = FileOutputStream(file)
                                    outputStream.write(bytes)
                                    outputStream.close()
                                }

                                currentPicture = file

                                Glide.with(requireContext())
                                    .setDefaultRequestOptions(profileRequestOptions)
                                    .load(BitmapFactory.decodeFile(file.absolutePath))
                                    .into(binding.profileImageView)

                                Tools.setNavigationProfile(requireContext(), file, null)
                            } else {
                                showSnackBar(requireContext(), requireView(), getString(R.string.SB_ImageNotUploaded))
                            }
                        }
                        ru.join()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun checkPermissions(){
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
            imageChooser()
        }else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showSnackBar(requireContext(), requireView(),getString(R.string.SB_GaleryNotAviable))
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
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val REQUEST_READ_EXTERNAL_STORAGE = 2
        private const val REQUEST_READ_CONTACTS = 3
    }
}