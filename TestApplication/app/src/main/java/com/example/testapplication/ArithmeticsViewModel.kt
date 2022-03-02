package com.example.testapplication

import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlin.coroutines.coroutineContext

class ArithmeticsViewModel: ViewModel() {

    var logic = ArithmeticsGameLogic(this)

    var score = 0

    fun enter(result: String?) {

        logic.enter(result)
    }

    fun reset(){
        score = 0
    }
}