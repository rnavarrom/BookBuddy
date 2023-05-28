package com.example.bookbuddy.ui.navdrawer.bookdisplay

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
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.DialogBookdisplayWriteCommentBinding
import com.example.bookbuddy.models.UserComments.Comment
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Constants
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Show write review display
 */
class CommentWriteDialog : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: DialogBookdisplayWriteCommentBinding
    private var job: Job = Job()
    private var bookId: Int = 0
    private var comment: Comment? = null
    private var maxCharactersComment: Int = 300
    private val api = CrudApi(this@CommentWriteDialog)
    private var onWriteCommentClose: OnWriteCommentClose? = null

    private var isOnCreateViewExecuted = false
    private var connectionError = false

    interface OnWriteCommentClose {
        fun onWriteCommentClose()
    }

    // Set fullscreen dialog style
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
        binding = DialogBookdisplayWriteCommentBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setToolBar(this, binding.toolbar, requireContext(), getString(R.string.TB_WriteComment))

        val bundle = arguments?.getBundle("bundle")
        bookId = bundle?.getInt("bookid")!!

        if (bundle.containsKey("fragment")) {
            val fragment = bundle.getParcelable("fragment") as? BookDisplayDialog?
            if (fragment != null) {
                onWriteCommentClose = fragment
            }
        } else if (bundle.containsKey("fragmentComments")) {
            val fragment = bundle.getParcelable("fragmentComments") as? CommentsListDialog?
            if (fragment != null) {
                onWriteCommentClose = fragment
            }
        }

        loadComment()
        onLoadingEnded()
        isOnCreateViewExecuted = true
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.etWriteComment.postDelayed({
            binding.etWriteComment.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etWriteComment, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun loadComment() {
        runBlocking {
            val coroutine = launch {
                comment = api.getCommentsFromUser(currentUser!!.userId, bookId)
            }
            coroutine.join()
        }

        if (!checkConnectionFailed()) {
            if (comment != null) {
                binding.etWriteComment.setText(comment!!.comentText)
                binding.ratingWrite.rating = comment!!.rating.toFloat()
                binding.wordCounter.text =
                    comment!!.comentText.length.toString() + "/" + maxCharactersComment.toString()
            }
        }
    }

    // Change visible layouts and add bindings
    private fun onLoadingEnded() {
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
        binding.etWriteComment.filters = arrayOf<InputFilter>(LengthFilter(maxCharactersComment))
        binding.etWriteComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.wordCounter.text =
                    s!!.length.toString() + "/" + maxCharactersComment.toString()
                if (s.length == maxCharactersComment) {
                    binding.etWriteComment.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.edit_text_border_comment_max
                    )
                } else {
                    binding.etWriteComment.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.edit_text_border_comment
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.btnSend.setOnClickListener {
            insertComment()
        }
    }

    private fun insertComment() {
        val text = binding.etWriteComment.text.toString()
        val stars = binding.ratingWrite.rating.toInt()
        if (text.isNotEmpty()) {
            runBlocking {
                val coroutine = launch {
                    if (comment != null) {
                        api.updateCommentToAPI(
                            comment!!.comentId!!, text, stars, currentUser!!.userId,
                            bookId
                        )
                    } else {
                        api.addCommentToAPI(text, stars, currentUser!!.userId, bookId)
                    }
                }
                coroutine.join()
            }
            // Close
            dismiss()
        }
    }

    private fun checkConnectionFailed(): Boolean {
        if (connectionError) {
            connectionError = false
            return true
        }
        return false
    }

    override fun onApiError(connectionFailed: Boolean) {
        if (isOnCreateViewExecuted) {
            if (connectionFailed) {
                connectionError = true
                showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
            }
        }
    }

    override fun onDestroy() {
        onWriteCommentClose?.onWriteCommentClose()
        super.onDestroy()
        job.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
}