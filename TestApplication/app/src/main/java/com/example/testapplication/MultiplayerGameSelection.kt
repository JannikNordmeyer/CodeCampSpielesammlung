package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MultiplayerGameSelection : AppCompatActivity() {
    //database instance
    private var database= FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference

    lateinit var onlineBtn : Button
    lateinit var offlineBtn : Button
    lateinit var onlineFriendBtn : Button
    lateinit var quickplayBtn : Button
    lateinit var cancelBtn : Button
    lateinit var headTV : TextView
    lateinit var loadingPB : ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_game_selection)
        onlineBtn = findViewById(R.id.idBtnOnline)
        offlineBtn = findViewById(R.id.idBtnOffline)
        onlineFriendBtn = findViewById(R.id.idBtnFriend)
        quickplayBtn = findViewById(R.id.idBtnQuickplay)
        cancelBtn = findViewById(R.id.idBtnCancel)
        headTV = findViewById(R.id.idTVHead)
        loadingPB = findViewById(R.id.idPBLoading)

        onlineBtn.setOnClickListener {
            val intent = Intent(this, OnlineCodeGeneratorActivity::class.java);
            startActivity(intent)
        }

        offlineBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent)
        }

        onlineFriendBtn.setOnClickListener {
            val intent = Intent(this, TicTacToeWithFriend::class.java);
            startActivity(intent)
        }

        myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null && snapshot.value != "")
                    startGame(snapshot.value as String)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        quickplayBtn.setOnClickListener {
            startLoad()
            myRef.child("Quickplay").get().addOnSuccessListener {
                if(it.value != null){
                    myRef.child("Users").child(SplitString(it.value.toString())).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                    myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue(it.value)
                    val intent = Intent(this, OnlineMultiplayerGameActivity::class.java);
                    code =  SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Guest Email*/ + SplitString(it.value.toString())/*Host Email*/
                    isCodeMaker = false
                    startActivity(intent)
                    myRef.child("Quickplay").setValue(null)
                    stopLoad()
                } else {
                    myRef.child("Quickplay").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                }

            }
        }

        cancelBtn.setOnClickListener {
            myRef.child("Quickplay").setValue(null)
            stopLoad()
        }

    }

    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }

    fun startLoad() {
        onlineBtn.visibility = View.GONE
        onlineFriendBtn.visibility = View.GONE
        offlineBtn.visibility = View.GONE
        quickplayBtn.visibility = View.GONE
        headTV.visibility = View.GONE
        loadingPB.visibility = View.VISIBLE
        cancelBtn.visibility = View.VISIBLE
    }

    fun stopLoad() {
        onlineBtn.visibility = View.VISIBLE
        onlineFriendBtn.visibility = View.VISIBLE
        offlineBtn.visibility = View.VISIBLE
        quickplayBtn.visibility = View.VISIBLE
        headTV.visibility = View.VISIBLE
        loadingPB.visibility = View.GONE
        cancelBtn.visibility = View.GONE
    }

    fun startGame(opponent : String){
        val intent = Intent(this, OnlineMultiplayerGameActivity::class.java);
        code = SplitString(opponent)/*Guest Email*/ + SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Host Email*/
        isCodeMaker = true
        startActivity(intent)
        myRef.child("Quickplay").setValue(null)
        stopLoad()
    }
}