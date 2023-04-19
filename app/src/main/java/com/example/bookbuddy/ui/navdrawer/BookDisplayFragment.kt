package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.GenreAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookDisplayBinding
import com.example.bookbuddy.databinding.FragmentProfileBinding
import com.example.bookbuddy.models.Book
import com.example.bookbuddy.models.Genre
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.ArrayList

class BookDisplayFragment : Fragment() {
    lateinit var binding: FragmentBookDisplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentBookDisplayBinding.inflate(layoutInflater, container, false)

        var book = getBook("9788408004097")

        setBook(book)

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
}