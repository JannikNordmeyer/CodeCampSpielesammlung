package com.example.testapplication

import android.app.Application
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

//Globale Kontrollvariablen
class MyApplication : Application() {
    companion object {
        var globalSelectedGame = GameNames.NONE         //Global ausgewähltes Spiel
        var globalSelectedGameStatLocation = ""

        var isLoggedIn = false;                         //Kontrollvariable um zu checken ob der User eingeloggt ist

        var isHost = true;                              //Kontrollvariable um zu schauen ob man Host ist
        var code = "null"                               //"Room" Code
        var onlineMode = false;
        var myTurn = false;
        var hostID = "null"
        var hostFriendID = ""
        var guestFriendID = ""
        var guestID = "null"
        var networkSetupComplete = false;
        var isLoading = false
        var Ileft = false;

        var ticTacToeOpen = false                       //Kontrollvariable um zu prüfen ob man TTT offen hat - für TTT-spezifische Push Notes

        var inviteFriendID = ""

        lateinit var database: FirebaseDatabase
        lateinit var myRef: DatabaseReference

        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = "AAAAj84vqms:APA91bECWo2WTCuaazKQixrRsjDKJFbiHcSskAK-JNBZgXBmPeeHqCbIutctvGqJokmb9Fes3Xjv-bOPZSJRJ3IXAmTcEDICYvH0VX28JbmLkJRSOchhOfyc3jzwgER3U6wLhDcV9qrt"
        const val CONTENT_TYPE = "application/json"
        const val FRIENDS_TOPIC = "/topics/Friends"

        //Globale Hilfsfunktion zum splitten von email addressen
        fun SplitString(str:String): String{
            var split=str.split("@")
            return split[0]
        }

        //Globale Hilfsfunktion zum senden von Notifications
        fun sendNotification(notification : PushNotification) = CoroutineScope(Dispatchers.IO).launch {
            try {
                var response = RetrofitInstance.api.postNotification(notification)
            }catch (e : Exception){}
        }
    }


}