package com.example.testapplication

import android.app.Application
import android.hardware.Sensor
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.lang.Exception

//Globale Kontroll Variablen. Sollten (Hoffe ich) nicht bei Application Death sterben ähnlich wie Zeug im Viewmodel.
class MyApplication : Application() {
    companion object {
        var globalSelectedGame = GameNames.NONE         //The selected game
        var globalSelectedGameStatLocation = ""

        var isLoggedIn = false;                         //Global control variable that checks if the user is logged in.

        var isHost = true;                         //If the player made the code - aka if they are the host, Player 1.
        var code = "null"                               //"Room" Code
        var onlineMode = false;                         //If the game is being played in online mode
        var myTurn = false;                             //If, regardless of game, the local player can make a move.
        var hostID = "null"
        var hostFriendID = ""
        var guestFriendID = ""
        var guestID = "null"
        var networkSetupComplete = false;
        var isLoading = false
        var Ileft = false;

        var inviteFriendID = ""

        lateinit var database: FirebaseDatabase
        lateinit var myRef: DatabaseReference

        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = "AAAAj84vqms:APA91bECWo2WTCuaazKQixrRsjDKJFbiHcSskAK-JNBZgXBmPeeHqCbIutctvGqJokmb9Fes3Xjv-bOPZSJRJ3IXAmTcEDICYvH0VX28JbmLkJRSOchhOfyc3jzwgER3U6wLhDcV9qrt"
        const val CONTENT_TYPE = "application/json"
        const val FRIENDS_TOPIC = "/topics/Friends"

        fun SplitString(str:String): String{
            var split=str.split("@")
            return split[0]
        }

        fun sendNotification(notification : PushNotification) = CoroutineScope(Dispatchers.IO).launch {
            try {
                var response = RetrofitInstance.api.postNotification(notification)
            }catch (e : Exception){}
        }
    }


}