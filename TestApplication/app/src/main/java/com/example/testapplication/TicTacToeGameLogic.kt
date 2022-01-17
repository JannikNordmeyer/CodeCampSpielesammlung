package com.example.testapplication
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TicTacToeGameLogic (var board: Array<Array<String>>){

    companion object{
        const val CROSS = "X"
        const val CIRCLE = "O"
        const val WINNER_PLAYER_ONE = 1
        const val WINNER_PLAYER_TWO = 2
        const val WINNER_DRAW = 0
    }

    var liveboard: MutableLiveData<Array<Array<String>>> = MutableLiveData<Array<Array<String>>>()
    var livewinner: MutableLiveData<Int?> = MutableLiveData<Int?>()

    var player = CROSS
    var winner:Int? = null

    fun toggle(){
        if(player == CROSS) player = CIRCLE
        else player = CROSS
    }

    fun networkOnFieldUpdate(data : String?){
        //TODO: Update Field with data received...
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

        var full = true
        for(i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") {
                    full = false
                }
            }
        }

        if(win || full){
            if(win){
                if(player == CROSS) winner = WINNER_PLAYER_ONE
                else winner = WINNER_PLAYER_TWO
            }
            else{
                winner = WINNER_DRAW
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                clear()
            }, 1500)
            livewinner.value = winner
            return true
        }
        return false
    }

    fun click(a: Int, b: Int){
        if(board[a][b] != "" || winner != null){
            return
        }
        board[a][b] = (player)
        liveboard.value = board     //Update Liveboard, which triggers observer
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
        if(player != CROSS){
            toggle()
        }
        winner = null
        livewinner.value = winner
        liveboard.value = board     //Update Liveboard, which triggers observer
    }

}