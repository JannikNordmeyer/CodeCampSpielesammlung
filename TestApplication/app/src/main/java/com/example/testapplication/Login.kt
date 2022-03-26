package com.example.testapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.testapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonreturn.setOnClickListener(){
            finish()
        }

        binding.buttonregister.setOnClickListener(){

            val intent = Intent(this, Registration::class.java);
            intent.putExtra("email", binding.email.text.toString())
            intent.putExtra("password", binding.password.text.toString())
            startActivity(intent)

        }

        //Führt Login in der Firebase-Datenbank durch
        binding.buttonlogin.setOnClickListener(){

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

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {

                            task ->

                        if (task.isSuccessful) {

                            Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT ).show()

                            val intent = Intent(this, GameSelect::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                        }
                        else{

                            Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT ).show()
                        }
                    }
                }
            }
        }
    }
}