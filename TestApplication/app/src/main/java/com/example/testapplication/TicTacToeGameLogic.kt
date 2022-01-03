package com.example.testapplication
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import com.example.testapplication.databinding.ActivityTictactoeBinding

class TicTacToeGameLogic (var board: Array<Array<String>>){

    companion object{
        const val CROSS = "X"
        const val CIRCLE = "O"
    }

    var change: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    var player = CROSS
    var winner:Int? = null

    fun toggle(){

        if(player == CROSS){
            player = CIRCLE
        }
        else{
            player = CROSS
        }
    }

    fun win() {
        if(player == CROSS){
            winner = 1
        }
        else{
            winner = 2
        }
    }

    fun draw() {
        winner = 0
    }

    fun checkField(): Boolean {

        var win: Boolean = false

        for(i in 0..2){

            var row = true
            for(j in 0..2){

                row = row && (board[i][j] == player)
            }
            win = win || row

        }

        for(i in 0..2){

            var column = true
            for(j in 0..2){

                column = column && (board[j][i] == player)
            }
            win = win || column

        }
        var diaR = true

        for(i in 0..2){

            diaR = diaR && (board[i][i] == player)

        }
        win = win || diaR


        var diaL = true

        for(i in 0..2){

            diaL = diaL && (board[i][2-i] == player)

        }
        win = win || diaL

        if(win){

            win()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                clear()
                change.value = true
            }, 1500)
            return true
        }

        var full = true
        for(i in 0..2) {
            for (j in 0..2) {

                if (board[i][j] == "") {

                    full = false
                }
            }
        }


        if(full){

            draw()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                clear()
                change.value = true
            }, 1500)
            return true
        }

        return false

    }

    fun click(a: Int, b: Int){
        if(board[a][b] != ""){
            return
        }
        board[a][b] = (player)
        if(checkField()){
            return
        }
        toggle()

    }

    fun clear() {
        for(i in 0..2){
            for(j in 0..2){

                board[i][j]= ""
            }

        }
        toggle()
        if(player != CROSS)
            toggle()
        winner = null

    }



}