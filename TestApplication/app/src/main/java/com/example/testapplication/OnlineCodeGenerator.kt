package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import com.example.testapplication.databinding.ActivityGameSelectBinding
import com.example.testapplication.databinding.ActivityOnlineCodeGeneratorBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//TODO: Sind das hier globale Variablen? Wenn ja sollte man die eigentlich in MyApplication umlegen!

// -> Hab das einfach mal gemacht. Bauchgefühl und so. Sind jetzt in MyApplication.

//var isCodeMaker = true;
//var code = "null"
//var codeFound = false
//var checkTemp = true
//var keyValue : String = "null"

class OnlineCodeGenerator : AppCompatActivity() {
    //database instance
    private var database=FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference

    private lateinit var binding: ActivityOnlineCodeGeneratorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_code_generator)

        binding = ActivityOnlineCodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BtnCreate.setOnClickListener {
            MyApplication.code = "null"
            MyApplication.codeFound = false
            MyApplication.checkTemp = true
            MyApplication.keyValue = "null"
            MyApplication.code = binding.EditCode.text.toString()
            startLoad()
            if(MyApplication.code != "null" && MyApplication.code != ""){
                MyApplication.isCodeMaker = true
                myRef.child("codes").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var check = isValueAvailable(snapshot, MyApplication.code)
                        Handler(Looper.getMainLooper()).postDelayed({
                            if(check == true){
                                stopLoad()
                            } else {
                                myRef.child("codes").push().setValue(MyApplication.code)
                                isValueAvailable(snapshot, MyApplication.code)
                                MyApplication.checkTemp = false
                                Handler(Looper.getMainLooper()).postDelayed({
                                    accepted()
                                    Toast.makeText(this@OnlineCodeGenerator, "Please dont go back", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@OnlineCodeGenerator, "Please enter valid code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.BtnJoin.setOnClickListener {
            MyApplication.code = "null"
            MyApplication.codeFound = false
            MyApplication.checkTemp = true
            MyApplication.keyValue = "null"
            MyApplication.code = binding.EditCode.text.toString()
            if(MyApplication.code != "null" && MyApplication.code != ""){
                startLoad()
                MyApplication.isCodeMaker = false
                myRef.child("codes").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var data : Boolean = isValueAvailable(snapshot, MyApplication.code)
                        Handler(Looper.getMainLooper()).postDelayed({
                            if(data == true){
                                MyApplication.codeFound = true
                                accepted()
                                stopLoad()
                            } else {
                                stopLoad()
                                Toast.makeText(this@OnlineCodeGenerator, "Invalid Code", Toast.LENGTH_SHORT).show()
                            }
                        }, 2000)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } else {
                Toast.makeText(this@OnlineCodeGenerator, "Please enter valid code", Toast.LENGTH_SHORT).show()
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
        intent.putExtra("code", MyApplication.code)
        startActivity(intent);
        stopLoad()
    }

    fun isValueAvailable(snapshot: DataSnapshot, code : String): Boolean{
        var data = snapshot.children
        data.forEach {
            var value = it.getValue().toString()
            if(value == code){
                MyApplication.keyValue = it.key.toString()
                return true
            }
        }
        return false
    }

    fun startLoad() {
        binding.BtnCreate.visibility    = View.GONE
        binding.BtnJoin.visibility      = View.GONE
        binding.EditCode.visibility     = View.GONE
        binding.TVHead.visibility       = View.GONE
        binding.PBLoading.visibility    = View.VISIBLE
    }

    fun stopLoad() {
        binding.BtnCreate.visibility    = View.VISIBLE
        binding.BtnJoin.visibility      = View.VISIBLE
        binding.EditCode.visibility     = View.VISIBLE
        binding.TVHead.visibility       = View.VISIBLE
        binding.PBLoading.visibility    = View.GONE
    }
}