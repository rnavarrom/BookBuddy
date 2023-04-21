package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.CommentAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookCommentsBinding
import com.example.bookbuddy.databinding.FragmentSettingsBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext

class BookCommentsFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentBookCommentsBinding
    private var job: Job = Job()
    private var bookId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentBookCommentsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        bookId = requireArguments().getInt("book_id")


        launch {
            getCommentsBook(bookId)
            loadingEnded()
        }

        return binding.root
    }

    fun getCommentsBook(bookId: Int){
        var comments: List<Comment>? = null
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                comments = crudApi.getCommentsFromBook(bookId,0)
            }
            corrutina.join()
        }

        binding.rvComments.layoutManager = LinearLayoutManager(context)
        binding.rvComments.adapter = CommentAdapter(comments as ArrayList<Comment>)
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE

        binding.addComment.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("book_id", bookId)
            navController.navigate(R.id.nav_write_comment, bundle)
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
}