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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.AdminAuthorsAdapter
import com.example.bookbuddy.adapters.AdminLibraryAdapter
import com.example.bookbuddy.adapters.LibraryAdapter
import com.example.bookbuddy.adapters.RecommendedBooksAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminBinding
import com.example.bookbuddy.databinding.FragmentAdminAuthorsBinding
import com.example.bookbuddy.databinding.FragmentAdminLibrariesBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.ui.navdrawer.AdminFragmentDirections
import com.example.bookbuddy.ui.navdrawer.BookDisplayFragmentDirections
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AdminLibrariesFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminLibrariesBinding
    private var job: Job = Job()
    lateinit var adapter: AdminLibraryAdapter

    private var position = 0
    private var lastPosition = -1
    private var libraries: MutableList<Library>? = null

    private lateinit var gMenu: Menu

    private lateinit var searchItem: MenuItem

    private var search: String? = null
    private var libraryName: String? = null

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
        var positiveText = ""
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        positiveText = getString(R.string.BT_Search)
        builder.setTitle(getString(R.string.SearchLibrary))
        editText.hint = getString(R.string.SearchLibrary)

        builder.setView(editText)

        builder.setPositiveButton(positiveText) { dialog, which ->
            // Handle "Buscar" button click here
            search = editText.text.toString().trim()
            position = 0
            lastPosition = -1
            getLibraries(false)
        }

        builder.setNegativeButton(getString(R.string.BT_Cancel)) { dialog, which ->
            // Handle "Cancelar" button click here
            dialog.cancel()
        }

        builder.setOnCancelListener(DialogInterface.OnCancelListener {
            // Handle cancel action here
            // This will be triggered when the dialog is canceled
        })

        val dialog = builder.create()
        dialog.show()

        editText.postDelayed({
            editText.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    fun insertLibrary(){
        var fra = requireArguments().getSerializable("fragment") as? AdminFragment?

        if (fra != null){
            println("OKS")
        }

        val bundle = Bundle()
        bundle.putSerializable("fragment", arguments?.getSerializable("fragment") as? AdminFragment?)
        //var action = AdminLibrariesFragmentDirections.actionNavLibraryToNavInsertLibrary(bundle)
        var action = AdminFragmentDirections.actionNavAdminToNavInsertLibrary(bundle)
        navController.navigate(action)
    }

    fun editLibrary(library: Library){
        val bundle = Bundle()
        bundle.putSerializable("fragment", arguments?.getSerializable("fragment") as? AdminFragment?)
        bundle.putSerializable("library", library)
        var action = AdminFragmentDirections.actionNavAdminToNavInsertLibrary(bundle)
        navController.navigate(action)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentAdminLibrariesBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getLibraries(true)
        loadingEnded()

        return binding.root
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            insertLibrary()
        }

        binding.btnEdit.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null){
                editLibrary(selection)
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.PickLibrary))
            }

        }

        binding.btnDelete.setOnClickListener {
            val selection = adapter.getSelected()
            var result = false
            if (selection != null){
                runBlocking {
                    var api = CrudApi(this@AdminLibrariesFragment)
                    var coroutine = launch {
                        result = api.deleteLibrary(selection.libraryId)!!
                    }
                    coroutine.join()
                }

                if (result) {
                    showSnackBar(requireContext(), requireView(), getString(R.string.LibraryDelete))
                    libraries!!.remove(selection)
                    adapter.updateList(libraries as ArrayList<Library>)
                } else {
                    showSnackBar(requireContext(), requireView(), getString(R.string.LibraryHasBook))
                }
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.PickLibrary))
            }
        }

        binding.mainContent.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener() {
            position = 0
            lastPosition = -1
            getLibraries(false)
            binding.mainContent.isRefreshing = false;
        });

        binding.rvLibraries.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun loadMoreItems() {
        getLibraries(false)
    }

    fun getLibraries(addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi(this@AdminLibrariesFragment)
            val corrutina = launch {
                if (position == 0){
                    if (search.isNullOrEmpty()){
                        libraries = crudApi.getLibraries("null", false, position) as MutableList<Library>?
                    } else {
                        libraries = crudApi.getLibraries(search!!, true, position) as MutableList<Library>?
                    }
                } else {
                    if (search.isNullOrEmpty()){
                        libraries!!.addAll((crudApi.getLibraries("null", false, position) as MutableList<Library>?)!!)
                    } else {
                        libraries!!.addAll((crudApi.getLibraries(search!!, true, position) as MutableList<Library>?)!!)
                    }
                }
                if (addAdapter){
                    binding.rvLibraries.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = AdminLibraryAdapter(libraries as ArrayList<Library>)
                    binding.rvLibraries.adapter = adapter
                } else {
                    adapter.updateList(libraries as ArrayList<Library>)
                }
            }
            corrutina.join()
        }
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

    override fun onApiError() {
        showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}