package com.example.testapplication

import androidx.lifecycle.ViewModel

class ArithmeticsViewModel: ViewModel() {

    var logic = ArithmeticsGameLogic()

    var score = 0

    fun enter(result: Int?) {

        logic.enter(result)
    }

}