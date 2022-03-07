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
                val pos = binding.tabLayout.getSelectedTabPosition()
                fragToLoad = tabs[pos]

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.FrameLayoutStats, fragToLoad)
                    commit()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        //Laden der Dated aus Firebase-Datenbank: TicTacToe

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
                viewmodel.TicTacToeWinPercentage = winPer.toInt()
                viewmodel.TicTacToeWinCount = winCount.toInt()
                viewmodel.TicTacToeData.value = data

            }
        }




    }
}