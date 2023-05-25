package com.example.bookbuddy.ui.navdrawer.adminnav

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.Utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentInsertBookDialogBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class InsertBookDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentInsertBookDialogBinding
    private var job: Job = Job()

    private var mode = "insert"
    private lateinit var book: Book

    var onAdminDialogClose: OnAdminDialogClose? = null

    private lateinit var tmpUri: Uri

    var fragment: AdminFragment? = null
    private var isRequest = false
    private var requestId = 0

    private val api = CrudApi(this@InsertBookDialog)
    interface OnAdminDialogClose {
        fun onAdminDialogClose()
    }

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
        binding =  FragmentInsertBookDialogBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)


        val bundle = arguments?.getBundle("bundle")
        var toolbarMessage = ""
        if (bundle != null && bundle.containsKey("fragment")){
            fragment = bundle.getSerializable("fragment") as? AdminFragment?
            if (fragment != null){
                onAdminDialogClose = fragment
            }
            if (bundle.containsKey("book")){
                mode = "edit"
                book = bundle.getSerializable("book") as Book
            }

            if (bundle.containsKey("isbn")){
                isRequest = true
                requestId = bundle.getInt("id")
                var isbn = bundle.getString("isbn")
                binding.etIsbn.setText(isbn)
                binding.etIsbn.focusable = View.NOT_FOCUSABLE
            }

            if (mode == "edit"){
                toolbarMessage = getString(R.string.EditBook)
                binding.etId.text = book.bookId.toString()
                binding.etIsbn.setText(book.isbn)
                binding.etTitle.setText(book.title)
                binding.etDescription.setText(book.description)
                binding.etPages.setText(book.pages.toString())
                binding.etDate.setText(book.publicationDate)
                binding.etCover.setText(book.cover)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            tmpUri = imageUri!!

            Glide.with(requireContext())
                .setDefaultRequestOptions(bookRequestOptions)
                .load(tmpUri)
                .into(binding.ivCover)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION) {
            // Verificar si se concedieron los permisos
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, abrir la galería
                imageChooser()
            } else {
                // Permiso denegado, manejar el caso de denegación de permisos
                // Puedes mostrar un mensaje al usuario o realizar alguna otra acción
                showSnackBar(
                    requireContext(),
                    requireView(),
                    getString(R.string.SB_GaleryAccesRequired)
                )
            }
        }
    }
    private fun openReferences(){
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

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)
            binding.etDate.setText(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        val datePicker = datePickerDialog.datePicker
        datePicker.updateDate(initialYear, initialMonth, initialDay)
        datePicker.maxDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }

    private fun editLibrary(){
        var result: Book? = null
        var editResult: Boolean? = null
        val isbn = binding.etIsbn.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val pages = binding.etPages.text.toString().toIntOrNull()
        val date = binding.etDate.text.toString()
        val cover = binding.etCover.text.toString().trim()

        if (isbn.isEmpty()){
            showSnackBar(requireContext(), requireView(), getString(R.string.ISBNEmptyWarning))
        }
        if (isbn.length < 13){
            showSnackBar(requireContext(), requireView(), getString(R.string.ISBNMaxLenght))
        }
        if (book.isbn != isbn){
            var isbnExist = false
            runBlocking {

                val coroutine = launch {
                    isbnExist = api.getBookExist(isbn)!!
                }
                coroutine.join()
            }
            if (isbnExist){
                showSnackBar(requireContext(), requireView(), getString(R.string.ISBNDuplicateWarning))
                return
            }
        }
        if (title.isEmpty()){
            showSnackBar(requireContext(), requireView(), getString(R.string.TitleEmptyWarning))
            return
        }
        if (pages == null){
            showSnackBar(requireContext(), requireView(), getString(R.string.PagesEmptyWarning))
            return
        }
        if (date.isEmpty()){
            showSnackBar(requireContext(), requireView(), getString(R.string.PublishEmptyWarning))
            return
        }
        if (cover.isEmpty()){
            return
        }

        runBlocking {
            val coroutine = launch {
                if (mode == "edit"){
                    //result = api.updateLibrary(library.libraryId,name, lat, lon, zip, "Edit failes")!!
                    editResult = api.updateBook(book.bookId,isbn, title, description, book.rating, pages, date , cover)
                } else {
                    val tmpResult = api.insertBook(isbn, title, description, pages, date , cover)
                    if (tmpResult != null){
                        result = tmpResult
                        api.deleteRequest(requestId)
                    }
                }
            }
            coroutine.join()
        }

        if (result != null) {
            onAdminDialogClose!!.onAdminDialogClose()
            showSnackBar(requireActivity().applicationContext, requireParentFragment().requireView(), getString(
                            R.string.BookAdded))
            dismiss()
        } else if (editResult != null && editResult as Boolean){
            onAdminDialogClose!!.onAdminDialogClose()
            showSnackBar(requireActivity().applicationContext, requireParentFragment().requireView(), getString(
                            R.string.BookEdited))
            dismiss()
        } else {
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_DuplicateLibraryName))
        }
    }

    private fun imageChooser(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }
    private fun checkPermissions(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED){
            imageChooser()
        }else{
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showSnackBar(requireContext(), requireView(),getString(R.string.SB_GaleryNotAviable))
            }

        }
    }

    override fun onApiError() {
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