package com.example.testapplication

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlin.system.exitProcess

var isMyMove = isCodeMaker
var playerTurn = true
class OnlineMultiplayerGameActivity : AppCompatActivity() {
    //database instance
    private var database= FirebaseDatabase.getInstance("https://spielesammulng-default-rtdb.europe-west1.firebasedatabase.app")
    private var myRef=database.reference

    lateinit var box1Btn : Button
    lateinit var box2Btn : Button
    lateinit var box3Btn : Button
    lateinit var box4Btn : Button
    lateinit var box5Btn : Button
    lateinit var box6Btn : Button
    lateinit var box7Btn : Button
    lateinit var box8Btn : Button
    lateinit var box9Btn : Button
    lateinit var resetBtn : Button
    lateinit var turnTV : TextView
    var player1count = 0
    var player2count = 0
    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var emptyCells = ArrayList<Int>()
    var activeUser = 1
    var exit : Boolean = false
    val myEmail = FirebaseAuth.getInstance().currentUser!!.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_multiplayer_game)

        box1Btn = findViewById(R.id.topleft)
        box2Btn = findViewById(R.id.topmid)
        box3Btn = findViewById(R.id.topright)
        box4Btn = findViewById(R.id.midleft)
        box5Btn = findViewById(R.id.midmid)
        box6Btn = findViewById(R.id.midright)
        box7Btn = findViewById(R.id.botleft)
        box8Btn = findViewById(R.id.botmid)
        box9Btn = findViewById(R.id.botright)
        resetBtn = findViewById(R.id.returnbutton)
        turnTV = findViewById(R.id.playerprompt)

        resetBtn.setOnClickListener {
            reset()
        }

        if (isCodeMaker){
            myRef.child("data").child(code).child("Host").setValue(FirebaseAuth.getInstance().currentUser!!.email)
        } else {
            myRef.child("data").child(code).child("Guest").setValue(FirebaseAuth.getInstance().currentUser!!.email)
        }

        myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if(!exit) {
                    exitProcess(1)
                    Toast.makeText(this@OnlineMultiplayerGameActivity, "Opponent left the game", Toast.LENGTH_SHORT).show()
                }
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


        myRef.child("data").child(code).child("Field").addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var data = snapshot.key
                /*if(isMyMove) {
                    isMyMove = false
                    moveOnline(data.toString(), snapshot.value.toString(), isMyMove)
                } else {
                    isMyMove = true
                    moveOnline(data.toString(), snapshot.value.toString(), isMyMove)
                }*/


                if(snapshot.value == "X") {
                    //Toast.makeText(this@OnlineMultiplayerGameActivity, "bis zum X", Toast.LENGTH_SHORT).show()
                    player1.add(data!!.toInt())
                    emptyCells.add(data!!.toInt())
                    isMyMove = !isCodeMaker
                    updateField(data!!.toInt(), snapshot.value.toString())
                } else if(snapshot.value == "O"){
                    //Toast.makeText(this@OnlineMultiplayerGameActivity, "bis zum O", Toast.LENGTH_SHORT).show()
                    player2.add(data!!.toInt())
                    emptyCells.add(data!!.toInt())
                    isMyMove = isCodeMaker
                    updateField(data!!.toInt(), snapshot.value.toString())
                }
                //isMyMove = !isMyMove

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                reset()
                //Toast.makeText(this@OnlineMultiplayerGameActivity, "Game Reset", Toast.LENGTH_SHORT).show()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //loadActiveGame()
    }
    /*fun moveOnline(data : String, symbol : String, move : Boolean) {
        if(move) {
            var buttonSelected : Button?
            buttonSelected = when(data.toInt()) {
                1 -> box1Btn
                2 -> box2Btn
                3 -> box3Btn
                4 -> box4Btn
                5 -> box5Btn
                6 -> box6Btn
                7 -> box7Btn
                8 -> box8Btn
                9 -> box9Btn
                else -> {box1Btn}
            }
            buttonSelected.text = if(!isCodeMaker) {
                "X"
            } else {
                "O"
            }
            turnTV.text = "Turn: Player 1"
            buttonSelected.setTextColor(Color.parseColor("#D22BB804"))
            player2.add(data.toInt())
            emptyCells.add(data.toInt())
            buttonSelected.isEnabled = false
            checkWinner()
        }
    }*/

    fun checkWinner() : Int {
        if( player1.contains(1) && player1.contains(2) && player1.contains(3) ||
            player1.contains(4) && player1.contains(5) && player1.contains(6) ||
            player1.contains(7) && player1.contains(8) && player1.contains(9) ||
            player1.contains(1) && player1.contains(4) && player1.contains(7) ||
            player1.contains(2) && player1.contains(5) && player1.contains(8) ||
            player1.contains(3) && player1.contains(6) && player1.contains(9) ||
            player1.contains(1) && player1.contains(5) && player1.contains(9) ||
            player1.contains(7) && player1.contains(5) && player1.contains(3) ){
            player1count += 1
            buttonDisable()
            disableReset()
            val build = AlertDialog.Builder(this)
            myRef.child("data").child(code).child("Host").get().addOnSuccessListener {
                build.setTitle("Game Over")
                build.setMessage("${it.value} has won!" + "\n\n" + "Do you want to play again")
            }

            build.setPositiveButton("Ok"){dialog, which->
                reset()
            }
            build.setNegativeButton("Exit") {dialog, which->
                removeCode()
                reset()
                myRef.child("data").child(code).removeValue()
                exit = true
                myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").get().addOnSuccessListener {
                    if(it.value != null && it.value != ""){
                        myRef.child("Users").child(SplitString(it.value.toString())).child("Request").setValue("")
                        myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue("")
                    }
                    Handler(Looper.getMainLooper()).postDelayed({exitProcess(1)}, 200)
                }
            }
            Handler(Looper.getMainLooper()).postDelayed(Runnable { build.show() }, 2000)
            return 1
        } else if ( player2.contains(1) && player2.contains(2) && player2.contains(3) ||
            player2.contains(4) && player2.contains(5) && player2.contains(6) ||
            player2.contains(7) && player2.contains(8) && player2.contains(9) ||
            player2.contains(1) && player2.contains(4) && player2.contains(7) ||
            player2.contains(2) && player2.contains(5) && player2.contains(8) ||
            player2.contains(3) && player2.contains(6) && player2.contains(9) ||
            player2.contains(1) && player2.contains(5) && player2.contains(9) ||
            player2.contains(7) && player2.contains(5) && player2.contains(3) ){
            player2count += 1
            buttonDisable()
            disableReset()
            val build = AlertDialog.Builder(this)
            myRef.child("data").child(code).child("Guest").get().addOnSuccessListener {
                build.setTitle("Game Over")
                build.setMessage("${it.value} has won the game" + "\n\n" + "Do you want to play again")
            }

            build.setPositiveButton("Ok"){dialog, which->
                reset()
            }
            build.setNegativeButton("Exit") {dialog, which->
                removeCode()
                reset()
                myRef.child("data").child(code).removeValue()
                exit = true
                myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").get().addOnSuccessListener {
                    if(it.value != null && it.value != ""){
                        myRef.child("Users").child(SplitString(it.value.toString())).child("Request").setValue("")
                        myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").setValue("")
                    }
                    Handler(Looper.getMainLooper()).postDelayed({exitProcess(1)}, 200)
                }

            }
            Handler(Looper.getMainLooper()).postDelayed(Runnable { build.show() }, 2000)
            return 1
        } else if(emptyCells.contains(1) && emptyCells.contains(2) && emptyCells.contains(3) && emptyCells.contains(4) &&
            emptyCells.contains(5) && emptyCells.contains(6) && emptyCells.contains(7) && emptyCells.contains(8) && emptyCells.contains(9)) {
            val build = AlertDialog.Builder(this)
            build.setTitle("Game Draw")
            build.setMessage("Game Draw" + "\n\n" + "Do you want to play again")
            build.setPositiveButton("Ok") {dialog, which ->
                reset()
            }
            build.setNegativeButton("Exit") {dialog, which ->
                exitProcess(1)
                reset()
                myRef.child("data").child(code).removeValue()
                removeCode()
            }
            build.show()
            return 1
        }
        return 0
    }

    /*fun playNow(buttonSelected : Button, currCell : Int) {
        buttonSelected.text = if(isCodeMaker) {
            "X"
        } else {
            "O"
        }
        emptyCells.remove(currCell)
        turnTV.text = "Turn : Player 2"
        buttonSelected.setTextColor(Color.parseColor("#EC0C0C"))
        player1.add(currCell)
        emptyCells.add(currCell)
        buttonSelected.isEnabled = false
        checkWinner()
    }*/

    private fun reset() {
        player1.clear()
        player2.clear()
        emptyCells.clear()
        activeUser = 1
        for (i in 1..9) {
            var buttonSelected : Button
            buttonSelected = when(i) {
                1 -> box1Btn
                2 -> box2Btn
                3 -> box3Btn
                4 -> box4Btn
                5 -> box5Btn
                6 -> box6Btn
                7 -> box7Btn
                8 -> box8Btn
                9 -> box9Btn
                else -> {box1Btn}
            }
            buttonSelected.isEnabled = true
            buttonSelected.text = ""
            isMyMove = isCodeMaker
            //f (isCodeMaker) {
            myRef.child("data").child(code).child("Field").removeValue()
            //}
        }
    }

    fun buttonDisable() {
        for (i in 1..9) {
            val buttonSelected = when (i){
                1 -> box1Btn
                2 -> box2Btn
                3 -> box3Btn
                4 -> box4Btn
                5 -> box5Btn
                6 -> box6Btn
                7 -> box7Btn
                8 -> box8Btn
                9 -> box9Btn
                else -> {box1Btn}
            }
            if (buttonSelected.isEnabled){
                buttonSelected.isEnabled = false
            }
        }
    }

    fun removeCode() {
        if (isCodeMaker) {
            myRef.child("codes").child(keyValue).removeValue()
        }
    }

    fun disableReset() {
        resetBtn.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed(Runnable { resetBtn.isEnabled = true }, 200)
    }

    fun updateDatabase(cellid : Int) {
        //myRef.child("data").child(code).push().setValue(cellid)
        if(isCodeMaker){
            myRef.child("data").child(code).child("Field").child(cellid.toString()).setValue("X")
        } else {
            myRef.child("data").child(code).child("Field").child(cellid.toString()).setValue("O")
        }
    }

    fun updateField(cellid: Int, symbol : String) {
        val buttonSelected = when (cellid){
            1 -> box1Btn
            2 -> box2Btn
            3 -> box3Btn
            4 -> box4Btn
            5 -> box5Btn
            6 -> box6Btn
            7 -> box7Btn
            8 -> box8Btn
            9 -> box9Btn
            else -> {box1Btn}
        }
        if (buttonSelected.isEnabled){
            buttonSelected.isEnabled = false
        }
        buttonSelected.text = symbol
        checkWinner()
    }

    /*override fun onBackPressed() {
        removeCode()
        if (isCodeMaker) {
            myRef.child("data").child(code).removeValue()
        }
        exitProcess(0)
    }*/

    fun onClick(view: View) {
        /*if(isMyMove) {
            val but = view as Button
            var cellOnline = 0
            when (but.id) {
                R.id.topleft    -> cellOnline = 1
                R.id.topmid     -> cellOnline = 2
                R.id.topright   -> cellOnline = 3
                R.id.midleft    -> cellOnline = 4
                R.id.midmid     -> cellOnline = 5
                R.id.midright   -> cellOnline = 6
                R.id.botleft    -> cellOnline = 7
                R.id.botmid     -> cellOnline = 8
                R.id.botright   -> cellOnline = 9
                else -> {cellOnline = 0}
            }
            playerTurn = false
            Handler(Looper.getMainLooper()).postDelayed(Runnable { playerTurn = true }, 600)
            playNow(but, cellOnline)
            updateDatabase(cellOnline)
        }*/
        //Toast.makeText(this@OnlineMultiplayerGameActivity, isMyMove.toString(), Toast.LENGTH_SHORT).show()
        if(isMyMove){
            val but = view as Button
            var cellOnline = 0
            when (but.id) {
                R.id.topleft    -> cellOnline = 1
                R.id.topmid     -> cellOnline = 2
                R.id.topright   -> cellOnline = 3
                R.id.midleft    -> cellOnline = 4
                R.id.midmid     -> cellOnline = 5
                R.id.midright   -> cellOnline = 6
                R.id.botleft    -> cellOnline = 7
                R.id.botmid     -> cellOnline = 8
                R.id.botright   -> cellOnline = 9
                else -> {cellOnline = 0}
            }
            //but.isEnabled = false
            updateDatabase(cellOnline)

        }
    }

    /*fun loadActiveGame(){
        myRef.child("data").child(code).child("isHostTurn").get().addOnSuccessListener {
            if(it.value != null){
                isMyMove = if (isCodeMaker) {
                    it.value as Boolean
                } else {
                    !(it.value as Boolean)
                }
                //Toast.makeText(this@OnlineMultiplayerGameActivity, isMyMove.toString(), Toast.LENGTH_SHORT).show()

            }

        }

        for(i in 1..9) {
            myRef.child("data").child(code).child(i.toString()).get().addOnSuccessListener {
                if(it.value == "X") {
                    player1.add(i)
                    emptyCells.add(i)
                } else if(it.value == "O"){
                    player2.add(i)
                    emptyCells.add(i)
                }
            }
        }

        /*for (i in player1) {
            Toast.makeText(this@OnlineMultiplayerGameActivity, i.toString(), Toast.LENGTH_SHORT).show()

            val buttonSelected = when (i){
                1 -> box1Btn
                2 -> box2Btn
                3 -> box3Btn
                4 -> box4Btn
                5 -> box5Btn
                6 -> box6Btn
                7 -> box7Btn
                8 -> box8Btn
                9 -> box9Btn
                else -> {box1Btn}
            }
            if (buttonSelected.isEnabled){
                buttonSelected.isEnabled = false
            }


            buttonSelected.text = "X"
        }
        for (i in player2) {
            val buttonSelected = when (i){
                1 -> box1Btn
                2 -> box2Btn
                3 -> box3Btn
                4 -> box4Btn
                5 -> box5Btn
                6 -> box6Btn
                7 -> box7Btn
                8 -> box8Btn
                9 -> box9Btn
                else -> {box1Btn}
            }
            if (buttonSelected.isEnabled){
                buttonSelected.isEnabled = false
            }
            buttonSelected.text = "O"
        }*/
    }*/


    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }
}