package com.example.bookbuddy.ui.navdrawer.profile

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.SearchAuthorsAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogProfileSearchAuthorBinding
import com.example.bookbuddy.models.Extra.Author
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.ApiErrorListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileAuthorDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: DialogProfileSearchAuthorBinding
    private var job: Job = Job()
    //var searchResultList: ArrayList<Genre> = arrayListOf()
    private lateinit var adapter: SearchAuthorsAdapter
    private val api = CrudApi(this@ProfileAuthorDialog)

    private var position = 0
    private var lastPosition = -1
    var authors: MutableList<Author>? = null

    var onAuthorSearchCompleteListener: OnAuthorSearchCompleteListener? = null
    interface OnAuthorSearchCompleteListener {
        fun onAuthorSearchComplete(result: Int, name: String)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  DialogProfileSearchAuthorBinding.inflate(layoutInflater, container, false)

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

                val searchValue = binding.searchThings.text.toString()

                performSearch(searchValue)

                if(authors!!.isNotEmpty()){
                    position = 0
                    lastPosition = -1
                    binding.rvSearch.layoutManager = LinearLayoutManager(context)
                    adapter = SearchAuthorsAdapter(this, onAuthorSearchCompleteListener, authors as java.util.ArrayList<Author>)
                    binding.rvSearch.adapter = adapter
                }
                true
            } else {
                false
            }
        }

        return binding.root
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

    override fun onResume() {
        super.onResume()
        binding.searchThings.postDelayed({
            binding.searchThings.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchThings, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        if (parentFragment is OnAuthorSearchCompleteListener) {
            onAuthorSearchCompleteListener = parentFragment
        } else {
            throw IllegalArgumentException(getString(R.string.Throw_ParentFragment))
        }
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().invalidateOptionsMenu()
    }

    private fun loadMoreItems() {
        runBlocking {
            
            val corrutina = launch {
                val tempAuthors= api.getSearchAuthors(binding.searchThings.text.toString(), position)
                if(tempAuthors != null){
                    authors!!.addAll(tempAuthors as MutableList<Author>)
                }
            }
            corrutina.join()
        }
        adapter.updateList(authors as ArrayList<Author>)
    }

    private fun performSearch(searchValue: String) {
        authors = mutableListOf()
        runBlocking {            
            val corrutina = launch {
                val tempAuthors = api.getSearchAuthors(searchValue, position)
                if(tempAuthors != null){
                    authors = tempAuthors as MutableList<Author>
                }
            }
            corrutina.join()
        }
    }

    override fun onApiError(connectionFailed: Boolean) {
        Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
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