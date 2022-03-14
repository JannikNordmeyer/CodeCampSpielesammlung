package com.example.testapplication

import android.content.Intent
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.testapplication.MyApplication.Companion.FRIENDS_TOPIC
import com.example.testapplication.MyApplication.Companion.sendNotification
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging


class GameSelect : AppCompatActivity() {

    private val TAG = GameSelect::class.java.simpleName
    private lateinit var binding: ActivityGameSelectBinding

    fun startGame(){
        val intent = Intent(this, GameSelectNetwork::class.java)   //Previously went to GameHolder
        startActivity(intent)
    }

    fun fadeOutButtons(){
        binding.FriendsButton.alpha = 0.5F
        binding.statsButton.alpha = 0.5F
    }

    fun fadeInButtons(){
        binding.FriendsButton.alpha = 1F
        binding.statsButton.alpha = 1F
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        binding = ActivityGameSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get reference to database once on game launch
        MyApplication.database = FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
        MyApplication.myRef = MyApplication.database.reference;

        FirebaseMessaging.getInstance().subscribeToTopic(FRIENDS_TOPIC)

        var currentuser = FirebaseAuth.getInstance().currentUser
        if(currentuser != null) {
            MyApplication.isLoggedIn = true
            fadeInButtons()
            binding.TextViewLoginStatus.setText("Logged in as " + currentuser.email)
        }
        else {
            MyApplication.isLoggedIn = false
            fadeOutButtons()
        }

        binding.ButtonLogout.setOnClickListener(){
            FirebaseAuth.getInstance().signOut()
            currentuser = null
            MyApplication.isLoggedIn = false
            fadeOutButtons()
            binding.TextViewLoginStatus.setText("You are not currently logged in.")
        }

        binding.statsButton.setOnClickListener(){
            if(MyApplication.isLoggedIn) {
                val intent = Intent(this, Statistics::class.java);
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }
        binding.ButtonTicTacToe.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.TICTACTOE
            startGame()
        }

        binding.ButtonPlaceholderSpiel1.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.COMPASS
            startGame()
        }

        binding.ButtonPlaceholderSpiel2.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.ARITHMETICS
            startGame()
        }

        binding.ButtonPlaceholderSpiel3.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.SCHRITTZAEHLER
            startGame()
        }

        binding.ButtonLogin.setOnClickListener(){
            val intent = Intent(this, Login::class.java);
            startActivity(intent)
        }

        binding.FriendsButton.setOnClickListener(){
            if(MyApplication.isLoggedIn) {
                val intent = Intent(this, FriendsList::class.java);
                startActivity(intent)
                }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }
    }
}