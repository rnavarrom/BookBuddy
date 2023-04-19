package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.GenreAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookDisplayBinding
import com.example.bookbuddy.databinding.FragmentProfileBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Genre
import kotlinx.coroutines.*
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext

class BookDisplayFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentBookDisplayBinding
    private var job: Job = Job()
    private var loading_ended = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentBookDisplayBinding.inflate(layoutInflater, container, false)

        loading_ended = false
        launch {
            var book = getBook("9788408004097")
            setBook(book)
            loading_ended = true
        }

        var progress = 0
        while (!loading_ended) {
            binding.loadingBar.progress = progress
            Thread.sleep(20) // set animation duration in ms
            progress += 1
            if (progress == 100){
                progress = 0
            }
        }

        binding.cl2.visibility = View.GONE
        binding.cl.visibility = View.VISIBLE

        binding.dBookDescription.movementMethod = ScrollingMovementMethod();

        binding.bookMark.setOnClickListener {
            if (binding.bookMark.id == R.drawable.ic_markbook_read){
                binding.bookMark.setImageResource(R.drawable.ic_markbook_pending)
                // Ad to pending
                Toast.makeText(activity, "Added to pending books", Toast.LENGTH_SHORT).show()
            } else {
                binding.bookMark.setImageResource(R.drawable.ic_markbook_read)
                // Remove from pending
                Toast.makeText(activity, "Remove book from pending", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    fun setBook(book: Book?){
        Glide.with(this)
            .load(book?.cover)
            .into(binding.dBookCover)
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
        // TODO: binding.dBookPages.text = "Pages: " + book!!.pages
        binding.bookRatingDisplay.rating = book?.rating!!.toFloat()
    }

    fun getBook(isbn: String) :Book?{
        var response: Book? = null
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                response = crudApi.getBook(isbn)
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