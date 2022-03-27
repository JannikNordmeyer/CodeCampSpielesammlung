package com.example.testapplication

import android.app.AlertDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentSchrittzaehlerBinding

/*  SPIEL KONZEPT
Host kann entscheiden wie viele Schritte man als Ziel laufen muss.
Wer auch immer diese Anzahl zuerst erreicht, hat gewonnen!
*/

class Schrittzaehler : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentSchrittzaehlerBinding

    lateinit var sensorManager: SensorManager
    lateinit var myContext: Context
    lateinit var viewmodel: SchrittzaehlerViewModel
    lateinit var goalAlert: AlertDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onDestroy() {
        super.onDestroy()
        if(this::goalAlert.isInitialized){
            goalAlert.dismiss()
        }
    }

    //UI-Hilfsfunktionen für das warten das der Host ein Ziel eingibt
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
        binding.editTextGoalNumber.visibility = View.VISIBLE
        binding.TextViewSchritte.visibility = View.VISIBLE
        binding.textFieldGoalSteps.visibility = View.VISIBLE
        binding.textFieldStepCounter.visibility = View.VISIBLE
        binding.idPB.visibility = View.GONE
    }

    //UI-Hilfsfunktionen für das Setup des Zieles
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentSchrittzaehlerBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(SchrittzaehlerViewModel::class.java) //Shared Viewmodel w/ GameHolder

        //Setup SensorManager
        sensorManager = myContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //Damit man aktiv die Schritte zählt bleibt der Bildschirm in dieser App an.
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //Hilfsfunktion für online Setup
        fun networkSetup() {
            if(MyApplication.isHost) showSetup()
            else startWait()
        }

        if(!MyApplication.onlineMode) {
            showSetup()
        }

        //Setup Livedata für score und network
        viewmodel.livenetworkReset.observe(viewLifecycleOwner){
            if(viewmodel.livenetworkReset.value == true){
                networkSetup()
            }
            else viewmodel.livenetworkReset.value = false
        }

        viewmodel.logic.livegoalscore.observe(viewLifecycleOwner){
            if(viewmodel.isWaiting){
                stopWait()
                viewmodel.score = 0
            }
        }

        //Button Listener für das starten der Challenge
        //Zeigt den eigentlichen Count Bildschirm, Reset Score und network Setup
        binding.BtnStartChallenge.setOnClickListener {
            hideSetup()
            var goalScore = binding.editTextGoalNumber.text.toString().toInt()
            binding.editTextGoalNumber.text.clear()
            viewmodel.goalscore = goalScore
            binding.textFieldGoalSteps.text = goalScore.toString()
            viewmodel.score = 0
            binding.textFieldStepCounter.text = "0"
            if(MyApplication.onlineMode) {
                //Übergebe Zielmenge an Guest
                MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GoalScore").setValue(viewmodel.goalscore).addOnSuccessListener {
                    MyApplication.myRef.child("data").child(MyApplication.code).child("FieldUpdate").setValue(true)
                }
            }
        }
        return view
    }

    //Regristriert den ACCELEROMETER Sensor
    override fun onResume() {
        super.onResume()
        viewmodel.running = true
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (stepsSensor == null) {
            Toast.makeText(activity, "No Step Counter Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    //Pausiert den Sensor und unregristriert ihn
    override fun onPause() {
        super.onPause()
        viewmodel.running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    //Wird ausgeführt wenn der Sensor Schritte erspürt
    override fun onSensorChanged(event: SensorEvent) {
        if (viewmodel.running) {
            //Wenn Sensor über einer gewissen Schranke erkennen wir es als Halbschritt
            if(kotlin.math.abs(event.values[0]) > 1.7){
                viewmodel.halfStep = true
            }
            //Fällt der Sensor unter einer Schranke nach einem Halfstep erkennen wir es als einen Schritt!
            if(viewmodel.halfStep && kotlin.math.abs(event.values[0]) < 0.5){

                viewmodel.halfStep = false
                viewmodel.score += 1
                binding.textFieldStepCounter.setText(viewmodel.score.toString())

                //Wenn man die Zielmenge erreicht hat, vibriere und ende Spiel falls online
                if(viewmodel.score >= viewmodel.goalscore){

                    val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (vibrator.hasVibrator()) { // Vibrator availability checking
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                        }
                    }

                    //Im Offline Modus, zeige Siegesmeldung und starte Neu
                    if(!MyApplication.onlineMode) {
                        val build = AlertDialog.Builder(activity!!);
                        build.setTitle("Goal reached!")
                        build.setMessage("You have walked "+viewmodel.goalscore.toString()+" Steps!")
                        goalAlert = build.show()

                        viewmodel.reset()
                        showSetup()
                    }
                    else{
                        //Im Online Modus, trage dich als Gewinner ein und schalte Sensor aus
                        if(MyApplication.isHost) {
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
    }

}