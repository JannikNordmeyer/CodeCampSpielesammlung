package com.example.testapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentPlaceholderspiel4Binding

class PlaceholderSpiel4 : Fragment() {

    private lateinit var binding: FragmentPlaceholderspiel4Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = FragmentPlaceholderspiel4Binding.inflate(inflater,container,false)
        val view = binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(PlaceholderSpiel4ViewModel::class.java) //Shared Viewmodel w/ GameHolder

        // DEIN CODE HIER

        return view
    }

}