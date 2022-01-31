package com.example.testapplication

import android.app.Application
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

//Globale Kontroll Variablen. Sollten (Hoffe ich) nicht bei Application Death sterben ähnlich wie Zeug im Viewmodel.
class MyApplication : Application() {
    companion object {
        var globalSelectedGame = GameNames.NONE         //The selected game

        //TODO: Rename isCodeMaker to isHost, but only do that after deleting the old files

        var isCodeMaker = true;                         //If the player made the code - aka if they are the host, Player 1.
        var code = "null"                               //"Room" Code
        var codeFound = false
        var checkTemp = true
        var keyValue : String = "null"
        var onlineMode = false;                         //If the game is being played in online mode
        var myTurn = false;                             //If, regardless of game, the local player can make a move.
        var hostID = "null"
        var guestID = "null"
        var networkSetupComplete = false;
        var isLoading = false

        lateinit var database: FirebaseDatabase
        lateinit var myRef: DatabaseReference
    }
}