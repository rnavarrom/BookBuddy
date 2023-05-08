package com.example.bookbuddy.ui.navdrawer

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.bookbuddy.R
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentWriteCommentBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class WriteCommentFragment : DialogFragment(), CoroutineScope {
    lateinit var binding: FragmentWriteCommentBinding
    private var job: Job = Job()

    private var bookId: Int = 0
    private var comment: Comment? = null
    private var maxCharactersComment: Int = 300
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
        binding =  FragmentWriteCommentBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setToolBar(this, binding.toolbar, (activity as AppCompatActivity?)!!, "Write Comment")

        val bundle = arguments?.getBundle("bundle")
        bookId = bundle?.getInt("bookid")!!

        launch {
            loadComment()
            loadingEnded()
        }

        return binding.root
    }

    fun loadComment(){
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                comment = crudApi.getCommentsFromUser(currentUser.userId, bookId)
            }
            corrutina.join()
        }

        if (comment != null){
            binding.etWriteComment.setText(comment!!.comentText)
            binding.ratingWrite.rating = comment!!.rating.toFloat()
            binding.wordCounter.text = comment!!.comentText.length.toString() + "/" + maxCharactersComment.toString()
        }
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
        binding.etWriteComment.filters = arrayOf<InputFilter>(LengthFilter(maxCharactersComment))
        binding.etWriteComment.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.wordCounter.text = s!!.length.toString() + "/" + maxCharactersComment.toString()
                if (s!!.length == maxCharactersComment){
                    binding.etWriteComment.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_border_comment_max)
                } else {
                    binding.etWriteComment.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_border_comment)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.bSendComment.setOnClickListener {
            insertComment()
        }
    }

    fun insertComment(){
        var text = binding.etWriteComment.text.toString()
        var stars = binding.ratingWrite.rating.toInt()
        println(text + "AAAAAAAAAAA")
        if (!text.isNullOrEmpty()){
            runBlocking {
                val crudApi = CrudApi()
                val corrutina = launch {
                    if (comment != null){
                        crudApi.updateCommentToAPI(comment!!.comentId!!, text, stars, currentUser.userId, bookId!!)
                    } else {
                        crudApi.addCommentToAPI(text, stars, currentUser.userId, bookId!!)
                    }
                }
                corrutina.join()
            }
            // Close
            dismiss()
        }
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