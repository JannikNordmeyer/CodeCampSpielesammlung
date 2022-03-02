package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.MyApplication.Companion.sendNotification
import com.example.testapplication.databinding.ActivityFriendsBinding
import com.example.testapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlin.random.Random

class FriendsList : AppCompatActivity() {

    private val TAG = FriendsList::class.java.simpleName

    private lateinit var binding: ActivityFriendsBinding

    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<Friend>
    lateinit var viewModel : FriendsListViewModel
    lateinit var names : ArrayList<String>
    lateinit var ids : ArrayList<String>



    override fun onCreate(savedInstanceState: Bundle?) {

        val viewmodel = ViewModelProvider(this).get(FriendsListViewModel::class.java)

        names = arrayListOf()
        ids = arrayListOf()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        newRecyclerView = binding.RecyclerViewFriends
        newRecyclerView.layoutManager = LinearLayoutManager(this)

        newArrayList = ArrayList<Friend>()
        getUserData()


        binding.IDButton.setOnClickListener{
            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            binding.CodeField.setText(currentUser)
        }

        binding.buttonreturn.setOnClickListener{
            finish()
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

                if(it != null){

                    binding.CodeField.setText(it.value.toString())

                    MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Friends").child(requestID.toString()).setValue(it.value)
                    MyApplication.myRef.child("Users").child(SplitString(it.value.toString())).child("Friends").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(FirebaseAuth.getInstance().currentUser!!.email.toString())
                    binding.CodeField.setText("")
                    getUserData()
                    Toast.makeText(this, "Added Friend.", Toast.LENGTH_SHORT ).show()

                    MyApplication.myRef.child("MessagingTokens").child(requestID.toString()).get().addOnSuccessListener {
                        if(it != null){
                            val id = it.value.toString()
                            val title = "New Friend"
                            val message = SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) + "has added you to their friend list."
                            PushNotification(NotificationData(title, message), id).also { sendNotification(it) }
                        }

                    }

                }
                else{

                    Toast.makeText(this, "Invalid Friend ID.", Toast.LENGTH_SHORT ).show()

                }

            }

        }

        MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Friends").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                getUserData()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                getUserData()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun getUserData() {

        MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Friends").get().addOnSuccessListener {

            if (it != null) {
                names.clear()
                ids.clear()
                val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

                it.children.forEach(){

                    names.add(it.value.toString())
                    ids.add(it.key.toString())

                }

                updateRecyclerView()

            }

        }

    }

    fun updateRecyclerView(){

        newArrayList.clear()
        var i = 0
        while(i < names.size){

            val friend = Friend(names[i], ids[i])
            newArrayList.add(friend)
            i += 1

        }
        newRecyclerView.adapter = ListAdapter(newArrayList)
    }

    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }


}