package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.google.firebase.auth.FirebaseAuth


class GameSelect : AppCompatActivity() {

    private lateinit var binding: ActivityGameSelectBinding
    private val TAG: String = GameSelect::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

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

            //val intent = Intent(this, TicTacToe::class.java);
            //startActivity(intent)

            Log.d(TAG,"TEST TEST TEST")

            val intent = Intent(this, GameHolder::class.java)
            startActivity(intent)


        }

        binding.loginbutton.setOnClickListener(){

            val intent = Intent(this, Login::class.java);
            startActivity(intent)

        }

    }
}