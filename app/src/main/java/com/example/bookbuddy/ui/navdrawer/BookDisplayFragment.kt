package com.example.bookbuddy.ui.navdrawer

import android.graphics.text.LineBreaker
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.GenreAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookDisplayBinding
import com.example.bookbuddy.models.*
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class BookDisplayFragment : DialogFragment(), CoroutineScope, TextToSpeech.OnInitListener {
    lateinit var binding: FragmentBookDisplayBinding
    private var job: Job = Job()
    private var book: Book? = null
    private var tts: TextToSpeech? = null
    private lateinit var textts: String
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
        binding =  FragmentBookDisplayBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setToolBar(this, binding.toolbar, (activity as AppCompatActivity?)!!, "Book Display")

        binding.bookMark.tag = "Add"
        tts = TextToSpeech(context,this)
        //val isbn = arguments?.getString("isbn")
        val bundle = arguments?.getBundle("bundle")
        val isbn = bundle?.getString("isbn")
        binding.iconTextToSpeach.setOnClickListener {
            Speak()
        }

        launch {
            book = getBook(isbn)!!
            if (book == null){
                //println("book null")
                //childFragmentManager.popBackStack()
                dismiss()
                //val fragmentManager = requireActivity().supportFragmentManager
                //fragmentManager.popBackStack()
                //navController.popBackStack(R.id.nav_scan, true)
                //navController.navigate(R.id.nav_scan, null)
            } else {
                setBook(book)
                getBookMark(book!!.bookId, currentUser.userId)
                getCommentsNumber(book!!.bookId)
                getLibraries(isbn)
                loadingEnded()
            }
        }

        return binding.root
    }

    fun setBookMark(bookId: Int, userId: Int){
        var added = false
        var readed = Readed(bookId = bookId, userId = userId, book = null, user = null, curreading = false, percentatgeRead = 0.0, readedId = null)
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                added = crudApi.addReadedToAPI(readed)
            }
            corrutina.join()
        }
        if (added){
            binding.bookMark.tag = "Remove"
            binding.bookMark.setImageResource(R.drawable.ic_markbook_pending)
        }
    }

    fun deleteBookMark(bookId: Int, userId: Int){
        var deleted = false
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                var readed = crudApi.getReadedsFromBook(bookId, userId)
                deleted = crudApi.deleteReadedToAPI(readed!!.readedId!!)
            }
            corrutina.join()
        }
        if (deleted){
            binding.bookMark.tag = "Add"
            binding.bookMark.setImageResource(R.drawable.ic_markbook_read)
        }
    }

    fun getBookMark(bookId: Int, userId: Int){
        var readed: Readed? = null
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                 readed = crudApi.getReadedsFromBook(bookId, userId)
            }
            corrutina.join()
        }
        if (readed != null){
            binding.bookMark.tag = "Remove"
            binding.bookMark.setImageResource(R.drawable.ic_markbook_pending)
        }
    }

    fun getCommentsNumber(bookId: Int){
        var commentsNumber: Int? = 0
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                commentsNumber = crudApi.getCommentsCounter(bookId)
            }
            corrutina.join()
        }
        binding.numberComments.text = commentsNumber.toString()
    }

    fun getLibraries(isbn: String?){
        var librariesNumber: Int? = 0
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                librariesNumber = crudApi.getBookLibrariesCount(isbn!!)
            }
            corrutina.join()
        }
        binding.numberLibraries.text = librariesNumber.toString()
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.cl.visibility = View.VISIBLE

        binding.dBookDescription.movementMethod = ScrollingMovementMethod();

        binding.bookMark.setOnClickListener {
            if (binding.bookMark.tag.equals("Add")){
                setBookMark(book!!.bookId, currentUser.userId)
                Toast.makeText(activity, "Added to pending books", Toast.LENGTH_SHORT).show()
            } else {
                deleteBookMark(book!!.bookId, currentUser.userId)
                Toast.makeText(activity, "Remove book from pending", Toast.LENGTH_SHORT).show()
            }
        }

        binding.iconAddComments.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookid", book!!.bookId)
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
            val bundle = Bundle()
            bundle.putString("isbn", book!!.isbn)
            var action = BookDisplayFragmentDirections.actionNavBookDisplayToNavLibrariesList(bundle)
            navController.navigate(action)
        }
    }

    fun setBook(book: Book?){
        try {
            Glide.with(requireActivity().applicationContext)
                .load(book?.cover)
                .error(R.drawable.ic_error)
                .into(binding.dBookCover)
        } catch (e: Exception){
            println(e)
        }

        binding.dBookTitle.text = book!!.title
        binding.dBookAuthor.text = book!!.authors.size.toString()
        binding.dBookPublishdate.text = book!!.publicationDate
        if (book.genres.size > 10) {
            binding.rvGenres.layoutManager = GridLayoutManager(context, 3)
        } else if (book.genres.size > 5) {
            binding.rvGenres.layoutManager = GridLayoutManager(context, 2)
        } else {
            binding.rvGenres.layoutManager = GridLayoutManager(context, 1)
        }
        binding.rvGenres.adapter = GenreAdapter(book.genres as ArrayList<Genre>)

        binding.dBookDescription.text = book!!.description
        binding.dBookDescription.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        binding.dBookPages.text = "Pages: " + book!!.pages
        binding.bookRatingDisplay.rating = book?.rating!!.toFloat()
    }

    fun getBook(isbn: String?) :Book?{
        var response: Book? = null
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                response = crudApi.getBook(isbn!!)
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
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
        job.cancel()
    }
private fun Speak(){
    if(tts!!.isSpeaking){
        tts!!.stop()
    }else{

        tts!!.getDefaultVoice()
        textts = binding.dBookTitle.text.toString() + "  " + binding.dBookDescription.text.toString()
        tts!!.speak(textts, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
    override fun onInit(p0: Int) {
        if(p0 == TextToSpeech.SUCCESS){
            var output = tts!!.setLanguage(Locale("es", "ES")) // tts!!.getDefaultVoice().getLocale()
            if(output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "The language is not supported")
            }else{

            }
        }
    }
}