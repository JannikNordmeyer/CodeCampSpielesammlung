package com.example.testapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.example.testapplication.databinding.ActivityGameSelectNetworkBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//TODO: Database sollte einen entry haben für welches Spiel man überhaupt spielt für verschiende Queues. Benutze die selectedGames Konstante dafür!

class GameSelectNetwork : AppCompatActivity() {
    private lateinit var binding: ActivityGameSelectNetworkBinding
    var host : Boolean = false
    private val TAG = GameSelectNetwork::class.java.simpleName

    private lateinit var quickplayListener: ValueEventListener

    var quickplayFilter = ""

    override fun onDestroy() {
        super.onDestroy()
        if(MyApplication.onlineMode) {
            Log.d(TAG, "##### GAME SELECT NETWORK DESTROY CALLED #######")
            if (this::quickplayListener.isInitialized) {
                MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").removeEventListener(quickplayListener)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select_network)

        Log.d(TAG, "##### GAME SELECT NETWORK ON CREATE CALLED #######")

        binding = ActivityGameSelectNetworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Select Quickplay Filter
        when (MyApplication.globalSelectedGame) {
            GameNames.PLACEHOLDERSPIEL1 -> {
                quickplayFilter = "PLACEHOLDERSPIEL1"
            }
            GameNames.ARITHMETICS -> {
                quickplayFilter = "ARITHMETICS"
            }
            GameNames.SCHRITTZAEHLER -> {
                quickplayFilter = "SCHRITTZAEHLER"
            }
            GameNames.PLACEHOLDERSPIEL4 -> {
                quickplayFilter = "PLACEHOlDERSPIEL4"
            }
            GameNames.PLACEHOLDERSPIEL5 -> {
                quickplayFilter = "PLACEHOLDERSPIEL5"
            }
            GameNames.TICTACTOE -> {
                quickplayFilter = "TICTACTOE"
            }
            else -> Log.d(TAG, " ERROR: FAILED TO LOAD QUICKPLAY FILTER")
        }

        fun startGame(){
            Log.d(TAG, "#### START GAME ####")
            if(MyApplication.onlineMode) {
                host = false
                MyApplication.myTurn = MyApplication.isCodeMaker
                MyApplication.networkSetupComplete = false
                MyApplication.Ileft = false;
            }
            val intent = Intent(this, GameHolder::class.java)
            startActivity(intent)
        }

        fun networkHostGame(opponent : String){
            Log.d(TAG, "NETWORK HOST GAME CALLED")
            //Generiere Raum Code
            MyApplication.code = SplitString(opponent)/*Guest Email*/ + SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Host Email*/
            MyApplication.isCodeMaker = true
            //Verlasse Quickplay Lobby
            MyApplication.myRef.child("Quickplay").child(quickplayFilter).setValue(null)
            MyApplication.onlineMode = true;
            //Markiere mich als Host im Raum
            Log.d(TAG, "ADDING LISTENER FOR HOST ENTRY IN ROOM ???")
            MyApplication.myRef.child("data").child(MyApplication.code).child("Host").setValue(FirebaseAuth.getInstance().currentUser!!.email, { error, ref ->
                if (error == null) {
                    Log.d(TAG, "ADDING LISTENER FOR GUEST ENTRY IN LOBBY")
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").addValueEventListener ( object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                Log.d(TAG,"START GAME FROM HOSTING GAME FUNCTION")
                                startGame()
                                stopLoad()
                                MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").removeEventListener(this)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                    }
            })
        }

        fun networkJoinGame(opponent : String){
            Log.d(TAG, "####### NETWORK JOIN GAME CALLED #######")
            MyApplication.onlineMode = true;
            //Merke Raum Code
            MyApplication.code =  SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Guest Email*/ + SplitString(opponent)/*Host Email*/
            MyApplication.isCodeMaker = false
            //Verlasse Quickplay Lobby
            MyApplication.myRef.child("Quickplay").child(quickplayFilter).setValue(null)
            //Markiere mich als Guest im Raum
            Log.d(TAG, "ADDING LISTENER FOR GUEST ENTRY IN ROOM")

            //TODO: Bruder was ist das überhaupt für ein code? Was machen wir hier? Ist das ein crackhead on success listener??

            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").setValue(FirebaseAuth.getInstance().currentUser!!.email, { error, ref ->
                if (error == null) {
                    Log.d(TAG, "ADDING LISTENER FOR HOST ENTRY IN ROOM")
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Host").addValueEventListener (object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                Log.d(TAG,"START GAME FROM JOINING GAME")
                                startGame()
                                MyApplication.myRef.child("data").child(MyApplication.code).child("Host").removeEventListener(this)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            })
        }

        //TODO: App sollte nicht explodieren wenn man nicht eingeloggt ist und offline spielen will
        //Wenn jemand während des wartens in der Quickplay Lobby deine Request animmt, hoste spiel.
        quickplayListener = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null && snapshot.value != "" && host) {
                    Log.d(TAG, "##### SOMEONE FOUND MY LOBBY: "+snapshot.value.toString()+"######")
                    networkHostGame(snapshot.value as String)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //TODO: Was ist das? Ist das nicht einfach nur Quickplay?
        binding.BtnOnline.setOnClickListener {

        }

        binding.BtnOffline.setOnClickListener {
            MyApplication.onlineMode = false
            Log.d(TAG,"START GAME FROM OFFLINE GAME")
            startGame()
        }

        binding.buttonreturn.setOnClickListener{
            finish()
        }

        binding.BtnFriend.setOnClickListener {
            //TODO: IMPLEMENT
        }

        binding.BtnQuickplay.setOnClickListener {
            startLoad()
            Log.d(TAG, "####### QUICKPLAY BUTTON PRESSED ########")
            Log.d(TAG, "ADDING ON SUCCESS LISTENER FOR GETTING QUICKPLAY LOBBY")
            //Hole die Liste von Spielern in der Quickplay Lobby
            MyApplication.myRef.child("Quickplay").child(quickplayFilter).get().addOnSuccessListener(this) {
                Log.d(TAG, "NSIDE ON SUCCESS LISTENER FOR QUICKPLAY LOBBY")
                if(it.value != null){  //Falls es Spieler gibt...
                    Log.d(TAG, "FOUND OTHER PLAYER IN QUICKPLAY LOBBY")
                    //Heirate
                    MyApplication.myRef.child("Users").child(SplitString(it.value.toString())).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                    MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue(it.value)
                    //Join game
                    Log.d(TAG, "CALLING NETWORK JOIN GAME FROM QUICKPLAY LISTENER")
                    networkJoinGame(it.value.toString())
                    stopLoad()
                } else { //Falls es keine Spieler gibt, werde ein Host und warte in der Quickplay lobby
                    Log.d(TAG, "NO PLAYERS IN QUICKPLAY LOBBY - BECOME HOST")
                    host = true
                    MyApplication.myRef.child("Quickplay").child(quickplayFilter).setValue(FirebaseAuth.getInstance().currentUser!!.email)
                }
                Log.d(TAG, "QUICKPLAY LOBBY LISTENER END")
            }
            Log.d(TAG, "####### QUICKPLAY BUTTON CODE END #######")
        }

        //Verlasse Quickplay Lobby wenn man als Host Wartet
        binding.BtnCancel.setOnClickListener {
            MyApplication.myRef.child("Quickplay").child(quickplayFilter).setValue(null)
            host = false
            stopLoad()
        }

    }

    //cant save @ as key in the database so this function returns only the first part of the email that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }

    fun startLoad() {
        binding.BtnOnline.visibility    = View.GONE
        binding.BtnFriend.visibility    = View.GONE
        binding.BtnOffline.visibility   = View.GONE
        binding.BtnQuickplay.visibility = View.GONE
        binding.buttonreturn.visibility = View.GONE
        //binding.TVHead.visibility       = View.GONE
        binding.PBLoading.visibility    = View.VISIBLE
        binding.BtnCancel.visibility    = View.VISIBLE
    }

    fun stopLoad() {
        binding.BtnOnline.visibility    = View.VISIBLE
        binding.BtnFriend.visibility    = View.VISIBLE
        binding.BtnOffline.visibility   = View.VISIBLE
        binding.BtnQuickplay.visibility = View.VISIBLE
        binding.buttonreturn.visibility = View.VISIBLE
        //binding.TVHead.visibility       = View.VISIBLE
        binding.PBLoading.visibility    = View.GONE
        binding.BtnCancel.visibility    = View.GONE
    }

}