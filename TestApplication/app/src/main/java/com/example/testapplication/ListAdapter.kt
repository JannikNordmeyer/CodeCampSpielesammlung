package com.example.testapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.databinding.ActivityFriendsBinding
import com.example.testapplication.databinding.ActivityLoginBinding
import com.example.testapplication.databinding.FriendCardBinding
import com.google.firebase.auth.FirebaseAuth

class ListAdapter(private val FriendsList : ArrayList<Friend>) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {


    private lateinit var binding: FriendCardBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friend_card, parent, false)

        val layoutInflater = LayoutInflater.from(parent.context)
        binding = FriendCardBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        itemView.findViewById<Button>(R.id.deleteButton).setOnClickListener(){



            MyApplication.myRef.child("FriendCodes").get().addOnSuccessListener {

                if (it != null && it.value != null) {

                    val currentUser = FirebaseAuth.getInstance().currentUser!!.email
                    val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
                    val friendName = itemView.findViewById<TextView>(R.id.FriendName).text
                    var friendID = ""
                    Toast.makeText(parent.context, "AAA", Toast.LENGTH_SHORT).show()

                    it.children.forEach(){

                        if(it.value == friendName){

                            friendID = it.key.toString()

                        }
                    }

                    MyApplication.myRef.child("Friends").get().addOnSuccessListener {

                        it.children.forEach(){

                            if(it.value == friendID){

                                MyApplication.myRef.child("Friends").child(currentUserID).removeValue().addOnSuccessListener {

                                    Toast.makeText(parent.context, "BBB", Toast.LENGTH_SHORT).show()
                                }


                            }
                            if(it.key == friendID){

                                MyApplication.myRef.child("Friends").child(it.key.toString()).removeValue().addOnSuccessListener {

                                    Toast.makeText(parent.context, "CCC", Toast.LENGTH_SHORT).show()
                                }

                            }

                        }

                    }

                }

            }

        }

        return ListViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val currentItem = FriendsList[position]
        holder.FriendName.setText(currentItem.Name)
    }

    override fun getItemCount(): Int {

        return FriendsList.size
    }

    class ListViewHolder(itemView : View) :RecyclerView.ViewHolder(itemView){



        val FriendName : TextView = itemView.findViewById(R.id.FriendName)
    }
}