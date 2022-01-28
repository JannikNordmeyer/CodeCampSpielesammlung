package com.example.testapplication

import android.app.AlertDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.widget.Placeholder
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameHolderBinding
import com.example.testapplication.databinding.FragmentTictactoeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.system.exitProcess

class GameHolder : AppCompatActivity() {

    private lateinit var binding: ActivityGameHolderBinding

    private val TAG = GameHolder::class.java.simpleName

    lateinit var fragToLoad: Fragment
    lateinit var viewmodel: ViewModel

    //TODO: Needing this function every single time is pretty stupid, find a better solution!
    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str: String): String {
        var split = str.split("@")
        return split[0]
    }

    //Call this function within games to switch network Turn! //TODO: Actually figure out how to do that instead of copy and pasting it in there
    fun toggleNetworkTurn(){
        val networkActivePlayer = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer")
        if(MyApplication.isCodeMaker) networkActivePlayer.setValue(MyApplication.guestID)
        else networkActivePlayer.setValue(MyApplication.hostID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_holder)

        binding = ActivityGameHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Select Fragment and load it's Viewmodel
        when (MyApplication.globalSelectedGame) {
            GameNames.PLACEHOLDERSPIEL1 -> {
                fragToLoad = PlaceholderSpiel1()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel1ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL1")
            }
            GameNames.PLACEHOLDERSPIEL2 -> {
                fragToLoad = PlaceholderSpiel2()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel2ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL2")
            }
            GameNames.PLACEHOLDERSPIEL3 -> {
                fragToLoad = PlaceholderSpiel3()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel3ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL3")
            }
            GameNames.PLACEHOLDERSPIEL4 -> {
                fragToLoad = PlaceholderSpiel4()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel4ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL4")
            }
            GameNames.PLACEHOLDERSPIEL5 -> {
                fragToLoad = PlaceholderSpiel5()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel5ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL5")
            }
            GameNames.TICTACTOE -> {
                fragToLoad = TicTacToe()
                viewmodel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)
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
            MyApplication.myRef.child("data").child(MyApplication.code).child("Host").get().addOnSuccessListener {
                MyApplication.hostID = it.value.toString()
                Log.d(TAG, MyApplication.hostID)
                //Setup ActivePlayer field which will be used to determine what player can make a move - the "Host" and "Guest" field is entered here and checked for, same goes for ExitPlayer.
                if (MyApplication.isCodeMaker)
                    MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").setValue(MyApplication.hostID)
            }
            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").get().addOnSuccessListener {
                MyApplication.guestID = it.value.toString()
            }



            // TODO: CLEAN UP DATABASE + FIX
            //Setup ExitPlayer to determine if and who has left a game.
            //MyApplication.myRef.child("data").child(MyApplication.code).child("ExitPlayer").setValue(false) //nodes needs a value != null to exist

            //Setup WinnerPlayer to determine if and who has won a game.
            //MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(false) //nodes needs a value != null to exist

            //Network setup work depending on game - e.g. setup a 9 field empty board for Tic Tac Toe.
            networkSetup(viewmodel)

            Log.d(TAG, MyApplication.code)

            //Setup field, listener and logic for the variable that controls whose turn it is
            MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "ACTIVE PLAYER LISTENER TRIGGERED")
                        val data_activePlayer = snapshot.value.toString()
                        Log.d(TAG, data_activePlayer)
                        if ((data_activePlayer == MyApplication.hostID) && MyApplication.isCodeMaker) MyApplication.myTurn = true
                        else MyApplication.myTurn = (data_activePlayer == MyApplication.guestID) && !MyApplication.isCodeMaker
                        Log.d(TAG, MyApplication.myTurn.toString())
                    }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "ACTIVE PLAYER CANCELLED LISTENER TRIGGERED")
                    TODO("Not yet implemented")
                }

            })

            //Setup field, listener and logic for the variable that controls who won
            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        Log.d(TAG, "WINNER TRIGGERED")
                        val value = snapshot.value
                        val build = AlertDialog.Builder(this@GameHolder);
                        build.setCancelable(false)
                        if (value == -1) {
                            build.setTitle("Draw")
                            build.setMessage("Game is a draw")
                        } else {
                            build.setTitle("Game Over!")
                            build.setMessage("$value has won the game!")
                        }

                        build.setPositiveButton("rematch") { dialog, which ->
                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(null)
                            /*when (viewmodel) {
                                is TicTacToeViewModel -> {
                                    (viewmodel as TicTacToeViewModel).logic.clear()
                                }
                                is PlaceholderSpiel1ViewModel -> { //Your winnerLock Code here...
                                }
                                is PlaceholderSpiel2ViewModel -> { //Your winnerLock Code here...
                                }
                                is PlaceholderSpiel3ViewModel -> { //Your winnerLock Code here...
                                }
                                is PlaceholderSpiel4ViewModel -> { //Your winnerLock Code here...
                                }
                                is PlaceholderSpiel5ViewModel -> { //Your winnerLock Code here...
                                }
                            }*/
                            networkSetup(viewmodel)
                        }

                        build.setNegativeButton("exit") { dialog, which ->
                            exitGame()
                            finish()
                        }

                        build.show()
                        //MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            //setup listener to call fragment's logic networkOnFieldUpdate function to update field contents whenever they update.
            MyApplication.myRef.child("data").child(MyApplication.code).child("Field")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                            Log.d(TAG, "Field update")
                            var data = snapshot.key
                            when (viewmodel) {
                                is TicTacToeViewModel -> if (snapshot.value != "")(viewmodel as TicTacToeViewModel).logic.networkOnFieldUpdate(data)
                                is PlaceholderSpiel1ViewModel -> (viewmodel as PlaceholderSpiel1ViewModel).logic.networkOnFieldUpdate(data)
                                is PlaceholderSpiel2ViewModel -> (viewmodel as PlaceholderSpiel2ViewModel).logic.networkOnFieldUpdate(data)
                                is PlaceholderSpiel3ViewModel -> (viewmodel as PlaceholderSpiel3ViewModel).logic.networkOnFieldUpdate(data)
                                is PlaceholderSpiel4ViewModel -> (viewmodel as PlaceholderSpiel4ViewModel).logic.networkOnFieldUpdate(data)
                                is PlaceholderSpiel5ViewModel -> (viewmodel as PlaceholderSpiel5ViewModel).logic.networkOnFieldUpdate(data)
                            }
                    }

                    //region
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "FIELD CANCELLED TRIGGERED")
                        TODO("Not yet implemented")
                    }
                    //endregion
                })

            //setup listener to quit game if Opponent leaves mid-match...
            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == ""){
                            Log.d(TAG, "EXIT TRIGGERED")
                            val build = AlertDialog.Builder(this@GameHolder);
                            build.setCancelable(false)
                            build.setTitle("Game Over!")
                            build.setMessage("Opponent has left")
                            build.setPositiveButton("OK") { dialog, which ->
                                finish()
                            }
                            build.show()
                            //TODO: Probably do something else here than just exiting the process when someone else leaves? Push message then kick back into networkSelect or something?

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
                exitGame()
            }
            finish()
        }

    }

    fun exitGame() {
        //cleanup
        MyApplication.myRef.child("Users").child(SplitString(MyApplication.guestID)).setValue("")
        MyApplication.myRef.child("Users").child(SplitString(MyApplication.hostID)).setValue("")
        MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
    }

    fun networkSetup(viewmodel : ViewModel) {
        Log.d(TAG, "NETWORK SETUP TRIGGERED")
        when (viewmodel) {
            is TicTacToeViewModel -> {
                val data_field = MyApplication.myRef.child("data").child(MyApplication.code).child("Field")
                data_field.child("0").setValue("", { error, ref ->
                    data_field.child("1").setValue("", { error, ref ->
                        data_field.child("2").setValue("", { error, ref ->
                            data_field.child("3").setValue("", { error, ref ->
                                data_field.child("4").setValue("", { error, ref ->
                                    data_field.child("5").setValue("", { error, ref ->
                                        data_field.child("6").setValue("", { error, ref ->
                                            data_field.child("7").setValue("", { error, ref ->
                                                data_field.child("8").setValue("", { error, ref ->
                                                    viewmodel.logic.networkBoardToLocalBoard()
                                                })
                                            })
                                        })
                                    })
                                })
                            })
                        })
                    })
                })







                if (!MyApplication.isCodeMaker) {
                    (viewmodel as TicTacToeViewModel).logic.player = "O"
                }
            }
            is PlaceholderSpiel1ViewModel -> { //Your Setup Code here...
            }
            is PlaceholderSpiel2ViewModel -> { //Your Setup Code here...
            }
            is PlaceholderSpiel3ViewModel -> { //Your Setup Code here...
            }
            is PlaceholderSpiel4ViewModel -> { //Your Setup Code here...
            }
            is PlaceholderSpiel5ViewModel -> { //Your Setup Code here...
            }
        }
    }
}