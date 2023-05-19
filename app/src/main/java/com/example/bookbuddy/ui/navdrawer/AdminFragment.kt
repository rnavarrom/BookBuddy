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
import com.example.bookbuddy.ui.navdrawer.adminnav.AdminAuthorsFragment
import com.example.bookbuddy.ui.navdrawer.adminnav.AdminGenresFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class AdminFragment : Fragment(), CoroutineScope {
    lateinit var binding: FragmentAdminBinding
    private var job: Job = Job()



    fun replaceFragment(fragment: Fragment){
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

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.books -> {
                    //loadFragment(HomeFragment())
                    true
                }
                R.id.genre -> {
                    replaceFragment(AdminGenresFragment())
                    true
                }
                R.id.Authors -> {
                    replaceFragment(AdminAuthorsFragment())
                    //loadFragment(SettingFragment())
                    true
                }
                R.id.Libraries -> {
                    //loadFragment(SettingFragment())
                    true
                }
                R.id.users -> {
                    //loadFragment(SettingFragment())
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