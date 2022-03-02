package com.example.testapplication



import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentPlaceholderspiel1Binding

/*import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView*/

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.io.IOException
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
//import com.esri.arcgisruntime.toolkit.compass.Compass
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.InputStream
import kotlin.random.Random


class PlaceholderSpiel1 : Fragment() {
    private val TAG = PlaceholderSpiel1::class.java.simpleName

    //private val fragmentPlaceholderspiel1Binding by lazy {        FragmentPlaceholderspiel1Binding.inflate(layoutInflater)    }

    //private val mapView: MapView by lazy { fragmentPlaceholderspiel1Binding.mapView    }

    private lateinit var binding: FragmentPlaceholderspiel1Binding

    lateinit var targetLocation: JSONObject
    lateinit var sensorManager: SensorManager
    lateinit var sensorAccelerometer: Sensor
    lateinit var sensorMagneticField: Sensor

    private var floatGravity = FloatArray(3)
    private var floatGeoMagnetic = FloatArray(3)
    private var floatOrientation = FloatArray(3)
    private var floatRotationMatrix = FloatArray(9)
    private var azimuth = 0f
    private var currentAzimuth = 0f
    lateinit var compass: ImageView

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

        val sensorEventListenerAccelerometer: SensorEventListener = object: SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                floatGravity = event!!.values
                val alpha = 0.97
                floatGravity[0] = (alpha*floatGravity[0]+(1-alpha)*event!!.values[0]).toFloat()
                floatGravity[1] = (alpha*floatGravity[1]+(1-alpha)*event!!.values[1]).toFloat()
                floatGravity[2] = (alpha*floatGravity[2]+(1-alpha)*event!!.values[2]).toFloat()
                val success = SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic)
                if (success) {
                    SensorManager.getOrientation(floatRotationMatrix, floatOrientation)
                    azimuth = Math.toDegrees(floatOrientation[0].toDouble()).toFloat()
                    azimuth = (azimuth+360)%360

                    var ani: RotateAnimation = RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    currentAzimuth = azimuth

                    ani.duration = 500
                    ani.repeatCount = 0
                    ani.fillAfter = true

                    compass.startAnimation(ani)
                }

                /*SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic)
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation)

                compass.rotation = (-floatOrientation[0]*180/3.14159).toFloat()*/
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }

        }

        val sensorEventListenerMagneticField: SensorEventListener = object: SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                floatGeoMagnetic = event!!.values
                val alpha = 0.97
                floatGeoMagnetic[0] = (alpha*floatGeoMagnetic[0]+(1-alpha)*event!!.values[0]).toFloat()
                floatGeoMagnetic[1] = (alpha*floatGeoMagnetic[1]+(1-alpha)*event!!.values[1]).toFloat()
                floatGeoMagnetic[2] = (alpha*floatGeoMagnetic[2]+(1-alpha)*event!!.values[2]).toFloat()
                val success = SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic)
                if (success) {
                    SensorManager.getOrientation(floatRotationMatrix, floatOrientation)
                    azimuth = Math.toDegrees(floatOrientation[0].toDouble()).toFloat()
                    azimuth = (azimuth+360)%360

                    var ani: RotateAnimation = RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    currentAzimuth = azimuth

                    ani.duration = 500
                    ani.repeatCount = 0
                    ani.fillAfter = true

                    compass.startAnimation(ani)
                }

                /*SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic)
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation)

                compass.rotation = (-floatOrientation[0]*180/3.14159).toFloat()*/
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }

        }

        sensorManager.registerListener(sensorEventListenerAccelerometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL)

        apiCall()

        binding.idBtnGenerate.setOnClickListener {
            apiCall()
        }
        //setApiKeyForApp()

        //setupMap()

        return view
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

    // set up your map here. You will call this method from onCreate()
    /*private fun setupMap() {

        // create a map with the BasemapStyle streets
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        // set the viewpoint, Viewpoint(latitude, longitude, scale)
        mapView.setViewpoint(Viewpoint(34.0270, -118.8050, 72000.0))
    }*/




}