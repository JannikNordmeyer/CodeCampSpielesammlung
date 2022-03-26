package com.example.testapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    override fun onCleared() {
        if (MyApplication.code != "") {
            MyApplication.Ileft = true
            exitGame()
        }
        super.onCleared()
    }

    fun exitGame() {
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(MyApplication.guestID)).child("Request").setValue("")
        MyApplication.myRef.child("Users").child(MyApplication.SplitString(MyApplication.hostID)).child("Request").setValue("")
        MyApplication.myRef.child("data").child(MyApplication.code).removeValue()
        MyApplication.code = ""
    }
}