package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityTictactoeBinding


class TicTacToe : Fragment() {

    private lateinit var binding: ActivityTictactoeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //super.onCreate(savedInstanceState)

        binding = ActivityTictactoeBinding.inflate(inflater,container,false)
        var view = binding.root
        //setContentView(binding.root)

        val viewmodel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)

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

        fun update(){
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
        }

        //viewmodel.game.change.observe(this, {
        //
        //    update()
        //})

        update()

        binding.returnbutton.setOnClickListener(){
            (this.getActivity())!!.finish()
        }

        binding.topleft.setOnClickListener(){

            viewmodel.click(0, 0)
            update()
        }
        binding.topmid.setOnClickListener(){

            viewmodel.click(0, 1)
            update()
        }
        binding.topright.setOnClickListener(){

            viewmodel.click(0, 2)
            update()
        }


        binding.midleft.setOnClickListener(){

            viewmodel.click(1, 0)
            update()
        }
        binding.midmid.setOnClickListener(){

            viewmodel.click(1, 1)
            update()
        }
        binding.midright.setOnClickListener(){

            viewmodel.click(1, 2)
            update()
        }


        binding.botleft.setOnClickListener(){

            viewmodel.click(2, 0)
            update()
        }
        binding.botmid.setOnClickListener(){

            viewmodel.click(2, 1)
            update()
        }
        binding.botright.setOnClickListener(){

            viewmodel.click(2, 2)
            update()
        }

        return inflater.inflate(R.layout.activity_tictactoe,container,false)

    }

}