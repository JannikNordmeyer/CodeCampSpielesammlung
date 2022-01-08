package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase

class test : AppCompatActivity() {
    //database instance
    private var database= FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference

    lateinit var headTV : TextView
    lateinit var codeEdt : EditText
    lateinit var createCodeBtn : Button
    lateinit var joinCodeBtn : Button
    lateinit var loadingPB : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        headTV = findViewById(R.id.idTVHead)
        codeEdt = findViewById(R.id.idEditCode)
        //createCodeBtn = findViewById(R.id.idBtnCreate)
        //joinCodeBtn = findViewById(R.id.idBtnJoin)
        loadingPB = findViewById(R.id.idPBLoading)

    }
}