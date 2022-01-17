package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameHolderBinding
import com.example.testapplication.databinding.FragmentTictactoeBinding
import com.google.firebase.auth.FirebaseAuth

class GameHolder : AppCompatActivity() {

    private lateinit var binding: ActivityGameHolderBinding

    private val TAG = GameHolder::class.java.simpleName

    lateinit var fragToLoad: Fragment
    lateinit var viewmodel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_holder)

        binding = ActivityGameHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //If we are in onlineMode, Setup a room and Field Listeners to call the Fragment's updateField functions!
        if(MyApplication.onlineMode){
            //Setup Host and Guest in Database
            if (MyApplication.isCodeMaker){
                MyApplication.myRef.child("data").child(MyApplication.code).child("Host").setValue(FirebaseAuth.getInstance().currentUser!!.email)
            } else {
                MyApplication.myRef.child("data").child(MyApplication.code).child("Guest").setValue(FirebaseAuth.getInstance().currentUser!!.email)
            }



        }

        when(MyApplication.globalSelectedGame) {
            GameNames.PLACEHOLDERSPIEL1 -> {
                fragToLoad = PlaceholderSpiel1()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel1ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL1")
            }
            GameNames.PLACEHOLDERSPIEL2 -> {
                fragToLoad = PlaceholderSpiel2()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel2ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL2")
            }
            GameNames.PLACEHOLDERSPIEL3 -> {
                fragToLoad = PlaceholderSpiel3()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel3ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL3")
            }
            GameNames.PLACEHOLDERSPIEL4 -> {
                fragToLoad = PlaceholderSpiel4()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel4ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL4")
            }
            GameNames.PLACEHOLDERSPIEL5 -> {
                fragToLoad = PlaceholderSpiel5()
                viewmodel = ViewModelProvider(this).get(PlaceholderSpiel5ViewModel::class.java)
                Log.d(TAG, "LOADED PLACEHOLDERSPIEL5")
            }
            GameNames.TICTACTOE -> {
                fragToLoad = TicTacToe()
                viewmodel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)
                Log.d(TAG, "LOADED TICTACTOE")
            }
            else ->  Log.d(TAG," ERROR: FAILED TO LOAD GAME")
        }

        supportFragmentManager.beginTransaction().apply{
            replace(R.id.FrameLayoutGameHolder,fragToLoad)
            commit()
        }

        //TODO: What if we are in online mode? Need to make a generic escape regardless of game!
        binding.ButtonGiveUp.setOnClickListener(){
            finish()
        }

    }
}