package com.example.bookbuddy.ui.navdrawer

import android.content.Context
import android.graphics.text.LineBreaker
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.GenreAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookDisplayBinding
import com.example.bookbuddy.models.*
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback.DismissEvent
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class BookDisplayFragment : DialogFragment(), CoroutineScope, TextToSpeech.OnInitListener,
    ApiErrorListener, WriteCommentFragment.OnWriteCommentClose, java.io.Serializable {
    lateinit var binding: FragmentBookDisplayBinding
    private var job: Job = Job()
    private var book: Book? = null
    private var readed: Readed? = null
    private var tts: TextToSpeech? = null
    private lateinit var textts: String
    private var popup: PopupMenu? = null
    public var onBookDisplayClose: OnBookDisplayClose? = null
    var api = CrudApi(this@BookDisplayFragment)

    public interface OnBookDisplayClose {
        fun onBookDisplayClose()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(book != null) {
            val bundle = requireArguments().getBundle("bundle")
            val fragment = bundle!!.getSerializable("fragment") as? HomeFragment?
            if (fragment != null) {
                onBookDisplayClose = fragment
            }
        }
    }
    override fun onWriteCommentClose(isbn: String) {
        print("CLOSE")
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!,true)
        val bundle = Bundle()
        bundle.putString("isbn", isbn)
        navController.navigate(id, bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    fun createRequest(isbn: String?) {
        if(isbn != null){
        runBlocking {
            var corroutine = launch {
                api.addRequestAPI(isbn)
            }
            corroutine.join()
        }
    }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookDisplayBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setToolBar(this, binding.toolbar, requireContext(), "Book Display")

        //binding.bookMark.tag = "Add"
        tts = TextToSpeech(context, this)
        //val isbn = arguments?.getString("isbn")
        val bundle = requireArguments().getBundle("bundle")
        var isbn: String? = null
        // TODO: null pointer from write comment back
        if (bundle != null){
            isbn = bundle!!.getString("isbn")!!
            binding.iconTextToSpeach.setOnClickListener {
                Speak()
            }

            val fragment = bundle!!.getSerializable("fragment") as? HomeFragment?
            if (fragment != null){
                onBookDisplayClose = fragment
            }

            //launch {
            book = getBook(isbn)
            if (book == null) {
                createRequest(isbn)
                dismiss()
            }else{


                readed = getReaded(book!!.bookId)
                if (book == null) {
                    //println("book null")
                    //childFragmentManager.popBackStack()
                    dismiss()
                    //val fragmentManager = requireActivity().supportFragmentManager
                    //fragmentManager.popBackStack()
                    //navController.popBackStack(R.id.nav_scan, true)
                    //navController.navigate(R.id.nav_scan, null)
                } else {
                    setBook(book)
                    //getBookMark(book!!.bookId, currentUser.userId)
                    getCommentsNumber(book!!.bookId)
                    getLibraries(isbn)
                    if (binding.bookMark != null) {
                        binding.bookMark.setOnClickListener {
                            popup = PopupMenu(context, binding.bookMark)
                            popup!!.getMenuInflater()
                                .inflate(R.menu.book_menu, popup!!.getMenu())
                            //popup.setOnDismissListener {
                            //holder.dropmenu.setImageResource(R.drawable.ic_drop_down_menu)
                            //}
                            // TODO: If checked no execute api call // Done

                            popup!!.setOnMenuItemClickListener { item ->
                                var result: Boolean? = false
                                when (item.itemId) {
                                    R.id.reading_book -> {
                                        if (readed == null || readed!!.curreading != 3) {
                                            runBlocking {
                                                val corroutine = launch {
                                                    result = api.setBookReading(
                                                        book!!.bookId,
                                                        currentUser.userId
                                                    )
                                                    if(result == true ){
                                                        readed = getReaded(book!!.bookId)
                                                        readed!!.curreading = 3
                                                    }
                                                }
                                                corroutine.join()
                                            }
                                        }
                                        true
                                    }
                                    R.id.pending_book -> {
                                        if (readed == null || readed!!.curreading != 1) {
                                            runBlocking {
                                                val corroutine = launch {
                                                    result = api.setBookPending(
                                                        book!!.bookId,
                                                        currentUser.userId
                                                    )
                                                    if(result == true) {
                                                        readed = getReaded(book!!.bookId)
                                                        readed!!.curreading = 1
                                                    }
                                                }
                                                corroutine.join()
                                            }
                                        }
                                        true
                                    }
                                    R.id.read_book -> {
                                        if (readed == null || readed!!.curreading != 2) {
                                            runBlocking {
                                                val corroutine = launch {
                                                    result = api.setBookRead(
                                                        book!!.bookId,
                                                        currentUser.userId
                                                    )
                                                    if(result == true) {
                                                        readed = getReaded(book!!.bookId)
                                                        readed!!.curreading = 2
                                                    }
                                                }
                                                corroutine.join()
                                            }
                                        }
                                        true
                                    }

                                    R.id.remove_book -> {
                                        runBlocking {
                                            val corroutine = launch {
                                                result = api.removeBookReading(
                                                    book!!.bookId,
                                                    currentUser.userId
                                                )
                                                if(result == true){
                                                    readed = null
                                                }
                                            }
                                            corroutine.join()
                                            //list.removeAt(position)
                                            //notifyDataSetChanged()
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            }
                            popup!!.show()

                            var remove = popup!!.menu.findItem(R.id.remove_book)
                            if (readed != null) {
                                remove.isVisible = true
                                var tmpItem = popup!!.menu.getItem(readed!!.curreading!! - 1)
                                tmpItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                                tmpItem.setCheckable(true)
                                tmpItem.isChecked = true
                            } else {
                                remove.isVisible = false
                            }
                        }
                    }
                    loadingEnded()
                }
            }
        }else{
            var action = BookDisplayFragmentDirections.actionNavBookDisplayToNavHome()
            navController.navigate(action)
            dismiss()
            //return view
        }

        return binding.root
    }
/*
    fun setBookMark(bookId: Int, userId: Int){
        var added = false
        var readed = Readed(bookId = bookId, userId = userId, book = null, user = null, curreading = 0, percentatgeRead = 0.0, readedId = null)
        runBlocking {
            val api = CrudApi()
            val corrutina = launch {
                added = api.addReadedToAPI(readed)
            }
            corrutina.join()
        }
        if (added){
            binding.bookMark.tag = "Remove"
            binding.bookMark.setImageResource(R.drawable.ic_markbook_pending)
        }
    }

 */
/*
    fun deleteBookMark(bookId: Int, userId: Int){
        var deleted = false
        runBlocking {
            val api = CrudApi()
            val corrutina = launch {
                var readed = api.getReadedsFromBook(bookId, userId)
                deleted = api.deleteReadedToAPI(readed!!.readedId!!)
            }
            corrutina.join()
        }
        if (deleted){
            binding.bookMark.tag = "Add"
            binding.bookMark.setImageResource(R.drawable.ic_markbook_read)
        }
    }

 */
/*
    fun getBookMark(bookId: Int, userId: Int){
        var readed: Readed? = null
        runBlocking {
            val api = CrudApi()
            val corrutina = launch {
                 readed = api.getReadedsFromBook(bookId, userId)
            }
            corrutina.join()
        }
        if (readed != null){
            binding.bookMark.tag = "Remove"
            binding.bookMark.setImageResource(R.drawable.ic_markbook_pending)
        }
    }

 */

    fun getCommentsNumber(bookId: Int) {
        var commentsNumber: Int? = 0
        runBlocking {
            val api = CrudApi(this@BookDisplayFragment)
            val corrutina = launch {
                commentsNumber = api.getCommentsCounter(bookId)
            }
            corrutina.join()
        }
        binding.numberComments.text = commentsNumber.toString()
    }

    fun getLibraries(isbn: String?) {
        var librariesNumber: Int? = 0
        runBlocking {
            val api = CrudApi(this@BookDisplayFragment)
            val corrutina = launch {
                librariesNumber = api.getBookLibrariesCount(isbn!!)
            }
            corrutina.join()
        }
        binding.numberLibraries.text = librariesNumber.toString()
    }

    fun loadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.cl.visibility = View.VISIBLE

        binding.dBookDescription.movementMethod = ScrollingMovementMethod()
        binding.iconAddComments.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookid", book!!.bookId)
            bundle.putString("isbn", book!!.isbn)
            bundle.putSerializable("fragment", this)
            var action = BookDisplayFragmentDirections.actionNavBookDisplayToNavWriteComment(bundle)
            navController.navigate(action)
        }

        binding.iconComments.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookid", book!!.bookId)
            var action = BookDisplayFragmentDirections.actionNavBookDisplayToNavReadComment(bundle)
            navController.navigate(action)
        }

        binding.iconLibraries.setOnClickListener {
            if (binding.numberLibraries.text.toString().toInt() == 0) {
                showSnackBar(requireContext(), requireView(), "Book not available in any library")
            } else {
                val bundle = Bundle()
                bundle.putString("isbn", book!!.isbn)
                val action = BookDisplayFragmentDirections.actionNavBookDisplayToNavLibrariesList(bundle)
                navController.navigate(action)
            }
        }
    }

    fun setBook(book: Book?) {
        try {
            Glide.with(requireActivity().applicationContext)
                .load(book?.cover)
                .error(R.drawable.ic_error)
                .into(binding.dBookCover)
        } catch (e: Exception) {
            println(e)
        }

        binding.dBookTitle.text = book!!.title
        //binding.dBookAuthor.text = book!!.authors.size.toString()

        if (book!!.authors.size <= 2) {
            book!!.authors.forEach {
                binding.dBookAuthor.text = binding.dBookAuthor.text.toString() + it.name + "\n"
            }
        }

        binding.dBookAuthor.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("authorid", book.authors[0].authorId)
            bundle.putString("name", book.authors[0].name)
            var action = BookDisplayFragmentDirections.actionNavBookDisplayToNavAuthorBookDialog(bundle)
            navController.navigate(action)
        }


        for (i in book.languages.indices) {
            binding.dBookLanguage.text =
                binding.dBookLanguage.text.toString() + book.languages[i].name
            if (i < book.languages.size - 1) {
                binding.dBookLanguage.text = binding.dBookLanguage.text.toString() + ", "
            }
        }
        binding.dBookPublishdate.text = book.publicationDate
        binding.dBookPages.text = "Pages: " + book.pages
        binding.dBookIsbn.text = "Isbn: " + book.isbn


        if (book.genres.size > 10) {
            binding.rvGenres.layoutManager = GridLayoutManager(context, 3)
        } else if (book.genres.size > 5) {
            binding.rvGenres.layoutManager = GridLayoutManager(context, 2)
        } else {
            binding.rvGenres.layoutManager = GridLayoutManager(context, 1)
        }
        binding.rvGenres.adapter = GenreAdapter(book.genres as ArrayList<Genre>)

        binding.dBookDescription.text = book.description
        binding.dBookDescription.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        binding.bookRatingDisplay.rating = book.rating.toFloat()
    }

    fun getBook(isbn: String?): Book? {
        var response: Book? = null
        runBlocking {
            val api = CrudApi(this@BookDisplayFragment)
            val corrutina = launch {
                response = api.getBook(isbn!!, currentUser.userId)
            }
            corrutina.join()
        }
        return response
    }

    fun getReaded(bookId: Int): Readed? {
        var response: Readed? = null
        runBlocking {
            val api = CrudApi(this@BookDisplayFragment)
            val corrutina = launch {
                response = api.getReadedsFromBook(bookId, currentUser.userId)
            }
            corrutina.join()
        }
        return response
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        onBookDisplayClose?.onBookDisplayClose()
        super.onDestroy()
        job.cancel()
    }
    private fun Speak() {
        if (tts!!.isSpeaking) {
            tts!!.stop()
        } else {

            tts!!.getDefaultVoice()
            textts =
                binding.dBookTitle.text.toString() + "  " + binding.dBookDescription.text.toString()
            tts!!.speak(textts, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            var output =
                tts!!.setLanguage(Locale("es", "ES")) // tts!!.getDefaultVoice().getLocale()
            if (output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The language is not supported")
            } else {

            }
        }
    }
    override fun onApiError() {
        dismiss()
        //showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}