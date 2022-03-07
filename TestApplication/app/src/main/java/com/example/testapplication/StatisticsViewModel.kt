package com.example.testapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint

class StatisticsViewModel: ViewModel() {

    var TicTacToeGamesPlayed = 0
    var TicTacToeWinPercentage = 0.0
    var TicTacToeWinCount = 0
    var TicTacToeData : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()

    var CompassGamesPlayed = 0
    var CompassWinPercentage = 0.0
    var CompassWinCount = 0
    var CompassData : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()

    var ArithmeticsGamesPlayed = 0
    var ArithmeticsWinPercentage = 0.0
    var ArithmeticsWinCount = 0
    var ArithmeticsData : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()
    var ArithmeticsHighScore : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()

    var ChallengeGamesPlayed = 0
    var ChallengeWinPercentage = 0.0
    var ChallengeWinCount = 0
    var ChallengeData : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()
    var ChallengeHighScore : MutableLiveData<Array<DataPoint>> = MutableLiveData<Array<DataPoint>>()
}