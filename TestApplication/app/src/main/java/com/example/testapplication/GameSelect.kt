package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class GameSelect : AppCompatActivity() {

    private lateinit var binding: ActivityGameSelectBinding

    fun startGame(){
        val intent = Intent(this, GameSelectNetwork::class.java)   //Previously went to GameHolder
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        binding = ActivityGameSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get reference to database once on game launch
        MyApplication.database = FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
        MyApplication.myRef = MyApplication.database.reference;

        var currentuser = FirebaseAuth.getInstance().currentUser
        if(currentuser != null) {
            binding.TextViewLoginStatus.setText("Logged in as " + currentuser.email)
        }

        binding.ButtonLogout.setOnClickListener(){
            FirebaseAuth.getInstance().signOut()
            binding.TextViewLoginStatus.setText("You are not currently logged in.")
        }

        binding.ButtonTicTacToe.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.TICTACTOE
            startGame()
        }

        binding.ButtonPlaceholderSpiel1.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.PLACEHOLDERSPIEL1
            startGame()
        }

        binding.ButtonPlaceholderSpiel2.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.PLACEHOLDERSPIEL2
            startGame()
        }

        binding.ButtonPlaceholderSpiel3.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.PLACEHOLDERSPIEL3
            startGame()
        }

        binding.ButtonPlaceholderSpiel4.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.PLACEHOLDERSPIEL4
            startGame()
        }

        binding.ButtonPlaceholderSpiel5.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.PLACEHOLDERSPIEL5
            startGame()
        }

        binding.ButtonLogin.setOnClickListener(){

            val intent = Intent(this, Login::class.java);
            startActivity(intent)

        }

    }
}