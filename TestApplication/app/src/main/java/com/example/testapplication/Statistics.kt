package com.example.testapplication

import ArithmeticsStats
import GeoportalStats
import ChallengeStats
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityStatisticsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.jjoe64.graphview.series.DataPoint

class Statistics : AppCompatActivity() {

    lateinit var fragToLoad: Fragment
    lateinit var tabs : ArrayList<Fragment>
    private lateinit var binding: ActivityStatisticsBinding
    lateinit var viewmodel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewmodel = ViewModelProvider(this).get(StatisticsViewModel::class.java)

        tabs = ArrayList()
        tabs.add(TicTacToeStats())
        tabs.add(ArithmeticsStats())
        tabs.add(GeoportalStats())
        tabs.add(ChallengeStats())

        fragToLoad = TicTacToeStats()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.FrameLayoutStats, fragToLoad)
            commit()
        }

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                val pos = binding.tabLayout.selectedTabPosition
                fragToLoad = tabs[pos]

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.FrameLayoutStats, fragToLoad)
                    commit()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        //Laden der Daten aus Firebase-Datenbank: TicTacToe

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("GamesPlayed").get().addOnSuccessListener {
            if(it != null){
                viewmodel.TicTacToeGamesPlayed = it.value.toString().toInt()
            }
        }
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("Win%").get().addOnSuccessListener {
            if(it != null){
                var winCount = 0.0
                var winPer = 0.0
                var data = arrayOf<DataPoint>()

                it.children.forEach(){
                    if(it.value.toString().toInt() > 0){
                        winCount += 1
                        winPer = winCount / it.value.toString().toFloat()
                        data = data.plus(DataPoint(winCount, winPer))
                    }
                }
                viewmodel.TicTacToeWinPercentage = winPer
                viewmodel.TicTacToeWinCount = winCount.toInt()
                viewmodel.TicTacToeData.value = data

            }
        }
        //Laden der Daten aus Firebase-Datenbank: Compass

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.COMPASS.toString()).child("GamesPlayed").get().addOnSuccessListener {
            if(it != null){
                viewmodel.CompassGamesPlayed = it.value.toString().toInt()
            }
        }
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.COMPASS.toString()).child("Win%").get().addOnSuccessListener {
            if(it != null){
                var winCount = 0.0
                var winPer = 0.0
                var data = arrayOf<DataPoint>()

                it.children.forEach(){
                    if(it.value.toString().toInt() > 0){
                        winCount += 1
                        winPer = winCount / it.value.toString().toFloat()
                        data = data.plus(DataPoint(winCount, winPer))
                    }
                }
                viewmodel.CompassWinPercentage = winPer
                viewmodel.CompassWinCount = winCount.toInt()
                viewmodel.CompassData.value = data

            }
        }

        //Laden der Daten aus Firebase-Datenbank: Arithmetics

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("GamesPlayed").get().addOnSuccessListener {
            if(it != null){
                viewmodel.ArithmeticsGamesPlayed = it.value.toString().toInt()
            }
        }
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("Win%").get().addOnSuccessListener {
            if(it != null){
                var winCount = 0.0
                var winPer = 0.0
                var data = arrayOf<DataPoint>()

                it.children.forEach(){
                    if(it.value.toString().toInt() > 0){
                        winCount += 1
                        winPer = winCount / it.value.toString().toFloat()
                        data = data.plus(DataPoint(winCount, winPer))
                    }
                }
                viewmodel.ArithmeticsWinPercentage = winPer
                viewmodel.ArithmeticsWinCount = winCount.toInt()
                viewmodel.ArithmeticsData.value = data

            }
        }
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(
            GameNames.ARITHMETICS.toString()).child("HighScore").get().addOnSuccessListener {
            if(it != null){
                var data = arrayOf<DataPoint>()
                var counter = 0.0
                it.children.forEach(){
                    data = data.plus(DataPoint(counter, it.value.toString().toDouble()))
                    counter += 1
                }
                viewmodel.ArithmeticsHighScore.value = data
            }
        }
        //Laden der Daten aus Firebase-Datenbank: Schrittzähler

        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("GamesPlayed").get().addOnSuccessListener {
            if(it != null){
                viewmodel.ChallengeGamesPlayed = it.value.toString().toInt()
            }
        }
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("Win%").get().addOnSuccessListener {
            if(it != null){
                var winCount = 0.0
                var winPer = 0.0
                var data = arrayOf<DataPoint>()

                it.children.forEach(){
                    if(it.value.toString().toInt() > 0){
                        winCount += 1
                        winPer = winCount / it.value.toString().toFloat()
                        data = data.plus(DataPoint(winCount, winPer))
                    }
                }
                viewmodel.ChallengeWinPercentage = winPer
                viewmodel.ChallengeWinCount = winCount.toInt()
                viewmodel.ChallengeData.value = data

            }
        }
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(
            GameNames.SCHRITTZAEHLER.toString()).child("HighScore").get().addOnSuccessListener {
            if(it != null){
                var data = arrayOf<DataPoint>()
                var counter = 0.0
                it.children.forEach(){
                    data = data.plus(DataPoint(counter, it.value.toString().toDouble()))
                    counter += 1
                }
                viewmodel.ChallengeHighScore.value = data
            }
        }


    }
}