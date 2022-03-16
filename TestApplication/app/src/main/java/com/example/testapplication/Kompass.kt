package com.example.testapplication



/*import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView*/

//import com.esri.arcgisruntime.toolkit.compass.Compass
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

    //private val fragmentPlaceholderspiel1Binding by lazy {        FragmentPlaceholderspiel1Binding.inflate(layoutInflater)    }

    //private val mapView: MapView by lazy { fragmentPlaceholderspiel1Binding.mapView    }

    private lateinit var binding: FragmentKompassBinding
    lateinit var viewmodel: KompassViewModel
    lateinit var goalAlert: AlertDialog

    lateinit var targetList: JSONArray

    lateinit var sensorManager: SensorManager
    lateinit var sensorAccelerometer: Sensor
    lateinit var sensorMagneticField: Sensor
    lateinit var mLocationManager: LocationManager


    private var floatGravity = FloatArray(3)
    private var floatGeoMagnetic = FloatArray(3)
    private var azimuth = 0f
    private var currentAzimuth = 0f
    lateinit var compass: ImageView
    lateinit var lastLocation: Location
    private var gps_enabled = false
    private var network_enabled = false
    lateinit var timer: CountDownTimer
    lateinit var vibrateTimer: CountDownTimer
    var vibTimerRunning = false
    var completionTime : Float = 0.0f
    var timerStarted = false

    override fun onDestroy() {
        super.onDestroy()
        viewmodel.completionTimer.cancel()
        timer.cancel()
        vibrateTimer.cancel()
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
        targetList = JSONArray()
        viewmodel.logic.indexList = ArrayList()
        viewmodel.targetLocation = JSONObject()
        timer = object : CountDownTimer(2000, 10) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                viewmodel.score += completionTime
                if (viewmodel.logic.listindex < viewmodel.logic.indexList.size) {
                    Log.d("Kompass", "Listindex: ${viewmodel.logic.listindex}")
                    viewmodel.apiCall(viewmodel.logic.indexList[viewmodel.logic.listindex], activity!!)
                    //viewmodel.getTargetDirection(activity!!)
                    Log.d("Kompass", "TIMER FINISH: POINTED AT NOT LAST")
                    viewmodel.logic.listindex++
                    Log.d("Kompass", "LISTINDEX INCREASED")
                } else {
                    viewmodel.completionTimer.cancel()
                    Log.d("Kompass", "TIMER FINISH: POINTED AT LAST")
                    if (MyApplication.onlineMode) {
                        winnerCheck()
                    } else {
                        resetGame()
                    }
                }
            }
        }

        viewmodel.completionTimer = object : CountDownTimer(10000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                completionTime = 10000 - millisUntilFinished.toFloat()
                var time = (completionTime/1000).toString().split(".")
                var sec = time[0]
                var mili = time[1]
                if (mili.length == 1) {
                    mili += "00"
                } else if (mili.length == 2) {
                    mili += "0"
                }
                binding.idTimer.text = "Time:\n"+ sec +"."+ mili
            }

            override fun onFinish() {
                timer.cancel()
                if (viewmodel.logic.listindex < viewmodel.logic.indexList.size) {
                    viewmodel.score += 10000
                    Log.d("Kompass", "Listindex: ${viewmodel.logic.listindex}")
                    viewmodel.apiCall(viewmodel.logic.indexList[viewmodel.logic.listindex], activity!!)
                    //viewmodel.getTargetDirection(activity!!)
                    Log.d("Kompass", "TIMER FINISH: OUT OF TIME NOT LAST")
                    viewmodel.logic.listindex++
                    Log.d("Kompass", "LISTINDEX INCREASED")

                    //Toast.makeText(context, "OUT OF TIME!", Toast.LENGTH_SHORT).show()
                } else {
                    viewmodel.score += 10000
                    Log.d("Kompass", "TIMER FINISH: OUT OF TIME LAST")
                    if (MyApplication.onlineMode) {
                        winnerCheck()
                    } else {
                        resetGame()
                    }
                }
            }
        }

        vibrateTimer = object : CountDownTimer(1000, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) { // Vibrator availability checking
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                    }
                }
                vibrateTimer.start()
            }

        }

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
                    if (location != null) {
                        //TODO: UI updates.
                    }
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
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(mLocationRequest, mLocationCallback, null)

        viewmodel.initGame(activity!!)
        return view
    }

    fun winnerCheck() {
        //Prüfe ob Raum existiert...
        MyApplication.myRef.child("data").child(MyApplication.code).get().addOnSuccessListener {
            if(it.value != null) {
                if(MyApplication.isCodeMaker) {
                    //Schreib deinen score in die DB...
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("HostScore").setValue(viewmodel.score)
                    //Warte darauf das Guest seinen Score einträgt...
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

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                } else {
                    //Schreib deinen score in die DB...
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").setValue(viewmodel.score)
                    //Warte darauf das Host entscheidet wer gewonnen hat
                }
            }
        }
    }

    fun resetGame() {
        val build = AlertDialog.Builder(activity!!);
        build.setTitle("Goal reached!")
        build.setMessage("You took "+viewmodel.score.toString()+" Seconds!")
        build.setPositiveButton("Restart") {dialog, which ->
            viewmodel.initGame(activity!!)
        }
        build.setCancelable(false)
        goalAlert = build.show()
    }






    private fun setApiKeyForApp(){
        // set your API key
        // Note: it is not best practice to store API keys in source code. The API key is referenced
        // here for the convenience of this tutorial.

        //ArcGISRuntimeEnvironment.setApiKey("AAPKa5c9d216029045fd884dc4daf9ed63555-Hc8HL0cr6zUfmclNZ1ZN4k_-_slINrK0IQjgPlN2x9jaJUNUhCZ6COFv_xxd55")

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
                    //start
                    if (!vibTimerRunning) {
                        //vibrateTimer.start()
                        val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (vibrator.hasVibrator()) { // Vibrator availability checking
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                            }
                        }
                        vibTimerRunning = true
                    }
                } else {
                    //stop
                    //vibrateTimer.cancel()
                    vibTimerRunning = false
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