package com.example.testapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentTictactoeBinding

class TicTacToe : Fragment() {

    private lateinit var binding: FragmentTictactoeBinding

    //Gibt Grafik für entsprechendes Symbol zurück
    private fun getIcon(str : String) : Int{
        if(str == "X"){ return R.drawable.x }
        if(str == "O"){ return return R.drawable.o }
        return android.R.color.transparent
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentTictactoeBinding.inflate(inflater,container,false)
        val view = binding.root

        val viewmodel = ViewModelProvider(requireActivity()).get(TicTacToeViewModel::class.java) //Shared Viewmodel w/ GameHolder
        fun updatePrompt(){
            if(viewmodel.logic.winner == null){
                if(!MyApplication.onlineMode) {
                    if (viewmodel.logic.player == "X") {
                        binding.playerprompt.text = "Player 1's Turn:"
                    } else {
                        binding.playerprompt.text = "Player 2's Turn:"
                    }
                }
                else{
                    if(MyApplication.myTurn) binding.playerprompt.text = "It's your turn!"
                    else binding.playerprompt.text = "Waiting for other player..."
                }
            }
            else{
                if(!MyApplication.onlineMode) {
                    if (viewmodel.logic.winner == 0) {
                        binding.playerprompt.text = "It's a Draw."
                    } else {
                        binding.playerprompt.text = "Player " + viewmodel.logic.winner + " wins."
                    }
                }
            }
        }

        viewmodel.logic.livewinner.observe(viewLifecycleOwner){
            updatePrompt()
        }

        viewmodel.logic.liveboard.observe(viewLifecycleOwner, {

            binding.topleft.setImageResource(getIcon(viewmodel.logic.board[0][0]))
            binding.topmid.setImageResource(getIcon(viewmodel.logic.board[0][1]))
            binding.topright.setImageResource(getIcon(viewmodel.logic.board[0][2]))

            binding.midleft.setImageResource(getIcon(viewmodel.logic.board[1][0]))
            binding.midmid.setImageResource(getIcon(viewmodel.logic.board[1][1]))
            binding.midright.setImageResource(getIcon(viewmodel.logic.board[1][2]))

            binding.botleft.setImageResource(getIcon(viewmodel.logic.board[2][0]))
            binding.botmid.setImageResource(getIcon(viewmodel.logic.board[2][1]))
            binding.botright.setImageResource(getIcon(viewmodel.logic.board[2][2]))
            updatePrompt()
        })

        binding.topleft.setOnClickListener(){ viewmodel.click(0, 0) }
        binding.topmid.setOnClickListener(){ viewmodel.click(0, 1) }
        binding.topright.setOnClickListener(){ viewmodel.click(0, 2) }
        binding.midleft.setOnClickListener(){ viewmodel.click(1, 0) }
        binding.midmid.setOnClickListener(){ viewmodel.click(1, 1) }
        binding.midright.setOnClickListener(){ viewmodel.click(1, 2) }
        binding.botleft.setOnClickListener(){ viewmodel.click(2, 0) }
        binding.botmid.setOnClickListener(){ viewmodel.click(2, 1) }
        binding.botright.setOnClickListener(){ viewmodel.click(2, 2) }

        return view
    }

    override fun onStart() {
        super.onStart()
        MyApplication.ticTacToeOpen = true
    }

    override fun onStop() {
        super.onStop()
        MyApplication.ticTacToeOpen = false
    }

}