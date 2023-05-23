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
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.AdminBookLibraryAdapter
import com.example.bookbuddy.adapters.AdminGenresAdapter
import com.example.bookbuddy.adapters.AdminLibraryAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminBookReferencesDialogBinding
import com.example.bookbuddy.databinding.FragmentAdminGenresBinding
import com.example.bookbuddy.models.*
import com.example.bookbuddy.ui.navdrawer.ProfileAuthorDialog
import com.example.bookbuddy.ui.navdrawer.ProfileLanguageDialog
import com.example.bookbuddy.ui.navdrawer.ProfileLibraryDialog
import com.example.bookbuddy.ui.navdrawer.ProfileSearchDialog
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ReferencesBookDialog : DialogFragment(), CoroutineScope, ApiErrorListener,
    ProfileSearchDialog.OnGenreSearchCompleteListener,
    ProfileAuthorDialog.OnAuthorSearchCompleteListener,
    ProfileLanguageDialog.OnLanguageSearchCompleteListener,
    ProfileLibraryDialog.OnLibrarySearchCompleteListener {
    lateinit var binding: FragmentAdminBookReferencesDialogBinding
    private var job: Job = Job()
    lateinit var adapterGenres: AdminGenresAdapter
    lateinit var adapterLibraries: AdminBookLibraryAdapter
    val api = CrudApi(this@ReferencesBookDialog)
    private var author: Author? = null
    private var lang: Language? = null
    private var genres: MutableList<Genre>? = null
    private var libraries: MutableList<LibraryExtended>? = null

    private var bookId: Int = 0
    /*
    editText.postDelayed({
            editText.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)*/
    /*
    fun insertGenre(){
        var result = false
        if (!genreName.isNullOrEmpty()){
            runBlocking {
                var api = CrudApi(this@ReferencesBookDialog)
                var coroutine = launch {
                    result = api.insertGenre(genreName!!, "Insert failed")!!
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
    */
    /*
    fun editGenre(){
        val selection = adapter.getSelected()
        var result = false
        if (!genreName.isNullOrEmpty()){
            runBlocking {
                var api = CrudApi(this@ReferencesBookDialog)
                var coroutine = launch {
                    result = api.updateGenre(selection!!.genreId, genreName!!, "Action failes")!!
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
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        );
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentAdminBookReferencesDialogBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        Tools.setToolBar(this, binding.toolbar, requireContext(), getString(R.string.TB_BookReferences))

        val bundle = arguments?.getBundle("bundle")
        bookId = bundle!!.getInt("bookid")

        getAuthor()
        getLang()
        getGenres(true)
        getLibraries(true)
        loadingEnded()

        return binding.root
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.etAuthor.setOnClickListener {
            val dialog = ProfileAuthorDialog()
            dialog.onAuthorSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "Date Picker")
        }

        binding.etLanguage.setOnClickListener {
            val dialog = ProfileLanguageDialog()
            dialog.onLanguageSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "Date Picker")
        }

        binding.btnAdd.setOnClickListener {
            val dialog = ProfileSearchDialog()
            dialog.onGenreSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "Date Picker")
        }

        binding.btnDelete.setOnClickListener {
            val selection = adapterGenres.getSelected()
            var result = false
            if (selection != null){
                runBlocking {                    
                    var coroutine = launch {
                        result = api.deleteBookGenre(bookId, selection.genreId)!!
                    }
                    coroutine.join()
                }

                if (result) {
                    showSnackBar(requireContext(), requireView(), getString(R.string.GenreDelete))
                    genres!!.remove(selection)
                    adapterGenres.updateList(genres as ArrayList<Genre>)
                } else {
                    showSnackBar(requireContext(), requireView(), getString(R.string.GenreHasBook))
                }
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.PickGenre))
            }
        }

        binding.btnAddLibrary.setOnClickListener {
            val dialog = ProfileLibraryDialog()
            dialog.onLibrarySearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "Date Picker")
        }

        binding.btnEditLibrary.setOnClickListener {
            var selection = adapterLibraries.getSelected()
            var result = false
            if (selection != null){
                val builder = AlertDialog.Builder(requireContext())
                val editText = EditText(requireContext())
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                builder.setTitle("Number of copies")
                editText.hint = selection.copies.toString() + " copies"
                builder.setView(editText)
                builder.setPositiveButton(getString(R.string.BT_Edit)) { dialog, which ->
                    var copiesString = editText.text.toString()

                    if (!copiesString.isEmpty()){
                        var copies = copiesString.toInt()
                        if (copies >= 0){
                            runBlocking {                                
                                var coroutine = launch {
                                    result = api.updateBookLibrary(bookId, selection.library.libraryId, copies)!!
                                }
                                coroutine.join()
                            }

                            if (result) {
                                showSnackBar(requireContext(), requireView(), getString(R.string.SB_CopiesEdited))
                                selection!!.copies = copies
                                adapterLibraries.updateList(libraries as ArrayList<LibraryExtended>)
                            } else {
                                showSnackBar(requireContext(), requireView(), getString(R.string.GenreDuplicated))
                            }
                        } else {
                            showSnackBar(requireContext(), requireView(), getString(R.string.SB_NoNegatives))
                        }
                    } else {
                        showSnackBar(requireContext(), requireView(), getString(R.string.SB_NotChanged))
                    }
                }

                builder.setNegativeButton("Cancel") { dialog, which ->
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
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_PickLibraryToEdit))
            }
        }

        binding.btnDeleteLibrary.setOnClickListener {
            val selection = adapterLibraries.getSelected()
            var result = false
            if (selection != null){
                runBlocking {                    
                    var coroutine = launch {
                        result = api.deleteBookLibrary(bookId, selection.library.libraryId)!!
                    }
                    coroutine.join()
                }

                if (result) {
                    showSnackBar(requireContext(), requireView(), getString(R.string.SB_LibraryEdited))
                    libraries!!.remove(selection)
                    adapterLibraries.updateList(libraries as ArrayList<LibraryExtended>)
                } else {
                    showSnackBar(requireContext(), requireView(), getString(R.string.GenreHasBook))
                }
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.PickLibrary))
            }
        }
    }


    fun getAuthor(){
        runBlocking {            
            val corrutina = launch {
                author = api.getBookAuthors(bookId)
            }
            corrutina.join()
        }
        if (author != null){
            binding.etAuthor.setText(author!!.name)
        }
    }

    fun getLang(){
        runBlocking {            
            val corrutina = launch {
                lang = api.getBookLang(bookId)
            }
            corrutina.join()
        }
        if (lang != null){
            binding.etLanguage.setText(lang!!.name)
        }
    }

    fun getGenres(addAdapter: Boolean){
        runBlocking {            
            val corrutina = launch {
                genres = api.getBookGenres(bookId) as MutableList<Genre>?
                if (addAdapter){
                    binding.rvGenres.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapterGenres = AdminGenresAdapter(genres as ArrayList<Genre>)
                    binding.rvGenres.adapter = adapterGenres
                } else {
                    adapterGenres.updateList(genres as ArrayList<Genre>)
                }
            }
            corrutina.join()
        }
    }

    fun getLibraries(addAdapter: Boolean){
        runBlocking {            
            val corrutina = launch {
                libraries = api.getBookLibraries(bookId) as MutableList<LibraryExtended>?
                if (addAdapter){
                    binding.rvLibraries.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapterLibraries = AdminBookLibraryAdapter(libraries as ArrayList<LibraryExtended>)
                    binding.rvLibraries.adapter = adapterLibraries
                } else {
                    adapterLibraries.updateList(libraries as ArrayList<LibraryExtended>)
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

    override fun onGenreSearchComplete(result: Int, name: String) {
        var genreExist = false
        var resultApi: Boolean? = null
        genres!!.forEach {
            if (it.genreId == result){
                genreExist = true
            }
        }
        if (!genreExist){
            runBlocking {
                var api = CrudApi(this@ReferencesBookDialog)
                var corrutine = launch {
                    resultApi = api.insertBookGenre(bookId, result)
                }
                corrutine.join()
            }
        } else {
            showSnackBar(requireContext(), requireView(), "Genre already in the list")
        }

        if (resultApi != null && resultApi as Boolean){
            var tmpGenre = Genre(result, name)
            genres!!.add(tmpGenre)
            genres!!.sortBy { it.name }
            adapterGenres.updateList(genres as ArrayList<Genre>)
            showSnackBar(requireContext(), requireView(), "Genre Added")
        }
    }

    override fun onAuthorSearchComplete(result: Int, name: String) {
        var authorExist = false
        var resultApi: Boolean? = null

        if (author != null && author!!.authorId == result){
            authorExist = true
        }

        if (!authorExist){
            runBlocking {
                var api = CrudApi(this@ReferencesBookDialog)
                var corrutine = launch {
                    if (author != null){
                        api.deleteBookAuthors(bookId, author!!.authorId)
                    }
                    resultApi = api.insertBookAuthors(bookId, result)
                }
                corrutine.join()
            }
        }

        if (resultApi != null && resultApi as Boolean){
            author = Author(result, name)
            binding.etAuthor.setText(author!!.name)
            showSnackBar(requireContext(), requireView(), "Author changed")
        }
    }

    override fun onLanguageSearchComplete(result: Int, name: String) {
        var languageExist = false
        var resultApi: Boolean? = null

        if (lang != null && lang!!.languageId == result){
            languageExist = true
        }

        if (!languageExist){
            runBlocking {
                var api = CrudApi(this@ReferencesBookDialog)
                var corrutine = launch {
                    if (author != null){
                        api.deleteBookLang(bookId, lang!!.languageId)
                    }
                    resultApi = api.insertBookLang(bookId, result)
                }
                corrutine.join()
            }
        }

        if (resultApi != null && resultApi as Boolean){
            lang = Language(result, name)
            binding.etLanguage.setText(lang!!.name)
            showSnackBar(requireContext(), requireView(), "Language changed")
        }
    }

    override fun onLibrarySearchComplete(result: Int, name: String) {
        var libraryExist = false
        var resultApi: Boolean? = null
        libraries!!.forEach {
            if (it.library.libraryId == result){
                libraryExist = true
            }
        }
        if (!libraryExist){
            runBlocking {
                var api = CrudApi(this@ReferencesBookDialog)
                var corrutine = launch {
                    resultApi = api.insertBookLibrary(bookId, result, 0)
                }
                corrutine.join()
            }
        } else {
            showSnackBar(requireContext(), requireView(), "Library already in the list")
        }

        if (resultApi != null && resultApi as Boolean){
            var tmpLibrary = LibraryExtended(library = Library(0.0, result, 0.0, name, ""), distance = null, copies = 0)
            libraries!!.add(tmpLibrary)
            libraries!!.sortBy { it.library.name}
            adapterLibraries.updateList(libraries as ArrayList<LibraryExtended>)
            showSnackBar(requireContext(), requireView(), "Library Added")
        }
    }
}