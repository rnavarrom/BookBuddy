package com.example.bookbuddy.ui.navdrawer.adminnav

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogAdminInsertBookBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.utils.Constants.Companion.BASE_URL
import com.example.bookbuddy.utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Dialog to insert or edit a book
 */
class InsertBookDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: DialogAdminInsertBookBinding
    private var job: Job = Job()
    private var mode = "insert"
    private lateinit var book: Book
    private var onAdminDialogClose: OnAdminDialogClose? = null
    private lateinit var tmpUri: Uri
    var fragment: AdminFragment? = null
    private var isRequest = false
    private var requestId = 0
    private val api = CrudApi(this@InsertBookDialog)

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

    // Get the mode of the dialog, insert or edit
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAdminInsertBookBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val bundle = arguments?.getBundle("bundle")
        var toolbarMessage = ""
        //Handle bundle value if any to load correct fragment
        if (bundle != null && bundle.containsKey("fragment")) {
            fragment = bundle.getParcelable("fragment") as? AdminFragment?
            if (fragment != null) {
                onAdminDialogClose = fragment
            }
            if (bundle.containsKey("book")) {
                mode = "edit"
                book = bundle.getParcelable("book")!!
            }

            if (bundle.containsKey("isbn")) {
                isRequest = true
                requestId = bundle.getInt("id")
                val isbn = bundle.getString("isbn")
                binding.etIsbn.setText(isbn)
                binding.etIsbn.focusable = View.NOT_FOCUSABLE
            }

            if (mode == "edit") {
                toolbarMessage = getString(R.string.EditBook)
                binding.etId.text = book.bookId.toString()
                binding.etIsbn.setText(book.isbn)
                binding.etTitle.setText(book.title)
                binding.etDescription.setText(book.description)
                binding.etPages.setText(book.pages.toString())
                binding.etDate.setText(book.publicationDate)
                binding.etCover.setText(book.cover)
                Glide.with(requireActivity().applicationContext)
                    .setDefaultRequestOptions(bookRequestOptions)
                    .load(book.cover)
                    .into(binding.ivCover)
            } else {
                toolbarMessage = getString(R.string.InsertBook)
                binding.tvId.visibility = View.GONE
                binding.etId.visibility = View.GONE
                binding.btnReferences.visibility = View.GONE
            }
        }

        setToolBar(this, binding.toolbar, requireContext(), toolbarMessage)

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnReferences.setOnClickListener {
            openReferences()
        }

        binding.btnCover.setOnClickListener {
            checkPermissions()
        }

        binding.btnAccept.setOnClickListener {
            editLibrary()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    // Save image after being selected on the galery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Load image from gallery to glide
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            tmpUri = imageUri!!

            Glide.with(requireContext())
                .setDefaultRequestOptions(bookRequestOptions)
                .load(tmpUri)
                .into(binding.ivCover)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION) {
            // Check granted permisions
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If granted open gallery
                imageChooser()
            } else {
                // Permisions not granted
                showSnackBar(
                    requireContext(),
                    requireView(),
                    getString(R.string.SB_GaleryAccesRequired)
                )
            }
        }
    }

    //Navigate to book references
    private fun openReferences() {
        val bundle = Bundle()
        bundle.putInt("bookid", book.bookId)
        val action = InsertBookDialogDirections.actionNavInsertBookToNavReferencesBook(bundle)
        navController.navigate(action)
    }

    private fun showDatePicker() {
        val dateString = binding.etDate.text.toString()

        val initialYear: Int
        val initialMonth: Int
        val initialDay: Int
        val calendar = Calendar.getInstance()
        if (dateString.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            initialYear = calendar.get(Calendar.YEAR)
            initialMonth = calendar.get(Calendar.MONTH)
            initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        } else {
            initialYear = calendar.get(Calendar.YEAR)
            initialMonth = calendar.get(Calendar.MONTH)
            initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                binding.etDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val datePicker = datePickerDialog.datePicker
        datePicker.updateDate(initialYear, initialMonth, initialDay)
        datePicker.maxDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }

    private fun editLibrary() {
        var result: Book? = null
        var editResult: Boolean? = null
        val isbn = binding.etIsbn.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val pages = binding.etPages.text.toString().toIntOrNull()
        val date = binding.etDate.text.toString()
        var cover = binding.etCover.text.toString().trim()
        //Check if the values ar correctly added
        if (isbn.isEmpty()) {
            showSnackBar(requireContext(), requireView(), getString(R.string.ISBNEmptyWarning))
        }
        if (isbn.length < 13) {
            showSnackBar(requireContext(), requireView(), getString(R.string.ISBNMaxLenght))
        }
        if (this::book.isInitialized && (book.isbn != isbn || !binding.etIsbn.isFocusable)) {
            var isbnExist = false
            runBlocking {

                val coroutine = launch {
                    isbnExist = api.getBookExist(isbn)!!
                }
                coroutine.join()
            }
            if (isbnExist) {
                showSnackBar(
                    requireContext(),
                    requireView(),
                    getString(R.string.ISBNDuplicateWarning)
                )
                return
            }
        }
        if (title.isEmpty()) {
            showSnackBar(requireContext(), requireView(), getString(R.string.TitleEmptyWarning))
            return
        }
        if (pages == null) {
            showSnackBar(requireContext(), requireView(), getString(R.string.PagesEmptyWarning))
            return
        }
        if (date.isEmpty()) {
            showSnackBar(requireContext(), requireView(), getString(R.string.PublishEmptyWarning))
            return
        }
        if (this::tmpUri.isInitialized) {
            var bitmap: Bitmap?
            // Use glide to get hte bitmap of an image to upload to the server, with glide the
            // image flips automatically
            Glide.with(this)
                .asBitmap()
                .load(tmpUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bitmap = resource
                        val outputStream = ByteArrayOutputStream()
                        val targetWidth = 120
                        val targetHeight = 180
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

                        val requestFile =
                            RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
                        val image = MultipartBody.Part.createFormData(
                            "image",
                            isbn + "book.jpg",
                            requestFile
                        )
                        cover = BASE_URL + "api/book/cover/" + isbn + "book.jpg"

                        // Upload image to te server
                        runBlocking {
                            val ru = launch {
                                val response = api.uploadImageToAPI(true, image)
                                if (response == null) {
                                    showSnackBar(
                                        requireContext(),
                                        requireView(),
                                        getString(R.string.MSG_ErrorUploadImg)
                                    )
                                }
                            }
                            ru.join()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            if (cover.isEmpty()) {
                showSnackBar(
                    requireContext(),
                    requireView(),
                    getString(R.string.CoverWarningMessage)
                )
                return
            }
        }

        // Insert or edit book
        runBlocking {
            val coroutine = launch {
                if (mode == "edit") {
                    editResult = api.updateBook(
                        book.bookId,
                        isbn,
                        title,
                        description,
                        book.rating,
                        pages,
                        date,
                        cover
                    )
                } else {
                    val tmpResult = api.insertBook(isbn, title, description, pages, date, cover)
                    if (tmpResult != null) {
                        result = tmpResult
                        api.deleteRequest(requestId)
                    }
                }
            }
            coroutine.join()
        }
        //Handle result on edit value
        if (result != null) {
            onAdminDialogClose!!.onAdminDialogClose()
            showSnackBar(
                requireActivity().applicationContext,
                requireParentFragment().requireView(),
                getString(
                    R.string.BookAdded
                )
            )
            dismiss()
        } else if (editResult != null && editResult as Boolean) {
            onAdminDialogClose!!.onAdminDialogClose()
            showSnackBar(
                requireActivity().applicationContext,
                requireParentFragment().requireView(),
                getString(
                    R.string.BookEdited
                )
            )
            dismiss()
        } else {
            showSnackBar(
                requireContext(),
                requireView(),
                getString(R.string.SB_DuplicateLibraryName)
            )
        }
    }

    // Open the image galery
    private fun imageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            imageChooser()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showSnackBar(
                    requireContext(),
                    requireView(),
                    getString(R.string.SB_GaleryNotAviable)
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
        private const val REQUEST_CODE_PERMISSION = 5
        private const val REQUEST_CODE_GALLERY = 10
    }
}