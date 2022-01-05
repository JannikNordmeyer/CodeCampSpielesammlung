package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

var isCodeMaker = true;
var code = "null"
var codeFound = false
var checkTemp = true
var keyValue : String = "null"

class OnlineCodeGeneratorActivity : AppCompatActivity() {
    //database instance
    private var database=FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference

    lateinit var headTV : TextView
    lateinit var codeEdt : EditText
    lateinit var createCodeBtn : Button
    lateinit var joinCodeBtn : Button
    lateinit var loadingPB : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_code_generator)
        headTV = findViewById(R.id.idTVHead)
        codeEdt = findViewById(R.id.idEditCode)
        createCodeBtn = findViewById(R.id.idBtnCreate)
        joinCodeBtn = findViewById(R.id.idBtnJoin)
        loadingPB = findViewById(R.id.idPBLoading)

        createCodeBtn.setOnClickListener {
            code = "null"
            codeFound = false
            checkTemp = true
            keyValue = "null"
            code = codeEdt.text.toString()
            startLoad()
            if(code != "null" && code != ""){
                isCodeMaker = true
                myRef.child("codes").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var check = isValueAvailable(snapshot, code)
                        Handler(Looper.getMainLooper()).postDelayed({
                            if(check == true){
                                stopLoad()
                            } else {
                                myRef.child("codes").push().setValue(code)
                                isValueAvailable(snapshot, code)
                                checkTemp = false
                                Handler(Looper.getMainLooper()).postDelayed({
                                    accepted()
                                    Toast.makeText(this@OnlineCodeGeneratorActivity, "Please dont go back", Toast.LENGTH_SHORT).show()
                                }, 300)
                            }
                        }, 2000)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } else {
                stopLoad()
                Toast.makeText(this@OnlineCodeGeneratorActivity, "Please enter valid code", Toast.LENGTH_SHORT).show()
            }
        }

        joinCodeBtn.setOnClickListener {
            code = "null"
            codeFound = false
            checkTemp = true
            keyValue = "null"
            code = codeEdt.text.toString()
            if(code != "null" && code != ""){
                startLoad()
                isCodeMaker = false
                myRef.child("codes").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var data : Boolean = isValueAvailable(snapshot, code)
                        Handler(Looper.getMainLooper()).postDelayed({
                            if(data == true){
                                codeFound = true
                                accepted()
                                stopLoad()
                            } else {
                                stopLoad()
                                Toast.makeText(this@OnlineCodeGeneratorActivity, "Invalid Code", Toast.LENGTH_SHORT).show()
                            }
                        }, 2000)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } else {
                Toast.makeText(this@OnlineCodeGeneratorActivity, "Please enter valid code", Toast.LENGTH_SHORT).show()
            }
        }

    }

    var sessionID:String?=null
    var PlayerSymbol:String?=null

    fun PlayerOnline(sessionID:String) {
        this.sessionID = sessionID
    }

    fun accepted() {
        val intent = Intent(this, OnlineMultiplayerGameActivity::class.java)
        intent.putExtra("code", code)
        startActivity(intent);
        stopLoad()
    }

    fun isValueAvailable(snapshot: DataSnapshot, code : String): Boolean{
        var data = snapshot.children
        data.forEach {
            var value = it.getValue().toString()
            if(value == code){
                keyValue = it.key.toString()
                return true
            }
        }
        return false
    }

    fun startLoad() {
        createCodeBtn.visibility = View.GONE
        joinCodeBtn.visibility = View.GONE
        codeEdt.visibility = View.GONE
        headTV.visibility = View.GONE
        loadingPB.visibility = View.VISIBLE
    }

    fun stopLoad() {
        createCodeBtn.visibility = View.VISIBLE
        joinCodeBtn.visibility = View.VISIBLE
        codeEdt.visibility = View.VISIBLE
        headTV.visibility = View.VISIBLE
        loadingPB.visibility = View.GONE
    }
}