package com.example.testapplication
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
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
        //setup work that is only done by the host
        if (MyApplication.isHost || !MyApplication.onlineMode) {
            //reset all used variables
            viewmodel.listindex = 0
            viewmodel.score = 0f
            viewmodel.indexList.clear()
            viewmodel.vibrateActive = true

            //fill the indexList with 5 random keys for the api call
            for (i in 0..4) {
                val rand = Random.nextInt(0,132)
                viewmodel.indexList.add(rand)
            }

            //push the indexList on the database for online mode
            if (MyApplication.onlineMode) {
                val childUpdates = hashMapOf<String, Any>("1" to viewmodel.indexList[0], "2" to viewmodel.indexList[1], "3" to viewmodel.indexList[2], "4" to viewmodel.indexList[3], "5" to viewmodel.indexList[4])
                MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").setValue(childUpdates).addOnSuccessListener {
                    //set FieldUpdate true to let the guest know that all data has been pushed
                    MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
                }
            }

            //first api call to start the game
            apiCall(viewmodel.indexList[viewmodel.listindex],passedActivity)
            viewmodel.listindex++
        } else {
            //FieldUpdate listener for guest to check for keys pushed by the host
            MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value == true) {
                        //reset all used variables
                        viewmodel.listindex = 0
                        viewmodel.score = 0f
                        viewmodel.vibrateActive = true

                        //pull the indexList from the database
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").get().addOnSuccessListener {
                            viewmodel.indexList.clear()
                            for (data in it.children){
                                viewmodel.indexList.add(data.value.toString().toInt())
                            }
                            //first api call for the guest to start his game
                            apiCall(viewmodel.indexList[viewmodel.listindex], passedActivity)
                            viewmodel.listindex++
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
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

        //request a jsonObject from the url
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {
                //save the returned jsonObject
                viewmodel.targetLocation = it.getJSONArray("features").getJSONObject(key)

                getTargetDirection(passedActivity)
                //update ui with livedata
                viewmodel.liveLocation.value = viewmodel.targetLocation.getJSONObject("properties").getString("Objekt")
            }, Response.ErrorListener {
                Log.d("MainActivity", "Api call failed")
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun getTargetDirection(passedActivity: Activity) {
        //check for location permission
        if(ActivityCompat.checkSelfPermission(passedActivity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //get the phone location
            val location = viewmodel.fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    viewmodel.completionTimer.cancel()
                    viewmodel.completionTimer.start()

                    //phone longitude and latitude
                    val longitude = it.longitude
                    val latitude = it.latitude

                    //target longitude and latitude
                    val long = viewmodel.targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)
                    val lat = viewmodel.targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)

                    //direction vector
                    val dir = FloatArray(2)
                    dir[0] = longitude.toFloat() - long.toFloat()
                    dir[1] = latitude.toFloat() - lat.toFloat()

                    //update ui
                    viewmodel.targetDirectionDegree = acos(dir[0]/(sqrt(  dir[0].pow(2) + dir[1].pow(2)) ) ) * 180/ PI
                } else {
                    //try again if location could not be found
                    getTargetDirection(passedActivity)
                }

            }

        } else {
            //request location permission
            ActivityCompat.requestPermissions(passedActivity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
        }
    }

}