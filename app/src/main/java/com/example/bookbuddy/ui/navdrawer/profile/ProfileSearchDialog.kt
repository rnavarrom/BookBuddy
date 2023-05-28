package com.example.bookbuddy.ui.navdrawer.profile

import android.content.Context
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
import com.example.bookbuddy.adapters.SearchGenresAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogProfileSearchBinding
import com.example.bookbuddy.models.Extra.Genre
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.utils.Tools
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Dialog to search preferred genre of the user
 */
class ProfileSearchDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: DialogProfileSearchBinding
    private var job: Job = Job()
    private val api = CrudApi(this@ProfileSearchDialog)
    private lateinit var adapter: SearchGenresAdapter

    private var position = 0
    private var lastPosition = -1
    var genres: MutableList<Genre>? = null

    var onGenreSearchCompleteListener: OnGenreSearchCompleteListener? = null

    interface OnGenreSearchCompleteListener {
        fun onGenreSearchComplete(result: Int, name: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogProfileSearchBinding.inflate(layoutInflater, container, false)

        // Load more items when scrolling the recycler view
        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        position = totalItemCount
                        if (lastPosition != totalItemCount) {
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
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

                val searchValue = binding.searchThings.text.toString()

                performSearch(searchValue)

                if (genres!!.isNotEmpty()) {
                    position = 0
                    lastPosition = -1
                    binding.rvSearch.layoutManager = LinearLayoutManager(context)
                    adapter = SearchGenresAdapter(
                        this,
                        onGenreSearchCompleteListener,
                        genres as java.util.ArrayList<Genre>
                    )
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
        if (parentFragment is OnGenreSearchCompleteListener) {
            onGenreSearchCompleteListener = parentFragment
        } else {
            throw IllegalArgumentException(getString(R.string.Throw_ParentFragment))
        }
    }

    private fun loadMoreItems() {
        runBlocking {
            val coroutine = launch {
                val tempGenres = api.getSearchGenres(binding.searchThings.text.toString(), position)
                if (tempGenres != null) {
                    genres!!.addAll(tempGenres as MutableList<Genre>)
                }
            }
            coroutine.join()
        }
        adapter.updateList(genres as ArrayList<Genre>)
    }

    private fun performSearch(searchValue: String) {
        genres = mutableListOf()
        runBlocking {
            val coroutine = launch {
                val tempGenres = api.getSearchGenres(searchValue, position)
                if (tempGenres != null) {
                    genres = tempGenres as MutableList<Genre>
                }
            }
            coroutine.join()
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