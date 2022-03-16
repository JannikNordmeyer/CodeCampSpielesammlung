package com.example.testapplication
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class KompassLogic (viewModel: KompassViewModel){


    var viewmodel = viewModel

    fun networkOnFieldUpdate(data : String?){
    }

    fun initGame(passedActivity: Activity) {
        if (MyApplication.isHost || !MyApplication.onlineMode) {
            Log.d("Kompass", "LISTINDEX RESET INIT GAME")
            viewmodel.listindex = 0
            viewmodel.score = 0f
            viewmodel.indexList.clear()
            viewmodel.vibrateActive = true
            for (i in 0..4) {
                val rand = Random.nextInt(0,132)
                //MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").push().setValue(rand)
                viewmodel.indexList.add(rand)
            }
            if (MyApplication.onlineMode) {
                val childUpdates = hashMapOf<String, Any>("1" to viewmodel.indexList[0], "2" to viewmodel.indexList[1], "3" to viewmodel.indexList[2], "4" to viewmodel.indexList[3], "5" to viewmodel.indexList[4])
                MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").setValue(childUpdates).addOnSuccessListener {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
                }
            }
            Log.d("Kompass", "API CALL")
            Log.d("Kompass", "Listindex: ${viewmodel.listindex}")
            apiCall(viewmodel.indexList[viewmodel.listindex],passedActivity)
            viewmodel.listindex++
            Log.d("Kompass", "LISTINDEX INCREASED")

        } else {
            MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value == true) {
                        Log.d("Kompass", "LISTINDEX RESET FIELDUPDATE")
                        viewmodel.listindex = 0
                        viewmodel.score = 0f
                        viewmodel.vibrateActive = true
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").get().addOnSuccessListener {
                            Log.d("Compass", "lookatme"+it.toString())
                            viewmodel.indexList.clear()
                            for (data in it.children){
                                viewmodel.indexList.add(data.value.toString().toInt())
                            }
                            Log.d("Kompass", "Listindex: ${viewmodel.listindex}")
                            apiCall(viewmodel.indexList[viewmodel.listindex], passedActivity)
                            viewmodel.listindex++
                            Log.d("Kompass", "LISTINDEX INCREASED")
                            Log.d("Compass", "lookatme"+viewmodel.indexList.toString())
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
                viewmodel.targetLocation = it.getJSONArray("features").getJSONObject(key)
                //targetList.put(viewmodel.targetLocation)
                //Log.d("Kompass", "LISTINDEX: $key")
                getTargetDirection(passedActivity)
                //TODO: LIVEDATE COMING SOON TM
                viewmodel.liveLocation.value = viewmodel.targetLocation.getJSONObject("properties").getString("Objekt")
                Log.d("MainActivity", "test: " + viewmodel.targetLocation.toString())
            }, Response.ErrorListener {
                Log.d("MainActivity", "Api call failed")
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun getTargetDirection(passedActivity: Activity) {
        //apiCall()
        if(ActivityCompat.checkSelfPermission(passedActivity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            val location = viewmodel.fusedLocationClient.lastLocation.addOnSuccessListener {
                Log.d("Kompass", "ONSUC START")
                if (it != null) {
                    Log.d("Kompass", "IT != NULL")
                    viewmodel.completionTimer.cancel()
                    viewmodel.completionTimer.start()
                    val longitude = it.longitude
                    val latitude = it.latitude
                    val long = viewmodel.targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)
                    val lat = viewmodel.targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)
                    val dir = FloatArray(2)
                    dir[0] = longitude.toFloat() - long.toFloat()
                    dir[1] = latitude.toFloat() - lat.toFloat()
                    viewmodel.targetDirectionDegree = acos(dir[0]/(sqrt(  dir[0].pow(2) + dir[1].pow(2)) ) ) * 180/ PI
                    Log.d("Compass", dir[0].toString() +" , " + dir[1].toString())
                    Log.d("Compass", viewmodel.targetDirectionDegree.toString())
                } else {
                    getTargetDirection(passedActivity)
                }

            }

        } else {
            ActivityCompat.requestPermissions(passedActivity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
            Log.d("Compass", "No Access")
        }
    }

}