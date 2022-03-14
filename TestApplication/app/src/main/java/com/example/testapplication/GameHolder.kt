package com.example.testapplication

import android.app.AlertDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameHolderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Integer.max
import kotlin.random.Random

class GameHolder : AppCompatActivity() {

    private lateinit var binding: ActivityGameHolderBinding

    private val TAG = GameHolder::class.java.simpleName

    lateinit var fragToLoad: Fragment
    lateinit var gameViewModel: ViewModel
    lateinit var viewmodel: GameHolderViewModel

    lateinit var rematchAlert: AlertDialog
    lateinit var exitAlert: AlertDialog

    lateinit var activePlayerListener: ValueEventListener
    lateinit var fieldUpdateListener: ValueEventListener
    lateinit var winnerPlayerListener: ValueEventListener
    lateinit var remachtListener: ValueEventListener
    lateinit var exitPlayerListener: ValueEventListener
    
    private fun updateStatistics() {
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("GamesPlayed").get().addOnSuccessListener(this) {
            if(it != null){
                var _gamesPlayed = it.value.toString().toInt()
                _gamesPlayed += viewmodel.gamesPlayed
                MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("GamesPlayed").setValue(_gamesPlayed)
            }
        }
    }

    override fun onDestroy() {
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
            MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").removeEventListener(exitPlayerListener)

            exitGame()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_holder)

        binding = ActivityGameHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewmodel = ViewModelProvider(this).get(GameHolderViewModel()::class.java)

        //Select Fragment and load it's Viewmodel
        when (MyApplication.globalSelectedGame) {
            GameNames.COMPASS -> {
                fragToLoad = PlaceholderSpiel1()
                gameViewModel = ViewModelProvider(this).get(PlaceholderSpiel1ViewModel()::class.java)
                viewmodel.quickplayFilter = "PLACEHOLDERSPIEL1"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL1")
            }
            GameNames.ARITHMETICS -> {
                fragToLoad = Arithmetics()
                gameViewModel = ViewModelProvider(this).get(ArithmeticsViewModel::class.java)
                viewmodel.quickplayFilter = "ARITHMETICS"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL2")
            }
            GameNames.SCHRITTZAEHLER -> {
                fragToLoad = Schrittzaehler()
                gameViewModel = ViewModelProvider(this).get(SchrittzaehlerViewModel::class.java)
                viewmodel.quickplayFilter = "SCHRITTZAEHLER"
                Log.d(TAG, "LOADED SCHRITTZAEHLER")
            }
            GameNames.PLACEHOLDERSPIEL4 -> {
                fragToLoad = PlaceholderSpiel4()
                gameViewModel = ViewModelProvider(this).get(PlaceholderSpiel4ViewModel::class.java)
                viewmodel.quickplayFilter = "PLACEHOlDERSPIEL4"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL4")
            }
            GameNames.PLACEHOLDERSPIEL5 -> {
                fragToLoad = PlaceholderSpiel5()
                gameViewModel = ViewModelProvider(this).get(PlaceholderSpiel5ViewModel::class.java)
                viewmodel.quickplayFilter = "PLACEHOLDERSPIEL5"
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL5")
            }
            GameNames.TICTACTOE -> {
                fragToLoad = TicTacToe()
                gameViewModel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)
                viewmodel.quickplayFilter = "TICTACTOE"
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
                //Setup ActivePlayer field which will be used to determine what player can make a move - the "Host" and "Guest" field is entered here and checked for, same goes for ExitPlayer.
                if (MyApplication.isCodeMaker)
                    MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").setValue(MyApplication.hostID)
            }
            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").get().addOnSuccessListener(this) {
                MyApplication.guestID = it.value.toString()
            }

            //Network setup work depending on game - e.g. setup a 9 field empty board for Tic Tac Toe.
            networkSetup(gameViewModel)

            //Setup field, listener and logic for the variable that controls whose turn it is
            activePlayerListener = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null) {
                        val data_activePlayer = snapshot.value.toString()
                        if ((data_activePlayer == MyApplication.hostID) && MyApplication.isCodeMaker) MyApplication.myTurn = true
                        else MyApplication.myTurn = (data_activePlayer == MyApplication.guestID) && !MyApplication.isCodeMaker
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            //Listener that calls the fragment's network field update function if the "FieldUpdate" flag has been turned to true.
            // Also sets the flag back to false once the field has been updated.
            fieldUpdateListener = MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null && snapshot.value != false){
                        MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(false)
                        var data = snapshot.key
                        when (gameViewModel) {
                            is TicTacToeViewModel -> if (snapshot.value != "")(gameViewModel as TicTacToeViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel1ViewModel -> (gameViewModel as PlaceholderSpiel1ViewModel).logic.networkOnFieldUpdate(data)
                            is SchrittzaehlerViewModel -> (gameViewModel as SchrittzaehlerViewModel).logic.networkOnFieldUpdate(data)
                            is ArithmeticsViewModel -> (gameViewModel as ArithmeticsViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel4ViewModel -> (gameViewModel as PlaceholderSpiel4ViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel5ViewModel -> (gameViewModel as PlaceholderSpiel5ViewModel).logic.networkOnFieldUpdate(data)
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
                        val value = snapshot.value
                        val build = AlertDialog.Builder(this@GameHolder);
                        build.setCancelable(false)
                        if (value == "-1") {
                            build.setTitle("Draw")
                            build.setMessage("Game is a draw")
                        } else {
                            build.setTitle("Game Over!")
                            build.setMessage("$value has won the game!")
                            //Win Percentage updaten
                            if(value == FirebaseAuth.getInstance().currentUser!!.email){
                                MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("GamesPlayed").get().addOnSuccessListener {
                                    if (it != null){
                                        val key: String? = MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("Win%").push().getKey()
                                        val map: MutableMap<String, Any> = HashMap()
                                        map[key!!] = (it.value.toString().toInt() + viewmodel.gamesPlayed).toString()
                                        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("Win%").updateChildren(map)
                                    }
                                }
                            }
                            //High Score loggen
                            if(MyApplication.globalSelectedGame == GameNames.ARITHMETICS || MyApplication.globalSelectedGame == GameNames.SCHRITTZAEHLER){
                                //Get Local Score
                                var score = 0
                                if(MyApplication.globalSelectedGame == GameNames.ARITHMETICS){ score = (gameViewModel as ArithmeticsViewModel).score }
                                else{ score = (gameViewModel as SchrittzaehlerViewModel).score }
                                MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("HighScore").get().addOnSuccessListener {
                                    if (it != null){
                                        val key: String? = MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("HighScore").push().getKey()
                                        val map: MutableMap<String, Any> = HashMap()
                                        map[key!!] = max(it.children.last().value.toString().toInt(),score)
                                        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(MyApplication.globalSelectedGameStatLocation).child("HighScore").updateChildren(map)
                                    }
                                }

                            }
                        }

                        build.setPositiveButton("rematch") { dialog, which ->
                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(null)
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").get().addOnSuccessListener {
                                viewmodel.gamesPlayed++
                                if (it.value == null) {
                                    startLoad()
                                    MyApplication.isLoading = true
                                    MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(true)
                                } else if (it.value == true) {
                                    networkSetup(gameViewModel)
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
                            if (MyApplication.isCodeMaker && MyApplication.globalSelectedGame == GameNames.COMPASS) {
                                Log.d("Compass", "INIT GAME")
                                (gameViewModel as PlaceholderSpiel1ViewModel).initGame(this@GameHolder)
                            }
                            stopLoad()
                            networkSetup(gameViewModel)
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
            exitPlayerListener = MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == "" && !MyApplication.Ileft){
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
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(MyApplication.guestID)).child("Request").setValue("")
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(MyApplication.hostID)).child("Request").setValue("")
        MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
    }

    fun networkSetup(gameViewModel : ViewModel) {
        when (gameViewModel) {
            is TicTacToeViewModel -> {
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    val childUpdates = hashMapOf<String, Any>("0" to "", "1" to "", "2" to "", "3" to "", "4" to "", "5" to "", "6" to "", "7" to "", "8" to "")

                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").updateChildren(childUpdates).addOnSuccessListener(this) {
                        gameViewModel.logic.networkBoardToLocalBoard()
                        if (MyApplication.networkSetupComplete) {
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                        }
                        MyApplication.networkSetupComplete = true
                    }

                    if (!MyApplication.isCodeMaker) {
                        (gameViewModel as TicTacToeViewModel).logic.player = "O"
                    }
                } else if (MyApplication.isLoading) {
                    gameViewModel.logic.networkBoardToLocalBoard()
                }
            }
            is PlaceholderSpiel1ViewModel -> { //Your Setup Code here...
                (gameViewModel as PlaceholderSpiel1ViewModel).logic.listindex = 0
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").removeValue()
                    if (MyApplication.networkSetupComplete) {
                        if (MyApplication.isCodeMaker) {
                            Log.d("Compass", "INIT GAME")
                            gameViewModel.initGame(this)
                        }
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                    }
                    MyApplication.networkSetupComplete = true
                }

            }
            is ArithmeticsViewModel -> {
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").removeValue()
                    if (MyApplication.networkSetupComplete) {
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                    }
                }
                if(MyApplication.networkSetupComplete) gameViewModel.gameTimer.start()
                gameViewModel.resetGame()
                MyApplication.networkSetupComplete = true
            }
            is SchrittzaehlerViewModel -> {
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").removeValue()
                    if (MyApplication.networkSetupComplete) {
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                    }
                }
                MyApplication.networkSetupComplete = true
                gameViewModel.livenetworkReset.value = true
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
