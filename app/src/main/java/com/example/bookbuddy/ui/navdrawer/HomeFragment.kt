package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.HomeBooksAdapter
import com.example.bookbuddy.adapters.HomeReadingBooksAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentHomeBinding
import com.example.bookbuddy.models.Test.ActualReading
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.utils.Tools
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.currentUser
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeFragment : Fragment(), ApiErrorListener, BookDisplayFragment.OnBookDisplayClose,
    java.io.Serializable {
    lateinit var binding: FragmentHomeBinding
    private lateinit var adapterPending: HomeBooksAdapter
    private lateinit var adapterReaded: HomeBooksAdapter
    private lateinit var adapterReading: HomeReadingBooksAdapter
    private val api = CrudApi(this@HomeFragment)
    private var pendingList: MutableList<Pending> = arrayListOf()
    private var readedList: MutableList<Pending> = arrayListOf()
    private var readingList: MutableList<ActualReading> = arrayListOf()
    private var activeFilterText: String = ""
    var lastPositionPending = -1
    var positionPending = 0
    var lastPositionRead = -1
    var positionRead = 0
    var lastPositionReading = -1
    var positionReading = 0
    val startingPosition = 0

    private var isOnCreateViewExecuted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val onBackPressedDispatcher = requireActivity().onBackPressedDispatcher

        // Agregar un callback al OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(requireContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.ClosingAPP))
                        .setMessage(getString(R.string.ClosingAPP2))
                        .setPositiveButton(getString(R.string.Yes)) { _, _ -> requireActivity().finish() }
                        .setNegativeButton(getString(R.string.No), null)
                        .show()
                }
            })

        binding.homeRv.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        adapterReading = HomeReadingBooksAdapter(readingList as ArrayList<ActualReading>, this)
        binding.homeRv.adapter = adapterReading
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.homeRv)

        callAdapterPending(pendingList as ArrayList<Pending>)
        callAdapterReaded(readedList as ArrayList<Pending>)

        if (pendingList.isEmpty())
            loadMorePending(startingPosition)
        if (readedList.isEmpty())
            loadMoreRead(startingPosition)
        if (readingList.isEmpty())
            loadMoreReading(startingPosition)

        binding.refresh.setOnRefreshListener {
            //getUser()
            //reloadFragment()
            binding.refresh.isRefreshing = false
        }
        binding.icPendingSearch.setOnClickListener {
            filterBooks(true) //pendingList as ArrayList<Pending>,
        }
        binding.icReadedSearch.setOnClickListener {
            filterBooks(false) //readedList as ArrayList<Pending>,
        }

        binding.rvReadedbooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= startingPosition) {
                    recyclerView.post {
                        positionRead = totalItemCount
                        if (lastPositionRead != totalItemCount) {
                            if (activeFilterText.isNotBlank()) {
                                filterReadBooks(activeFilterText, positionRead)
                            } else {
                                loadMoreRead(positionRead)
                            }
                        }
                        lastPositionRead = totalItemCount
                    }
                }
            }
        })

        binding.rvPendingbooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= startingPosition) {
                    recyclerView.post {
                        positionPending = totalItemCount
                        if (lastPositionPending != totalItemCount) {
                            if (activeFilterText.isNotBlank()) {
                                filterPendingBooks(activeFilterText, positionPending)
                            } else {
                                loadMorePending(positionPending)
                            }
                        }
                        lastPositionPending = totalItemCount
                    }
                }
            }
        })

        binding.homeRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItem == totalItemCount - 1 && dy >= startingPosition) {
                    recyclerView.post {
                        positionReading = totalItemCount
                        if (lastPositionReading != totalItemCount) {
                            loadMoreReading(positionReading)
                        }
                        lastPositionReading = totalItemCount
                    }
                }
            }
        })
        isOnCreateViewExecuted = true
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        pendingList = arrayListOf()
        loadMorePending(startingPosition)
        readedList = arrayListOf()
        loadMoreRead(startingPosition)
        readingList = arrayListOf()
        loadMoreReading(startingPosition)

        emptyReading()
        emptyPending()
        emptyReaded()
    }

    private fun emptyReading(){
        if (readingList.isEmpty()){
            binding.emptyReading.text = "No reading Books"
            binding.emptyReading.visibility = View.VISIBLE
        } else {
            binding.emptyReading.visibility = View.GONE
        }
    }

    private fun emptyPending(){
        if (pendingList.isEmpty()){
            binding.emptyPending.text = "No pending Books"
            binding.emptyPending.visibility = View.VISIBLE
        } else {
            binding.emptyPending.visibility = View.GONE
        }
    }

    private fun emptyReaded(){
        if (readedList.isEmpty()){
            binding.emptyReaded.text = "No readed Books"
            binding.emptyReaded.visibility = View.VISIBLE
        } else {
            binding.emptyReaded.visibility = View.GONE
        }
    }

    fun loadMoreRead(position: Int) {
        runBlocking {
            val corrutina = launch {
                   val tempRead = api.getReadBooksFromUser(
                        currentUser.userId,
                        position)
                if(tempRead != null){
                    readedList.addAll(tempRead as MutableList<Pending>)
                }
            }
            corrutina.join()
        }
        adapterReaded.updateList(readedList as ArrayList<Pending>)
    }

    fun loadMorePending(position: Int) {
        runBlocking {
            val corrutina = launch {
                val tempList = api.getPendingBooksFromUser(
                    currentUser.userId,
                    position
                )
                if (tempList != null) {
                    pendingList.addAll(tempList as MutableList<Pending>)
                }
            }
            corrutina.join()
        }
        adapterPending.updateList(pendingList as ArrayList<Pending>)
    }

    private fun loadMoreReading(position: Int) {
        runBlocking {
            val corrutina = launch {
                val tempList = api.getReadingBooksFromUser(
                    currentUser.userId,
                    position
                )
                if (tempList != null) {
                    readingList.addAll(tempList as MutableList<ActualReading>)
                }
            }
            corrutina.join()
        }
        adapterReading.updateList(readingList as ArrayList<ActualReading>)
    }

    private fun filterBooks(choseList: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_filter_books, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.filter_et)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Filter") { _, _ ->

            activeFilterText = editText.text.toString()
            val position = startingPosition
            if (editText.text.isNotBlank()) {
                if (choseList) {
                    pendingList = arrayListOf()
                    filterPendingBooks(activeFilterText, position)
                    loadMoreRead(position)
                    adapterPending.updateList(pendingList as ArrayList<Pending>)
                    adapterReaded.updateList(readedList as ArrayList<Pending>)
                } else {
                    readedList = arrayListOf()
                    filterReadBooks(activeFilterText, position)
                    loadMorePending(position)
                    adapterPending.updateList(pendingList as ArrayList<Pending>)
                    adapterReaded.updateList(readedList as ArrayList<Pending>)
                }
            } else {
                pendingList = arrayListOf()
                readedList = arrayListOf()
                loadMoreRead(position)
                loadMorePending(position)
                callAdapterPending(pendingList as ArrayList<Pending>)
                callAdapterReaded(readedList as ArrayList<Pending>)
            }
        }
        builder.setNegativeButton(getString(R.string.BT_Cancel)) { dialogInterface, i -> }
        builder.show()
    }

    private fun callAdapterPending(pendingList: ArrayList<Pending>) {
        binding.rvPendingbooks.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        adapterPending = HomeBooksAdapter(pendingList, this)
        binding.rvPendingbooks.adapter = adapterPending
    }

    private fun callAdapterReaded(readedList: ArrayList<Pending>) {
        binding.rvReadedbooks.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        adapterReaded = HomeBooksAdapter(readedList, this)
        binding.rvReadedbooks.adapter = adapterReaded
    }

    private fun getUser() {
        runBlocking {
            val corrutina = launch {
                currentUser = api.getUserId(currentUser.userId)!!
            }
            corrutina.join()
        }
    }

    private fun filterPendingBooks(filter: String, position: Int) {
        runBlocking {
            val corrutina = launch {
                if (position == startingPosition) {
                    val tempList = api.filterPendingBook(
                        currentUser.userId,
                        filter,
                        startingPosition
                    ) as MutableList<Pending>
                    if (tempList != null) {
                        pendingList = tempList
                    }

                } else {
                    val tempList = api.filterPendingBook(
                        currentUser.userId,
                        filter,
                        position
                    )
                    if (tempList != null) {
                        pendingList.addAll(tempList as MutableList<Pending>)
                    }
                }
            }
            corrutina.join()
        }
    }

    fun filterReadBooks(filter: String, position: Int) {
        runBlocking {
            val corrutina = launch {
                if (position == startingPosition) {
                    val tempList =
                        api.filterReadBook(
                            currentUser.userId,
                            filter,
                            startingPosition
                        )
                    readedList = tempList as MutableList<Pending>
                } else {
                    val tempList = api.filterReadBook(
                        currentUser.userId,
                        filter,
                        position
                    )
                    readedList.addAll(tempList as MutableList<Pending>)
                }
            }
            corrutina.join()
        }
    }
/*
    fun reloadFragment() {
        getUser()
        Toast.makeText(context, "Reloading fragment", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            parentFragmentManager.beginTransaction().detach(this).commitNow()
            parentFragmentManager.beginTransaction().attach(this).commitNow()
        } else {
            parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
        }
    }

 */

    override fun onApiError() {
        if (isOnCreateViewExecuted){
            showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
        }
    }

    override fun onBookDisplayClose() {
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!, true)
        navController.navigate(id)
    }
}
