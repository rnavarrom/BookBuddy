package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.bookbuddy.R
import com.example.bookbuddy.databinding.FragmentHomeBinding
import com.example.bookbuddy.databinding.FragmentRecommendationsBinding
import com.example.bookbuddy.utils.navController

class RecommendationsFragment : Fragment() {
    lateinit var binding: FragmentRecommendationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentRecommendationsBinding.inflate(layoutInflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        val bundle = Bundle()
        bundle.putInt("book_id", 1)
        navController.navigate(R.id.nav_read_comment, bundle)

        return binding.root
    }
}