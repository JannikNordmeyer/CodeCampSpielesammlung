package com.example.testapplication

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentArithmeticsBinding

class Arithmetics : Fragment() {

    private lateinit var binding: FragmentArithmeticsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        binding = FragmentArithmeticsBinding.inflate(inflater,container,false)
        val view = binding.root
        val viewmodel = ViewModelProvider(requireActivity()).get(ArithmeticsViewModel::class.java)


        viewmodel.logic.cycle()
        viewmodel.logic.liveExpr.observe(viewLifecycleOwner, ){
            binding.operand1.setText(it.first.toString())
            binding.operand2.setText(it.second.toString())
            binding.operator.setText(it.third.toString())
        }
        viewmodel.logic.liveScore.observe(viewLifecycleOwner, ){
            viewmodel.score += 1
        }

        binding.button.setOnClickListener(){
            viewmodel.enter(binding.resultField.text.toString().toIntOrNull())
            binding.resultField.setText("")
        }
        val timer = object: CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timer.setText((millisUntilFinished/1000).toString())
            }
            override fun onFinish() {
                Toast.makeText(context, "You have reached a score of " + viewmodel.score.toString() + ".", Toast.LENGTH_SHORT ).show()
            }
        }
        timer.start()



        return view
    }

}