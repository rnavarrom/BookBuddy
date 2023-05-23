package com.example.bookbuddy.ui.navdrawer.adminnav

import android.content.Context
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
import com.example.bookbuddy.adapters.AdminGenresAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminGenresBinding
import com.example.bookbuddy.models.Genre
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AdminGenresFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminGenresBinding
    private var job: Job = Job()
    lateinit var adapter: AdminGenresAdapter

    private var position = 0
    private var lastPosition = -1
    private var genres: MutableList<Genre>? = null

    private lateinit var gMenu: Menu

    private lateinit var searchItem: MenuItem

    private var search: String? = null
    private var genreName: String? = null
    private val api = CrudApi(this@AdminGenresFragment)

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
        when(type){
            0 -> {
                positiveText = "Insert"
                builder.setTitle("Insert Genre")
                editText.hint = "Insert genre"
            }
            1 ->  {
                positiveText = "Edit"
                builder.setTitle("Edit Genre " + adapter.getSelected()!!.name)
                editText.hint = "Edit genre"
            }
            2 -> {
                positiveText = "Search"
                builder.setTitle("Search genre")
                editText.hint = "Search genre"
            }
        }

        builder.setView(editText)

        builder.setPositiveButton(positiveText) { _, _ ->
            // Handle "Buscar" button click here

            when(type){
                0 -> {
                    genreName = editText.text.toString().trim()
                    insertGenre()
                }
                1 ->  {
                    genreName = editText.text.toString().trim()
                    editGenre()
                }
                2 -> {
                    search = editText.text.toString().trim()
                    position = 0
                    lastPosition = -1
                    getGenres(false)
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

    private fun insertGenre(){
        var result = false
        if (!genreName.isNullOrEmpty()){
            runBlocking {
                val coroutine = launch {
                    result = api.insertGenre(genreName!!)!!
                }
                coroutine.join()
            }

            if (result) {
                showSnackBar(requireContext(), requireView(), "Genre Inserted")
                //adapter.updateList(genres as ArrayList<Genre>)
            } else {
                showSnackBar(requireContext(), requireView(), "Genre already exist")
            }
        } else {
            showSnackBar(requireContext(), requireView(), "Name empty")
        }
    }

    private fun editGenre(){
        val selection = adapter.getSelected()
        var result = false
        if (!genreName.isNullOrEmpty()){
            runBlocking {
                val coroutine = launch {
                    result = api.updateGenre(selection!!.genreId, genreName!!)!!
                }
                coroutine.join()
            }

            if (result) {
                showSnackBar(requireContext(), requireView(), "Genre Edited")
                selection!!.name = genreName!!
                adapter.updateList(genres as ArrayList<Genre>)
            } else {
                showSnackBar(requireContext(), requireView(), "Duplicated genre")
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
        binding =  FragmentAdminGenresBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getGenres(true)
        loadingEnded()

        return binding.root
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            showCustomDialog(0)
        }

        binding.btnEdit.setOnClickListener {
            val selection = adapter.getSelected()
            if (selection != null){
                showCustomDialog(1)
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a Genre first")
            }

        }

        binding.btnDelete.setOnClickListener {
            val selection = adapter.getSelected()
            var result = false
            if (selection != null){
                runBlocking {
                    val coroutine = launch {
                        result = api.deleteGenre(selection.genreId)!!
                    }
                    coroutine.join()
                }

                if (result) {
                    showSnackBar(requireContext(), requireView(), "Genre deleted")
                    genres!!.remove(selection)
                    adapter.updateList(genres as ArrayList<Genre>)
                } else {
                    showSnackBar(requireContext(), requireView(), "Genre has books")
                }
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a Genre first")
            }
        }

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getGenres(false)
            binding.mainContent.isRefreshing = false
        }

        binding.rvGenres.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        getGenres(false)
    }

    private fun getGenres(addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi(this@AdminGenresFragment)
            val corrutina = launch {
                if (position == 0){
                    genres = if (search.isNullOrEmpty()){
                        crudApi.getGenres("null", false, position) as MutableList<Genre>?
                    } else {
                        crudApi.getGenres(search!!, true, position) as MutableList<Genre>?
                    }
                } else {
                    if (search.isNullOrEmpty()){
                        genres!!.addAll((crudApi.getGenres("null", false, position) as MutableList<Genre>?)!!)
                    } else {
                        genres!!.addAll((crudApi.getGenres(search!!, true, position) as MutableList<Genre>?)!!)
                    }
                }
                if (addAdapter){
                    binding.rvGenres.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    genres!!.forEach {
                        println(it.genreId)
                        println(it.name)
                        println(it.cardview)
                    }
                    adapter = AdminGenresAdapter(genres as ArrayList<Genre>)
                    binding.rvGenres.adapter = adapter
                } else {
                    adapter.updateList(genres as ArrayList<Genre>)
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