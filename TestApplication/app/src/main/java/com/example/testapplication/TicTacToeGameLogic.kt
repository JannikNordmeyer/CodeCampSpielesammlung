package com.example.testapplication
import android.app.PendingIntent.getActivity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TicTacToeGameLogic (var board: Array<Array<String>>){

    companion object{
        const val CROSS = "X"
        const val CIRCLE = "O"
        const val UNDEFINED = ""
        const val WINNER_PLAYER_ONE = 1
        const val WINNER_PLAYER_TWO = 2
        const val WINNER_DRAW = 0
    }

    private val TAG = TicTacToeGameLogic::class.java.simpleName

    var liveboard: MutableLiveData<Array<Array<String>>> = MutableLiveData<Array<Array<String>>>()
    var livewinner: MutableLiveData<Int?> = MutableLiveData<Int?>()

    var player = CROSS
    var winner:Int? = null

    fun toggle(){   //Also switches the turn for Network
        if(player == CROSS){ player = CIRCLE }
        else player = CROSS
        if(MyApplication.onlineMode) {  //TODO: Figure out how to call GameHolder's toggleNetworkTurn() instead.
            val networkActivePlayer = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer")
            if(MyApplication.isCodeMaker) networkActivePlayer.setValue(MyApplication.guestID)
            else networkActivePlayer.setValue(MyApplication.hostID)
        }
    }

    //Called from GameHolder whenever the Field changes.
    fun networkOnFieldUpdate(data : String?){
        //Update Local Board with Network Board
        Log.d(TAG, "networkOnFieldUpdate")
        networkBoardToLocalBoard();
        //Update Liveboard to update UI and whatever else is controlled by livedata
        liveboard.value = board
        //Check board
        checkField()
    }

    // Updates local board by taking in the values from the network board
    fun networkBoardToLocalBoard(){
        Log.d(TAG, "networkBoardToLocalBoard")
        val field_data = MyApplication.myRef.child("data").child(MyApplication.code).child("Field");
        field_data.child("0").get().addOnSuccessListener {
            board[0][0] = it.value.toString()
        }
        field_data.child("1").get().addOnSuccessListener {
            board[0][1] = it.value.toString()
        }
        field_data.child("2").get().addOnSuccessListener {
            board[0][2] = it.value.toString()
        }
        field_data.child("3").get().addOnSuccessListener {
            board[1][0] = it.value.toString()
        }
        field_data.child("4").get().addOnSuccessListener {
            board[1][1] = it.value.toString()
        }
        field_data.child("5").get().addOnSuccessListener {
            board[1][2] = it.value.toString()
        }
        field_data.child("6").get().addOnSuccessListener {
            board[2][0] = it.value.toString()
        }
        field_data.child("7").get().addOnSuccessListener {
            board[2][1] = it.value.toString()
        }
        field_data.child("8").get().addOnSuccessListener {
            board[2][2] = it.value.toString()
        }
    }

    // Updates network board by translating the values from the local board
    fun localBoardToNetworkBoard(){
        Log.d(TAG, "LOCALBOARDTONETWORKBOARD TRIGGERED")
        val field_data = MyApplication.myRef.child("data").child(MyApplication.code).child("Field");
        field_data.child("0").setValue(board[0][0])
        field_data.child("1").setValue(board[0][1])
        field_data.child("2").setValue(board[0][2])
        field_data.child("3").setValue(board[1][0])
        field_data.child("4").setValue(board[1][1])
        field_data.child("5").setValue(board[1][2])
        field_data.child("6").setValue(board[2][0])
        field_data.child("7").setValue(board[2][1])
        field_data.child("8").setValue(board[2][2])
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
        if(!MyApplication.onlineMode || MyApplication.myTurn) {
            if (board[a][b] != "" || winner != null) {
                return
            }
            board[a][b] = (player)
            checkField()
            toggle()
            liveboard.value = board     //Update Liveboard, which triggers observer
            if(MyApplication.onlineMode){ //Update Network Board
                localBoardToNetworkBoard()
            }
        }
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