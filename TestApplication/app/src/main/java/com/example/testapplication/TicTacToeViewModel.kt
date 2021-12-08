package com.example.testapplication


import androidx.lifecycle.ViewModel

class TicTacToeViewModel: ViewModel() {


    val emptyboard = arrayOf(
        arrayOf("", "", ""),
        arrayOf("", "", ""),
        arrayOf("", "", ""),
    )

    var game = Game()

    var field = Field(emptyboard, game)

    fun click(x:Int, y:Int){
        field.click(x, y)
    }

}