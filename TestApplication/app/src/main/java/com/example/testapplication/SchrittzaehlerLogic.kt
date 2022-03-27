package com.example.testapplication
import androidx.lifecycle.MutableLiveData

class SchrittzaehlerLogic (viewmodel: SchrittzaehlerViewModel){

    var viewmodel = viewmodel

    var livegoalscore: MutableLiveData<Int?> = MutableLiveData<Int?>()

    fun networkOnFieldUpdate(data : String?){
        //Nimmt GoalScore von Server an
        MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GoalScore").get().addOnSuccessListener {
            viewmodel.goalscore = it.value.toString().toInt()
            livegoalscore.value = viewmodel.goalscore
        }
    }

}