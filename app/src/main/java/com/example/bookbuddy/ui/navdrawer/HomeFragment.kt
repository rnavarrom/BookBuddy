package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookbuddy.R
import com.example.bookbuddy.adapters.HomeBooksAdapter
import com.example.bookbuddy.adapters.HomeReadingBooksAdapter
import com.example.bookbuddy.databinding.FragmentHomeBinding
import com.example.bookbuddy.models.Test.Pending

import com.example.bookbuddy.utils.currentUser

//import com.example.bookbuddy.utils.currentUser

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private lateinit var adapterPending: HomeBooksAdapter
    private lateinit var adapterReaded: HomeBooksAdapter
    private lateinit var adapterReading: HomeReadingBooksAdapter
    private lateinit var pendingList: ArrayList<Pending>
    private lateinit var readedList: ArrayList<Pending>
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

        binding.homeRv.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false))
        adapterReading = HomeReadingBooksAdapter(pendingList) //, requireContext(), layoutInflater
        binding.homeRv.adapter = adapterReading






        //binding.pagesCurrent.text = dialogValue
        //MakePercent()

        //Glide.with(requireContext()).load(currentUser.actualReading.cover).into(binding.actualBookImage)



        CallAdapterPending(pendingList)
        CallAdapterReaded(readedList)

        //binding.homell.setOnClickListener {
        //    ChangeReaded()
        //}
        binding.icPendingSearch.setOnClickListener {
            FilterBooks(pendingList)
        }
        binding.icReadedSearch.setOnClickListener {
            FilterBooks(pendingList)
        }
        return binding.root
    }
    /*
    fun ChangeReaded() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        //builder.setTitle("Book progress")
        val dialogLayout = inflater.inflate(R.layout.dialog_readed_pages, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.dialog_readed_pages)
        editText.setText(dialogValue)
        builder.setView(dialogLayout)
        builder.setNegativeButton("Cancel"){dialogInterface, i ->}
        builder.setPositiveButton("Save") { dialogInterface, i ->
            binding.pagesCurrent.text = editText.text
            dialogValue = editText.text.toString()
            MakePercent()
        }
        builder.show()
    }

     */
    fun FilterBooks(list: ArrayList<Pending>) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        //builder.setTitle("Book progress")
        val dialogLayout = inflater.inflate(R.layout.dialog_filter_books, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.filter_et)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Filter") { dialogInterface, i ->
            if(editText.text.isNotBlank()){
                val filteredList = list.filter {
                        pending -> pending.title.contains("searchString", ignoreCase = true)
                } as ArrayList<Pending>
                CallAdapterPending(filteredList)
            }else{
                CallAdapterPending(currentUser.pending as ArrayList<Pending>)
            }

        }
        builder.setNegativeButton("Cancel"){dialogInterface, i ->}
        builder.show()
    }
/*
    fun MakePercent(){
        var current = Integer.parseInt(binding.pagesCurrent.text.toString())
        var total = Integer.parseInt(binding.pagesTotal.text.toString())

        var percent : Int= ((current*100)/total)
        binding.progressText.text = percent.toString()
        binding.progressBar.progress = percent
    }


 */
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


}
