package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.fragment.app.Fragment
import com.example.bookbuddy.R
import com.example.bookbuddy.databinding.FragmentAdminBinding
import com.example.bookbuddy.ui.navdrawer.adminnav.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.CoroutineContext
/**
 * Fragment to display Admin navMenu.
 */
@Parcelize
class AdminFragment() : Fragment(), CoroutineScope, Parcelable, InsertLibraryDialog.OnAdminDialogClose,
    InsertBookDialog.OnAdminDialogClose {
    lateinit var binding: FragmentAdminBinding
    private var job: Job = Job()
    private var fragmentSaved = "books"
    var gMenu: Menu? = null
    private var isFragmentReplaced = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        gMenu = menu

        if (!isFragmentReplaced) {
            menu.clear()
            inflater.inflate(R.menu.search_menu, menu)
            gMenu = menu
            replaceFragment(BooksFragment())
            isFragmentReplaced = true
        }
    }

    lateinit var searchItem: MenuItem
    override fun onResume() {
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAdminBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.books -> {
                    fragmentSaved = "books"
                    replaceFragment(BooksFragment())
                    true
                }
                R.id.genre -> {
                    fragmentSaved = "genre"
                    replaceFragment(GenresFragment())
                    true
                }
                R.id.Authors -> {
                    fragmentSaved = "authors"
                    replaceFragment(AuthorsFragment())
                    true
                }
                R.id.Libraries -> {
                    fragmentSaved = "libraries"
                    replaceFragment(LibrariesFragment())
                    true
                }
                R.id.users -> {
                    gMenu!!.findItem(R.id.action_search).isVisible =false
                    fragmentSaved = "requests"
                    replaceFragment(RequestsFragment())
                    true
                }
                else -> {
                    true
                }
            }
        }
        return binding.root
    }

    private fun replaceFragment(fragment: Fragment){
        val bundle = Bundle()
        bundle.putParcelable("fragment", this)
        fragment.arguments = bundle
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }
    /**
     * Reload fragments on close
     */
    override fun onAdminDialogClose() {
        var fragment: Fragment? = null
        when(fragmentSaved){
            "books" -> fragment = BooksFragment()
            "genre" -> fragment = GenresFragment()
            "authors" -> fragment = AuthorsFragment()
            "libraries" -> fragment = LibrariesFragment()
            "requests" -> fragment = RequestsFragment()
        }

        val bundle = Bundle()
        bundle.putParcelable("fragment", this)
        fragment!!.arguments = bundle
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
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