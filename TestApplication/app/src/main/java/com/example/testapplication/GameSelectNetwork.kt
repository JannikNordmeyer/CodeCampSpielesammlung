package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameSelectNetworkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GameSelectNetwork : AppCompatActivity() {
    private lateinit var binding: ActivityGameSelectNetworkBinding
    var host : Boolean = false
    private val TAG = GameSelectNetwork::class.java.simpleName

    private lateinit var quickplayListener: ValueEventListener
    lateinit var viewmodel: GameSelectNetworkViewModel

    //Cleanup bei verlassen
    override fun onDestroy() {
        super.onDestroy()
        if(MyApplication.isLoggedIn) {
            //Unsub listener
            if (this::quickplayListener.isInitialized) {
                MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").removeEventListener(quickplayListener)
            }

            //close lobby if we kill app while running
            if(viewmodel.lobbyName != "" && host){
                if(viewmodel.lobbyName.equals("Quickplay")){
                    MyApplication.myRef.child(viewmodel.lobbyName).get().addOnSuccessListener {
                        for (child in it.children) {
                            if (child.value == viewmodel.quickplayName) {
                                MyApplication.myRef.child(viewmodel.lobbyName).child(child.key!!).removeValue()
                            }
                        }
                    }
                }
                else MyApplication.myRef.child(viewmodel.lobbyName).child(viewmodel.quickplayFilter).removeValue()
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

        viewmodel = ViewModelProvider(this).get(GameSelectNetworkViewModel()::class.java)

        if(!MyApplication.isLoggedIn){
            fadeOutButtons()
        }

        //Select Quickplay Filter
        when (MyApplication.globalSelectedGame) {
            GameNames.COMPASS -> {
                viewmodel.quickplayFilter = "COMPASS"
                MyApplication.globalSelectedGameStatLocation = "COMPASS"
            }
            GameNames.ARITHMETICS -> {
                viewmodel.quickplayFilter = "ARITHMETICS"
                MyApplication.globalSelectedGameStatLocation = "ARITHMETICS"
            }
            GameNames.SCHRITTZAEHLER -> {
                viewmodel.quickplayFilter = "SCHRITTZAEHLER"
                MyApplication.globalSelectedGameStatLocation = "SCHRITTZAEHLER"
            }
            GameNames.TICTACTOE -> {
                viewmodel.quickplayFilter = "TICTACTOE"
                MyApplication.globalSelectedGameStatLocation = "TICTACTOE"
            }
            else -> Log.d(TAG, " ERROR: FAILED TO LOAD QUICKPLAY FILTER")
        }

        //Hilfsfunktion zum Spiele starten - ??bergeht zum GameHolder und leistet Setup arbeit
        fun startGame(){
            if(MyApplication.onlineMode) {
                host = false
                viewmodel.quickplayName = ""
                viewmodel.lobbyName = ""
                MyApplication.myTurn = MyApplication.isHost
                MyApplication.networkSetupComplete = false
                MyApplication.Ileft = false;
            }
            val intent = Intent(this, GameHolder::class.java)
            startActivity(intent)
        }

        //Hilfsfunktion zum Hosten von Spielen wenn jemand eine Lobby betritt
        fun networkHostGame(opponent : String){
            //Generiere Raum Code
            MyApplication.code = MyApplication.SplitString(opponent)/*Guest Email*/ + MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Host Email*/
            MyApplication.isHost = true
            //Verlasse Lobby
            MyApplication.myRef.child(viewmodel.lobbyName).child(viewmodel.quickplayFilter).setValue(null)
            MyApplication.onlineMode = true;
            //Markiere mich als Host im Raum + notiere meine FriendID
            MyApplication.myRef.child("data").child(MyApplication.code).child("HostFC").setValue(FirebaseAuth.getInstance().currentUser!!.uid)
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
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            })
        }

        //Hilfsfunktion zum beitreten eines Spieles nachdem man jemanden in einer Lobby gefunden hat
        fun networkJoinGame(opponent : String){
            MyApplication.onlineMode = true;
            //Merke Raum Code
            MyApplication.code =  MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Guest Email*/ + MyApplication.SplitString(opponent)/*Host Email*/
            MyApplication.isHost = false
            //Verlasse Quickplay Lobby
            MyApplication.myRef.child(viewmodel.lobbyName).child(viewmodel.quickplayFilter).setValue(null)
            //Markiere mich als Guest im Raum + notiere meine FriendID
            MyApplication.myRef.child("data").child(MyApplication.code).child("GuestFC").setValue(FirebaseAuth.getInstance().currentUser!!.uid)
            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").setValue(FirebaseAuth.getInstance().currentUser!!.email, { error, ref ->
                if (error == null) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Host").addValueEventListener (object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                startGame()
                                MyApplication.myRef.child("data").child(MyApplication.code).child("Host").removeEventListener(this)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            })
        }

        //Wenn jemand w??hrend des wartens in der Quickplay Lobby deine Request animmt, hoste spiel.
        if(MyApplication.isLoggedIn) {
            quickplayListener = MyApplication.myRef.child("Users")
                .child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null && snapshot.value != "" && host) {
                        networkHostGame(snapshot.value as String)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        //Hilfsfunktion zum erschaffen einer Lobby bzw. dem beitreten einer Lobby falls sie schon existiert
        fun createOrJoinLobby(LobbyName: String){
            startLoad()
            //Hole die Liste von Spielern in der Lobby
            MyApplication.myRef.child(LobbyName).child(viewmodel.quickplayFilter).get().addOnSuccessListener(this) {
                if(it.value != null){  //Falls es Spieler gibt...
                    //Heirate
                    MyApplication.myRef.child("Users").child(MyApplication.SplitString(it.value.toString())).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                    MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue(it.value)
                    //Join game
                    networkJoinGame(it.value.toString())
                    stopLoad()
                } else { //Falls es keine Spieler gibt, werde ein Host und warte in der lobby
                    host = true
                    viewmodel.quickplayName = FirebaseAuth.getInstance().currentUser!!.email.toString()
                    MyApplication.myRef.child(LobbyName).child(viewmodel.quickplayFilter).setValue(viewmodel.quickplayName)
                }
            }
        }

        //Hilfsfunktion zum "Einladen" eines Freundes - sendet Notifikation f??r Spiel und Lobby Name.
        fun inviteFriend(lobbyName: String){
            MyApplication.myRef.child("MessagingTokens").child(MyApplication.inviteFriendID.toString()).get().addOnSuccessListener {
                if(it != null){
                    val id = it.value.toString()
                    val title = MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) + " has invited you to a game of " + viewmodel.quickplayFilter
                    val message = "Lobby Name: " + lobbyName
                    PushNotification(NotificationData(title, message), id).also { MyApplication.sendNotification(it) }
                }
            }
        }

        //Button Listeners:
        // Erschafft oder tritt Lobby bei
        binding.BtnOnlineRoomCode.setOnClickListener {
            if(MyApplication.isLoggedIn) {
                if(binding.InviteLobbyName.text.isNotEmpty()) {
                    viewmodel.lobbyName = binding.InviteLobbyName.text.toString()
                    if (MyApplication.inviteFriendID != "") {
                        inviteFriend(viewmodel.lobbyName)
                        MyApplication.inviteFriendID = ""
                        Toast.makeText(this, "Invited Friend!", Toast.LENGTH_SHORT).show()
                    }
                    createOrJoinLobby(viewmodel.lobbyName)
                }
                else{
                    Toast.makeText(this, "Please enter a valid lobby name.", Toast.LENGTH_SHORT ).show()
                }
            }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }

        //Startet ein Offline Spiel.
        binding.BtnOffline.setOnClickListener {
            MyApplication.onlineMode = false
            startGame()
        }

        //Geht zur??ck zum Game Select Bildschirm
        binding.buttonreturn.setOnClickListener{
            finish()
        }

        //??ffnet eine Quickplay Lobby
        binding.BtnQuickplay.setOnClickListener {
            if(MyApplication.isLoggedIn) {
                viewmodel.lobbyName = "Quickplay"
                createOrJoinLobby(viewmodel.lobbyName)
            }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }

        //Button zum verlassen einer Lobby falls man wartet
        binding.BtnCancel.setOnClickListener {
            MyApplication.myRef.child(viewmodel.lobbyName).child(viewmodel.quickplayFilter).setValue(null)
            host = false
            viewmodel.quickplayName = ""
            stopLoad()
            viewmodel.lobbyName = ""
        }

    }

    //UI Hilfsfunktionen f??r das warten auf andere Spieler wenn man hostet
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