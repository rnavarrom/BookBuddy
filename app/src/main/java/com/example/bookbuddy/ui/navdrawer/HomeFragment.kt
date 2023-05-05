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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.HomeBooksAdapter
import com.example.bookbuddy.adapters.HomeReadingBooksAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentHomeBinding
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
    private lateinit var pendingList: ArrayList<Pending>
    private lateinit var readedList: ArrayList<Pending>
    private lateinit var readingList: ArrayList<ActualReading>
    private var choseList : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);



/*
        var pending = Pending(123456, emptyList(), emptyList(), "https://i.gr-assets.com/images/S/compressed.photo.goodreads.com/books/1522157426l/19063._SY475_.jpg",
        "", "", 500, "",3.5, emptyList(),"Libro de Prueba" )
        pendingList = ArrayList<Pending>()
        pendingList.add(pending)
 */

        pendingList = currentUser.pending as ArrayList<Pending>
        readedList = currentUser.readed as ArrayList<Pending>
        readingList = currentUser.actualReading as ArrayList<ActualReading>

        binding.homeRv.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false))
        adapterReading = HomeReadingBooksAdapter(readingList, this)
        binding.homeRv.adapter = adapterReading

        CallAdapterPending(pendingList)
        CallAdapterReaded(readedList)
        binding.refresh.setOnRefreshListener {
            //getUser()
            reloadFragment()
            binding.refresh.isRefreshing = false
        }
        binding.icPendingSearch.setOnClickListener {
            FilterBooks(pendingList, true)
        }
        binding.icReadedSearch.setOnClickListener {
            FilterBooks(readedList, false)
        }
        return binding.root
    }
    fun FilterBooks(list: ArrayList<Pending>, choseList: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_filter_books, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.filter_et)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Filter") { dialogInterface, i ->
            var searchString = editText.text.toString()
            if(editText.text.isNotBlank()){
                val filteredList = list.filter {
                        pending -> pending.title.unaccent().contains(searchString, ignoreCase = true)
                } as ArrayList<Pending>
                if(choseList){
                    CallAdapterPending(filteredList)
                    CallAdapterReaded(readedList)
                }else{
                    CallAdapterPending(pendingList)
                    CallAdapterReaded(filteredList)
                }
            }else{
                CallAdapterPending(pendingList)
                CallAdapterReaded(readedList)
            }
        }
        builder.setNegativeButton("Cancel"){dialogInterface, i ->}
        builder.show()
    }
    fun CallAdapterPending(pendingList: ArrayList<Pending>){
        binding.rvPendingbooks.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false))
        adapterPending = HomeBooksAdapter(pendingList)
        binding.rvPendingbooks.adapter = adapterPending
    }
    fun CallAdapterReaded(readedList: ArrayList<Pending>){
        binding.rvReadedbooks.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false))
        adapterReaded = HomeBooksAdapter(readedList)
        binding.rvReadedbooks.adapter = adapterReaded
    }

    fun getUser(){
        runBlocking {
            val crudApi = CrudApi()
            val corrutina = launch {
                currentUser = crudApi.getUserId(currentUser.userId)
            }
            corrutina.join()
        }
    }


    fun reloadFragment(){
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
