package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.testapplication.databinding.ActivityLoginBinding
import com.example.testapplication.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class Registration : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email:String = intent.getStringExtra("email").toString()
        val password:String = intent.getStringExtra("password").toString()

        if(email != null){
            binding.email.setText(email)
        }
        if(password != null){
            binding.password.setText(password)
        }


        binding.buttonreturn.setOnClickListener() {
            finish()
        }

        binding.buttonregister.setOnClickListener(){

            when {
                TextUtils.isEmpty(binding.email.text.toString().trim() { it <= ' '}) ->{

                    Toast.makeText(this, "Please Enter a Valid Email Address.", Toast.LENGTH_SHORT ).show()

                }
                TextUtils.isEmpty(binding.password.text.toString().trim() { it <= ' '}) ->{

                    Toast.makeText(this, "Please Enter a Valid Password.", Toast.LENGTH_SHORT ).show()

                }
                else ->{

                    val email = binding.email.text.toString()
                    val password = binding.password.text.toString()

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                            task ->

                        if (task.isSuccessful) {

                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            MyApplication.myRef.child("FriendCodes").child(FirebaseAuth.getInstance().uid.toString()).setValue(FirebaseAuth.getInstance().currentUser!!.email)
                            FirebaseMessaging.getInstance().token.addOnSuccessListener {

                                if(it != null){
                                MyApplication.myRef.child("MessagingTokens").child(FirebaseAuth.getInstance().uid.toString()).setValue(it)}
                            }


                            //Anlegen der Zähler für die Statistiken
                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("GamesPlayed").setValue(0)
                            val key: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("Win%").push().getKey()
                            val map: MutableMap<String, Any> = HashMap()
                            map[key!!] = 0
                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.TICTACTOE.toString()).child("Win%").updateChildren(map)

                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("GamesPlayed").setValue(0)
                            val key2: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("Win%").push().getKey()
                            val map2: MutableMap<String, Any> = HashMap()
                            map2[key2!!] = 0
                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("Win%").updateChildren(map)

                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("GamesPlayed").setValue(0)
                            val key3: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("Win%").push().getKey()
                            val map3: MutableMap<String, Any> = HashMap()
                            map3[key3!!] = 0
                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.ARITHMETICS.toString()).child("HighScore").updateChildren(map)

                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("GamesPlayed").setValue(0)
                            val key4: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("Win%").push().getKey()
                            val map4: MutableMap<String, Any> = HashMap()
                            map4[key4!!] = 0
                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("Win%").updateChildren(map)

                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("GamesPlayed").setValue(0)
                            val key5: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("Win%").push().getKey()
                            val map5: MutableMap<String, Any> = HashMap()
                            map5[key5!!] = 0
                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.SCHRITTZAEHLER.toString()).child("HighScore").updateChildren(map)

                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.COMPASS.toString()).child("GamesPlayed").setValue(0)
                            val key6: String? = MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.COMPASS.toString()).child("Win%").push().getKey()
                            val map6: MutableMap<String, Any> = HashMap()
                            map6[key6!!] = 0
                            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child(GameNames.COMPASS.toString()).child("Win%").updateChildren(map)



                            Toast.makeText(this, "Registration Successful.", Toast.LENGTH_SHORT ).show()

                        }
                        else{

                            Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT ).show()

                        }


                    }

                }



            }
            }


    }

    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }
}