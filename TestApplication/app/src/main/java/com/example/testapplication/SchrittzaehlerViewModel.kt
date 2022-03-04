package com.example.testapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SchrittzaehlerViewModel: ViewModel() {

    var logic = SchrittzaehlerLogic(this)

    var score = 0
    var running = false
    var halfStep = false
    var goalscore = 0
    var isWaiting = false

    fun reset(){
        score = 0
        goalscore = 0
        running = false
        halfStep = false
        isWaiting = false
    }

    var livenetworkReset: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()

}