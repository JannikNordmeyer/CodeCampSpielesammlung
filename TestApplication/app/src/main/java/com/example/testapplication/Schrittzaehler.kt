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

/*  SPIEL KONZEPT
Host kann entscheiden wie viele Schritte man als Ziel laufen muss.
Wer auch immer diese Anzahl zuerst erreicht, hat gewonnen!
*/

class Schrittzaehler : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentSchrittzaehlerBinding

    lateinit var sensorManager: SensorManager
    lateinit var myContext: Context
    lateinit var viewmodel: SchrittzaehlerViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    fun startWait(){
        viewmodel.isWaiting = true
        viewmodel.running = false
        binding.BtnStartChallenge.visibility = View.GONE
        binding.editTextGoalNumber.visibility = View.GONE
        binding.TextViewSchritte.visibility = View.GONE
        binding.textFieldGoalSteps.visibility = View.GONE
        binding.textFieldStepCounter.visibility = View.GONE
        binding.editTextGoalNumber.visibility = View.GONE
        binding.idPB.visibility = View.VISIBLE
    }

    fun stopWait(){
        viewmodel.isWaiting = false
        viewmodel.running = true
        binding.BtnStartChallenge.visibility = View.VISIBLE
        binding.editTextGoalNumber.visibility = View.VISIBLE
        binding.TextViewSchritte.visibility = View.VISIBLE
        binding.textFieldGoalSteps.visibility = View.VISIBLE
        binding.textFieldStepCounter.visibility = View.VISIBLE
        binding.idPB.visibility = View.GONE
    }

    fun showSetup() {
        viewmodel.running = false
        binding.BtnStartChallenge.visibility = View.VISIBLE
        binding.editTextGoalNumber.visibility = View.VISIBLE

        binding.textFieldGoalSteps.visibility = View.GONE
        binding.TextViewSchritte.visibility = View.GONE
        binding.textFieldStepCounter.visibility = View.GONE
    }

    fun hideSetup() {
        viewmodel.running = true
        binding.BtnStartChallenge.visibility = View.GONE
        binding.editTextGoalNumber.visibility = View.GONE

        binding.textFieldGoalSteps.visibility = View.VISIBLE
        binding.TextViewSchritte.visibility = View.VISIBLE
        binding.textFieldStepCounter.visibility = View.VISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentSchrittzaehlerBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(SchrittzaehlerViewModel::class.java) //Shared Viewmodel w/ GameHolder

        sensorManager = myContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        fun networkSetup() {
            if(MyApplication.isCodeMaker){
                showSetup()
            }
            else{
                startWait()
            }
        }

        if(!MyApplication.onlineMode) {
            showSetup()
        }

        viewmodel.livenetworkReset.observe(viewLifecycleOwner){
            if(viewmodel.livenetworkReset.value == true){
                networkSetup()
            }
            else viewmodel.livenetworkReset.value = false
        }

        viewmodel.logic.livegoalscore.observe(viewLifecycleOwner){
            if(viewmodel.isWaiting){
                stopWait()
            }
        }

        binding.BtnStartChallenge.setOnClickListener {
            hideSetup()
            var goalScore = binding.editTextGoalNumber.text.toString().toInt()
            binding.editTextGoalNumber.text.clear()
            viewmodel.goalscore = goalScore
            binding.textFieldGoalSteps.text = goalScore.toString()
            viewmodel.score = 0
            binding.textFieldStepCounter.text = "0"
            if(MyApplication.onlineMode) {
                //Pass goalScore to guest
                MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GoalScore").setValue(viewmodel.goalscore).addOnSuccessListener {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
                }
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        viewmodel.running = true
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
        viewmodel.running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    //Called when sensor detects steps
    override fun onSensorChanged(event: SensorEvent) {
        if (viewmodel.running) {
            if(kotlin.math.abs(event.values[0]) > 1.7){
                viewmodel.halfStep = true
                Log.d("Schrittzaehler:"," ############### HALSTEP ################")
            }
            if(viewmodel.halfStep && kotlin.math.abs(event.values[0]) < 0.5){

                viewmodel.halfStep = false
                viewmodel.score += 1
                binding.textFieldStepCounter.setText(viewmodel.score.toString())

                //You did it :)
                if(viewmodel.score >= viewmodel.goalscore){
                    if(!MyApplication.onlineMode) {
                        viewmodel.reset()
                        showSetup()
                    }
                    else{
                        if(MyApplication.isCodeMaker) {
                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(MyApplication.hostID)
                        }
                        else{
                            MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(MyApplication.guestID)
                        }
                        viewmodel.running = false
                    }
                }

            }
        }
        else Log.d("Schrittzaehler:"," ################### Ich spüre, running aber false ###################")
    }

}