package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.bookbuddy.R
import com.example.bookbuddy.databinding.FragmentAdminBinding
import com.example.bookbuddy.ui.navdrawer.adminnav.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class AdminFragment() : Fragment(), CoroutineScope, Parcelable, InsertLibraryDialog.OnAdminDialogClose,
    InsertBookDialog.OnAdminDialogClose {
    lateinit var binding: FragmentAdminBinding
    private var job: Job = Job()

    private var fragmentSaved = "books"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAdminBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        replaceFragment(BooksFragment())

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
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

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

    constructor(parcel: Parcel) : this() {
        fragmentSaved = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fragmentSaved)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AdminFragment> {
        override fun createFromParcel(parcel: Parcel): AdminFragment {
            return AdminFragment(parcel)
        }

        override fun newArray(size: Int): Array<AdminFragment?> {
            return arrayOfNulls(size)
        }
    }
}