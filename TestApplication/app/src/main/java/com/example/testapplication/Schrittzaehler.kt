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

class Schrittzaehler : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentSchrittzaehlerBinding

    var running = false
    lateinit var sensorManager: SensorManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = FragmentSchrittzaehlerBinding.inflate(inflater,container,false)
        val view = binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(SchrittzaehlerViewModel::class.java) //Shared Viewmodel w/ GameHolder

        sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        return view
    }

    override fun onResume() {
        super.onResume()
        running = true
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(activity, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
            Log.d("StepSensor:","Der Shit läuft nicht")
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepSensor:","Der Shit läuft")
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
            binding.viewSchritteCounter.setText("" + event.values[0])
            Log.d("Schrittzaehler:","Step detected")
        }
        else Log.d("Schrittzaehler:","Ich spüre, running aber false")
    }

}