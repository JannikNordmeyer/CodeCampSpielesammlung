package com.example.testapplication


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentKompassBinding
import com.google.android.gms.location.LocationServices
import org.json.JSONObject

import android.os.*
import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import kotlin.collections.ArrayList
import android.content.Intent

import android.content.DialogInterface
import android.provider.Settings
import java.lang.Exception
import kotlin.system.exitProcess


class Kompass : Fragment(), SensorEventListener, LocationListener {
    private val TAG = Kompass::class.java.simpleName

    private lateinit var binding: FragmentKompassBinding
    lateinit var viewmodel: KompassViewModel
    lateinit var goalAlert: AlertDialog

    lateinit var sensorManager: SensorManager
    lateinit var sensorAccelerometer: Sensor
    lateinit var sensorMagneticField: Sensor
    lateinit var mLocationManager: LocationManager


    private var floatGravity = FloatArray(3)
    private var floatGeoMagnetic = FloatArray(3)
    private var azimuth = 0f
    private var currentAzimuth = 0f
    lateinit var compass: ImageView
    lateinit var timer: CountDownTimer
    var vibrate = false
    var completionTime : Float = 0.0f
    var timerStarted = false

    override fun onDestroy() {
        super.onDestroy()
        viewmodel.completionTimer.cancel()
        timer.cancel()

        if(this::goalAlert.isInitialized){
            goalAlert.dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentKompassBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(KompassViewModel::class.java) //Shared Viewmodel w/ GameHolder
        //Set text
        viewmodel.liveLocation.observe(viewLifecycleOwner, {
            binding.idTarget.text = "Ort:\n"+viewmodel.liveLocation.value.toString()
        })

        compass = binding.compass
        sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mLocationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewmodel.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        viewmodel.indexList = ArrayList()
        viewmodel.targetLocation = JSONObject()

        //timer that triggers when phone is pointing towards the target
        timer = object : CountDownTimer(2000, 10) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                //adds the time it took to the total score
                viewmodel.score += completionTime

                //not last target
                if (viewmodel.listindex < viewmodel.indexList.size) {
                    //next api call
                    viewmodel.logic.apiCall(viewmodel.indexList[viewmodel.listindex], activity!!)
                    viewmodel.listindex++
                }
                //last target
                else {
                    //stops the completion timer
                    viewmodel.completionTimer.cancel()

                    //stops phone from vibrating after game is complete
                    viewmodel.vibrateActive = false
                    if (MyApplication.onlineMode) {
                        //online mode rematch/exit
                        winnerCheck()
                    } else {
                        //offline mode restart
                        resetGame()
                    }
                }
            }
        }

        //timer that sets the max amount of time you have for a single target
        viewmodel.completionTimer = object : CountDownTimer(30000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                //calculate passed time
                completionTime = 30000 - millisUntilFinished.toFloat()

                //string format the time to 3 digits after the .
                var time = (completionTime/1000).toString().split(".")
                var sec = time[0]
                var mili = time[1]
                if (mili.length == 1) {
                    mili += "00"
                } else if (mili.length == 2) {
                    mili += "0"
                }

                //update ui
                binding.idTimer.text = "Time:\n"+ sec +"."+ mili
            }

            override fun onFinish() {
                timer.cancel()
                //not last target
                if (viewmodel.listindex < viewmodel.indexList.size) {
                    //add the max time to the score
                    viewmodel.score += 30000

                    //next api call
                    viewmodel.logic.apiCall(viewmodel.indexList[viewmodel.listindex], activity!!)
                    viewmodel.listindex++
                }
                //last target
                else {
                    //add max time to the score
                    viewmodel.score += 30000

                    //stops phone from vibrating after game is complete
                    viewmodel.vibrateActive = false
                    if (MyApplication.onlineMode) {
                        //online mode reset/exit
                        winnerCheck()
                    } else {
                        //offline mode reset
                        resetGame()
                    }
                }
            }
        }

        //location request to make sure that the phone has a last location for getTargetDirection()
        var mLocationRequest = LocationRequest()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        //mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val mLocationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) { }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { }
        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(mLocationRequest, mLocationCallback, null)

        //initialize the game for the first time
        viewmodel.logic.initGame(activity!!)
        return view
    }

    fun winnerCheck() {
        //check if room exists
        MyApplication.myRef.child("data").child(MyApplication.code).get().addOnSuccessListener {
            if(it.value != null) {
                if(MyApplication.isHost) {
                    //write score to the database
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("HostScore").setValue(viewmodel.score)
                    //wait for guest to insert his score
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                var networkWinner = ""
                                if (viewmodel.score < snapshot.value.toString().toInt()) {
                                    networkWinner = MyApplication.hostID
                                } else if (viewmodel.score > snapshot.value.toString().toInt()) {
                                    networkWinner = MyApplication.guestID
                                } else networkWinner = "-1"  //Draw
                                //Enter Winner
                                MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(networkWinner)
                                MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").removeEventListener(this)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                } else {
                    //write your score to the database
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").setValue(viewmodel.score)
                    //wait for host to determine the winner
                }
            }
        }
    }

    fun resetGame() {
        //game over alert dialoge
        val build = AlertDialog.Builder(activity!!);
        build.setTitle("Game Over!")
        build.setMessage("You took "+viewmodel.score.toString()+" Seconds!")

        //restart option
        build.setPositiveButton("Restart") {dialog, which ->
            viewmodel.logic.initGame(activity!!)
        }
        build.setCancelable(false)
        goalAlert = build.show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val alpha = 0.97f
        synchronized(this) {
            if(event!!.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                floatGravity[0] = (alpha*floatGravity[0]+(1-alpha)*event!!.values[0])
                floatGravity[1] = (alpha*floatGravity[1]+(1-alpha)*event!!.values[1])
                floatGravity[2] = (alpha*floatGravity[2]+(1-alpha)*event!!.values[2])
            }

            if(event!!.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                floatGeoMagnetic[0] = (alpha*floatGeoMagnetic[0]+(1-alpha)*event!!.values[0])
                floatGeoMagnetic[1] = (alpha*floatGeoMagnetic[1]+(1-alpha)*event!!.values[1])
                floatGeoMagnetic[2] = (alpha*floatGeoMagnetic[2]+(1-alpha)*event!!.values[2])
            }

            var R = FloatArray(9)
            var I = FloatArray(9)
            var success = SensorManager.getRotationMatrix(R, I, floatGravity, floatGeoMagnetic)
            if(success) {
                var orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuth = (azimuth+360)%360

                var ani: RotateAnimation = RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                currentAzimuth = azimuth

                ani.duration = 500
                ani.repeatCount = 0
                ani.fillAfter = true

                compass.startAnimation(ani)

                //check for right direction
                if ((viewmodel.targetDirectionDegree - 5)%360 <= azimuth && azimuth <= (viewmodel.targetDirectionDegree + 5)%360) {
                    //start
                    if (!timerStarted) {
                        timer.start()
                        timerStarted = true
                    }

                } else {
                    //stop
                    timer.cancel()
                    timerStarted = false
                }

                if ((viewmodel.targetDirectionDegree - 90)%360 <= azimuth && azimuth <= (viewmodel.targetDirectionDegree + 90)%360) {
                    //start //vibrateTimer.start()
                    val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (!vibrate && viewmodel.vibrateActive) {
                        if (vibrator.hasVibrator()) { // Vibrator availability checking
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        500,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            }
                        }
                        vibrate = true
                    }
                } else {
                    vibrate = false
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onLocationChanged(location: Location) {
    }


}