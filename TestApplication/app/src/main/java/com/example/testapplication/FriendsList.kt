package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.databinding.ActivityFriendsBinding
import com.example.testapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class FriendsList : AppCompatActivity() {

    private val TAG = FriendsList::class.java.simpleName

    private lateinit var binding: ActivityFriendsBinding

    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<Friend>
    lateinit var names : Array<String>



    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "AAA")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        names = arrayOf("A", "B", "C")

        newRecyclerView = binding.RecyclerViewFriends
        newRecyclerView.layoutManager = LinearLayoutManager(this)

        newArrayList = ArrayList<Friend>()
        getUserData()

        newRecyclerView.adapter = ListAdapter(newArrayList)


        binding.IDButton.setOnClickListener{


            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

            MyApplication.myRef.child("FriendCodes").child(currentUser).get().addOnSuccessListener {

                if(it != null){
                    
                    MyApplication.myRef.child("FriendCodes").child(currentUser).setValue(currentUser)
                }

                MyApplication.myRef.child("FriendCodes").child(currentUser).get().addOnSuccessListener {

                    if(it != null){

                        binding.CodeField.setText(it.value.toString())
                    }


                }



            }



        }

    }

    private fun getUserData() {

        for(i in names.indices){

            val friend = Friend(names[i])
            newArrayList.add(friend)

        }
    }


}