package com.example.testapplication

import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.coroutines.coroutineContext

class ArithmeticsViewModel: ViewModel() {

    var logic = ArithmeticsGameLogic(this)

    var score = 0
    var opponent_score = 0

    fun enter(result: String?) {

        logic.enter(result)
    }

    fun resetGame(){
        score = 0
        opponent_score = 0
        logic.start()
        Log.d("VIEWMODEL RESET CALLED","VIEWMODEL RESET CALLED")
    }

    lateinit var gameTimer: CountDownTimer

}