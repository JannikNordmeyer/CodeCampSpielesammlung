package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.Placeholder
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.ActivityGameHolderBinding
import com.example.testapplication.databinding.FragmentTictactoeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

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

        //Select Fragment and load it's Viewmodel
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

        //Load Fragment
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.FrameLayoutGameHolder,fragToLoad)
            commit()
        }

        //If we are in online mode, setup listener to call fragment's logic networkOnFieldUpdate function to update field contents.
        if(MyApplication.onlineMode){
            MyApplication.myRef.child(MyApplication.code).child("Field").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    var data = snapshot.key
                    when(viewmodel){
                        is TicTacToeViewModel -> (viewmodel as TicTacToeViewModel).logic.networkOnFieldUpdate(data)
                        is PlaceholderSpiel1ViewModel -> (viewmodel as PlaceholderSpiel1ViewModel).logic.networkOnFieldUpdate(data)
                        is PlaceholderSpiel2ViewModel -> (viewmodel as PlaceholderSpiel2ViewModel).logic.networkOnFieldUpdate(data)
                        is PlaceholderSpiel3ViewModel -> (viewmodel as PlaceholderSpiel3ViewModel).logic.networkOnFieldUpdate(data)
                        is PlaceholderSpiel4ViewModel -> (viewmodel as PlaceholderSpiel4ViewModel).logic.networkOnFieldUpdate(data)
                        is PlaceholderSpiel5ViewModel -> (viewmodel as PlaceholderSpiel5ViewModel).logic.networkOnFieldUpdate(data)
                    }
                }
                //region
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                //endregion
            })
        }

        //TODO: What if we are in online mode? Need to make a generic escape regardless of game!
        binding.ButtonGiveUp.setOnClickListener(){
            finish()
        }

    }
}