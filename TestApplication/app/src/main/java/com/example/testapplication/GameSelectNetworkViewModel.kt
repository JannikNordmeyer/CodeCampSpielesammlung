package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ValueEventListener

class GameSelectNetworkViewModel : ViewModel(){
    var quickplayFilter = ""
    var lobbyName = ""
    var quickplayName = ""
}