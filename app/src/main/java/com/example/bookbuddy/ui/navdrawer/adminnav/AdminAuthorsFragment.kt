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
import com.example.bookbuddy.adapters.AdminAuthorsAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminAuthorsBinding
import com.example.bookbuddy.models.Author
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AdminAuthorsFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminAuthorsBinding
    private var job: Job = Job()
    lateinit var adapter: AdminAuthorsAdapter

    private var position = 0
    private var lastPosition = -1
    private var authors: MutableList<Author>? = null

    private lateinit var gMenu: Menu

    private lateinit var searchItem: MenuItem

    private var search: String? = null
    private var authorName: String? = null
    private val api = CrudApi(this@AdminAuthorsFragment)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        gMenu = menu
        searchItem = gMenu.findItem(R.id.action_search)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_search -> {
                showCustomDialog(2)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showCustomDialog(type: Int) {
        //type 0 -> insert, 1 -> edit, 2 -> search
        val builder = AlertDialog.Builder(requireContext())
        var positiveText = ""
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        when (type) {
            0 -> {
                positiveText = "Insert"
                builder.setTitle("Insert Author")
                editText.hint = "Insert author"
            }
            1 -> {
                positiveText = "Edit"
                builder.setTitle("Edit Author " + adapter.getSelected()!!.name)
                editText.hint = "Edit author"
            }
            2 -> {
                positiveText = "Search"
                builder.setTitle("Search author")
                editText.hint = "Search author"
            }
        }

        builder.setView(editText)

        builder.setPositiveButton(positiveText) { _, _ ->
            // Handle "Buscar" button click here

            when (type) {
                0 -> {
                    authorName = editText.text.toString().trim()
                    insertAuthor()
                }
                1 -> {
                    authorName = editText.text.toString().trim()
                    editAuthor()
                }
                2 -> {
                    search = editText.text.toString().trim()
                    position = 0
                    lastPosition = -1
                    getAuthors(false)
                }
            }
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

    private fun insertAuthor() {
        var result = false
        if (!authorName.isNullOrEmpty()) {
            runBlocking {
                val coroutine = launch {
                    result = api.insertAuthor(authorName!!)!!
                }
                coroutine.join()
            }

            if (result) {
                showSnackBar(requireContext(), requireView(), "Author Inserted")
                //adapter.updateList(authors as ArrayList<Author>)
            } else {
                showSnackBar(requireContext(), requireView(), "Author already exist")
            }
        } else {
            showSnackBar(requireContext(), requireView(), "Name empty")
        }
    }

    private fun editAuthor() {
        val selection = adapter.getSelected()
        var result = false
        if (!authorName.isNullOrEmpty()) {
            runBlocking {
                val coroutine = launch {
                    result = api.updateAuthor(selection!!.authorId, authorName!!)!!
                }
                coroutine.join()
            }

            if (result) {
                showSnackBar(requireContext(), requireView(), "Author Edited")
                selection!!.name = authorName!!
                adapter.updateList(authors as ArrayList<Author>)
            } else {
                showSnackBar(requireContext(), requireView(), "Duplicated author")
            }
        } else {
            showSnackBar(requireContext(), requireView(), "Name empty")
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
        binding = FragmentAdminAuthorsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary_green
            )
        )

        getAuthors(true)
        loadingEnded()

        return binding.root
    }

    fun loadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            showCustomDialog(0)
        }

        binding.btnEdit.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null) {
                showCustomDialog(1)
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a Author first")
            }

        }

        binding.btnDelete.setOnClickListener {
            val selection = adapter.getSelected()
            var result = false
            if (selection != null) {
                runBlocking {
                    val coroutine = launch {
                        result = api.deleteAuthor(selection.authorId)!!
                    }
                    coroutine.join()
                }

                if (result) {
                    showSnackBar(requireContext(), requireView(), "Author deleted")
                    authors!!.remove(selection)
                    adapter.updateList(authors as ArrayList<Author>)
                } else {
                    showSnackBar(requireContext(), requireView(), "Author has books")
                }
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a Author first")
            }
        }

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getAuthors(false)
            binding.mainContent.isRefreshing = false
        }

        binding.rvAuthors.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    }

    private fun loadMoreItems() {
        getAuthors(false)
    }

    private fun getAuthors(addAdapter: Boolean) {
        runBlocking {
            val crudApi = CrudApi(this@AdminAuthorsFragment)
            val corrutine = launch {
                if (position == 0) {
                    authors = if (search.isNullOrEmpty()) {
                        crudApi.getAuthors("null", false, position) as MutableList<Author>?
                    } else {
                        crudApi.getAuthors(search!!, true, position) as MutableList<Author>?
                    }
                } else {
                    if (search.isNullOrEmpty()) {
                        authors!!.addAll(
                            (crudApi.getAuthors(
                                "null",
                                false,
                                position
                            ) as MutableList<Author>?)!!
                        )
                    } else {
                        authors!!.addAll(
                            (crudApi.getAuthors(
                                search!!,
                                true,
                                position
                            ) as MutableList<Author>?)!!
                        )
                    }
                }
                if (addAdapter) {
                    binding.rvAuthors.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    authors!!.forEach {
                        println(it.authorId)
                        println(it.name)
                        println(it.cardview)
                    }
                    adapter = AdminAuthorsAdapter(authors as ArrayList<Author>)
                    binding.rvAuthors.adapter = adapter
                } else {
                    adapter.updateList(authors as ArrayList<Author>)
                }
            }
            corrutine.join()
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