package com.example.bookbuddy.ui.navdrawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookbuddy.R
import com.example.bookbuddy.databinding.FragmentScanBinding
import com.example.bookbuddy.databinding.FragmentSearchBinding

class ScanFragment : Fragment() {
    lateinit var binding: FragmentScanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentScanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}