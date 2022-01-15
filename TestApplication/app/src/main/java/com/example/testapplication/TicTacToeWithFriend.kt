package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlin.system.exitProcess

class TicTacToeWithFriend : AppCompatActivity() {
    //database instance
    private var database= FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference

    lateinit var headTV : TextView
    lateinit var codeEdt : EditText
    lateinit var createCodeBtn : Button
    lateinit var joinCodeBtn : Button
    lateinit var loadingPB : ProgressBar
    var request : String? = null
    var sendRequest = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tic_tac_toe_with_friend)
        headTV = findViewById(R.id.idTVHead)
        codeEdt = findViewById(R.id.idEditCode)
        createCodeBtn = findViewById(R.id.idBtnInvite)
        joinCodeBtn = findViewById(R.id.idBtnJoin)
        loadingPB = findViewById(R.id.idPBLoading)

        myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").get().addOnSuccessListener {
            request = it.value.toString()
        }

        myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                request = snapshot.value.toString()
                if(request != "" && !sendRequest){
                    notification()
                } else if(sendRequest) {
                    myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Host").setValue(true)
                    accepted(true)
                }
                stopLoad()

                //Toast.makeText(this@TicTacToeWithFriend, request, Toast.LENGTH_SHORT).show()
                }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        createCodeBtn.setOnClickListener {
            //myRef.child("Users").child(SplitString(codeEdt.text.toString())).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
            //accepted(true)

            myRef.child("Users").child(SplitString(codeEdt.text.toString())).child("Request").get().addOnSuccessListener {
                val busy = it.value.toString()
                val opponent = codeEdt.text.toString()
                Toast.makeText(this@TicTacToeWithFriend, busy, Toast.LENGTH_SHORT).show()

                if(busy == "" || busy == null) {
                    myRef.child("Users").child(SplitString(opponent)).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
                    myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue(opponent)
                    sendRequest = true
                    startLoad()
                //accepted(true)
                } else {
                    Toast.makeText(this@TicTacToeWithFriend, "requested user is already in a game", Toast.LENGTH_SHORT).show()
                }
            }

        }

        joinCodeBtn.setOnClickListener {

            myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Host").get().addOnSuccessListener {
                isCodeMaker = it.value as Boolean
                //Toast.makeText(this@TicTacToeWithFriend, isCodeMaker.toString(), Toast.LENGTH_SHORT).show()


                code = if(isCodeMaker) {
                    SplitString(request!!) + SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())
                } else {
                    SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) + SplitString(request!!)
                }
                //Toast.makeText(this@TicTacToeWithFriend, code, Toast.LENGTH_SHORT).show()
                if(code != "" && code != "null") {
                    val intent = Intent(this, OnlineMultiplayerGameActivity::class.java)
                    startActivity(intent)
                    stopLoad()
                }
            }
                .addOnFailureListener {
                    Toast.makeText(this@TicTacToeWithFriend, "no active game found", Toast.LENGTH_SHORT).show()
                }

        }


    }

    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }

    fun accepted(host: Boolean) {
        val intent = Intent(this, OnlineMultiplayerGameActivity::class.java)
        code = if(host) {
            SplitString(codeEdt.text.toString()) + SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())
        } else {
            SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString()) + SplitString(request!!)
        }
        Toast.makeText(this@TicTacToeWithFriend, code, Toast.LENGTH_SHORT).show()

        //intent.putExtra("code", code)
        isCodeMaker = host
        //code = "test"
        startActivity(intent)
        stopLoad()
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

    fun notification(){
        val build = AlertDialog.Builder(this)
        build.setTitle("Game Request")
        build.setMessage("Game request by $request\nDo you want to accept?")
        build.setPositiveButton("yes"){dialog, which->
            myRef.child("Users").child(SplitString(request!!)).child("Request").setValue(FirebaseAuth.getInstance().currentUser!!.email)
            myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Host").setValue(false)
            accepted(false)
        }
        build.setNegativeButton("no") {dialog, which->
            myRef.child("Users").child(SplitString(request!!)).child("Request").setValue("")
            myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue("")
            //exitProcess(1)
        }
        build.show()
        //Handler(Looper.getMainLooper()).postDelayed(Runnable { build.show() }, 2000)
    }
}