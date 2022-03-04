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

class PlaceholderSpiel1ViewModel(): ViewModel() {

    fun networkOnFieldUpdate(data : String?){
        //TODO: Update Field with data received...
    }

    var logic = PlaceholderSpiel1Logic()
    var liveLocation:MutableLiveData<String?> = MutableLiveData<String?>()

    //WEITERER CODE HIER
    var score : Float = 0.0f
    lateinit var targetLocation: JSONObject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var targetDirectionDegree: Double = 0.0
    lateinit var completionTimer: CountDownTimer




    fun initGame(passedActivity: Activity) {
        if (MyApplication.isCodeMaker || !MyApplication.onlineMode) {
            logic.listindex = 0
            score = 0f
            logic.indexList.clear()
            for (i in 0..4) {
                val rand = Random.nextInt(0,132)
                //MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").push().setValue(rand)
                logic.indexList.add(rand)
            }
            if (MyApplication.onlineMode) {
                val childUpdates = hashMapOf<String, Any>("1" to logic.indexList[0], "2" to logic.indexList[1], "3" to logic.indexList[2], "4" to logic.indexList[3], "5" to logic.indexList[4])
                MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").setValue(childUpdates).addOnSuccessListener {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
                }
            }
            apiCall(logic.indexList[logic.listindex++],passedActivity)

        } else {
            MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value == true) {
                        logic.listindex = 0
                        score = 0f
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").get().addOnSuccessListener {
                            Log.d("Compass", "lookatme"+it.toString())
                            logic.indexList.clear()
                            for (data in it.children){
                                logic.indexList.add(data.value.toString().toInt())
                            }
                            apiCall(logic.indexList[logic.listindex++], passedActivity)
                            Log.d("Compass", "lookatme"+logic.indexList.toString())
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    fun apiCall(key: Int, passedActivity: Activity) {
        val url = "https://geoportal.kassel.de/arcgis/rest/services/Service_Daten/Freizeit_Kultur/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&" +
                "geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&distance=&units=esriSRUnit_Foot&relationPar" +
                "am=&outFields=*&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&havingClause=&retu" +
                "rnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVers" +
                "ion=&historicMoment=&returnDistinctValues=false&resultOffset=&resultRecordCount=&returnExtentOnly=false&datumTransformation=&par" +
                "ameterValues=&rangeValues=&quantizationParameters=&featureEncoding=esriDefault&f=geojson"
        val queue = Volley.newRequestQueue(passedActivity)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {
                targetLocation = it.getJSONArray("features").getJSONObject(key)
                //targetList.put(targetLocation)
                getTargetDirection(passedActivity)
                //TODO: LIVEDATE COMING SOON TM
                liveLocation.value = targetLocation.getJSONObject("properties").getString("Objekt")
                Log.d("MainActivity", "test: " + targetLocation.toString())
            }, Response.ErrorListener {
                Log.d("MainActivity", "Api call failed")
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun getTargetDirection(passedActivity: Activity) {
        //apiCall()
        if(ActivityCompat.checkSelfPermission(passedActivity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val location = fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    completionTimer.cancel()
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
            ActivityCompat.requestPermissions(passedActivity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
            Log.d("Compass", "No Access")
        }
    }
}