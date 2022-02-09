package com.example.testapplication

import android.os.Bundle
import com.example.testapplication.databinding.ActivityGameSelectNetworkBinding
import com.example.testapplication.databinding.FriendCardBinding

data class Friend( var Name : String, var ID : String){

    private lateinit var binding: FriendCardBinding
    
}
