package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel

class GameHolderViewModel : ViewModel(){
    var quickplayFilter = ""
    var gamesPlayed = 1
}