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
        //setup das nur vom Host ausgeführt wird
        if (MyApplication.isHost || !MyApplication.onlineMode) {
            //reset alle genutzten Variablen
            viewmodel.listindex = 0
            viewmodel.score = 0f
            viewmodel.indexList.clear()
            viewmodel.vibrateActive = true

            //füllt die indexList mit 5 zufälligen keys für die api calls
            for (i in 0..4) {
                val rand = Random.nextInt(0,132)
                viewmodel.indexList.add(rand)
            }

            //pusht die indexList auf die Datenbank für online mode
            if (MyApplication.onlineMode) {
                val childUpdates = hashMapOf<String, Any>("1" to viewmodel.indexList[0], "2" to viewmodel.indexList[1], "3" to viewmodel.indexList[2], "4" to viewmodel.indexList[3], "5" to viewmodel.indexList[4])
                MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").setValue(childUpdates).addOnSuccessListener {
                    //setzt FieldUpdate auf true um den Gast wissen zu lassen dass alle daten gepusht wurden
                    MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
                }
            }

            //ertser api call um das Spiel zu starten
            apiCall(viewmodel.indexList[viewmodel.listindex],passedActivity)
            viewmodel.listindex++
        } else {
            //FieldUpdate listener für den Gast um zu checken ob der Host die keys gepusht hat
            MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value == true) {
                        //reset alle genutzten Variablen
                        viewmodel.listindex = 0
                        viewmodel.score = 0f
                        viewmodel.vibrateActive = true

                        //pullt die indexList von der Datenbank
                        MyApplication.myRef.child("data").child(MyApplication.code).child("Locations").get().addOnSuccessListener {
                            viewmodel.indexList.clear()
                            for (data in it.children){
                                viewmodel.indexList.add(data.value.toString().toInt())
                            }
                            //erster api call für den Gast um das Spiel zu starten
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

        //jsonObject von der url anfordern
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {
                //speichert zurückgegebenes jsonObject
                viewmodel.targetLocation = it.getJSONArray("features").getJSONObject(key)

                getTargetDirection(passedActivity)
                //update ui mit livedata
                viewmodel.liveLocation.value = viewmodel.targetLocation.getJSONObject("properties").getString("Objekt")
            }, Response.ErrorListener {
                Log.d("MainActivity", "Api call failed")
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun getTargetDirection(passedActivity: Activity) {
        //checkt für location permission
        if(ActivityCompat.checkSelfPermission(passedActivity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //holt die Handy location
            val location = viewmodel.fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    viewmodel.completionTimer.cancel()
                    viewmodel.completionTimer.start()

                    //Handy Längengrad und Breitengrad
                    val longitude = it.longitude
                    val latitude = it.latitude

                    //Ziel Längengrad und Breitengrad
                    val long = viewmodel.targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)
                    val lat = viewmodel.targetLocation.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)

                    //Richtungsgrad
                    val dir = FloatArray(2)
                    dir[0] = longitude.toFloat() - long.toFloat()
                    dir[1] = latitude.toFloat() - lat.toFloat()

                    //update ui
                    viewmodel.targetDirectionDegree = acos(dir[0]/(sqrt(  dir[0].pow(2) + dir[1].pow(2)) ) ) * 180/ PI
                } else {
                    //versuchs nochmal wenn location nicht gefunden wurde
                    getTargetDirection(passedActivity)
                }

            }

        } else {
            //location permission anfordern
            ActivityCompat.requestPermissions(passedActivity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
        }
    }

}