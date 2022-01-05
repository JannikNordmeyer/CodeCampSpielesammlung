package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference





class GameSelect : AppCompatActivity() {

    private var mAuth:FirebaseAuth?=null
    private var database= FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference

    private lateinit var binding: ActivityGameSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)
        mAuth = FirebaseAuth.getInstance()

        binding = ActivityGameSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var currentuser = FirebaseAuth.getInstance().currentUser
        if(currentuser != null) {
            binding.logstatus.setText("Logged in as " + currentuser.email)
        }



        binding.buttonlogout.setOnClickListener(){

            FirebaseAuth.getInstance().signOut()
            binding.logstatus.setText("You are not currently logged in.")

        }

        binding.TicTacToeButton.setOnClickListener(){
            val intent = Intent(this, MultiplayerGameSelection::class.java)
            startActivity(intent)

        }

        binding.loginbutton.setOnClickListener(){

            val intent = Intent(this, Login::class.java)
            startActivity(intent)

        }




    }
}