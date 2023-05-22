package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.bookbuddy.R
import com.example.bookbuddy.databinding.FragmentAdminBinding
import com.example.bookbuddy.databinding.FragmentBookCommentsBinding
import com.example.bookbuddy.ui.navdrawer.adminnav.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class AdminFragment : Fragment(), CoroutineScope, java.io.Serializable, InsertLibraryDialog.OnAdminDialogClose,
    InsertBookDialog.OnAdminDialogClose {
    lateinit var binding: FragmentAdminBinding
    private var job: Job = Job()

    var fragmentSaved = "books"

    fun replaceFragment(fragment: Fragment){
        val bundle = Bundle()
        bundle.putSerializable("fragment", this)
        fragment.arguments = bundle
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

    override fun onAdminDialogClose() {
        var fragment: Fragment? = null
        when(fragmentSaved){
            "books" -> fragment = AdminBooksFragment()
            "genre" -> fragment = AdminGenresFragment()
            "authors" -> fragment = AdminAuthorsFragment()
            "libraries" -> fragment = AdminLibrariesFragment()
            "requests" -> fragment = AdminRequestsFragment()        }

        val bundle = Bundle()
        bundle.putSerializable("fragment", this)
        fragment!!.arguments = bundle
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentAdminBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        replaceFragment(AdminBooksFragment())

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.books -> {
                    fragmentSaved = "books"
                    replaceFragment(AdminBooksFragment())
                    true
                }
                R.id.genre -> {
                    fragmentSaved = "genre"
                    replaceFragment(AdminGenresFragment())
                    true
                }
                R.id.Authors -> {
                    fragmentSaved = "authors"
                    replaceFragment(AdminAuthorsFragment())
                    true
                }
                R.id.Libraries -> {
                    fragmentSaved = "libraries"
                    replaceFragment(AdminLibrariesFragment())
                    true
                }
                R.id.users -> {
                    fragmentSaved = "requests"
                    replaceFragment(AdminRequestsFragment())
                    true
                }
                else -> {
                    true
                }
            }
        }

        return binding.root
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