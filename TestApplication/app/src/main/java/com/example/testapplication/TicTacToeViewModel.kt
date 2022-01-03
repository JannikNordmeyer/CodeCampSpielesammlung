package com.example.testapplication


import androidx.lifecycle.ViewModel

class TicTacToeViewModel: ViewModel() {


    val emptyboard = arrayOf(
        arrayOf("", "", ""),
        arrayOf("", "", ""),
        arrayOf("", "", ""),
    )

    var game = Game(emptyboard)

    fun click(x:Int, y:Int){
        game.click(x, y)
    }

}