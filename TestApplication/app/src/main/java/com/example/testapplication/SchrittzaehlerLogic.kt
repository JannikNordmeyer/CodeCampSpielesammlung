package com.example.testapplication
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

class SchrittzaehlerLogic (viewmodel: SchrittzaehlerViewModel){

    var viewmodel = viewmodel

    var livegoalscore: MutableLiveData<Int?> = MutableLiveData<Int?>()

    fun networkOnFieldUpdate(data : String?){
        MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GoalScore").get().addOnSuccessListener {
            viewmodel.goalscore = it.value.toString().toInt()
            livegoalscore.value = viewmodel.goalscore
        }
    }

    //DEIN CODE HIER

}