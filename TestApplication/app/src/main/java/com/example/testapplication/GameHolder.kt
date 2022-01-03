package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class GameHolder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_holder)

        val test_fragment = FragmentTest()
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.flTest,test_fragment)
            commit()
        }

        //To-do: actually test this shit

    }
}