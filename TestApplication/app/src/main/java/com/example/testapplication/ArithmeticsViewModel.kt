package com.example.testapplication

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel

class ArithmeticsViewModel: ViewModel() {

    var logic = ArithmeticsGameLogic(this)

    var score = 0
    var opponentScore = 0

    fun enter(result: String?) {
        logic.enter(result)
    }

    //Reset für Spiel Variablen
    fun resetGame(){
        score = 0
        opponentScore = 0
        logic.start()
    }

    lateinit var gameTimer: CountDownTimer

}