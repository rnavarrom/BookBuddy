package com.example.bookbuddy.ui.navdrawer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentWriteCommentBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.models.User.Comment2
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class WriteCommentFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentWriteCommentBinding
    private var job: Job = Job()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentWriteCommentBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        val book_id = arguments?.getInt("book_id")

        binding.bSendComment.setOnClickListener {
            var text = binding.etWriteComment.text.toString()
            println(text + "AAAAAAAAAAA")
            if (!text.isNullOrEmpty()){
                runBlocking {
                    val crudApi = CrudApi()
                    val corrutina = launch {
                        crudApi.addCommentToAPI(text as String, 1, book_id!!)
                    }
                    corrutina.join()
                }
                val bundle = Bundle()
                bundle.putInt("book_id", book_id!!)
                navController.navigate(R.id.nav_read_comment, bundle)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etWriteComment.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etWriteComment.requestFocus()
        imm.showSoftInput(binding.etWriteComment, InputMethodManager.SHOW_IMPLICIT)
        binding.etWriteComment.requestFocus()
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