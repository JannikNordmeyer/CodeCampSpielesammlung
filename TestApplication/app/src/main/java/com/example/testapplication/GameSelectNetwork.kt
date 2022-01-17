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

class GameSelectNetwork : AppCompatActivity() {
    //database instance
    private lateinit var binding: ActivityGameSelectNetworkBinding
    private var database= FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference
    var host : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select_network)

        binding = ActivityGameSelectNetworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TO-DO: Figure out what the fuck code and isCoderMaker are
        fun startGameOnline(opponent : String){
            val intent = Intent(this, OnlineMultiplayerGameActivity::class.java);
            MyApplication.code = SplitString(opponent)/*Guest Email*/ + SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Host Email*/
            MyApplication.isCodeMaker = true
            startActivity(intent)
            myRef.child("Quickplay").setValue(null)
            stopLoad()
        }

        fun startGameOffline(){
            val intent = Intent(this, GameHolder::class.java)
            startActivity(intent)
        }

        myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null && snapshot.value != "" && host)
                    startGameOnline(snapshot.value as String)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        binding.BtnOnline.setOnClickListener {
            //TO-DO: Set some settings for Online mode?

            //Old Code v v
            //val intent = Intent(this, OnlineCodeGeneratorActivity::class.java);
            //startActivity(intent)

            //TODO: Implement this? What string to feed?
            //startGameOnline()
        }

        binding.BtnOffline.setOnClickListener {
            //TO-DO: Set some settings for Offline mode?

            //Old Code v v
            //val intent = Intent(this, MainActivity::class.java);
            //startActivity(intent)

            startGameOffline()
        }

        binding.BtnFriend.setOnClickListener {
            //TO-DO: Set some settings for Friend mode?

            //Old Code v v
            //val intent = Intent(this, TicTacToeWithFriend::class.java);
            //startActivity(intent)

            //TODO: Implement this? What string to feed?
            //startGameOnline()
        }

        //TODO: Understand what the fuck is happening here
        binding.BtnQuickplay.setOnClickListener {
            startLoad()
            myRef.child("Quickplay").get().addOnSuccessListener {
                if(it.value != null){
                    myRef.child("Users").child(SplitString(it.value.toString())).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                    myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue(it.value)
                    val intent = Intent(this, OnlineMultiplayerGameActivity::class.java);
                    MyApplication.code =  SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) /*Guest Email*/ + SplitString(it.value.toString())/*Host Email*/
                    MyApplication.isCodeMaker = false
                    startActivity(intent)
                    myRef.child("Quickplay").setValue(null)
                    stopLoad()
                } else {
                    host = true
                    myRef.child("Quickplay").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                }

            }
        }

        binding.BtnCancel.setOnClickListener {
            myRef.child("Quickplay").setValue(null)
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