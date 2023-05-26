package com.example.bookbuddy.adapters

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.bookbuddy.ui.navdrawer.profile.ProfileBookMarksFragment
import com.example.bookbuddy.ui.navdrawer.profile.ProfileCommentsFragment

@Suppress("DEPRECATION")
internal class ProfileAdapter(
    var context: Context?,
    fm: FragmentManager,
    private var totalTabs: Int,
    var userId: Int,
    private val isProfileFragment: Boolean
) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        return when (position) {
            0 -> {
                bundle.putInt("userid", userId)
                bundle.putBoolean("isfragment", isProfileFragment)
                val fragment = ProfileCommentsFragment()
                fragment.arguments = bundle
                return fragment
            }
            1 -> {
                bundle.putInt("userid", userId)
                bundle.putBoolean("isfragment", isProfileFragment)
                val fragment = ProfileBookMarksFragment()
                fragment.arguments = bundle
                return fragment
            }
            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }
}