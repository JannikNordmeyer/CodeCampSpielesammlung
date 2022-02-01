package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.databinding.ActivityFriendsBinding
import com.example.testapplication.databinding.ActivityLoginBinding

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

    }

    private fun getUserData() {

        for(i in names.indices){

            val friend = Friend(names[i])
            newArrayList.add(friend)

        }
    }
}