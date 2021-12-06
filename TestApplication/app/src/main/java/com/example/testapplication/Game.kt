package com.example.testapplication
import android.content.Intent
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.example.testapplication.databinding.TictactoeBinding


class Game (val prompt: TextView, val binding: TictactoeBinding){

    var player = "X"
    var field = mutableListOf<Field>()

    fun rungame(){


        binding.topleft.setOnClickListener(){

            field[0].click(0, 0)
            }
        binding.topmid.setOnClickListener(){

            field[0].click(0, 1)
        }
        binding.topright.setOnClickListener(){

            field[0].click(0, 2)
        }


        binding.midleft.setOnClickListener(){

            field[0].click(1, 0)
        }
        binding.midmid.setOnClickListener(){

            field[0].click(1, 1)
        }
        binding.midright.setOnClickListener(){

            field[0].click(1, 2)
        }


        binding.botleft.setOnClickListener(){

            field[0].click(2, 0)
        }
        binding.botmid.setOnClickListener(){

            field[0].click(2, 1)
        }
        binding.botright.setOnClickListener(){

            field[0].click(2, 2)
        }







    }

    fun toggle(){

        if(player == "X"){
            player = "O"
            prompt.setText("Player 2: Make a move.")
        }
        else{
            player = "X"
            prompt.setText("Player 1: make a move.")
        }
    }

    fun win() {
        if(player == "X"){
            prompt.setText("Player 1 Wins!")
        }
        else{
            prompt.setText("Player 2 Wins!")
        }
    }

    fun draw() {
        prompt.setText("It's a draw.")
    }


}