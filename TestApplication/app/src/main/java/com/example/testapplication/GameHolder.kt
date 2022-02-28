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
                fragToLoad = Arithmetics()
                viewmodel = ViewModelProvider(this).get(ArithmeticsViewModel::class.java)
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

            //Network setup work depending on game - e.g. setup a 9 field empty board for Tic Tac Toe.
            networkSetup(viewmodel)

            Log.d(TAG, MyApplication.code)

            //Setup field, listener and logic for the variable that controls whose turn it is
            MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //TODO: NULL CHECK(?)
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

            //NOTE REGARDING THE OLD DATA PROBLEM: Consider REMOVING and entirely recreating the board once we dont need it anymore (on game restart...)
            //TODO: IMPLEMENT THIS
            //Listener that calls the fragment's network field update function if the "FieldUpdate" flag has been turned to true.
            // Also sets the flag back to false once the field has been updated.
            MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null && snapshot.value != false){
                        Log.d(TAG, "Field update")
                        MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(false)
                        var data = snapshot.key
                        when (viewmodel) {
                            is TicTacToeViewModel -> if (snapshot.value != "")(viewmodel as TicTacToeViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel1ViewModel -> (viewmodel as PlaceholderSpiel1ViewModel).logic.networkOnFieldUpdate(data)
                            is ArithmeticsViewModel -> (viewmodel as ArithmeticsViewModel).logic.networkOnFieldUpdate(data)
                            is PlaceholderSpiel3ViewModel -> (viewmodel as PlaceholderSpiel3ViewModel).logic.networkOnFieldUpdate(data)
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
            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        Log.d(TAG, "WINNER TRIGGERED")
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
                                MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("GamesPlayed").get().addOnSuccessListener {
                                    if (it != null){
                                        val key: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("Win%").push().getKey()
                                        val map: MutableMap<String, Any> = HashMap()
                                        map[key!!] = it.value.toString()
                                        MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("Win%").updateChildren(map)
                                    }
                                }
                            }
                        }

                        build.setPositiveButton("rematch") { dialog, which ->
                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(null)
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").get().addOnSuccessListener {
                                if (it.value == null) {
                                    startLoad()
                                    MyApplication.isLoading = true
                                    MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(true)
                                } else if (it.value == true) {
                                    //TODO REMATCH (RESET BOARD)
                                    networkSetup(viewmodel)
                                }
                            }
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
            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").addValueEventListener(object : ValueEventListener {
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
            //TODO: CLEAN THIS TERRIBLENESS UP
            /*
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

             */

            //setup listener to quit game if Opponent leaves mid-match...
            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request")
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
                                finish()
                            }
                            build.show()
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

    //TODO FIX LEAVE JANK
    fun exitGame() {
        //cleanup
        MyApplication.myRef.child("Users").child(SplitString(MyApplication.guestID)).child("Request").setValue("")
        MyApplication.myRef.child("Users").child(SplitString(MyApplication.hostID)).child("Request").setValue("")
        MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
    }

    fun networkSetup(viewmodel : ViewModel) {
        Log.d(TAG, "NETWORK SETUP TRIGGERED")
        when (viewmodel) {
            is TicTacToeViewModel -> {
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    /*val data_field = MyApplication.myRef.child("data").child(MyApplication.code).child("Field")
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
                                                        Log.d(TAG, MyApplication.networkSetupComplete.toString())
                                                        if (MyApplication.networkSetupComplete) {
                                                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                                                        }
                                                        MyApplication.networkSetupComplete = true
                                                        Log.d(TAG, "NETWORK SETUP BOARD UPDATE")
                                                    })
                                                })
                                            })
                                        })
                                    })
                                })
                            })
                        })
                    })*/
                    val childUpdates = hashMapOf<String, Any>("0" to "", "1" to "", "2" to "", "3" to "", "4" to "", "5" to "", "6" to "", "7" to "", "8" to "")

                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").updateChildren(childUpdates).addOnSuccessListener {
                        viewmodel.logic.networkBoardToLocalBoard()
                        if (MyApplication.networkSetupComplete) {
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                        }
                        MyApplication.networkSetupComplete = true
                    }


                    /*MyApplication.myRef.child("data").child(MyApplication.code).runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            for (i in 0..8) {
                                currentData.child("Field").child(i.toString()).value = ""
                            }
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {
                            Log.d(TAG, "COMPLETED")
                            (viewmodel as TicTacToeViewModel).logic.networkBoardToLocalBoard()
                        }

                    })*/

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
            is ArithmeticsViewModel -> { //Your Setup Code here...
            }
            is PlaceholderSpiel3ViewModel -> { //Your Setup Code here...
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
