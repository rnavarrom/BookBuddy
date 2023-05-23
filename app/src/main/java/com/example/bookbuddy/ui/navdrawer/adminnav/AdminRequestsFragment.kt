package com.example.bookbuddy.ui.navdrawer.adminnav

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbuddy.R
import com.example.bookbuddy.Utils.Constants
import com.example.bookbuddy.adapters.AdminRequestsAdapter
import com.example.bookbuddy.api.CrudApi
import com.example.bookbuddy.databinding.FragmentAdminRequestsBinding
import com.example.bookbuddy.models.BookRequest
import com.example.bookbuddy.ui.navdrawer.AdminFragment
import com.example.bookbuddy.ui.navdrawer.AdminFragmentDirections
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.base.ApiErrorListener
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AdminRequestsFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminRequestsBinding
    private var job: Job = Job()
    lateinit var adapter: AdminRequestsAdapter

    private var position = 0
    private var lastPosition = -1
    private var bookRequests: MutableList<BookRequest>? = null

    private var isbn: String? = null
    private val api = CrudApi(this@AdminRequestsFragment)
    private fun insertBookRequest(){
        var result = false
        if (!isbn.isNullOrEmpty()){
            // TODO: This?
            if (result) {
                showSnackBar(requireContext(), requireView(), "BookRequest Inserted")
                //adapter.updateList(BookRequests as ArrayList<BookRequest>)
            } else {
                showSnackBar(requireContext(), requireView(), "BookRequest already exist")
            }
        } else {
            showSnackBar(requireContext(), requireView(), "Name empty")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAdminRequestsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.mainContent.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary_green))

        getRequests(true)
        loadingEnded()

        return binding.root
    }

    fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            val selection = adapter.getSelected()
            var result = false
            if (selection != null){
                val fra = requireArguments().getSerializable("fragment") as? AdminFragment?
                val bundle = Bundle()
                bundle.putInt("id", selection.bookRequest1)
                bundle.putString("isbn", selection.bookIsbn)
                bundle.putSerializable("fragment", fra)
                //val action = AdminLibrariesFragmentDirections.actionNavBookToNavInsertBook(bundle)
                val action = AdminFragmentDirections.actionNavAdminToNavInsertBook(bundle)
                navController.navigate(action)
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a BookRequest first")
            }
        }

        binding.btnDelete.setOnClickListener {
            val selection = adapter.getSelected()
            var result = false
            if (selection != null){
                val builder = AlertDialog.Builder(requireContext())

                builder.setTitle("Delete request")
                builder.setMessage("Are you sure you want to delete this request?")
                builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
                    // Acciones a realizar si el usuario selecciona "Sí"
                    runBlocking {
                        val coroutine = launch {
                            result = api.deleteRequest(selection.bookRequest1)!!
                        }
                        coroutine.join()
                    }

                    if (result) {
                        showSnackBar(requireContext(), requireView(), "BookRequest deleted")
                        bookRequests!!.remove(selection)
                        adapter.updateList(bookRequests as ArrayList<BookRequest>)
                    }

                    dialogInterface.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                showSnackBar(requireContext(), requireView(), "Pick a BookRequest first")
            }
        }

        binding.mainContent.setOnRefreshListener {
            position = 0
            lastPosition = -1
            getRequests(false)
            binding.mainContent.isRefreshing = false
        }

        binding.rvRequests.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun loadMoreItems() {
        getRequests(false)
    }

    private fun getRequests(addAdapter: Boolean){
        runBlocking {
            val corrutina = launch {
                if (position == 0){
                    bookRequests = api.getRequests(position) as MutableList<BookRequest>?
                } else {
                    bookRequests!!.addAll((api.getRequests(position) as MutableList<BookRequest>?)!!)
                }
                if (addAdapter){
                    binding.rvRequests.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = AdminRequestsAdapter(bookRequests as ArrayList<BookRequest>)
                    binding.rvRequests.adapter = adapter
                } else {
                    adapter.updateList(bookRequests as ArrayList<BookRequest>)
                }
            }
            corrutina.join()
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

    override fun onApiError() {
        showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
    }
}