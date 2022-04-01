package com.example.testapplication

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject

class KompassViewModel(): ViewModel() {

    fun networkOnFieldUpdate(data : String?){
    }

    var logic = KompassLogic(this)
    var liveLocation:MutableLiveData<String?> = MutableLiveData<String?>()
    var livenetworkReset: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()

    var score : Float = 0.0f
    lateinit var targetLocation: JSONObject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var targetDirectionDegree: Double = 0.0
    lateinit var completionTimer: CountDownTimer
    var indexList: ArrayList<Int> = ArrayList()
    var listindex = 0
    var vibrateActive = true
    var running = true





}