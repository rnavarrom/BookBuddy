package com.example.bookbuddy.ui.navdrawer

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.adapters.SearchAuthorsAdapter
import com.example.bookbuddy.adapters.SearchLanguagesAdapter
import com.example.bookbuddy.adapters.SearchLibrariesAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentProfileSearchAuthorDialogBinding
import com.example.bookbuddy.databinding.FragmentProfileSearchLanguageDialogBinding
import com.example.bookbuddy.databinding.FragmentProfileSearchLibraryDialogBinding
import com.example.bookbuddy.models.Language
import com.example.bookbuddy.models.LibraryExtended
import com.example.bookbuddy.models.Test.Author
import com.example.bookbuddy.utils.base.ApiErrorListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileLibraryDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentProfileSearchLibraryDialogBinding
    private var job: Job = Job()
    //var searchResultList: ArrayList<Genre> = arrayListOf()
    private lateinit var adapter: SearchLibrariesAdapter

    private var position = 0
    private var lastPosition = -1
    var libraries: MutableList<LibraryExtended>? = null

    public var onLibrarySearchCompleteListener: OnLibrarySearchCompleteListener? = null
    public interface OnLibrarySearchCompleteListener {
        fun onLibrarySearchComplete(result: Int, name: String)
    }

    override fun onResume() {
        super.onResume()
        binding.searchThings.postDelayed({
            binding.searchThings.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchThings, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().invalidateOptionsMenu()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        if (parentFragment is OnLibrarySearchCompleteListener) {
            onLibrarySearchCompleteListener = parentFragment
        } else {
            throw IllegalArgumentException("Parent fragment must implement OnSearchCompleteListener")
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels

            val marginInPixels = (80 * resources.displayMetrics.density).toInt()
            val width = screenWidth - marginInPixels
            val height = ViewGroup.LayoutParams.WRAP_CONTENT

            val window = dialog.window
            window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentProfileSearchLibraryDialogBinding.inflate(layoutInflater, container, false)

        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        // Inflate the layout for this fragment
        binding.searchThings.setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                // Realizar búsqueda
                //Toast.makeText(context, "", Toast.LENGTH_LONG).show()
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

                var searchValue = binding.searchThings.text.toString()

                performSearch(searchValue)

                if(libraries!!.isNotEmpty()){
                    position = 0
                    lastPosition = -1
                    binding.rvSearch.layoutManager = LinearLayoutManager(context)
                    adapter = SearchLibrariesAdapter(this, onLibrarySearchCompleteListener, libraries as java.util.ArrayList<LibraryExtended>)
                    binding.rvSearch.adapter = adapter
                }
                true
            } else {
                false
            }
        }

        return binding.root
    }

    private fun loadMoreItems() {
        runBlocking {
            val crudApi = CrudApi(this@ProfileLibraryDialog)
            val corrutina = launch {
                libraries!!.addAll(crudApi.getSearchLibraries(binding.searchThings.text.toString(), position, "Error") as MutableList<LibraryExtended>)
            }
            corrutina.join()
        }
        adapter.updateList(libraries as ArrayList<LibraryExtended>)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState)
    }

    private fun performSearch(searchValue: String) {
        // Aquí se realiza la búsqueda con el texto ingresado en el AutoCompleteTextView
        //Toast.makeText(requireContext(), "Realizando búsqueda: $searchValue", Toast.LENGTH_SHORT).show()
        position = 0
        lastPosition = -1
        libraries = mutableListOf<LibraryExtended>()

        if (searchValue.isNotEmpty()){
            runBlocking {
                val crudApi = CrudApi(this@ProfileLibraryDialog)
                val corrutina = launch {
                    libraries = crudApi.getSearchLibraries(searchValue, position, "Error") as MutableList<LibraryExtended>
                }
                corrutina.join()
            }
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

    override fun onApiError(errorMessage: String) {
        TODO("Not yet implemented")
    }
}