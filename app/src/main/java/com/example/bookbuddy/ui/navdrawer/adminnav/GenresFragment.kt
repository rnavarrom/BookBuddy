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
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.adapters.AdminGenresAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminGenresBinding
import com.example.bookbuddy.models.Genre
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
/**
 * Fragment to display the genres list crud.
 */
class GenresFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminGenresBinding
    private var job: Job = Job()
    lateinit var adapter: AdminGenresAdapter
    private var position = 0
    private var lastPosition = -1
    private var genres: MutableList<Genre>? = null
    private var search: String? = null
    private var genreName: String? = null
    private val api = CrudApi(this@GenresFragment)
    private var isOnCreateViewExecuted = false
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
        onLoadingEnded()
        isOnCreateViewExecuted = true
        return binding.root
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
    //Open the custom dialog with the correct
    private fun showCustomDialog(type: Int) {
        //type 0 -> insert, 1 -> edit, 2 -> search
        val builder = AlertDialog.Builder(requireContext())
        var positiveText = ""
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        //Load the correct values for the dialog
        when(type){
            0 -> {
                positiveText = getString(R.string.BT_Insert)
                builder.setTitle(getString(R.string.InsertGenre))
                editText.hint = getString(R.string.InsertGenre)
            }
            1 ->  {
                positiveText = getString(R.string.BT_Edit)
                builder.setTitle(getString(R.string.EditGenre) + adapter.getSelected()!!.name)
                editText.hint = getString(R.string.EditGenre)
            }
            2 -> {
                positiveText = getString(R.string.BT_Search)
                builder.setTitle(getString(R.string.SearchGenre))
                editText.hint = getString(R.string.SearchGenre)
            }
        }

        builder.setView(editText)
        //Positive response
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
        //Negative response
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
    //Call to insert new genre
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
                showSnackBar(requireContext(), requireView(), getString(R.string.GenreInserted))
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.GenreExists))
            }
        } else {
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_NameEmpty))
        }
    }
    //Call to edit genre
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
                showSnackBar(requireContext(), requireView(), getString(R.string.GenreEdited))
                selection!!.name = genreName!!
                adapter.updateList(genres as ArrayList<Genre>)
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.GenreDuplicated))
            }
        } else {
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_NameEmpty))
        }
    }

    /**
     * Load the configuration upon ending the loading animation
     */
    fun onLoadingEnded(){
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
                showSnackBar(requireContext(), requireView(), getString(R.string.PickGenre))
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
                    showSnackBar(requireContext(), requireView(), getString(R.string.GenreDelete))
                    genres!!.remove(selection)
                    adapter.updateList(genres as ArrayList<Genre>)
                } else {
                    showSnackBar(requireContext(), requireView(), getString(R.string.GenreHasBook))
                }
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.PickGenre))
            }
        }

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getGenres(false)
            binding.mainContent.isRefreshing = false
        }

        // Load more items when scrolling the recycler view
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
    /**
     * Function to load or add more values to a list
     * @param addAdapter To check if the adapter is active
     */
    private fun getGenres(addAdapter: Boolean){
        runBlocking {
            val crudApi = CrudApi(this@GenresFragment)
            val coroutine = launch {
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
                if (genres != null){
                    if (addAdapter){
                        binding.rvGenres.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        adapter = AdminGenresAdapter(genres as ArrayList<Genre>)
                        binding.rvGenres.adapter = adapter
                    } else {
                        adapter.updateList(genres as ArrayList<Genre>)
                    }
                }
            }
            coroutine.join()
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