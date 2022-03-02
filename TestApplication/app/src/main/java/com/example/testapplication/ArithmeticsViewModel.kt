package com.example.testapplication

import android.os.CountDownTimer
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

    fun reset(){
        score = 0
        opponent_score = 0
    }

    lateinit var gameTimer: CountDownTimer
}