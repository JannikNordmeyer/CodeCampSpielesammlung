package com.example.testapplication

import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlin.coroutines.coroutineContext

class ArithmeticsViewModel: ViewModel() {

    var logic = ArithmeticsGameLogic()

    var score = 0

    fun enter(result: Int?) {

        logic.enter(result)
    }

}