package com.example.bookbuddy.ui.navdrawer.adminnav

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
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
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AdminBooksFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminBooksBinding
    private var job: Job = Job()
    lateinit var adapter: AdminBooksAdapter

    private var position = 0
    private var lastPosition = -1
    private var books: MutableList<Book>? = null

    private lateinit var gMenu: Menu

    private lateinit var searchItem: MenuItem

    private var search: String? = null
    private val api = CrudApi(this@AdminBooksFragment)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAdminBooksBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getBooks(true)
        loadingEnded()

        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        gMenu = menu
        searchItem = gMenu.findItem(R.id.action_search)
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

    private fun showCustomDialog() {
        //type 0 -> insert, 1 -> edit, 2 -> search
        val builder = AlertDialog.Builder(requireContext())
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        val positiveText = "Search"
        builder.setTitle("Search book")
        editText.hint = "Search book"

        builder.setView(editText)

        builder.setPositiveButton(positiveText) { _, _ ->
            // Handle "Buscar" button click here
            search = editText.text.toString().trim()
            position = 0
            lastPosition = -1
            getBooks(false)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Handle "Cancelar" button click here
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()

        editText.postDelayed({
            editText.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun insertBook(){
        val fra = requireArguments().getSerializable("fragment") as? AdminFragment?

        if (fra != null){
            println("OKS")
        }

        val bundle = Bundle()
        //bundle.putSerializable("fragment", arguments?.getSerializable("fragment") as? AdminFragment?)
        bundle.putSerializable("fragment", fra)
        //val action = AdminLibrariesFragmentDirections.actionNavBookToNavInsertBook(bundle)
        val action = AdminFragmentDirections.actionNavAdminToNavInsertBook(bundle)
        navController.navigate(action)
    }

    private fun editBook(book: Book){
        val fra = requireArguments().getSerializable("fragment") as? AdminFragment?

        val bundle = Bundle()
        bundle.putSerializable("book", book)
        bundle.putSerializable("fragment", fra)
        //val action = AdminLibrariesFragmentDirections.actionNavBookToNavInsertBook(bundle)
        val action = AdminFragmentDirections.actionNavAdminToNavInsertBook(bundle)
        navController.navigate(action)
    /*
        val bundle = Bundle()
        bundle.putSerializable("fragment", arguments?.getSerializable("fragment") as? AdminFragment?)
        bundle.putSerializable("book", book)
        val action = AdminFragmentDirections.actionNavAdminToNavInsertBook(bundle)
        navController.navigate(action)*/
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            insertBook()
        }

        binding.btnEdit.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null){
                editBook(selection)
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a Book first")
            }

        }

        binding.btnDelete.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null){
                val builder = AlertDialog.Builder(requireContext())

                builder.setTitle("Do you want to delete this book?")
                builder.setMessage("Are you sure you want to delete book with isbn = " + selection.isbn + "?")
                builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
                    // Acciones a realizar si el usuario selecciona "Sí"
                    deleteBook(selection)
                    dialogInterface.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                    // Acciones a realizar si el usuario selecciona "No"
                    dialogInterface.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a Book first")
            }
        }

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getBooks(false)
            binding.mainContent.isRefreshing = false
        }

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

        if (result) {
            showSnackBar(requireContext(), requireView(), "Book deleted")
            books!!.remove(book)
            adapter.updateList(books as ArrayList<Book>)
        }
    }

    private fun loadMoreItems() {
        getBooks(false)
    }

    private fun getBooks(addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi(this@AdminBooksFragment)
            val corrutina = launch {
                if (position == 0){
                    books = if (search.isNullOrEmpty()){
                        crudApi.getAllBooksSearch("null", false, position) as MutableList<Book>?
                    } else {
                        crudApi.getAllBooksSearch(search!!, true, position) as MutableList<Book>?
                    }
                } else {
                    if (search.isNullOrEmpty()){
                        books!!.addAll((crudApi.getAllBooksSearch("null", false, position) as MutableList<Book>?)!!)
                    } else {
                        books!!.addAll((crudApi.getAllBooksSearch(search!!, true, position) as MutableList<Book>?)!!)
                    }
                }
                if (addAdapter){
                    binding.rvBooks.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = AdminBooksAdapter(books as ArrayList<Book>)
                    binding.rvBooks.adapter = adapter
                } else {
                    adapter.updateList(books as ArrayList<Book>)
                }
            }
            corrutina.join()
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
}