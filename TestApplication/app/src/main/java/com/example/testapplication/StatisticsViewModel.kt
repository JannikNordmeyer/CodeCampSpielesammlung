package com.example.testapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint

class StatisticsViewModel: ViewModel() {

    var TicTacToeGamesPlayed = 0
    var TicTacToeWinPercentage = 0
    var TicTacToeWinCount = 0
    var TicTacToeData : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()

    var CompassGamesPlayed = 0
    var CompassWinPercentage = 0
    var CompassWinCount = 0
    var CompassData : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()

}