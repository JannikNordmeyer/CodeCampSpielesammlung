package com.example.testapplication

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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

    //Online Mode Stuff?
    var exit = false;
    var player1count = 0
    var player2count = 0
    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var emptyCells = ArrayList<Int>()
    var activeUser = 1


}