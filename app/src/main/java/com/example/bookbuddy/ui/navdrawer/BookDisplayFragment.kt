package com.example.bookbuddy.ui.navdrawer

import android.graphics.text.LineBreaker
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.GenreAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookDisplayBinding
import com.example.bookbuddy.models.*
import com.example.bookbuddy.utils.navController
import com.example.bookbuddy.utils.navView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class BookDisplayFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentBookDisplayBinding
    private var job: Job = Job()
    private var book: Book? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentBookDisplayBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        println("ON CREATE VIEW EXECUTED")
        binding.bookMark.tag = "Add"

        val isbn = arguments?.getString("isbn")

        launch {
            book = getBook(isbn)!!
            setBook(book)
            getBookMark(book!!.bookId, 1)
            getCommentsNumber(book!!.bookId)
            getLibraries(isbn)
            loadingEnded()
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
                setBookMark(book!!.bookId, 1)
                Toast.makeText(activity, "Added to pending books", Toast.LENGTH_SHORT).show()
            } else {
                deleteBookMark(book!!.bookId, 1)
                Toast.makeText(activity, "Remove book from pending", Toast.LENGTH_SHORT).show()
            }
        }

        binding.iconAddComments.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("book_id", book!!.bookId)
            navController.navigate(R.id.nav_write_comment, bundle)
        }

        binding.iconComments.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("book_id", book!!.bookId)
            navController.navigate(R.id.nav_read_comment, bundle)
        }

        binding.iconLibraries.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("isbn", book!!.isbn)
            navController.navigate(R.id.nav_libraries_list, bundle)
            /*
            val newFragment = LibrariesListFragment()
            newFragment.arguments = bundle
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.fragment_book_display, newFragment)
                .addToBackStack(null)
                .commit()

             */
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
        super.onDestroy()
        job.cancel()
    }
}