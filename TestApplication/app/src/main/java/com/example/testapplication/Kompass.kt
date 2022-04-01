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
import kotlin.collections.ArrayList


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

    //UI-Hilfsfunktionen
    fun showStuff() {
        viewmodel.running = true
        binding.idPB.visibility = View.GONE

        binding.compass.visibility = View.VISIBLE
        binding.idTimer.visibility = View.VISIBLE
        binding.idTarget.visibility = View.VISIBLE
    }

    fun hideStuff() {
        viewmodel.running = false
        binding.idPB.visibility = View.VISIBLE

        binding.compass.visibility = View.GONE
        binding.idTimer.visibility = View.GONE
        binding.idTarget.visibility = View.GONE
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

        //Setup Livedata für score und network
        viewmodel.livenetworkReset.observe(viewLifecycleOwner){
            if(viewmodel.livenetworkReset.value == true){
                showStuff()
            }
            else viewmodel.livenetworkReset.value = false
        }

        //Timer startet wenn das Handy auf das Ziel zeigt
        timer = object : CountDownTimer(2000, 10) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                //erhöht den timer um die vergangene Zeit
                viewmodel.score += completionTime

                //nicht letztes Ziel
                if (viewmodel.listindex < viewmodel.indexList.size) {
                    //nächster api call
                    viewmodel.logic.apiCall(viewmodel.indexList[viewmodel.listindex], activity!!)
                    viewmodel.listindex++
                }
                //letztes Ziel
                else {
                    //stoppt den completion timer
                    viewmodel.completionTimer.cancel()

                    //stoppt Handy das zu vibrieren wenn das Spiel vorbei ist
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

        //Timer der die max Zeit setzt die man für jedes Ziel hat
        viewmodel.completionTimer = object : CountDownTimer(30000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                //berechnet die vergangene Zeit
                completionTime = 30000 - millisUntilFinished.toFloat()

                //string formatiert den Timer auf 3 Stellen nach dem .
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
                //nicht letztes Ziel
                if (viewmodel.listindex < viewmodel.indexList.size) {
                    //erhöht den score um die max Zeit
                    viewmodel.score += 30000

                    //nächster api call
                    viewmodel.logic.apiCall(viewmodel.indexList[viewmodel.listindex], activity!!)
                    viewmodel.listindex++
                }
                //letztes Ziel
                else {
                    //erhöht den score um die max Zeit
                    viewmodel.score += 30000

                    //stoppt Handy das zu vibrieren wenn das Spiel vorbei ist
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

        //location request um sicher zu gehen dass das Handy eine lastlocation für getTargetDirection() hat
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

        //initialisiere das Spiel zum ersten mal
        viewmodel.logic.initGame(activity!!)
        return view
    }

    fun winnerCheck() {
        //Prüfe ob Raum existiert...
        MyApplication.myRef.child("data").child(MyApplication.code).get().addOnSuccessListener {
            if(it.value != null) {
                if(MyApplication.isHost) {
                    //Schreib deinen score in die DB...
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("HostScore").setValue(viewmodel.score)
                    //Warte darauf das Guest seinen Score einträgt...
                    hideStuff()
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                //Entscheide Gewinner!
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
                    //Schreib deinen score in die DB...
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").setValue(viewmodel.score)
                    //Warte darauf das Host entscheidet wer gewonnen hat
                    hideStuff()
                }
            }
        }
    }

    fun resetGame() {
        //game over alert dialoge
        val build = AlertDialog.Builder(activity!!);
        build.setTitle("Game Over!")
        build.setMessage("You took "+ (viewmodel.score/1000).toString()+" Seconds!")

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
                //speichert Werte des Accelerometer Sensor
                floatGravity[0] = (alpha*floatGravity[0]+(1-alpha)*event!!.values[0])
                floatGravity[1] = (alpha*floatGravity[1]+(1-alpha)*event!!.values[1])
                floatGravity[2] = (alpha*floatGravity[2]+(1-alpha)*event!!.values[2])
            }

            if(event!!.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                //speichert Wert des MagneticField Sensor
                floatGeoMagnetic[0] = (alpha*floatGeoMagnetic[0]+(1-alpha)*event!!.values[0])
                floatGeoMagnetic[1] = (alpha*floatGeoMagnetic[1]+(1-alpha)*event!!.values[1])
                floatGeoMagnetic[2] = (alpha*floatGeoMagnetic[2]+(1-alpha)*event!!.values[2])
            }

            var R = FloatArray(9)
            var I = FloatArray(9)
            var success = SensorManager.getRotationMatrix(R, I, floatGravity, floatGeoMagnetic)
            if(success && viewmodel.running) {
                //bestimmt Ausrichtung
                var orientation = FloatArray(3)
                //berechnet die Handyaurichtung zu Norden in Grad
                SensorManager.getOrientation(R, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuth = (azimuth+360)%360

                //animiert Kopmass
                var ani: RotateAnimation = RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                currentAzimuth = azimuth

                ani.duration = 500
                ani.repeatCount = 0
                ani.fillAfter = true

                compass.startAnimation(ani)

                //checkt ob die Ausrichtung stimmt
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

                //checkt ob die Ausrichtung in einem 90 Grad (links und rechts) abstand zum Ziel ist
                if ((viewmodel.targetDirectionDegree - 90)%360 <= azimuth && azimuth <= (viewmodel.targetDirectionDegree + 90)%360) {
                    //vibriert das Handy wenn das Handy in der nähe zum Ziel ist
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
        //SensorManager abmelden wenn pausiert
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        //SensorManager anmelden wenn app fortgesetzt wird
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onLocationChanged(location: Location) {
    }


}