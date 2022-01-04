package com.example.testapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider

class FragmentTest : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        var fragToLoad = R.layout.fragment_test
        val viewmodel = ViewModelProvider(this).get(GameHolderViewModel::class.java)

        when(viewmodel.selectedGame) {
            GameNames.TICTACTOE -> fragToLoad = R.layout.fragment_test
            else -> {
                print("ERROR: DONT KNOW WHAT GAME TO LOAD")
            }
        }

        return inflater.inflate(fragToLoad,container,false)
    }



}