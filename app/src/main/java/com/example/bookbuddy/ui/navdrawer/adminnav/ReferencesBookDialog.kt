package com.example.bookbuddy.ui.navdrawer.adminnav

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookbuddy.R
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.adapters.AdminBookLibraryAdapter
import com.example.bookbuddy.adapters.AdminGenresAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogAdminBookReferencesBinding
import com.example.bookbuddy.models.*
import com.example.bookbuddy.ui.navdrawer.profile.ProfileAuthorDialog
import com.example.bookbuddy.ui.navdrawer.profile.ProfileLanguageDialog
import com.example.bookbuddy.ui.navdrawer.profile.ProfileLibraryDialog
import com.example.bookbuddy.ui.navdrawer.profile.ProfileSearchDialog
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Dialog to assign references to a book, such as author, language, genres and libraries
 */
class ReferencesBookDialog : DialogFragment(), CoroutineScope, ApiErrorListener,
    ProfileSearchDialog.OnGenreSearchCompleteListener,
    ProfileAuthorDialog.OnAuthorSearchCompleteListener,
    ProfileLanguageDialog.OnLanguageSearchCompleteListener,
    ProfileLibraryDialog.OnLibrarySearchCompleteListener {
    lateinit var binding: DialogAdminBookReferencesBinding
    private var job: Job = Job()
    private lateinit var adapterGenres: AdminGenresAdapter
    private lateinit var adapterLibraries: AdminBookLibraryAdapter

    private var author: Author? = null
    private var lang: Language? = null
    private var genres: MutableList<Genre>? = null
    private var libraries: MutableList<LibraryExtended>? = null

    private var bookId: Int = 0
    private val api = CrudApi(this@ReferencesBookDialog)

    // Add the genre to the book when the user ends the dialog
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
                val corrutine = launch {
                    resultApi = api.insertBookGenre(bookId, result)
                }
                corrutine.join()
            }
        } else {
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_GenreInList))
        }

        if (resultApi != null && resultApi as Boolean){
            val tmpGenre = Genre(result, name)
            genres!!.add(tmpGenre)
            genres!!.sortBy { it.name }
            adapterGenres.updateList(genres as ArrayList<Genre>)
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_GenreAdded))
        }
    }

    // Add the author to the book when the user ends the dialog
    override fun onAuthorSearchComplete(result: Int, name: String) {
        var authorExist = false
        var resultApi: Boolean? = null

        if (author != null && author!!.authorId == result){
            authorExist = true
        }

        if (!authorExist){
            runBlocking {
                val corrutine = launch {
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
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_AuthorChanged))
        }
    }

    // Add the language to the book when the user ends the dialog
    override fun onLanguageSearchComplete(result: Int, name: String) {
        var languageExist = false
        var resultApi: Boolean? = null

        if (lang != null && lang!!.languageId == result){
            languageExist = true
        }

        if (!languageExist){
            runBlocking {
                val corrutine = launch {
                    if (lang != null){
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
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_LanguageChanged))
        }
    }

    // Add the library to the book when the user ends the dialog
    override fun onLibrarySearchComplete(result: Int, name: String, zipCode: String) {
        var libraryExist = false
        var resultApi: Boolean? = null
        libraries!!.forEach {
            if (it.library.libraryId == result){
                libraryExist = true
            }
        }
        if (!libraryExist){
            runBlocking {
                val corrutine = launch {
                    resultApi = api.insertBookLibrary(bookId, result, 0)
                }
                corrutine.join()
            }
        } else {
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_LibraryAlreadyInList))
        }

        if (resultApi != null && resultApi as Boolean){
            val tmpLibrary = LibraryExtended(library = Library(0.0, result, 0.0, name, zipCode), distance = null, copies = 0)
            libraries!!.add(tmpLibrary)
            libraries!!.sortBy { it.library.name}
            adapterLibraries.updateList(libraries as ArrayList<LibraryExtended>)
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_LibraryAdded))
        }
    }

    // Set fullscreen dialog style
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  DialogAdminBookReferencesBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        Tools.setToolBar(this, binding.toolbar, requireContext(), getString(R.string.TB_BookReferences))

        val bundle = arguments?.getBundle("bundle")
        bookId = bundle!!.getInt("bookid")

        // Load all the references
        getAuthor()
        getLang()
        getGenres(true)
        getLibraries(true)
        onLoadingEnded()

        return binding.root
    }

    // Change visible layouts and add bindings
    private fun onLoadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.etAuthor.setOnClickListener {
            val dialog = ProfileAuthorDialog()
            dialog.onAuthorSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "")
        }

        binding.etLanguage.setOnClickListener {
            val dialog = ProfileLanguageDialog()
            dialog.onLanguageSearchCompleteListener = this //Changed
            dialog.show(childFragmentManager, "")
        }

        binding.btnAdd.setOnClickListener {
            if (genres == null || genres!!.size <= 10){
                val dialog = ProfileSearchDialog()
                dialog.onGenreSearchCompleteListener = this //Changed
                dialog.show(childFragmentManager, "")
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_GenresLimit))
            }
        }

        binding.btnDelete.setOnClickListener {
            val selection = adapterGenres.getSelected()
            var result = false
            if (selection != null){
                runBlocking {
                    val coroutine = launch {
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
            dialog.show(childFragmentManager, "")
        }

        binding.btnEditLibrary.setOnClickListener {
            val selection = adapterLibraries.getSelected()
            var result = false
            if (selection != null){
                val builder = AlertDialog.Builder(requireContext())
                val editText = EditText(requireContext())
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                val title = builder.setTitle(getString(R.string.SB_TitleNumberCopies))
                editText.hint = selection.copies.toString() + getString(R.string.MSG_Copies)
                builder.setView(editText)
                builder.setPositiveButton(getString(R.string.BT_Edit)) { _, _ ->
                    var copiesString = editText.text.toString()

                    if (copiesString.isNotEmpty()){
                        val copies = copiesString.toInt()
                        if (copies >= 0){
                            runBlocking {
                                val coroutine = launch {
                                    result = api.updateBookLibrary(bookId, selection.library.libraryId, copies)!!
                                }
                                coroutine.join()
                            }

                            if (result) {
                                showSnackBar(requireContext(), requireView(), getString(R.string.SB_CopiesEdited))
                                selection.copies = copies
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

                builder.setNegativeButton(getString(R.string.BT_Cancel)) { dialog, _ ->
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
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_PickLibraryToEdit))
            }
        }

        binding.btnDeleteLibrary.setOnClickListener {
            val selection = adapterLibraries.getSelected()
            var result = false
            if (selection != null){
                runBlocking {
                    val coroutine = launch {
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

    private fun getAuthor(){
        runBlocking {
            val coroutine = launch {
                author = api.getBookAuthors(bookId)
            }
            coroutine.join()
        }
        if (author != null){
            binding.etAuthor.setText(author!!.name)
        }
    }

    private fun getLang(){
        runBlocking {
            val coroutine = launch {
                lang = api.getBookLang(bookId)
            }
            coroutine.join()
        }
        if (lang != null){
            binding.etLanguage.setText(lang!!.name)
        }
    }

    private fun getGenres(addAdapter: Boolean){
        runBlocking {
            val coroutine = launch {
                genres = api.getBookGenres(bookId) as MutableList<Genre>?
                if (addAdapter){
                    binding.rvGenres.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapterGenres = AdminGenresAdapter(genres as ArrayList<Genre>)
                    binding.rvGenres.adapter = adapterGenres
                } else {
                    adapterGenres.updateList(genres as ArrayList<Genre>)
                }
            }
            coroutine.join()
        }
    }

    private fun getLibraries(addAdapter: Boolean){
        runBlocking {
            val coroutine = launch {
                libraries = api.getBookLibraries(bookId) as MutableList<LibraryExtended>?
                if (addAdapter){
                    binding.rvLibraries.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapterLibraries = AdminBookLibraryAdapter(libraries as ArrayList<LibraryExtended>)
                    binding.rvLibraries.adapter = adapterLibraries
                } else {
                    adapterLibraries.updateList(libraries as ArrayList<LibraryExtended>)
                }
            }
            coroutine.join()
        }
    }

    override fun onApiError(connectionFailed: Boolean) {
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