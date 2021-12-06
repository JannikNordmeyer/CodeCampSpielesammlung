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

class Registration : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}