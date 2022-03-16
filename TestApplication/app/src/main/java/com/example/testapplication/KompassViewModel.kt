package com.example.testapplication

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class KompassViewModel(): ViewModel() {

    fun networkOnFieldUpdate(data : String?){
        //TODO: Update Field with data received...
    }

    var logic = KompassLogic(this)
    var liveLocation:MutableLiveData<String?> = MutableLiveData<String?>()

    var score : Float = 0.0f
    lateinit var targetLocation: JSONObject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var targetDirectionDegree: Double = 0.0
    lateinit var completionTimer: CountDownTimer
    var indexList: ArrayList<Int> = ArrayList()
    var listindex = 0
    var vibrateActive = true





}