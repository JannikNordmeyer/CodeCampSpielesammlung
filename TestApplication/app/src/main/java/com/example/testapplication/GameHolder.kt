package com.example.testapplication

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameHolderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GameHolder : AppCompatActivity() {

    private lateinit var binding: ActivityGameHolderBinding

    private val TAG = GameHolder::class.java.simpleName

    lateinit var fragToLoad: Fragment
    lateinit var viewmodel: ViewModel

    lateinit var rematchAlert: AlertDialog
    lateinit var exitAlert: AlertDialog

    lateinit var activePlayerListener: ValueEventListener
    lateinit var fieldUpdateListener: ValueEventListener
    lateinit var winnerPlayerListener: ValueEventListener
    lateinit var remachtListener: ValueEventListener
    lateinit var exitPlayerListener: ValueEventListener

    //Session stat variables
    var gamesPlayed = 1

    var quickplayFilter = ""

    //TODO: Needing this function every single time is pretty stupid, find a better solution!
    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str: String): String {
        var split = str.split("@")
        return split[0]
    }

    private fun updateStatistics() {
        MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("GamesPlayed").get().addOnSuccessListener(this) {
            if(it != null){
                var _gamesPlayed = it.value.toString().toInt()
                _gamesPlayed += gamesPlayed
                MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("GamesPlayed").setValue(_gamesPlayed)
            }
        }
    }

    //Call this function within games to switch network Turn! //TODO: Actually figure out how to do that instead of copy and pasting it in there
    fun toggleNetworkTurn(){
        val networkActivePlayer = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer")
        if(MyApplication.isCodeMaker) networkActivePlayer.setValue(MyApplication.guestID)
        else networkActivePlayer.setValue(MyApplication.hostID)
    }

    override fun onDestroy() {
        Log.d(TAG, "##################### GAME HOLDER "+android.os.Process.myTid().toString()+" FUCKING DIED ####################")
        super.onDestroy()
        if (MyApplication.onlineMode) {
            //Update stats
            updateStatistics()

            if(this::rematchAlert.isInitialized){
                rematchAlert.dismiss()
            }
            if(this::exitAlert.isInitialized){
                exitAlert.dismiss()
            }
            MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").removeEventListener(activePlayerListener)
            MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").removeEventListener(fieldUpdateListener)
            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").removeEventListener(winnerPlayerListener)
            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").removeEventListener(remachtListener)
            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").removeEventListener(exitPlayerListener)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_holder)

        binding = ActivityGameHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Select Fragment and load it's Viewmodel
        when (MyApplication.globalSelectedGame) {
            GameNames.COMPASS -> {
                fragToLoad = PlaceholderSpiel1()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel1ViewModel::class.java)
                quickplayFilter = "PLACEHOLDERSPIEL1"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL1")
            }
            GameNames.ARITHMETICS -> {
                fragToLoad = Arithmetics()
                viewmodel = ViewModelProvider(this).get(ArithmeticsViewModel::class.java)
                quickplayFilter = "ARITHMETICS"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL2")
            }
            GameNames.SCHRITTZAEHLER -> {
                fragToLoad = Schrittzaehler()
                viewmodel = ViewModelProvider(this).get(SchrittzaehlerViewModel::class.java)
                quickplayFilter = "SCHRITTZAEHLER"
                Log.d(TAG, "LOADED SCHRITTZAEHLER")
            }
            GameNames.PLACEHOLDERSPIEL4 -> {
                fragToLoad = PlaceholderSpiel4()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel4ViewModel::class.java)
                quickplayFilter = "PLACEHOlDERSPIEL4"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL4")
            }
            GameNames.PLACEHOLDERSPIEL5 -> {
                fragToLoad = PlaceholderSpiel5()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel5ViewModel::class.java)
                quickplayFilter = "PLACEHOLDERSPIEL5"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL5")
            }
            GameNames.TICTACTOE -> {
                fragToLoad = TicTacToe()
                viewmodel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)
                quickplayFilter = "TICTACTOE"
                Log.d(TAG, "LOADED TICTACTOE")
            }
            else -> Log.d(TAG, " ERROR: FAILED TO LOAD GAME")
        }

        //Load Fragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.FrameLayoutGameHolder, fragToLoad)
            commit()
        }

        //If we are in online mode...
        if (MyApplication.onlineMode && !MyApplication.networkSetupComplete) {
            //Network setup work independent of game
            //Get and save ID of host and guest as a global control var
            MyApplication.myRef.child("data").child(MyApplication.code).child("Host").get().addOnSuccessListener(this) {
                MyApplication.hostID = it.value.toString()
                Log.d(TAG, MyApplication.hostID)
                //Setup ActivePlayer field which will be used to determine what player can make a move - the "Host" and "Guest" field is entered here and checked for, same goes for ExitPlayer.
                if (MyApplication.isCodeMaker)
                    MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").setValue(MyApplication.hostID)
            }
            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").get().addOnSuccessListener(this) {
                MyApplication.guestID = it.value.toString()
            }

            //Network setup work depending on game - e.g. setup a 9 field empty board for Tic Tac Toe.
            networkSetup(viewmodel)

            Log.d(TAG, MyApplication.code)

            //Setup field, listener and logic for the variable that controls whose turn it is
            activePlayerListener = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null) {
                        Log.d(TAG, "ACTIVE PLAYER LISTENER TRIGGERED")
                        val data_activePlayer = snapshot.value.toString()
                        Log.d(TAG, data_activePlayer)
                        if ((data_activePlayer == MyApplication.hostID) && MyApplication.isCodeMaker) MyApplication.myTurn = true
                        else MyApplication.myTurn = (data_activePlayer == MyApplication.guestID) && !MyApplication.isCodeMaker
                        Log.d(TAG, MyApplication.myTurn.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "ACTIVE PLAYER CANCELLED LISTENER TRIGGERED")
                    TODO("Not yet implemented")
                }

            })

            //NOTE REGARDING THE OLD DATA PROBLEM: Consider REMOVING and entirely recreating the board once we dont need it anymore (on game restart...)
            //TODO: IMPLEMENT THIS
            //Listener that calls the fragment's network field update function if the "FieldUpdate" flag has been turned to true.
            // Also sets the flag back to false once the field has been updated.
            fieldUpdateListener = MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null && snapshot.value != false){
                        Log.d(TAG, "Field update")
                        MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(false)
                        var data = snapshot.key
                        when (viewmodel) {
                            is TicTacToeViewModel -> if (snapshot.value != "")(viewmodel as TicTacToeViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel1ViewModel -> (viewmodel as PlaceholderSpiel1ViewModel).logic.networkOnFieldUpdate(data)
                            is SchrittzaehlerViewModel -> (viewmodel as SchrittzaehlerViewModel).logic.networkOnFieldUpdate(data)
                            //FieldUpdate -> Partner has added their score to DB
                            is ArithmeticsViewModel -> (viewmodel as ArithmeticsViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel4ViewModel -> (viewmodel as PlaceholderSpiel4ViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel5ViewModel -> (viewmodel as PlaceholderSpiel5ViewModel).logic.networkOnFieldUpdate(data)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            //Setup field, listener and logic for the variable that controls who won
            winnerPlayerListener = MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        Log.d(TAG, android.os.Process.myTid().toString()+": WINNER TRIGGERED")
                        val value = snapshot.value
                        val build = AlertDialog.Builder(this@GameHolder);
                        build.setCancelable(false)
                        if (value == "-1") {
                            build.setTitle("Draw")
                            build.setMessage("Game is a draw")
                        } else {
                            build.setTitle("Game Over!")
                            build.setMessage("$value has won the game!")
                            //Win Percentage updaten - alle Spiele?
                            if(value == FirebaseAuth.getInstance().currentUser!!.email){
                                MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGame.toString()).child("GamesPlayed").get().addOnSuccessListener {
                                    if (it != null){
                                        val key: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGame.toString()).child("Win%").push().getKey()
                                        val map: MutableMap<String, Any> = HashMap()
                                        map[key!!] = it.value.toString()
                                        MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGame.toString()).child("Win%").updateChildren(map)
                                    }
                                }
                            }
                        }

                        build.setPositiveButton("rematch") { dialog, which ->
                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(null)
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").get().addOnSuccessListener {
                                gamesPlayed++
                                if (it.value == null) {
                                    startLoad()
                                    MyApplication.isLoading = true
                                    MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(true)
                                } else if (it.value == true) {
                                    networkSetup(viewmodel)
                                }
                            }
                        }

                        build.setNegativeButton("exit") { dialog, which ->
                            exitGame()
                            finish()
                        }

                        if(!isFinishing()) {
                            rematchAlert = build.show()
                        }
                        //MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
                    }
                }



                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            remachtListener = MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        if (snapshot.value == false && MyApplication.isLoading) {
                            stopLoad()
                            networkSetup(viewmodel)
                            MyApplication.isLoading = false
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").removeValue()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            //setup listener to quit game if Opponent leaves mid-match...
            exitPlayerListener = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == "" && !MyApplication.Ileft){
                            Log.d(TAG, "EXIT TRIGGERED")
                            val build = AlertDialog.Builder(this@GameHolder);
                            build.setCancelable(false)
                            build.setTitle("Game Over!")
                            build.setMessage("Opponent has left")
                            build.setPositiveButton("OK") { dialog, which ->
                                MyApplication.Ileft = true;
                                exitGame()
                                finish()
                            }
                            if(!isFinishing()) {
                                exitAlert = build.show()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "EXIT CANCELLED TRIGGERED")
                        TODO("Not yet implemented")
                    }
                    //endregion

                })

        }

        binding.ButtonGiveUp.setOnClickListener() {
            if(MyApplication.onlineMode){
                MyApplication.Ileft = true;
                exitGame()
            }
            finish()
        }

    }

    fun exitGame() {
        //cleanup
        MyApplication.myRef.child("Users").child(SplitString(MyApplication.guestID)).child("Request").setValue("")
        MyApplication.myRef.child("Users").child(SplitString(MyApplication.hostID)).child("Request").setValue("")
        MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
    }

    //TODO: GENERALIZE STUFF
    fun networkSetup(viewmodel : ViewModel) {
        Log.d(TAG, "NETWORK SETUP TRIGGERED")
        when (viewmodel) {
            is TicTacToeViewModel -> {
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    val childUpdates = hashMapOf<String, Any>("0" to "", "1" to "", "2" to "", "3" to "", "4" to "", "5" to "", "6" to "", "7" to "", "8" to "")

                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").updateChildren(childUpdates).addOnSuccessListener(this) {
                        viewmodel.logic.networkBoardToLocalBoard()
                        if (MyApplication.networkSetupComplete) {
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                        }
                        MyApplication.networkSetupComplete = true
                    }

                    if (!MyApplication.isCodeMaker) {
                        (viewmodel as TicTacToeViewModel).logic.player = "O"
                    }
                } else if (MyApplication.isLoading) {
                    viewmodel.logic.networkBoardToLocalBoard()
                    Log.d(TAG, "NETWORK SETUP BOARD UPDATE LOADING")
                }
            }
            is PlaceholderSpiel1ViewModel -> { //Your Setup Code here...
            }
            is ArithmeticsViewModel -> {
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    val childUpdates = hashMapOf<String, Any>("HostScore" to "-1", "GuestScore" to "-1")

                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").updateChildren(childUpdates).addOnSuccessListener(this) {
                        if (MyApplication.networkSetupComplete) {
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                        }
                        MyApplication.networkSetupComplete = true
                    }

                }
                if(MyApplication.networkSetupComplete) viewmodel.gameTimer.start()
            }
            is SchrittzaehlerViewModel -> { //Your Setup Code here...
            }
            is PlaceholderSpiel4ViewModel -> { //Your Setup Code here...
            }
            is PlaceholderSpiel5ViewModel -> { //Your Setup Code here...
            }
        }
    }

    fun startLoad() {
        binding.FrameLayoutGameHolder.visibility = View.GONE
        binding.idPB.visibility = View.VISIBLE
    }

    fun stopLoad() {
        binding.FrameLayoutGameHolder.visibility = View.VISIBLE
        binding.idPB.visibility = View.GONE
    }
}
