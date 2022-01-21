package com.example.testapplication

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
        val networkActivePlayer = MyApplication.myRef.child(MyApplication.code).child("ActivePlayer")
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
            MyApplication.hostID = MyApplication.myRef.child("Host").get().toString()
            MyApplication.guestID = MyApplication.myRef.child("Guest").get().toString()

            //Setup ActivePlayer field which will be used to determine what player can make a move - the "Host" and "Guest" field is entered here and checked for, same goes for ExitPlayer.
            MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").setValue(MyApplication.hostID)

            //Setup ExitPlayer to determine if and who has left a game.
            MyApplication.myRef.child("data").child(MyApplication.code).child("ExitPlayer")

            //Setup WinnerPlayer to determine if and who has won a game.
            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer")

            //Network setup work depending on game - e.g. setup a 9 field empty board for Tic Tac Toe.
            when (viewmodel) {
                is TicTacToeViewModel -> {
                    val data_field = MyApplication.myRef.child("data").child(MyApplication.code).child("Field")
                    data_field.child("0").setValue("")
                    data_field.child("1").setValue("")
                    data_field.child("2").setValue("")
                    data_field.child("3").setValue("")
                    data_field.child("4").setValue("")
                    data_field.child("5").setValue("")
                    data_field.child("6").setValue("")
                    data_field.child("7").setValue("")
                    data_field.child("8").setValue("")
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

            //Setup field, listener and logic for the variable that controls whose turn it is
            MyApplication.myRef.child(MyApplication.code).child("ActivePlayer").addChildEventListener(object : ChildEventListener {
                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        val data_activePlayer = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").get().toString()
                        if ((data_activePlayer == MyApplication.hostID) && MyApplication.isCodeMaker) MyApplication.myTurn = true
                        else if ((data_activePlayer == MyApplication.guestID) && !MyApplication.isCodeMaker) MyApplication.myTurn = true
                        else MyApplication.myTurn = false
                    }

                    //region
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                    //endregion
                })

            //Setup field, listener and logic for the variable that controls who won
            MyApplication.myRef.child(MyApplication.code).child("WinnerPlayer").addChildEventListener(object : ChildEventListener {
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    //TODO: Actually unsure if this is needed. Test the thing.
                }

                //region
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                //endregion
            })

            //setup listener to call fragment's logic networkOnFieldUpdate function to update field contents whenever they update.
            MyApplication.myRef.child(MyApplication.code).child("Field")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        var data = snapshot.key
                        when (viewmodel) {
                            is TicTacToeViewModel -> (viewmodel as TicTacToeViewModel).logic.networkOnFieldUpdate(data)
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
                        TODO("Not yet implemented")
                    }
                    //endregion
                })

            //setup listener to quit game if Opponent leaves mid-match...
            MyApplication.myRef.child(MyApplication.code).child("ExitPlayer")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        val data = snapshot.key //Who left?
                        //TODO: Probably do something else here than just exiting the process when someone else leaves? Push message then kick back into networkSelect or something?
                        exitProcess(1)
                    }

                    //region
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                    //endregion

                })

            binding.ButtonGiveUp.setOnClickListener() {
                if(MyApplication.onlineMode){
                    //Announce that you are leaving when hitting Give Up.
                    var to_enter = ""
                    if(MyApplication.isCodeMaker) to_enter = MyApplication.hostID
                    else to_enter = MyApplication.guestID
                    MyApplication.myRef.child(MyApplication.code).child("ExitPlayer").setValue(to_enter)
                }
                finish()
            }

        }
    }
}