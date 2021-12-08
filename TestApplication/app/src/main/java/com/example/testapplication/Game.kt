package com.example.testapplication
import android.content.Intent
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import com.example.testapplication.databinding.TictactoeBinding


class Game (){

    var change: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    var player = "X"
    var winner:Int? = null


    fun toggle(){

        if(player == "X"){
            player = "O"
        }
        else{
            player = "X"
        }
    }

    fun win() {
        if(player == "X"){
            winner = 1
        }
        else{
            winner = 2
        }
    }

    fun draw() {
        winner = 0
    }


}