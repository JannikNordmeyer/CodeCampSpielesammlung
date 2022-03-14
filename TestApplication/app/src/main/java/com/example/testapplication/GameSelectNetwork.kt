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

class GameSelectNetwork : AppCompatActivity() {
    private lateinit var binding: ActivityGameSelectNetworkBinding
    var host : Boolean = false
    private val TAG = GameSelectNetwork::class.java.simpleName

    private lateinit var quickplayListener: ValueEventListener

    var quickplayFilter = ""
    var lobbyName = ""

    override fun onDestroy() {
        super.onDestroy()
        if(MyApplication.onlineMode) {
            if (this::quickplayListener.isInitialized) {
                MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").removeEventListener(quickplayListener)
            }
        }
    }

    fun fadeOutButtons(){
        binding.BtnQuickplay.alpha = 0.5F
        binding.BtnOnlineRoomCode.alpha = 0.5F
    }

    fun fadeInButtons(){
        binding.BtnQuickplay.alpha = 1F
        binding.BtnOnlineRoomCode.alpha = 1F
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select_network)

        binding = ActivityGameSelectNetworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!MyApplication.isLoggedIn){
            fadeOutButtons()
        }

        //Select Quickplay Filter
        when (MyApplication.globalSelectedGame) {
            GameNames.COMPASS -> {
                quickplayFilter = "PLACEHOLDERSPIEL1"
                MyApplication.globalSelectedGameStatLocation = "COMPASS"
            }
            GameNames.ARITHMETICS -> {
                quickplayFilter = "ARITHMETICS"
                MyApplication.globalSelectedGameStatLocation = "ARITHMETICS"
            }
            GameNames.SCHRITTZAEHLER -> {
                quickplayFilter = "SCHRITTZAEHLER"
                MyApplication.globalSelectedGameStatLocation = "SCHRITTZAEHLER"
            }
            GameNames.PLACEHOLDERSPIEL4 -> {
                quickplayFilter = "PLACEHOlDERSPIEL4"
                MyApplication.globalSelectedGameStatLocation = "PLACEHOLDERSPIEL4"
            }
            GameNames.PLACEHOLDERSPIEL5 -> {
                quickplayFilter = "PLACEHOLDERSPIEL5"
                MyApplication.globalSelectedGameStatLocation = "PLACEHOLDERSPIEL5"
            }
            GameNames.TICTACTOE -> {
                quickplayFilter = "TICTACTOE"
                MyApplication.globalSelectedGameStatLocation = "TICTACTOE"
            }
            else -> Log.d(TAG, " ERROR: FAILED TO LOAD QUICKPLAY FILTER")
        }

        fun startGame(){
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
            //Generiere Raum Code
            MyApplication.code = SplitString(opponent)/*Guest Email*/ + SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Host Email*/
            MyApplication.isCodeMaker = true
            //Verlasse Quickplay Lobby
            MyApplication.myRef.child(lobbyName).child(quickplayFilter).setValue(null)
            MyApplication.onlineMode = true;
            //Markiere mich als Host im Raum
            MyApplication.myRef.child("data").child(MyApplication.code).child("Host").setValue(FirebaseAuth.getInstance().currentUser!!.email, { error, ref ->
                if (error == null) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").addValueEventListener ( object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
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
            MyApplication.onlineMode = true;
            //Merke Raum Code
            MyApplication.code =  SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Guest Email*/ + SplitString(opponent)/*Host Email*/
            MyApplication.isCodeMaker = false
            //Verlasse Quickplay Lobby
            MyApplication.myRef.child(lobbyName).child(quickplayFilter).setValue(null)
            //Markiere mich als Guest im Raum

            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").setValue(FirebaseAuth.getInstance().currentUser!!.email, { error, ref ->
                if (error == null) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Host").addValueEventListener (object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
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

        //Wenn jemand während des wartens in der Quickplay Lobby deine Request animmt, hoste spiel.
        if(MyApplication.isLoggedIn) {
            quickplayListener = MyApplication.myRef.child("Users")
                .child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null && snapshot.value != "" && host) {
                        networkHostGame(snapshot.value as String)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        fun createLobby(LobbyName: String){
            startLoad()
            //Hole die Liste von Spielern in der Quickplay Lobby
            MyApplication.myRef.child(LobbyName).child(quickplayFilter).get().addOnSuccessListener(this) {
                if(it.value != null){  //Falls es Spieler gibt...
                    //Heirate
                    MyApplication.myRef.child("Users").child(SplitString(it.value.toString())).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                    MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue(it.value)
                    //Join game
                    networkJoinGame(it.value.toString())
                    stopLoad()
                } else { //Falls es keine Spieler gibt, werde ein Host und warte in der Quickplay lobby
                    host = true
                    MyApplication.myRef.child(LobbyName).child(quickplayFilter).setValue(FirebaseAuth.getInstance().currentUser!!.email)
                }
            }
        }

        fun inviteFriend(lobbyName: String){
            //Send my friend a push notification with a random lobby code for our lobby :)
            MyApplication.myRef.child("MessagingTokens").child(MyApplication.inviteFriendID.toString()).get().addOnSuccessListener {
                if(it != null){
                    val id = it.value.toString()
                    val title = SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) + " has invited you to a game of " + quickplayFilter
                    val message = "Lobby Name: " + lobbyName
                    PushNotification(NotificationData(title, message), id).also { MyApplication.sendNotification(it) }
                }
            }
        }

        // Lobby w/Name
        binding.BtnOnlineRoomCode.setOnClickListener {
            if(MyApplication.isLoggedIn) {
                lobbyName = binding.InviteLobbyName.text.toString()
                if (MyApplication.inviteFriendID != "") {
                    inviteFriend(lobbyName)
                    MyApplication.inviteFriendID = ""
                    Toast.makeText(this, "Invited Friend!", Toast.LENGTH_SHORT).show()
                }
                createLobby(lobbyName)
            }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }


        binding.BtnOffline.setOnClickListener {
            MyApplication.onlineMode = false
            startGame()
        }

        binding.buttonreturn.setOnClickListener{
            finish()
        }

        binding.BtnQuickplay.setOnClickListener {
            if(MyApplication.isLoggedIn) {
                lobbyName = "Quickplay"
                createLobby(lobbyName)
            }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }

        //Verlasse Quickplay Lobby wenn man als Host Wartet
        binding.BtnCancel.setOnClickListener {
            if(MyApplication.isLoggedIn) {
                MyApplication.myRef.child(lobbyName).child(quickplayFilter).setValue(null)
                host = false
                stopLoad()
            }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }

    }

    //cant save @ as key in the database so this function returns only the first part of the email that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }

    fun startLoad() {
        binding.BtnOnlineRoomCode.visibility    = View.GONE
        binding.InviteLobbyName.visibility    = View.GONE
        binding.BtnOffline.visibility   = View.GONE
        binding.BtnQuickplay.visibility = View.GONE
        binding.buttonreturn.visibility = View.GONE
        binding.PBLoading.visibility    = View.VISIBLE
        binding.BtnCancel.visibility    = View.VISIBLE
    }

    fun stopLoad() {
        binding.BtnOnlineRoomCode.visibility    = View.VISIBLE
        binding.InviteLobbyName.visibility    = View.VISIBLE
        binding.BtnOffline.visibility   = View.VISIBLE
        binding.BtnQuickplay.visibility = View.VISIBLE
        binding.buttonreturn.visibility = View.VISIBLE
        binding.PBLoading.visibility    = View.GONE
        binding.BtnCancel.visibility    = View.GONE
    }

}