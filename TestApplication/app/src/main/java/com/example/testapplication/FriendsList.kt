package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.MyApplication.Companion.sendNotification
import com.example.testapplication.databinding.ActivityFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.content.Intent

class FriendsList : AppCompatActivity() {

    private lateinit var binding: ActivityFriendsBinding

    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<Friend>
    lateinit var names : ArrayList<String>
    lateinit var ids : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {

        names = arrayListOf()
        ids = arrayListOf()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        newRecyclerView = binding.RecyclerViewFriends
        newRecyclerView.layoutManager = LinearLayoutManager(this)

        newArrayList = ArrayList()
        getUserData()

        //Liest eigenen Freundescode aus
        binding.IDButton.setOnClickListener{
            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            binding.CodeField.setText(currentUser)
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val username = MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email!!)

            val shareBody = "Friend Request by $username: $currentUser"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Friend Request")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }

        binding.buttonreturn.setOnClickListener{
            finish()
        }

        //Sendet Anfrage an die Datenbank, um Nutzer als Freund hinzuzuf??gen
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

                    MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Friends").child(requestID.toString()).setValue(it.value)
                    MyApplication.myRef.child("Users").child(MyApplication.SplitString(it.value.toString())).child("Friends").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(FirebaseAuth.getInstance().currentUser!!.email.toString())
                    binding.CodeField.setText("")
                    getUserData()
                    Toast.makeText(this, "Added Friend.", Toast.LENGTH_SHORT ).show()

                    //Push Notification senden
                    MyApplication.myRef.child("MessagingTokens").child(requestID.toString()).get().addOnSuccessListener {
                        if(it != null){
                            val id = it.value.toString()
                            val title = "New Friend"
                            val message = MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) + " has added you to their friend list."
                            PushNotification(NotificationData(title, message), id).also { sendNotification(it) }
                        }
                    }
                }
                else{
                    Toast.makeText(this, "Invalid Friend ID.", Toast.LENGTH_SHORT ).show()
                }
            }
        }

        //Listener f??r Echtzeit-Darstellung der Freundesliste
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Friends").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                getUserData()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                getUserData()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    //Liest alle Freunde aus der Datenbank aus
    private fun getUserData() {
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Friends").get().addOnSuccessListener {
            if (it != null) {
                names.clear()
                ids.clear()
                it.children.forEach(){
                    names.add(it.value.toString())
                    ids.add(it.key.toString())
                }
                updateRecyclerView()
            }
        }
    }

    //Erstellt die Freundesliste neu
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
}