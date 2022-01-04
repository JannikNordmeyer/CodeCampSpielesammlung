package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class GameHolder : AppCompatActivity() {

    private val TAG = GameHolder::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_holder)


        lateinit var fragToLoad: Fragment
        var thing = MyApplication.globalSelectedGame
        Log.d(TAG, thing.toString())

        when(MyApplication.globalSelectedGame) {
            GameNames.TEST1 -> {
                fragToLoad = FragmentTest1()
                Log.d(TAG, "LOADED FRAGMENT TEST 1")
            }
            GameNames.TEST2 -> {
                fragToLoad = FragmentTest2()
                Log.d(TAG, "LOADED FRAGMENT TEST 2")
            }
            else ->  Log.d(TAG," ERROR: COULDNT LOAD GAME FRAGMENT")
        }

        supportFragmentManager.beginTransaction().apply{
            replace(R.id.flTest,fragToLoad)
            commit()
        }

    }
}