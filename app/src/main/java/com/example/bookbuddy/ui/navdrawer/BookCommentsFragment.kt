package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.CommentAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentBookCommentsBinding
import com.example.bookbuddy.models.User.Comment
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.setToolBar
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class BookCommentsFragment : DialogFragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentBookCommentsBinding
    private var job: Job = Job()
    private var bookId: Int = 0
    lateinit var adapter: CommentAdapter
    private val api = CrudApi(this@BookCommentsFragment)

    private var position = 0
    private var lastPosition = -1
    var comments: MutableList<Comment>? = null

    private var onReadCommentClose: OnReadCommentClose? = null

    private var isOnCreateViewExecuted = false

    interface OnReadCommentClose {
        fun onReadCommentClose()
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
        binding =  FragmentBookCommentsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        setToolBar(this, binding.toolbar, requireContext(), "Write Comment")

        val bundle = arguments?.getBundle("bundle")
        bookId = bundle!!.getInt("bookid")

        if (bundle.containsKey("fragment")){
            val fragment = bundle.getSerializable("fragment") as? BookDisplayFragment?
            if (fragment != null){
                onReadCommentClose = fragment
            }
        }

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getCommentsBook(bookId, true)
        loadingEnded()
        isOnCreateViewExecuted = true
        return binding.root
    }

    private fun getCommentsBook(bookId: Int, addAdapter: Boolean){
        runBlocking {
            
            val corrutina = launch {
                if (position == 0){
                    comments = setCardview(api.getCommentsFromBook(bookId,position) as ArrayList<Comment>)
                } else {
                    comments!!.addAll((setCardview(api.getCommentsFromBook(bookId,position) as ArrayList<Comment>) as MutableList<Comment>?)!!)
                }
            }
            corrutina.join()
        }
        if (addAdapter){
            binding.rvComments.layoutManager = LinearLayoutManager(context)
            adapter = CommentAdapter(comments as ArrayList<Comment>)
            binding.rvComments.adapter = adapter
        } else {
            adapter.updateList(comments as ArrayList<Comment>)
        }
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE

        binding.addComment.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookid", bookId)
            val action = BookCommentsFragmentDirections.actionNavReadCommentToNavWriteComment(bundle)
            navController.navigate(action)

        }

        binding.mainContent.setOnRefreshListener {
            position = 0
           lastPosition = -1
            getCommentsBook(bookId, false)
            binding.mainContent.isRefreshing = false
        }

        binding.rvComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        position = totalItemCount
                        if (lastPosition != totalItemCount){
                            loadMoreItems()
                        }
                        lastPosition = totalItemCount
                    }
                }
            }
        })
    }

    private fun setCardview(coms: ArrayList<Comment>): ArrayList<Comment>{
        coms.forEach { c ->
            if (c.user!!.userId == currentUser.userId){
                c.typeCardview = 1
            }
        }
        return coms
    }

    private fun loadMoreItems() {
        binding.loadingComment.visibility = View.VISIBLE
        getCommentsBook(bookId, false)
        binding.loadingComment.visibility = View.GONE
    }

    override fun onApiError() {
        if (isOnCreateViewExecuted){
            showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
        }
    }

    override fun onDestroy() {
        onReadCommentClose?.onReadCommentClose()
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