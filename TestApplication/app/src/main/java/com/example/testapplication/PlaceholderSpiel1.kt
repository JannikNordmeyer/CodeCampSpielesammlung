package com.example.testapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentPlaceholderspiel1Binding

class PlaceholderSpiel1 : Fragment() {

    private lateinit var binding: FragmentPlaceholderspiel1Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = FragmentPlaceholderspiel1Binding.inflate(inflater,container,false)
        val view = binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(PlaceholderSpiel1ViewModel::class.java) //Shared Viewmodel w/ GameHolder

        // DEIN CODE HIER

        return view
    }



}