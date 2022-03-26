package com.example.testapplication
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData

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

    private fun toggle(){   //Also switches the turn for Network
        if(MyApplication.onlineMode) {
            val networkActivePlayer = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer")
            if(MyApplication.isHost) networkActivePlayer.setValue(MyApplication.guestID, { error, ref ->
                //setValue operation is done, you'll get null in errror and ref is the path reference for firebase database
                if (error != null) {
                    Log.d(TAG, "Host to Guest failed")
                }
                else if(MyApplication.guestFriendID != ""){
                    MyApplication.myRef.child("MessagingTokens").child(MyApplication.guestFriendID).get().addOnSuccessListener {
                        if(it != null){
                            val id = it.value.toString()
                            val title = "Your Turn!"
                            val message = "Your Opponent has Passed the Turn."
                            PushNotification(NotificationData(title, message), id).also {MyApplication.sendNotification(it)}
                        }
                    }
                }
            })
            else networkActivePlayer.setValue(MyApplication.hostID, { error, ref ->
                //setValue operation is done, you'll get null in errror and ref is the path reference for firebase database
                if (error != null) {
                    Log.d(TAG, "Guest to Host failed")
                }
                else if(MyApplication.hostFriendID != ""){
                    MyApplication.myRef.child("MessagingTokens").child(MyApplication.hostFriendID).get().addOnSuccessListener {
                        if(it != null){
                            val id = it.value.toString()
                            val title = "Your Turn!"
                            val message = "Your Opponent has Passed the Turn."
                            PushNotification(NotificationData(title, message), id).also {MyApplication.sendNotification(it)}
                        }
                    }
                }
            })
            networkActivePlayer.get().addOnSuccessListener {
                Log.d(TAG, it.value.toString())
            }
        } else {
            if(player == CROSS){ player = CIRCLE }
            else player = CROSS
        }
    }

    //Called from GameHolder whenever the Field changes.
    fun networkOnFieldUpdate(data : String?){
        //Update Local Board with Network Board
        networkBoardToLocalBoard();

        //Check board
        checkField()
    }

    // Updates local board by taking in the values from the network board
    fun networkBoardToLocalBoard(){
        val field_data = MyApplication.myRef.child("data").child(MyApplication.code).child("Field");

        field_data.get().addOnSuccessListener {
            for (child in it.children) {
                when(child.key) {
                    "0" -> {board[0][0] = child.value as String}
                    "1" -> {board[0][1] = child.value as String}
                    "2" -> {board[0][2] = child.value as String}
                    "3" -> {board[1][0] = child.value as String}
                    "4" -> {board[1][1] = child.value as String}
                    "5" -> {board[1][2] = child.value as String}
                    "6" -> {board[2][0] = child.value as String}
                    "7" -> {board[2][1] = child.value as String}
                    "8" -> {board[2][2] = child.value as String}
                    else -> {board[0][0] = child.value as String}
                }
            }
            liveboard.value = board
        }
    }

    // Updates network board by translating the values from the local board
    // Whenever we update the network board, the opponent's listener will be triggered to update the network board aswell.
    fun localBoardToNetworkBoard(){
        val childUpdates = hashMapOf<String, Any>("0" to board[0][0], "1" to board[0][1], "2" to board[0][2], "3" to board[1][0], "4" to board[1][1], "5" to board[1][2], "6" to board[2][0], "7" to board[2][1], "8" to board[2][2])

        MyApplication.myRef.child("data").child(MyApplication.code).child("Field").updateChildren(childUpdates).addOnSuccessListener {
            MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
        }
    }

    //Überprüft Spielfeld auf Gewinn oder Unentschieden
    private fun checkField(): Boolean {
        var win = false

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
            livewinner.value = winner
            if (!MyApplication.onlineMode) {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    clear()
                }, 1500)
            } else {
                var networkWinner = if (MyApplication.isHost) {
                    MyApplication.hostID
                } else {
                    MyApplication.guestID
                }
                if (full) networkWinner = "-1"
                MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(networkWinner)
                winner = null
            }
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
            if (!checkField()) {
                toggle()
            }
            liveboard.value = board     //Update Liveboard, which triggers observer
            if(MyApplication.onlineMode){ //Update Network Board
                localBoardToNetworkBoard()
            }
        }
    }

    private fun clear() {
        for(i in 0..2){
            for(j in 0..2){
                board[i][j]= ""
            }
        }

        winner = null
        livewinner.value = winner
        liveboard.value = board     //Update Liveboard, which triggers observer
    }

}