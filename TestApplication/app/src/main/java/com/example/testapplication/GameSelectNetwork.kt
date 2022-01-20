package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.example.testapplication.databinding.ActivityGameSelectNetworkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//TODO: Database sollte einen entry haben für welches Spiel man überhaupt spielt für verschiende Queues. Benutze die selectedGames Konstante dafür!

class GameSelectNetwork : AppCompatActivity() {
    private lateinit var binding: ActivityGameSelectNetworkBinding
    var host : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select_network)

        binding = ActivityGameSelectNetworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fun startGame(){
            val intent = Intent(this, GameHolder::class.java)
            startActivity(intent)
        }

        fun networkHostGame(opponent : String){
            //Generiere Raum Code
            MyApplication.code = SplitString(opponent)/*Guest Email*/ + SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Host Email*/
            MyApplication.isCodeMaker = true
            //Verlasse Quickplay Lobby
            MyApplication.myRef.child("Quickplay").setValue(null)
            MyApplication.onlineMode = true;
            //Markiere mich als Host im Raum
            MyApplication.myRef.child("data").child(MyApplication.code).child("Host").setValue(FirebaseAuth.getInstance().currentUser!!.email)
            startGame()
            stopLoad()
        }

        fun networkJoinGame(opponent : String){
            MyApplication.onlineMode = true;
            //Merke Raum Code
            MyApplication.code =  SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Guest Email*/ + SplitString(opponent)/*Host Email*/
            MyApplication.isCodeMaker = false
            //Verlasse Quickplay Lobby
            MyApplication.myRef.child("Quickplay").setValue(null)
            //Markiere mich als Guest im Raum
            MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").setValue(FirebaseAuth.getInstance().currentUser!!.email)
            startGame()
        }

        //Wenn jemand während des wartens in der Quickplay Lobby deine Request animmt, hoste spiel.
        MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null && snapshot.value != "" && host)
                    networkHostGame(snapshot.value as String)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //region TODO: Was ist das? Ist das nicht einfach nur Quickplay?
        binding.BtnOnline.setOnClickListener {

            //Old Code v v
            //val intent = Intent(this, OnlineCodeGeneratorActivity::class.java);
            //startActivity(intent)

            //startGameOnline()
        }
        //endregion

        binding.BtnOffline.setOnClickListener {
            MyApplication.onlineMode = false
            startGame()
        }

        //region TODO: Was ist überhaupt dein TicTacToeWithFriend Ding? Deprecated?
        binding.BtnFriend.setOnClickListener {

            //Old Code v v
            //val intent = Intent(this, TicTacToeWithFriend::class.java);
            //startActivity(intent)

            //startGameOnline()
        }
        //endregion

        binding.BtnQuickplay.setOnClickListener {
            startLoad()
            //Hole die Liste von Spielern in der Quickplay Lobby
            MyApplication.myRef.child("Quickplay").get().addOnSuccessListener {
                if(it.value != null){  //Falls es Spieler gibt...
                    //Heirate
                    MyApplication.myRef.child("Users").child(SplitString(it.value.toString())).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                    MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue(it.value)
                    //Join game
                    networkJoinGame(it.value.toString())
                    stopLoad()
                } else { //Falls es keine Spieler gibt, werde ein Host und warte in der Quickplay lobby
                    host = true
                    MyApplication.myRef.child("Quickplay").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                }

            }
        }

        //Verlasse Quickplay Lobby wenn man als Host Wartet
        binding.BtnCancel.setOnClickListener {
            MyApplication.myRef.child("Quickplay").setValue(null)
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
        binding.TVHead.visibility       = View.GONE
        binding.PBLoading.visibility    = View.VISIBLE
        binding.BtnCancel.visibility    = View.VISIBLE
    }

    fun stopLoad() {
        binding.BtnOnline.visibility    = View.VISIBLE
        binding.BtnFriend.visibility    = View.VISIBLE
        binding.BtnOffline.visibility   = View.VISIBLE
        binding.BtnQuickplay.visibility = View.VISIBLE
        binding.TVHead.visibility       = View.VISIBLE
        binding.PBLoading.visibility    = View.GONE
        binding.BtnCancel.visibility    = View.GONE
    }

}