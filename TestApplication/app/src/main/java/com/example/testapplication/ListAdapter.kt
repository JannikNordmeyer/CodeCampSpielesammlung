package com.example.testapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.databinding.ActivityFriendsBinding
import com.example.testapplication.databinding.ActivityLoginBinding
import com.example.testapplication.databinding.FriendCardBinding

class ListAdapter(private val FriendsList : ArrayList<Friend>) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {


    private lateinit var binding: FriendCardBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friend_card, parent, false)

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