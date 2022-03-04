package com.example.testapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentSchrittzaehlerBinding
import java.lang.Math.abs

class Schrittzaehler : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentSchrittzaehlerBinding

    var running = false
    lateinit var sensorManager: SensorManager

    lateinit var myContext: Context

    var halfStep = false
    var steps = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = FragmentSchrittzaehlerBinding.inflate(inflater,container,false)
        val view = binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(SchrittzaehlerViewModel::class.java) //Shared Viewmodel w/ GameHolder

        sensorManager = myContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        return view
    }

    override fun onResume() {
        super.onResume()
        running = true
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (stepsSensor == null) {
            Toast.makeText(activity, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
            Log.d("StepSensor:","####################### KEIN SENSOR GEFUNDEN ##################")
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepSensor:"," ################# SENSOR LÄUFT ###################")
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    //Called when sensor detects steps
    override fun onSensorChanged(event: SensorEvent) {
        if (running) {

            if(kotlin.math.abs(event.values[0]) > 1.7){
                halfStep = true
                Log.d("Schrittzaehler:"," ############### HALSTEP ################")
            }
            if(halfStep && kotlin.math.abs(event.values[0]) < 0.5){

                halfStep = false
                steps += 1
                binding.viewSchritteCounter.setText(steps.toString())

            }
        }
        else Log.d("Schrittzaehler:"," ################### Ich spüre, running aber false ###################")
    }

}