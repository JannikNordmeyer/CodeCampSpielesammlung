package com.example.testapplication

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameHolderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Integer.max

class GameHolder : AppCompatActivity() {

    private lateinit var binding: ActivityGameHolderBinding

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

    //Hilf Funktion welche Statistik aktualisiert
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

        //Je nach ausgewählten Spiel wird das Fragment zum laden ausgewählt und dessen Viewmodel
        when (MyApplication.globalSelectedGame) {
            GameNames.COMPASS -> {
                fragToLoad = Kompass()
                gameViewModel = ViewModelProvider(this).get(KompassViewModel()::class.java)
                viewmodel.quickplayFilter = "COMPASS"
            }
            GameNames.ARITHMETICS -> {
                fragToLoad = Arithmetics()
                gameViewModel = ViewModelProvider(this).get(ArithmeticsViewModel::class.java)
                viewmodel.quickplayFilter = "ARITHMETICS"
            }
            GameNames.SCHRITTZAEHLER -> {
                fragToLoad = Schrittzaehler()
                gameViewModel = ViewModelProvider(this).get(SchrittzaehlerViewModel::class.java)
                viewmodel.quickplayFilter = "SCHRITTZAEHLER"
            }
            GameNames.TICTACTOE -> {
                fragToLoad = TicTacToe()
                gameViewModel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)
                viewmodel.quickplayFilter = "TICTACTOE"
            }
        }

        //Lade Fragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.FrameLayoutGameHolder, fragToLoad)
            commit()
        }

        //Falls wir im online Mode sind, übernehme Spiel-unabhängiges Setup
        if (MyApplication.onlineMode && !MyApplication.networkSetupComplete) {
            //Hole und speichere Host/Guest Daten
            MyApplication.myRef.child("data").child(MyApplication.code).child("Host").get().addOnSuccessListener(this) {
                MyApplication.hostID = it.value.toString()
                if (MyApplication.isHost)
                    MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").setValue(MyApplication.hostID)
            }
            MyApplication.myRef.child("data").child(MyApplication.code).child("HostFC").get().addOnSuccessListener(this) {
                MyApplication.hostFriendID = it.value.toString()
            }

            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").get().addOnSuccessListener(this) {
                MyApplication.guestID = it.value.toString()
            }
            MyApplication.myRef.child("data").child(MyApplication.code).child("GuestFC").get().addOnSuccessListener(this) {
                MyApplication.guestFriendID = it.value.toString()
            }

            //Call networkSetup - je nach Spiel werden hier spielspezifische Sachen unternommen
            networkSetup(gameViewModel)

            //activePlayer Listener ändert je nach dem was in dem activePlayer Feld steht ob man lokal erlaubt ist einen Zug zu machen
            activePlayerListener = MyApplication.myRef.child("data").child(MyApplication.code).child("ActivePlayer").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null) {
                        val data_activePlayer = snapshot.value.toString()
                        if ((data_activePlayer == MyApplication.hostID) && MyApplication.isHost) MyApplication.myTurn = true
                        else MyApplication.myTurn = (data_activePlayer == MyApplication.guestID) && !MyApplication.isHost
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

            //Listener welche networkOnFieldUpdate() von dem geladenen Fragment callt und die flagge wieder zurück zu false setzt.
            //Benutzt um z.b. in TTT anzukündigen das man das Feld verändert hat.
            fieldUpdateListener = MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null && snapshot.value != false){
                        MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(false)
                        var data = snapshot.key
                        when (gameViewModel) {
                            is TicTacToeViewModel -> if (snapshot.value != "")(gameViewModel as TicTacToeViewModel).logic.networkOnFieldUpdate(data)
                            is KompassViewModel -> (gameViewModel as KompassViewModel).logic.networkOnFieldUpdate(data)
                            is SchrittzaehlerViewModel -> (gameViewModel as SchrittzaehlerViewModel).logic.networkOnFieldUpdate(data)
                            is ArithmeticsViewModel -> (gameViewModel as ArithmeticsViewModel).logic.networkOnFieldUpdate(data)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            //Listener für das Feld welches den Gewinner ankündigt. Zeigt beim erkennen ein Pop-Up welches den Gewinner deklariert und
            //erlaubt dem User entweder das Spiel zu verlassen oder ein Rematch zu verlangen.
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

                        //Beim rematch verlangen setzt der User eine Flagge auf dem Server und geht in den UI Lade-Modus während er auf
                        //den anderen Spieler wartet.
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
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
            //Der rematch listener betrachtet die rematch flagge falls man auf den zweiten Spieler wartet.
            //Diese initalisiert dann das setup für das nächste Spiel falls er zweite Spieler das rematch akzeptiert.
            remachtListener = MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        if (snapshot.value == false && MyApplication.isLoading) {
                            if (MyApplication.isHost && MyApplication.globalSelectedGame == GameNames.COMPASS) {
                                (gameViewModel as KompassViewModel).logic.initGame(this@GameHolder)
                            }
                            stopLoad()
                            networkSetup(gameViewModel)
                            MyApplication.isLoading = false
                            MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").removeValue()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            //Listener für die exitGame Flagge - verlässt ein Spieler das Spiel wird diese Flagge gesetzt und der andere User
            //reagiert auf das verlassen mit einem Pop-up.
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
                    override fun onCancelled(error: DatabaseError) {}
                    //endregion
                })
        }

        //Exit Game Button Listener - feuert Hilfsfunktion zum verlassen des Spieles.
        binding.ButtonGiveUp.setOnClickListener() {
            if(MyApplication.onlineMode){
                MyApplication.Ileft = true;
                exitGame()
            }
            finish()
        }

    }

    //Hilfsfunktion zum verlassen des Spieles - räumt Database auf.
    fun exitGame() {
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(MyApplication.guestID)).child("Request").setValue("")
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(MyApplication.hostID)).child("Request").setValue("")
        MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
        MyApplication.code = ""
    }

    //Spielabhängige setup funktion um die Spiele einzurichten bevor man spielt oder bei neustart (rematch)
    fun networkSetup(gameViewModel : ViewModel) {
        when (gameViewModel) {

            //TicTacToe Setup: Reset Field
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

                    if (!MyApplication.isHost) {
                        (gameViewModel).logic.player = "O"
                    }
                } else if (MyApplication.isLoading) {
                    gameViewModel.logic.networkBoardToLocalBoard()
                }
            }

            //Kompass Setup: initialisiere Spiel falls man Host ist
            is KompassViewModel -> {
                if (!MyApplication.networkSetupComplete || !MyApplication.isLoading) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").removeValue()
                    if (MyApplication.networkSetupComplete) {
                        if (MyApplication.isHost) {
                            gameViewModel.logic.initGame(this)
                        }
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Rematch").setValue(false)
                    }
                    MyApplication.networkSetupComplete = true
                }
            }

            //Arithmetics Setup: Reset Game und starte Timer
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

            //Schrittzaehler Setup: Reset Game
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
        }
    }

    //Hilf Funktionen fürs UI Hiding während warten
    fun startLoad() {
        binding.FrameLayoutGameHolder.visibility = View.GONE
        binding.idPB.visibility = View.VISIBLE
    }

    fun stopLoad() {
        binding.FrameLayoutGameHolder.visibility = View.VISIBLE
        binding.idPB.visibility = View.GONE
    }
}
