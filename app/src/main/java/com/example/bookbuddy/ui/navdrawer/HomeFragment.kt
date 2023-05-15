package com.example.bookbuddy.ui.navdrawer

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.HomeBooksAdapter
import com.example.bookbuddy.adapters.HomeReadingBooksAdapter
import com.example.bookbuddy.adapters.ProfileBookMarkAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentHomeBinding
import com.example.bookbuddy.models.Readed
import com.example.bookbuddy.models.Test.ActualReading
import com.example.bookbuddy.models.Test.Pending
import com.example.bookbuddy.utils.Tools.Companion.unaccent
import com.example.bookbuddy.utils.currentUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private lateinit var adapterPending: HomeBooksAdapter
    private lateinit var adapterReaded: HomeBooksAdapter
    private lateinit var adapterReading: HomeReadingBooksAdapter
    private lateinit var pendingList: MutableList<Pending>
    private lateinit var readedList: MutableList<Pending>
    private lateinit var readingList: MutableList<ActualReading>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

/*
        var pending = Pending(123456, emptyList(), emptyList(), "https://i.gr-assets.com/images/S/compressed.photo.goodreads.com/books/1522157426l/19063._SY475_.jpg",
        "", "", 500, "",3.5, emptyList(),"Libro de Prueba" )
        pendingList = ArrayList<Pending>()
        pendingList.add(pending)
 */

        pendingList = currentUser.pending as MutableList<Pending>
        readedList = currentUser.readed as MutableList<Pending>
        readingList = currentUser.actualReading as MutableList<ActualReading>

        //val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        //layoutManager.snapToInterval = itemWidth // ajusta el valor a la anchura de tus elementos
        //recyclerView.layoutManager = layoutManager


        binding.homeRv.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )


        adapterReading = HomeReadingBooksAdapter(readingList as ArrayList<ActualReading>, this)
        binding.homeRv.adapter = adapterReading
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.homeRv)

        CallAdapterPending(pendingList as ArrayList<Pending>)
        CallAdapterReaded(readedList as ArrayList<Pending>)
        binding.refresh.setOnRefreshListener {
            //getUser()
            reloadFragment()
            binding.refresh.isRefreshing = false
        }
        binding.icPendingSearch.setOnClickListener {
            FilterBooks(pendingList as ArrayList<Pending>, true)
        }
        binding.icReadedSearch.setOnClickListener {
            FilterBooks(readedList as ArrayList<Pending>, false)
        }

        binding.rvReadedbooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        var position = totalItemCount
                        LoadMoreRead(position)
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

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        var position = totalItemCount
                        LoadMorePending(position)
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

                if (lastVisibleItem == totalItemCount - 1 && dy >= 0) {
                    recyclerView.post {
                        var position = totalItemCount
                        LoadMoreReading(position)
                    }
                }
            }
        })
        return binding.root
    }
    fun LoadMoreRead(position : Int) {
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                readedList!!.addAll(
                    crudApi.getReadBooksFromUser(
                        currentUser.userId,
                        position
                    ) as MutableList<Pending>
                )
            }
            corrutina.join()
        }
        adapterReaded.updateList(readedList as ArrayList<Pending>)
    }
    fun LoadMorePending(position : Int) {
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                pendingList!!.addAll(
                    crudApi.getPendingBooksFromUser(
                        currentUser.userId,
                        position
                    ) as MutableList<Pending>
                )
            }
            corrutina.join()
        }
        adapterPending.updateList(pendingList as ArrayList<Pending>)
    }
    fun LoadMoreReading(position : Int) {
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                readingList!!.addAll(
                    crudApi.getReadingBooksFromUser(
                        currentUser.userId,
                        position
                    ) as MutableList<ActualReading>
                )
            }
            corrutina.join()
        }
        adapterReading.updateList(readingList as ArrayList<ActualReading>)
    }
    fun FilterBooks(list: ArrayList<Pending>, choseList: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_filter_books, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.filter_et)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Filter") { dialogInterface, i ->
            var searchString = editText.text.toString()
            if (editText.text.isNotBlank()) {
                val filteredList = list.filter { pending ->
                    pending.title.unaccent().contains(searchString, ignoreCase = true)
                } as ArrayList<Pending>
                if (choseList) {
                    CallAdapterPending(filteredList)
                    CallAdapterReaded(readedList as ArrayList<Pending>)
                } else {
                    CallAdapterPending(pendingList as ArrayList<Pending>)
                    CallAdapterReaded(filteredList)
                }
            } else {
                CallAdapterPending(pendingList as ArrayList<Pending>)
                CallAdapterReaded(readedList as ArrayList<Pending>)
            }
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i -> }
        builder.show()
    }
    fun CallAdapterPending(pendingList: ArrayList<Pending>) {
        binding.rvPendingbooks.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        adapterPending = HomeBooksAdapter(pendingList)
        binding.rvPendingbooks.adapter = adapterPending
    }
    fun CallAdapterReaded(readedList: ArrayList<Pending>) {
        binding.rvReadedbooks.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        adapterReaded = HomeBooksAdapter(readedList)
        binding.rvReadedbooks.adapter = adapterReaded
    }
    fun getUser() {
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                currentUser = crudApi.getUserId(currentUser.userId)
            }
            corrutina.join()
        }
    }
    fun reloadFragment() {
        getUser()
        Toast.makeText(context, "Reloading fragment", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            parentFragmentManager.beginTransaction().detach(this).commitNow();
            parentFragmentManager.beginTransaction().attach(this).commitNow();
        } else {
            parentFragmentManager.beginTransaction().detach(this).attach(this).commit();
        }
    }
}
