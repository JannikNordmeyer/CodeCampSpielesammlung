package com.example.testapplication

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import com.example.testapplication.databinding.TictactoeBinding

class Field(var board: Array<Array<Button>>, var game: Game) {


    private fun checkField(): Boolean {

        var win: Boolean = false

        for(i in 0..2){

            var row = true
            for(j in 0..2){

                row = row && (board[i][j].text == game.player)
            }
            win = win || row

        }

        for(i in 0..2){

            var column = true
            for(j in 0..2){

                column = column && (board[j][i].text == game.player)
            }
            win = win || column

        }
        var diaR = true

        for(i in 0..2){

            diaR = diaR && (board[i][i].text == game.player)

        }
        win = win || diaR


        var diaL = true

        for(i in 0..2){

            diaL = diaL && (board[i][2-i].text == game.player)

        }
        win = win || diaL

        if(win){

            game.win()
            val handler = Handler()
            handler.postDelayed({ clear() }, 1500)
            return true
        }

            var full = true
            for(i in 0..2) {
                for (j in 0..2) {

                    if (board[i][j].text == "  ") {

                        full = false
                    }
                }
            }


            if(full){

                game.draw()
                val handler = Handler()
                handler.postDelayed({ clear() }, 1500)
                return true
            }



        return false

    }
    fun click(a: Int, b: Int){
        if(board[a][b].text != "  "){
            return
        }
        board[a][b].setText(game.player)
        if(checkField()){
            return
        }
        game.toggle()

    }

    private fun clear() {
        for(i in 0..2){
            for(j in 0..2){

                board[i][j].text = "  "
            }

        }
        game.toggle()
        if(game.player != "X")
        game.toggle()

    }


}









