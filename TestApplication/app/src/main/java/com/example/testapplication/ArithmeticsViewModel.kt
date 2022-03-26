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

    fun resetGame(){
        score = 0
        opponentScore = 0
        logic.start()
        Log.d("VIEWMODEL RESET CALLED","VIEWMODEL RESET CALLED")
    }

    lateinit var gameTimer: CountDownTimer

}