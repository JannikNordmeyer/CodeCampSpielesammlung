package com.example.testapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentPlaceholderspiel3Binding

class PlaceholderSpiel3 : Fragment() {

    private lateinit var binding: FragmentPlaceholderspiel3Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = FragmentPlaceholderspiel3Binding.inflate(inflater,container,false)
        val view = binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(PlaceholderSpiel3ViewModel::class.java) //Shared Viewmodel w/ GameHolder

        // DEIN CODE HIER

        return view
    }

}