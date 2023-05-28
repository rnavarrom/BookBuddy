package com.example.bookbuddy.ui.navdrawer.adminnav

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.AdminBooksAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminBooksBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.ui.navdrawer.AdminFragmentDirections
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.navController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class BooksFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminBooksBinding
    private var job: Job = Job()
    lateinit var adapter: AdminBooksAdapter
    private var position = 0
    private var lastPosition = -1
    private var books: MutableList<Book>? = null
    private lateinit var gMenu: Menu
    private lateinit var searchItem: MenuItem
    private var search: String? = null
    private val api = CrudApi(this@BooksFragment)
    private var isOnCreateViewExecuted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                showCustomDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAdminBooksBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)



        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getBooks(true)
        onLoadingEnded()
        isOnCreateViewExecuted = true
        return binding.root
    }

    private fun showCustomDialog() {
        //type 0 -> insert, 1 -> edit, 2 -> search
        val builder = AlertDialog.Builder(requireContext())
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        val positiveText = getString(R.string.BT_Search)
        builder.setTitle(getString(R.string.SearchBook))
        editText.hint = getString(R.string.SearchBook)

        builder.setView(editText)

        builder.setPositiveButton(positiveText) { _, _ ->
            search = editText.text.toString().trim()
            position = 0
            lastPosition = -1
            getBooks(false)
        }

        builder.setNegativeButton(getString(R.string.BT_Cancel)) { dialog, which ->
            dialog.cancel()
        }
        //Show the dialog
        val dialog = builder.create()
        dialog.show()

        editText.postDelayed({
            editText.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    //Navigate to insert book insert mode
    private fun insertBook(){
        val fra = requireArguments().getParcelable("fragment") as? AdminFragment?
        val bundle = Bundle()
        bundle.putParcelable("fragment", fra)
        val action = AdminFragmentDirections.actionNavAdminToNavInsertBook(bundle)
        navController.navigate(action)
    }
    //Navigate to insert book update mode
    private fun editBook(book: Book){
        val fra = requireArguments().getParcelable("fragment") as? AdminFragment?
        val bundle = Bundle()
        bundle.putParcelable("book", book)
        bundle.putParcelable("fragment", fra)
        val action = AdminFragmentDirections.actionNavAdminToNavInsertBook(bundle)
        navController.navigate(action)
    }

    // Change visible layouts and add bindings
    private fun onLoadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            insertBook()
        }
        //handle edit button
        binding.btnEdit.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null){
                editBook(selection)
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_PickABook))
            }
        }
        //handle delete button
        binding.btnDelete.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null){
                val builder = AlertDialog.Builder(requireContext())

                builder.setTitle(getString(R.string.DeleteBookQuestion))
                builder.setMessage(getString(R.string.DeleteBookQuestion2) + selection.isbn + "?")
                // Action to perform on affirmative responce
                builder.setPositiveButton(getString(R.string.Yes)) { dialogInterface: DialogInterface, _: Int ->
                    deleteBook(selection)
                    dialogInterface.dismiss()
                }
                //Action to perform on negative responce
                builder.setNegativeButton(getString(R.string.BT_Cancel)) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_PickABook))
            }
        }
        //Handle print button
        binding.btnPrint.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null){
                var barcodeValue = generateBarcode(selection.isbn)
                saveImageToGallery(barcodeValue, selection.isbn)
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_PickABook))
            }
        }
        //Restart values on refresh
        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getBooks(false)
            binding.mainContent.isRefreshing = false
        }

        //Handle scroll listener to load or add values to the list
        binding.rvBooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        position = totalItemCount
                        if (lastPosition != totalItemCount){
                            loadMoreItems()
                        }
                        lastPosition = totalItemCount
                    }
                }
            }
        })
    }
    //Generate barcode image
    private fun generateBarcode(content: String): Bitmap? {
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

        try {
            //Configure values for the bitmap
            val writer = Code128Writer()
            val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.CODE_128, 512, 256, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            //Draw the bitmap
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val barcodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            barcodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return barcodeBitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveImageToGallery(bitmap: Bitmap?, isbn: String) {
        //Configure values to save the image
        bitmap?.let {
            val currentTimeMillis = System.currentTimeMillis()
            val contentResolver: ContentResolver = requireActivity().applicationContext.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, isbn + "_" + currentTimeMillis.toString())
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.WIDTH, it.width)
                put(MediaStore.Images.Media.HEIGHT, it.height)
            }
            //Generate the url for the image
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { imageUri ->
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                // Notify the media scanner about the new image
                MediaScannerConnection.scanFile(
                    requireActivity().applicationContext,
                    arrayOf(imageUri.path),
                    arrayOf("image/png")
                ) { _: String?, _: Uri? -> }
            }
        }
    }
    //Request delete book to the api
    private fun deleteBook(book: Book){
        var result = false
        runBlocking {
            val coroutine = launch {
                val tmpResult = api.deleteBook(book.isbn, false)
                if (tmpResult != null){
                    result = tmpResult
                }
            }
            coroutine.join()
        }
        //Handle result and update adapter
        if (result) {
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_BookDelete))
            books!!.remove(book)
            adapter.updateList(books as ArrayList<Book>)
        }
    }

    private fun loadMoreItems() {
        getBooks(false)
    }
    //Load or add more books
    private fun getBooks(addAdapter: Boolean){
        runBlocking {
            val corrutina = launch {
                if (position == 0){
                    books = if (search.isNullOrEmpty()){
                        api.getAllBooksSearch("null", false, position) as MutableList<Book>?
                    } else {
                        api.getAllBooksSearch(search!!, true, position) as MutableList<Book>?
                    }
                } else {
                    if (search.isNullOrEmpty()){
                        books!!.addAll((api.getAllBooksSearch("null", false, position) as MutableList<Book>?)!!)
                    } else {
                        books!!.addAll((api.getAllBooksSearch(search!!, true, position) as MutableList<Book>?)!!)
                    }
                }
                if (books != null){
                    if (addAdapter){
                        binding.rvBooks.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        adapter = AdminBooksAdapter(books as ArrayList<Book>)
                        binding.rvBooks.adapter = adapter
                    } else {
                        adapter.updateList(books as ArrayList<Book>)
                    }
                }
            }
            corrutina.join()
        }
    }
    override fun onApiError(connectionFailed: Boolean) {
        if (isOnCreateViewExecuted){
            showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
        }
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        private const val PERMISSION_REQUEST_WRITE_STORAGE = 1
    }
}