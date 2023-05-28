package com.example.bookbuddy.ui.navdrawer.adminnav

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
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
import com.example.bookbuddy.utils.ApiErrorListener
import com.example.bookbuddy.utils.Tools.Companion.showSnackBar
import com.example.bookbuddy.utils.navController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class RequestsFragment : Fragment(), CoroutineScope, ApiErrorListener {
    lateinit var binding: FragmentAdminRequestsBinding
    private var job: Job = Job()
    lateinit var adapter: AdminRequestsAdapter

    private var position = 0
    private var lastPosition = -1
    private var bookRequests: MutableList<BookRequest>? = null

    private var isbn: String? = null
    private val api = CrudApi(this@RequestsFragment)
    private var isOnCreateViewExecuted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu!!.findItem(R.id.action_search).isVisible =false
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
        isOnCreateViewExecuted = true
        return binding.root
    }
    /*
    private fun insertBookRequest(){
        var result = false
        if (!isbn.isNullOrEmpty()){
            // TODO: This?
            if (result) {
                showSnackBar(requireContext(), requireView(), getString(R.string.BookRequestInserted))
                //adapter.updateList(BookRequests as ArrayList<BookRequest>)
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.BookRequestExists))
            }
        } else {
            showSnackBar(requireContext(), requireView(), getString(R.string.SB_NameEmpty))
        }
    }
    */
    private fun loadingEnded(){
        binding.loadingView.visibility = View.GONE
        binding.mainParent.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            val selection = adapter.getSelected()
            var result = false
            if (selection != null){
                val fra = requireArguments().getParcelable("fragment") as? AdminFragment?
                val bundle = Bundle()
                bundle.putInt("id", selection.bookRequest1)
                bundle.putString("isbn", selection.bookIsbn)
                bundle.putParcelable("fragment", fra)
                //val action = AdminLibrariesFragmentDirections.actionNavBookToNavInsertBook(bundle)
                val action = AdminFragmentDirections.actionNavAdminToNavInsertBook(bundle)
                navController.navigate(action)
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.PickBookFirst))
            }
        }

        binding.btnDelete.setOnClickListener {
            val selection = adapter.getSelected()
            var result = false
            if (selection != null){
                val builder = AlertDialog.Builder(requireContext())

                builder.setTitle(getString(R.string.DeleteRequest))
                builder.setMessage(getString(R.string.DeleteRequestQuestion))
                builder.setPositiveButton(getString(R.string.Yes)) { dialogInterface: DialogInterface, _: Int ->
                    // Acciones a realizar si el usuario selecciona "Sí"
                    runBlocking {
                        val coroutine = launch {
                            result = api.deleteRequest(selection.bookRequest1)!!
                        }
                        coroutine.join()
                    }

                    if (result) {
                        showSnackBar(requireContext(), requireView(), getString(R.string.SB_BookRequestDeleted))
                        bookRequests!!.remove(selection)
                        adapter.updateList(bookRequests as ArrayList<BookRequest>)
                    }

                    dialogInterface.dismiss()
                }
                builder.setNegativeButton(getString(R.string.BT_Cancel)) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                showSnackBar(requireContext(), requireView(), getString(R.string.PickBookFirst))
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

    override fun onApiError(connectionFailed: Boolean) {
        if (isOnCreateViewExecuted){
            showSnackBar(requireContext(), requireView(), Constants.ErrrorMessage)
        }
    }
    override fun onDestroy() {
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