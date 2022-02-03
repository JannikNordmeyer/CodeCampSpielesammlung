package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
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
    lateinit var viewModel : FriendsListViewModel
    lateinit var names : ArrayList<String>



    override fun onCreate(savedInstanceState: Bundle?) {

        val viewmodel = ViewModelProvider(this).get(FriendsListViewModel::class.java)

        names = arrayListOf()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        newRecyclerView = binding.RecyclerViewFriends
        newRecyclerView.layoutManager = LinearLayoutManager(this)

        newArrayList = ArrayList<Friend>()
        getUserData()

        //newRecyclerView.adapter = ListAdapter(newArrayList)


        binding.IDButton.setOnClickListener{


            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

            MyApplication.myRef.child("FriendCodes").child(currentUser).get().addOnSuccessListener {

                if(it != null){

                    MyApplication.myRef.child("FriendCodes").child(currentUser).setValue(FirebaseAuth.getInstance().currentUser!!.email)
                }
            }
            binding.CodeField.setText(currentUser)
        }

        binding.RequestButton.setOnClickListener{

            val requestID = binding.CodeField.text

            if(requestID.toString() == FirebaseAuth.getInstance().currentUser!!.uid){

                Toast.makeText(this, "You cannot use your own ID.", Toast.LENGTH_SHORT ).show()
                return@setOnClickListener
            }
            if(requestID.toString() == ""){
                return@setOnClickListener
            }

            MyApplication.myRef.child("FriendCodes").child(requestID.toString()).get().addOnSuccessListener {

                if(it != null && it.value != null){

                    binding.CodeField.setText(it.value.toString())

                    MyApplication.myRef.child("Friends").child(requestID.toString()).setValue(FirebaseAuth.getInstance().currentUser!!.uid)
                    binding.CodeField.setText("")
                    getUserData()
                    Toast.makeText(this, "Added Friend.", Toast.LENGTH_SHORT ).show()
                }
                else{

                    Toast.makeText(this, "Invalid Friend ID.", Toast.LENGTH_SHORT ).show()

                }

            }




            }

    }

    private fun getUserData() {

        MyApplication.myRef.child("Friends").get().addOnSuccessListener {

            if (it != null) {

                val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

                it.children.forEach(){

                    if(it.value.toString() == currentUser){

                        MyApplication.myRef.child("FriendCodes").child(it.key.toString()).get().addOnSuccessListener {

                            if (it != null) {

                                names.add(it.value.toString())
                                updateRecyclerView()

                            }
                        }

                    }
                    if(it.key.toString() == currentUser){

                        MyApplication.myRef.child("FriendCodes").child(it.value.toString()).get().addOnSuccessListener {

                            if (it != null) {

                                names.add(it.value.toString())
                                updateRecyclerView()

                            }
                        }

                    }

                }

                updateRecyclerView()

            }

        }

    }

    private fun updateRecyclerView(){

        newArrayList.clear()
        var i = 0
        while(i < names.size){

            val friend = Friend(names[i])
            newArrayList.add(friend)
            i += 1

        }
        newRecyclerView.adapter = ListAdapter(newArrayList)
    }


}