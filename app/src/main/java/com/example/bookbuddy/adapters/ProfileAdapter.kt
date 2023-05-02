package com.example.bookbuddy.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.bookbuddy.ui.navdrawer.ProfileFragment

@Suppress("DEPRECATION")
internal class ProfileAdapter(
    var context: Context?,
    fm: FragmentManager,
    var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ProfileFragment()
            }
            1 -> {
                ProfileFragment()
            }
            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }
}