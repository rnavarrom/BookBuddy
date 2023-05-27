package com.example.bookbuddy.ui.navdrawer.bookdisplay

import android.content.Context
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.speech.tts.TextToSpeech
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.Utils.Constants.Companion.bookRequestOptions
import com.example.bookbuddy.adapters.GenreAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogBookdisplayBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Genre
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.ui.navdrawer.HomeFragment
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.coroutines.CoroutineContext

@Parcelize
class BookDisplayDialog : DialogFragment(), CoroutineScope, TextToSpeech.OnInitListener,
    ApiErrorListener, CommentWriteDialog.OnWriteCommentClose,
    CommentsListDialog.OnReadCommentClose, Parcelable {
    lateinit var binding: DialogBookdisplayBinding
    private var job: Job = Job()
    private var book: Book? = null
    private var readed: Readed? = null
    private var tts: TextToSpeech? = null
    private lateinit var textts: String
    private var popup: PopupMenu? = null
    private var onBookDisplayClose: OnBookDisplayClose? = null
    private val api = CrudApi(this@BookDisplayDialog)
    private var isOnCreateViewExecuted = false
    private var isPlaying = false


    interface OnBookDisplayClose {
        fun onBookDisplayClose()
    }

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
        binding = DialogBookdisplayBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        setToolBar(this, binding.toolbar, requireContext(), getString(R.string.TB_BookDisplay))

        tts = TextToSpeech(context, this)

        val bundle = requireArguments().getBundle("bundle")
        val isbn: String?
        if (bundle != null){
            isbn = bundle.getString("isbn")!!
            println("AAAAAAAAAaa")
            println(isbn)
            binding.iconTextToSpeach.setOnClickListener {
                speak()
            }

            if (bundle.containsKey("fragment")){
                val fragment = bundle.getParcelable("fragment") as? HomeFragment?
                onBookDisplayClose = fragment
            }

            //launch {
            book = getBook(isbn)
            if (book != null) {
                //readed = null
                //getBookMark(book!!.bookId, currentUser.userId)
                loadBook(book!!, isbn)
                binding.bookMark.setOnClickListener {
                    popup = PopupMenu(context, binding.bookMark)
                    popup!!.menuInflater
                        .inflate(R.menu.book_menu, popup!!.menu)
                    //popup.setOnDismissListener {
                    //holder.dropmenu.setImageResource(R.drawable.ic_drop_down_menu)
                    //}
                    popup!!.setOnMenuItemClickListener { item ->
                        var result: Boolean?
                        when (item.itemId) {
                            R.id.reading_book -> {
                                if (readed == null || readed!!.curreading != 3) {
                                    runBlocking {
                                        val corroutine = launch {
                                            result = api.setBookReading(
                                                book!!.bookId,
                                                currentUser!!.userId
                                            )
                                            if(result == true ){
                                                getReaded(book!!.bookId)
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
                                                currentUser!!.userId
                                            )
                                            if(result == true) {
                                                getReaded(book!!.bookId)
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
                                                currentUser!!.userId
                                            )
                                            if(result == true) {
                                                getReaded(book!!.bookId)
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
                                            currentUser!!.userId
                                        )
                                        if(result == true){
                                            readed = null
                                        }
                                    }
                                    corroutine.join()
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    popup!!.show()

                    val remove = popup!!.menu.findItem(R.id.remove_book)
                    if (readed != null) {
                        remove.isVisible = true
                        val tmpItem = popup!!.menu.getItem(readed!!.curreading!! - 1)
                        tmpItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                        tmpItem.isCheckable = true
                        tmpItem.isChecked = true
                    } else {
                        remove.isVisible = false
                    }
                }
                loadingEnded()
                //createRequest(isbn)
                //dismiss()
            }
        }
        isOnCreateViewExecuted = true
        return binding.root
    }

    fun loadBook(book: Book, isbn: String){
        var corroutine = launch {
            println("Starting")
            getReaded(book.bookId)
            setBook(book)
            getCommentsNumber(book.bookId)
            getLibraries(isbn)
        }
        corroutine.invokeOnCompletion {
            println("completed")
            loadingEnded()
        }
    }

    override fun onStart() {
        super.onStart()
        if (book == null){
            navController.popBackStack()
        }
    }

    override fun onWriteCommentClose() {
        if (book != null){
            getCommentsNumber(book!!.bookId)
        }
    }

    override fun onReadCommentClose() {
        // TODO : prints
        println("PROBANDO")
        if (book != null){
            getCommentsNumber(book!!.bookId)
        }
    }

    private fun getCommentsNumber(bookId: Int) {
        var commentsNumber: Int? = 0
        val corrutina = launch {
            commentsNumber = api.getCommentsCounter(bookId)
            binding.numberComments.text = commentsNumber.toString()
        }
    }

    private fun getLibraries(isbn: String?) {
        var librariesNumber: Int? = 0
        val corrutina = launch {
            librariesNumber = api.getBookLibrariesCount(isbn!!)
            binding.numberLibraries.text = librariesNumber.toString()
        }
    }

    private fun loadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.cl.visibility = View.VISIBLE
        binding.dBookDescription.movementMethod = ScrollingMovementMethod()
        binding.iconAddComments.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookid", book!!.bookId)
            bundle.putString("isbn", book!!.isbn)
            bundle.putParcelable("fragment", this)
            val action = BookDisplayDialogDirections.actionNavBookDisplayToNavWriteComment(bundle)
            navController.navigate(action)
        }
        binding.iconComments.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookid", book!!.bookId)
            bundle.putString("title", book!!.title)
            bundle.putParcelable("fragment", this)
            val action = BookDisplayDialogDirections.actionNavBookDisplayToNavReadComment(bundle)
            navController.navigate(action)
        }
        binding.iconLibraries.setOnClickListener {
            if (binding.numberLibraries.text.toString().toInt() == 0) {
                showSnackBar(requireContext(), requireView(), getString(R.string.SB_BookNotAviable))
            } else {
                val bundle = Bundle()
                bundle.putString("isbn", book!!.isbn)
                val action =
                    BookDisplayDialogDirections.actionNavBookDisplayToNavLibrariesList(bundle)
                navController.navigate(action)
            }
        }
    }

    private fun setBook(book: Book?) {
        Glide.with(requireActivity().applicationContext)
            .setDefaultRequestOptions(bookRequestOptions)
            .load(book?.cover)
            .into(binding.dBookCover)

        binding.dBookTitle.text = book!!.title
        //binding.dBookAuthor.text = book!!.authors.size.toString()
        binding.dBookAuthor.text = book.authors[0].name

        binding.dBookAuthor.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("authorid", book.authors[0].authorId)
            bundle.putString("name", book.authors[0].name)
            val action =
                BookDisplayDialogDirections.actionNavBookDisplayToNavAuthorBookDialog(bundle)
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
        binding.bookRatingDisplay.rating = book.rating.toFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.dBookDescription.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }
    }

    private fun getBook(isbn: String?): Book? {
        var response: Book? = null
        runBlocking {
            val corrutina = launch {
                response = api.getBook(isbn!!, currentUser!!.userId)
            }
            corrutina.join()
        }
        return response
    }

    private fun getReaded(bookId: Int): Readed? {
        var response: Readed? = null
        runBlocking {
            val coroutine = launch {
                response = api.getReadedsFromBook(bookId, currentUser!!.userId)
                readed = response
            }
            coroutine.join()
        }
        return response
    }

    private fun speak() {
        if (tts!!.isSpeaking) {
            tts!!.stop()
            isPlaying = false
        } else {
            if(!isPlaying){
                tts!!.defaultVoice
                textts =
                    binding.dBookTitle.text.toString() + "  " + binding.dBookDescription.text.toString()
                tts!!.speak(textts, TextToSpeech.QUEUE_FLUSH, null, null)
                isPlaying = true
            }

        }
    }
    override fun onInit(p0: Int) {
        var lang = getStoredLanguage()
        var country = getStoredLanguage()
        if (lang == "en"){
            country = "us"
        }else if (lang == "ca"){
            country = "es"
        }
        if (p0 == TextToSpeech.SUCCESS) {
            val output = tts!!.setLanguage(Locale(lang, country))
            if (output == TextToSpeech.LANG_MISSING_DATA ||
                output == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", getString(R.string.MSG_LangNotSuported))
            }
        }
    }


    override fun onApiError() {
        if (isOnCreateViewExecuted){
            showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
        }
    }
    override fun onDestroy() {
        if (onBookDisplayClose != null){
            onBookDisplayClose?.onBookDisplayClose()
        }
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
        job.cancel()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    //TODO : this go here?
    private fun getStoredLanguage(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        var code = sharedPreferences?.getString("language_code", "") ?: ""
        if (code.isEmpty()){
            code = context?.resources?.configuration?.locales?.get(0)?.language.toString()
        }
        return code
    }
}