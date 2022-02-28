package com.example.testapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentPlaceholderspiel1Binding

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView

import android.content.Context
import java.io.IOException
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream

class PlaceholderSpiel1 : Fragment() {
    private val TAG = PlaceholderSpiel1::class.java.simpleName

    private val fragmentPlaceholderspiel1Binding by lazy {
        FragmentPlaceholderspiel1Binding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        fragmentPlaceholderspiel1Binding.mapView
    }

    private lateinit var binding: FragmentPlaceholderspiel1Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        //binding = FragmentPlaceholderspiel1Binding.inflate(inflater,container,false)
        val view = fragmentPlaceholderspiel1Binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(PlaceholderSpiel1ViewModel::class.java) //Shared Viewmodel w/ GameHolder

        // DEIN CODE HIER
        //++setContentView(fragmentPlaceholderspiel1Binding.root)

        setApiKeyForApp()

        setupMap()

        Log.d(TAG, resources.openRawResource(R.raw.kassel).toString())


        return view
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }

    private fun setApiKeyForApp(){
        // set your API key
        // Note: it is not best practice to store API keys in source code. The API key is referenced
        // here for the convenience of this tutorial.

        ArcGISRuntimeEnvironment.setApiKey("AAPKa5c9d216029045fd884dc4daf9ed63555-Hc8HL0cr6zUfmclNZ1ZN4k_-_slINrK0IQjgPlN2x9jaJUNUhCZ6COFv_xxd55")

    }

    // set up your map here. You will call this method from onCreate()
    private fun setupMap() {

        // create a map with the BasemapStyle streets
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        // set the viewpoint, Viewpoint(latitude, longitude, scale)
        mapView.setViewpoint(Viewpoint(34.0270, -118.8050, 72000.0))
    }

    fun readJSONFromAsset(): String? {
        var json: String? = null
        try {
            val  inputStream:InputStream = resources.openRawResource(R.raw.kassel)
            json = inputStream.bufferedReader().use{it.readText()}
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }



}