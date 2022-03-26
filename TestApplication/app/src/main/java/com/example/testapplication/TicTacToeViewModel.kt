package com.example.testapplication

import androidx.lifecycle.ViewModel

class TicTacToeViewModel: ViewModel() {

    val emptyboard = arrayOf(
        arrayOf("", "", ""),
        arrayOf("", "", ""),
        arrayOf("", "", ""),
    )

    var logic = TicTacToeGameLogic(emptyboard)

    fun click(x:Int, y:Int){
        logic.click(x, y)
    }
}