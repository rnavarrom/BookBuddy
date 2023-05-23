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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentWriteCommentBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class WriteCommentFragment : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentWriteCommentBinding
    private var job: Job = Job()
    private var bookId: Int = 0
    private var isbn: String = ""
    private var comment: Comment? = null
    private var maxCharactersComment: Int = 300
    private val api = CrudApi(this@WriteCommentFragment)

    private var onWriteCommentClose: OnWriteCommentClose? = null
    interface OnWriteCommentClose {
        fun onWriteCommentClose(isbn: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    override fun onResume() {
        super.onResume()
        binding.etWriteComment.postDelayed({
            binding.etWriteComment.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etWriteComment, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentWriteCommentBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setToolBar(this, binding.toolbar, requireContext(), "Write Comment")

        val bundle = arguments?.getBundle("bundle")
        bookId = bundle?.getInt("bookid")!!
        val fragment = bundle.getSerializable("fragment") as? BookDisplayFragment?
        if (fragment != null){
            println("YES2")
            onWriteCommentClose = fragment
        }

        launch {
            loadComment()
            loadingEnded()
        }

        return binding.root
    }

    private fun loadComment(){
        runBlocking {            
            val corrutina = launch {
                comment = api.getCommentsFromUser(currentUser.userId, bookId)
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
                if (s.length == maxCharactersComment){
                    binding.etWriteComment.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_border_comment_max)
                } else {
                    binding.etWriteComment.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_border_comment)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.btnSend.setOnClickListener {
            insertComment()
        }
    }

    private fun insertComment(){
        val text = binding.etWriteComment.text.toString()
        val stars = binding.ratingWrite.rating.toInt()
        if (text.isNotEmpty()){
            runBlocking {                
                val corrutina = launch {
                    if (comment != null){
                        api.updateCommentToAPI(comment!!.comentId!!, text, stars, currentUser.userId,
                            bookId
                        )
                    } else {
                        api.addCommentToAPI(text, stars, currentUser.userId, bookId)
                    }
                }
                corrutina.join()
            }
            // Close
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        onWriteCommentClose?.onWriteCommentClose(isbn)
        super.onDestroy()
        job.cancel()
    }

    override fun onApiError() {
        Tools.showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}