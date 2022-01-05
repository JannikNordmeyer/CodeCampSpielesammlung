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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentTictactoeBinding.inflate(inflater,container,false)
        val view = binding.root

        val viewmodel = ViewModelProvider(requireActivity()).get(TicTacToeViewModel::class.java) //Shared Viewmodel w/ GameHolder

        fun updatePrompt(){
            if(viewmodel.game.winner == null){
                if(viewmodel.game.player == "X"){
                    binding.playerprompt.setText("Player 1's Turn:")}
                else{
                    binding.playerprompt.setText("Player 2's Turn:")
                }
            }
            else{
                if(viewmodel.game.winner == 0){
                    binding.playerprompt.setText("It's a Draw.")
                }
                else{
                    binding.playerprompt.setText("Player "+viewmodel.game.winner+" wins.")
                }
            }
        }

        viewmodel.game.livewinner.observe(viewLifecycleOwner, ){
            updatePrompt()
        }

        viewmodel.game.liveboard.observe(viewLifecycleOwner, {
            binding.topleft.setText(viewmodel.game.board[0][0])
            binding.topmid.setText(viewmodel.game.board[0][1])
            binding.topright.setText(viewmodel.game.board[0][2])

            binding.midleft.setText(viewmodel.game.board[1][0])
            binding.midmid.setText(viewmodel.game.board[1][1])
            binding.midright.setText(viewmodel.game.board[1][2])

            binding.botleft.setText(viewmodel.game.board[2][0])
            binding.botmid.setText(viewmodel.game.board[2][1])
            binding.botright.setText(viewmodel.game.board[2][2])
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

}