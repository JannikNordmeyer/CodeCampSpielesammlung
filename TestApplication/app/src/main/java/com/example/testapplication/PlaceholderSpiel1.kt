package com.example.testapplication



/*import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView*/

//import com.esri.arcgisruntime.toolkit.compass.Compass
import android.Manifest
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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.*
import com.example.testapplication.databinding.FragmentPlaceholderspiel1Binding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.util.*
import kotlin.random.Random
import android.R
import android.app.AlertDialog

import android.content.Intent

import android.content.DialogInterface
import android.location.LocationRequest
import android.os.*
import android.provider.Settings
import android.widget.Toast
import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import kotlin.collections.ArrayList

import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.getSystemService
import kotlin.math.*


class PlaceholderSpiel1 : Fragment(), SensorEventListener, LocationListener {
    private val TAG = PlaceholderSpiel1::class.java.simpleName

    //private val fragmentPlaceholderspiel1Binding by lazy {        FragmentPlaceholderspiel1Binding.inflate(layoutInflater)    }

    //private val mapView: MapView by lazy { fragmentPlaceholderspiel1Binding.mapView    }

    private lateinit var binding: FragmentPlaceholderspiel1Binding

    lateinit var targetLocation: JSONObject
    lateinit var targetList: JSONArray
    lateinit var indexList: ArrayList<Int>
    var listindex = 0
    lateinit var sensorManager: SensorManager
    lateinit var sensorAccelerometer: Sensor
    lateinit var sensorMagneticField: Sensor
    lateinit var mLocationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var floatGravity = FloatArray(3)
    private var floatGeoMagnetic = FloatArray(3)
    private var azimuth = 0f
    private var currentAzimuth = 0f
    private var targetDirectionDegree: Double = 0.0
    lateinit var compass: ImageView
    lateinit var lastLocation: Location
    private var gps_enabled = false
    private var network_enabled = false
    lateinit var timer: CountDownTimer
    lateinit var completionTimer: CountDownTimer
    lateinit var vibrateTimer: CountDownTimer
    var vibTimerRunning = false
    var completionTime : Float = 0.0f
    var timerStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentPlaceholderspiel1Binding.inflate(inflater,container,false)
        //val view = fragmentPlaceholderspiel1Binding.root
        val view = binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(PlaceholderSpiel1ViewModel::class.java) //Shared Viewmodel w/ GameHolder

        // DEIN CODE HIER
        //++setContentView(fragmentPlaceholderspiel1Binding.root)
        compass = binding.compass
        sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mLocationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        targetList = JSONArray()
        indexList = ArrayList()
        targetLocation = JSONObject()
        timer = object : CountDownTimer(2000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("Compass", millisUntilFinished.toString())
                viewmodel.score += completionTime
            }

            override fun onFinish() {
                //TODO: FIX LIST LOOP
                if (listindex < indexList.size) {
                    apiCall(indexList[listindex++])
                    getTargetDirection()
                    Log.d("Compass", "YOU FUCKING DID IT ${targetLocation.getJSONObject("properties").getString("Objekt")}")
                } else {
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
                                            if (viewmodel.score > snapshot.value.toString().toInt()) {
                                                networkWinner = MyApplication.hostID
                                            } else if (viewmodel.score < snapshot.value.toString().toInt()) {
                                                networkWinner = MyApplication.guestID
                                            } else networkWinner = "-1"  //Draw
                                            //Enter Winner
                                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(networkWinner)
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
            }
        }

        completionTimer = object : CountDownTimer(60000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                completionTime = 60000 - millisUntilFinished.toFloat()
                binding.idTimer.text = (completionTime/1000).toString()
            }

            override fun onFinish() {
                if (listindex < indexList.size) {
                    apiCall(indexList[listindex++])
                    getTargetDirection()
                    Toast.makeText(context, "OUT OF TIME!", Toast.LENGTH_SHORT).show()
                } else {
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
                                            if (viewmodel.score > snapshot.value.toString().toInt()) {
                                                networkWinner = MyApplication.hostID
                                            } else if (viewmodel.score < snapshot.value.toString().toInt()) {
                                                networkWinner = MyApplication.guestID
                                            } else networkWinner = "-1"  //Draw
                                            //Enter Winner
                                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(networkWinner)
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

        if (MyApplication.isCodeMaker) {
            for (i in 0..4) {
                val rand = Random.nextInt(0,132)
                //MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").push().setValue(rand)
                indexList.add(rand)
            }
            Log.d("Compass", "lookatme"+indexList.toString())
            val childUpdates = hashMapOf<String, Any>("1" to indexList[0], "2" to indexList[1], "3" to indexList[2], "4" to indexList[3], "5" to indexList[4])
            MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").setValue(childUpdates).addOnSuccessListener {
                MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
            }

        }

        MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == true) {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").get().addOnSuccessListener {
                        Log.d("Compass", "lookatme"+it.toString())

                        for (data in it.children){
                            indexList.add(data.value.toString().toInt())
                        }
                        Log.d("Compass", "lookatme"+indexList.toString())
                        apiCall(indexList[listindex++])
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })




        try {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }


        binding.idBtnGenerate.setOnClickListener {
            val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) { // Vibrator availability checking
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                }
            }
        }

        return view
    }

    private fun getTargetDirection() {
        //apiCall()
        if(ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val location = fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    completionTimer.start()
                    val longitude = it.longitude
                    val latitude = it.latitude
                    val long = targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)
                    val lat = targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)
                    val dir = FloatArray(2)
                    dir[0] = longitude.toFloat() - long.toFloat()
                    dir[1] = latitude.toFloat() - lat.toFloat()
                    targetDirectionDegree = acos(dir[0]/(sqrt(  dir[0].pow(2) + dir[1].pow(2)) ) ) * 180/ PI
                    Log.d("Compass", dir[0].toString() +" , " + dir[1].toString())
                    Log.d("Compass", targetDirectionDegree.toString())
                }
            }

        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
            Log.d("Compass", "No Access")
        }
    }

    private fun apiCall() {
        val url = "https://geoportal.kassel.de/arcgis/rest/services/Service_Daten/Freizeit_Kultur/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&" +
                "geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&distance=&units=esriSRUnit_Foot&relationPar" +
                "am=&outFields=*&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&havingClause=&retu" +
                "rnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVers" +
                "ion=&historicMoment=&returnDistinctValues=false&resultOffset=&resultRecordCount=&returnExtentOnly=false&datumTransformation=&par" +
                "ameterValues=&rangeValues=&quantizationParameters=&featureEncoding=esriDefault&f=geojson"
        val queue = Volley.newRequestQueue(activity)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {
                val rand = Random.nextInt(0,132)
                targetLocation = it.getJSONArray("features").getJSONObject(rand)
                targetList.put(targetLocation)
                //binding.idTarget.text = targetLocation.getJSONObject("properties").getString("Objekt")
                targetLocation = targetList.getJSONObject(listindex)
                Log.d("MainActivity", "test: " + targetLocation.toString())
            }, Response.ErrorListener {
                Log.d("MainActivity", "Api call failed")
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun apiCall(key: Int) {
        val url = "https://geoportal.kassel.de/arcgis/rest/services/Service_Daten/Freizeit_Kultur/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&" +
                "geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&distance=&units=esriSRUnit_Foot&relationPar" +
                "am=&outFields=*&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&havingClause=&retu" +
                "rnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVers" +
                "ion=&historicMoment=&returnDistinctValues=false&resultOffset=&resultRecordCount=&returnExtentOnly=false&datumTransformation=&par" +
                "ameterValues=&rangeValues=&quantizationParameters=&featureEncoding=esriDefault&f=geojson"
        val queue = Volley.newRequestQueue(activity)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {
                targetLocation = it.getJSONArray("features").getJSONObject(key)
                targetList.put(targetLocation)
                getTargetDirection()
                binding.idTarget.text = targetLocation.getJSONObject("properties").getString("Objekt")
                Log.d("MainActivity", "test: " + targetLocation.toString())
            }, Response.ErrorListener {
                Log.d("MainActivity", "Api call failed")
            }
        )
        queue.add(jsonObjectRequest)
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
                //Log.d("Compass", currentAzimuth.toString())

                //check for right direction
                if ((targetDirectionDegree - 5)%360 <= azimuth && azimuth <= (targetDirectionDegree + 5)%360) {
                    //Log.d("Compass", "YOU FUCKING DID IT ${targetLocation.getJSONObject("properties").getString("Objekt")}")
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

                if ((targetDirectionDegree - 90)%360 <= azimuth && azimuth <= (targetDirectionDegree + 90)%360) {
                    //Log.d("Compass", "YOU FUCKING DID IT ${targetLocation.getJSONObject("properties").getString("Objekt")}")
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
        Log.d("Compass", location.toString())
    }


}