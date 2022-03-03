package com.example.testapplication

import android.app.PendingIntent.getActivity
import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.databinding.FriendCardBinding
import com.google.firebase.auth.FirebaseAuth

class ListAdapter(private val FriendsList : ArrayList<Friend>) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    private lateinit var binding: FriendCardBinding

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ListViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        context = holder.itemView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friend_card, parent, false)

        val layoutInflater = LayoutInflater.from(parent.context)
        binding = FriendCardBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        //Invite Button technology
        itemView.findViewById<Button>(R.id.BtnInvite).setOnClickListener(){

            Toast.makeText(context,"Create a lobby, and they will be invited.",Toast.LENGTH_LONG).show()

            MyApplication.myRef.child("FriendCodes").get().addOnSuccessListener {

                if (it != null && it.value != null) {
                    val currentUser = FirebaseAuth.getInstance().currentUser!!.email
                    val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
                    val friendName = itemView.findViewById<TextView>(R.id.FriendName).text
                    var friendID = ""
                    //Get friend id and push Toast that you will invite the friend with the room name
                    it.children.forEach(){
                        if(it.value == friendName){
                            MyApplication.inviteFriendID = it.key.toString()
                            Log.d("FREUNDES SHIT", friendID)
                        }
                    }
                }
            }
        }

        //Delete button technology
        itemView.findViewById<ImageButton>(R.id.deleteButton).setOnClickListener(){

            MyApplication.myRef.child("FriendCodes").get().addOnSuccessListener {

                if (it != null && it.value != null) {
                    val currentUser = FirebaseAuth.getInstance().currentUser!!.email
                    val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
                    val friendName = itemView.findViewById<TextView>(R.id.FriendName).text
                    var friendID = ""
                    it.children.forEach(){
                        if(it.value == friendName){
                            friendID = it.key.toString()
                        }
                    }

                    MyApplication.myRef.child("Users").child(SplitString(currentUser.toString())).child("Friends").get().addOnSuccessListener {
                        it.children.forEach(){
                            if(it.key == friendID){
                                MyApplication.myRef.child("Users").child(SplitString(currentUser.toString())).child("Friends").child(friendID).removeValue().addOnSuccessListener {
                                }
                                MyApplication.myRef.child("Users").child(SplitString(friendName.toString())).child("Friends").child(currentUserID).removeValue()
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

    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }

    class ListViewHolder(itemView : View) :RecyclerView.ViewHolder(itemView){
        val FriendName : TextView = itemView.findViewById(R.id.FriendName)
    }
}