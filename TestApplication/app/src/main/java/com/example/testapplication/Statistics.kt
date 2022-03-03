package com.example.testapplication

import ArithmeticsStats
import GeoportalStats
import ChallengeStats
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.testapplication.databinding.ActivityStatisticsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener


class Statistics : AppCompatActivity() {

    lateinit var fragToLoad: Fragment
    lateinit var tabs : ArrayList<Fragment>
    private lateinit var binding: ActivityStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
}