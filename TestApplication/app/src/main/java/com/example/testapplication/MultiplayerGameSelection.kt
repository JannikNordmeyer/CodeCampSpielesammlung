package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MultiplayerGameSelection : AppCompatActivity() {
    lateinit var onlineBtn : Button
    lateinit var offlineBtn : Button
    lateinit var onlineFriendBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_game_selection)
        onlineBtn = findViewById(R.id.idBtnOnline)
        offlineBtn = findViewById(R.id.idBtnOffline)
        onlineFriendBtn = findViewById(R.id.idBtnFriend)

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
    }
}