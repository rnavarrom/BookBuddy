package com.example.bookbuddy.ui.navdrawer.adminnav

import android.content.Context
import android.os.Bundle
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
import com.example.bookbuddy.adapters.AdminLibraryAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminLibrariesBinding
import com.example.bookbuddy.models.Library
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.ui.navdrawer.AdminFragmentDirections
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class LibrariesFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminLibrariesBinding
    private var job: Job = Job()
    lateinit var adapter: AdminLibraryAdapter
    private var position = 0
    private var lastPosition = -1
    private var libraries: MutableList<Library>? = null
    private lateinit var gMenu: Menu
    private lateinit var searchItem: MenuItem
    private var search: String? = null
    private val api = CrudApi(this@LibrariesFragment)
    private var isOnCreateViewExecuted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAdminLibrariesBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getLibraries(true)
        onLoadingEnded()
        isOnCreateViewExecuted = true
        return binding.root
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
        val positiveText = getString(R.string.BT_Search)
        builder.setTitle(getString(R.string.SearchLibrary))
        editText.hint = getString(R.string.SearchLibrary)

        builder.setView(editText)

        builder.setPositiveButton(positiveText) { _, _ ->
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

        val dialog = builder.create()
        dialog.show()

        editText.postDelayed({
            editText.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun insertLibrary(){
        val bundle = Bundle()
        bundle.putParcelable("fragment", arguments?.getParcelable("fragment") as? AdminFragment?)
        val action = AdminFragmentDirections.actionNavAdminToNavInsertLibrary(bundle)
        navController.navigate(action)
    }

    private fun editLibrary(library: Library){
        val bundle = Bundle()
        bundle.putParcelable("fragment", arguments?.getParcelable("fragment") as? AdminFragment?)
        bundle.putParcelable("library", library)
        val action = AdminFragmentDirections.actionNavAdminToNavInsertLibrary(bundle)
        navController.navigate(action)
    }

    // Change visible layouts and add bindings
    private fun onLoadingEnded(){
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
                    val coroutine = launch {
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

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getLibraries(false)
            binding.mainContent.isRefreshing = false
        }

        // Load more items when scrolling the recycler view
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

    private fun getLibraries(addAdapter: Boolean){
        runBlocking {

            val corrutina = launch {
                if (position == 0){
                    libraries = if (search.isNullOrEmpty()){
                        api.getLibraries("null", false, position) as MutableList<Library>?
                    } else {
                        api.getLibraries(search!!, true, position) as MutableList<Library>?
                    }
                } else {
                    if (search.isNullOrEmpty()){
                        libraries!!.addAll((api.getLibraries("null", false, position) as MutableList<Library>?)!!)
                    } else {
                        libraries!!.addAll((api.getLibraries(search!!, true, position) as MutableList<Library>?)!!)
                    }
                }
                if (libraries != null){
                    if (addAdapter){
                        binding.rvLibraries.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        adapter = AdminLibraryAdapter(libraries as ArrayList<Library>)
                        binding.rvLibraries.adapter = adapter
                    } else {
                        adapter.updateList(libraries as ArrayList<Library>)
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