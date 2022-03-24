package com.example.testapplication

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentArithmeticsBinding
import android.view.KeyEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class Arithmetics : Fragment() {

    private lateinit var binding: FragmentArithmeticsBinding
    lateinit var viewmodel: ArithmeticsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = FragmentArithmeticsBinding.inflate(inflater,container,false)
        val view = binding.root
        viewmodel = ViewModelProvider(requireActivity()).get(ArithmeticsViewModel::class.java)

        viewmodel.logic.start()

        viewmodel.logic.liveExpr.observe(viewLifecycleOwner){
            binding.operand1.text = it.first.toString()
            binding.operand2.text = it.second.toString()
            binding.operator.text = it.third.toString()
        }
        viewmodel.logic.liveScore.observe(viewLifecycleOwner){
            binding.score.text = (viewmodel.score*1000).toString()
        }

        //Submit
        binding.button.setOnClickListener(){
            if(binding.timer.text.toString().toInt() > 0) {
                val input = binding.resultField.text.toString()
                viewmodel.enter(input)
                binding.resultField.text.clear()
                binding.button.isEnabled = false
                binding.resultField.isEnabled = false
                val timer = object: CountDownTimer(800, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        binding.button.isEnabled = true
                        binding.resultField.isEnabled = true
                    }
                }
                timer.start()
            }
        }

        //On enter...
        binding.resultField.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            binding.button.performClick()
                            return true
                        }
                        else -> {}
                    }
                }
                return false
            }
        })

        //Timer
        val timer = object: CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timer.text = (millisUntilFinished/1000).toString()
            }
            override fun onFinish() {
                if(!MyApplication.onlineMode) {
                    Toast.makeText(context, "You have reached a score of " + (viewmodel.score).toString() + ".", Toast.LENGTH_SHORT).show()
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        this.start()
                        viewmodel.resetGame()
                    }, 6000)
                }else{
                    //Prüfe ob Raum existiert...
                    MyApplication.myRef.child("data").child(MyApplication.code).get().addOnSuccessListener {
                        if(it.value != null) {
                            if(MyApplication.isHost) {
                                //Schreib deinen score in die DB...
                                MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("HostScore").setValue(viewmodel.score)
                                    //Warte darauf das Guest seinen Score einträgt...
                                    MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.value != null) {
                                                    var networkWinner = ""
                                                    if (viewmodel.score > snapshot.value.toString().toInt()) {
                                                        networkWinner = MyApplication.hostID
                                                        } else if (viewmodel.score < snapshot.value.toString().toInt()) {
                                                            networkWinner = MyApplication.guestID
                                                        } else networkWinner = "-1"  //Draw
                                                        //Enter Winner
                                                        MyApplication.myRef.child("data").child(MyApplication.code).child("WinnerPlayer").setValue(networkWinner)
                                                        MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").removeEventListener(this)
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }

                                            })
                                    } else {
                                        //Schreib deinen score in die DB...
                                        MyApplication.myRef.child("data").child(MyApplication.code).child("Field").child("GuestScore").setValue(viewmodel.score)
                                        //Warte darauf das Host entscheidet wer gewonnen hat
                                    }
                            }
                    }
                }
            }
        }
        timer.start()
        viewmodel.gameTimer = timer
        return view
    }
    override fun onDestroy() {
        super.onDestroy()
        viewmodel.gameTimer.cancel()
    }

}