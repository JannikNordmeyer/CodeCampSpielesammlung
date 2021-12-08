package com.example.testapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import com.example.testapplication.databinding.TictactoeBinding

class Field(var board: Array<Array<String>>, var game: Game) {



    private fun checkField(): Boolean {

        var win: Boolean = false

        for(i in 0..2){

            var row = true
            for(j in 0..2){

                row = row && (board[i][j] == game.player)
            }
            win = win || row

        }

        for(i in 0..2){

            var column = true
            for(j in 0..2){

                column = column && (board[j][i] == game.player)
            }
            win = win || column

        }
        var diaR = true

        for(i in 0..2){

            diaR = diaR && (board[i][i] == game.player)

        }
        win = win || diaR


        var diaL = true

        for(i in 0..2){

            diaL = diaL && (board[i][2-i] == game.player)

        }
        win = win || diaL

        if(win){

            game.win()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                clear()
                game.change.value = true
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

                game.draw()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    clear()
                    game.change.value = true
                                    }, 1500)
                return true
            }



        return false

    }
    fun click(a: Int, b: Int){
        if(board[a][b] != ""){
            return
        }
        board[a][b] = (game.player)
        if(checkField()){
            return
        }
        game.toggle()

    }
    private fun clear() {
        for(i in 0..2){
            for(j in 0..2){

                board[i][j]= ""
            }

        }
        game.toggle()
        if(game.player != "X")
            game.toggle()
        game.winner = null

    }


}









