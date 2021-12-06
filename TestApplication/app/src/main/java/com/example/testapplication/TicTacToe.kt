package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.testapplication.databinding.TictactoeBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: TictactoeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.tictactoe.xml)

        binding = TictactoeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnbutton.setOnClickListener(){

            finish()

        }


        val board = arrayOf(
            arrayOf(binding.topleft, binding.topmid, binding.topright),
            arrayOf(binding.midleft, binding.midmid, binding.midright),
            arrayOf(binding.botleft, binding.botmid, binding.botright),
        )

        var game = Game(binding.playerprompt, binding)

        var field = Field(board, game)

        game.field.add(field)

        game.rungame()

    }

}