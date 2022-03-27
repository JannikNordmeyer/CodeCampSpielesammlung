package com.example.testapplication

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.MyApplication.Companion.FRIENDS_TOPIC
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.Exception


class GameSelect : AppCompatActivity() {

    private lateinit var binding: ActivityGameSelectBinding
    lateinit var viewmodel: ViewModel
    lateinit var mainViewModel: MainViewModel
    lateinit var mLocationManager : LocationManager

    //Hilfsfunktion zum übergehen zum SelectNetwork
    fun goToNetworkSetup(){
        val intent = Intent(this, GameSelectNetwork::class.java)
        startActivity(intent)
    }

    //UI-Hilfs funktionen um online-only buttons auszublenden falls man nicht eingeloggt ist
    fun fadeOutButtons(){
        binding.FriendsButton.alpha = 0.5F
        binding.statsButton.alpha = 0.5F
    }

    fun fadeInButtons(){
        binding.FriendsButton.alpha = 1F
        binding.statsButton.alpha = 1F
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        binding = ActivityGameSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewmodel = ViewModelProvider(this).get(GameSelectNetworkViewModel()::class.java)

        mainViewModel = ViewModelProvider(this).get(MainViewModel(application)::class.java)

        //Sensor-Check: Welche Spiele kann der Nutzer spielen? Nicht spielbare Spiele werden ausgeblendet.
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val kompassAvailable = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
        val schrittzaehlerAvailable = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

        if(!kompassAvailable){
            binding.ButtonKompass.alpha = 0.5F
        }
        if(!schrittzaehlerAvailable){
            binding.ButtonSchrittzaehler.alpha = 0.5F
        }


        //Hol Database Referenz
        MyApplication.database = FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
        MyApplication.myRef = MyApplication.database.reference;
        FirebaseMessaging.getInstance().subscribeToTopic(FRIENDS_TOPIC)

        //Check ob man schon eingeloggt ist
        var currentuser = FirebaseAuth.getInstance().currentUser
        if(currentuser != null) {
            MyApplication.isLoggedIn = true
            fadeInButtons()
            binding.TextViewLoginStatus.setText("Logged in as " + currentuser.email)
        }
        else {
            MyApplication.isLoggedIn = false
            fadeOutButtons()
        }

        //Button Listeners:
        //logt den User aus und schaltet online-only funktionen aus
        binding.ButtonLogout.setOnClickListener(){
            FirebaseAuth.getInstance().signOut()
            currentuser = null
            MyApplication.isLoggedIn = false
            fadeOutButtons()
            binding.TextViewLoginStatus.setText("You are not currently logged in.")
        }

        //geht in den Stat Bildschrim oder zeigt Fehlermeldung falls man nicht eingeloggt ist.
        binding.statsButton.setOnClickListener(){
            if(MyApplication.isLoggedIn) {
                val intent = Intent(this, Statistics::class.java);
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }

        //Wählt TTT als Spiel aus und geht in den NetworkSelect Bildschrim
        binding.ButtonTicTacToe.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.TICTACTOE
            goToNetworkSetup()
        }

        //Wählt Kompass als Spiel aus + führt GPS Rechte check aus
        binding.ButtonKompass.setOnClickListener(){
            if(kompassAvailable) {
                //Prüft für GPS Rechte...
                mLocationManager =
                    this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                var gps_enabled = false
                var permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                try {
                    gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                } catch (ex: Exception) {
                }

                if (!gps_enabled || !permission) {
                    // Falls keine vorhanden, verlange diese.
                    if (!gps_enabled) {
                        val build = AlertDialog.Builder(this)
                            .setMessage("GPS is not enabled")
                            .setPositiveButton("open location settings",
                                DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                                    this.startActivity(
                                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    )
                                })
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    if (!permission) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
                    }
                } else {
                    //Falls vorhanden, gehe zu networkSetup.
                    MyApplication.globalSelectedGame = GameNames.COMPASS
                    goToNetworkSetup()
                }
            }
            else{
                Toast.makeText(this, "Your device does not support this game.", Toast.LENGTH_SHORT ).show()
            }

        }

        //Wählt Arithmetik als Spiel aus
        binding.ButtonArithmetik.setOnClickListener(){
            MyApplication.globalSelectedGame = GameNames.ARITHMETICS
            goToNetworkSetup()
        }

        //Wählt Schrittzaehler als Spiel aus
        binding.ButtonSchrittzaehler.setOnClickListener(){
            if(schrittzaehlerAvailable) {
                MyApplication.globalSelectedGame = GameNames.SCHRITTZAEHLER
                goToNetworkSetup()
            }
            else{
                Toast.makeText(this, "Your device does not support this game.", Toast.LENGTH_SHORT ).show()
            }
        }

        //Geht in den Login screen
        binding.ButtonLogin.setOnClickListener(){
            val intent = Intent(this, Login::class.java);
            startActivity(intent)
        }

        //Geht in den Freundes Screen
        binding.FriendsButton.setOnClickListener(){
            if(MyApplication.isLoggedIn) {
                val intent = Intent(this, FriendsList::class.java);
                startActivity(intent)
                }
            else{
                Toast.makeText(this, "You can only use this feature while logged in.", Toast.LENGTH_SHORT ).show()
            }
        }
    }
}