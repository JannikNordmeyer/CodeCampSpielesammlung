package com.example.testapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testapplication.databinding.FragmentTictactoeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.system.exitProcess


class TicTacToe : Fragment() {

    private lateinit var binding: FragmentTictactoeBinding

    //cant save @ as key in the database so this function returns only the first part of the emil that is used as the key instead
    fun SplitString(str:String): String{
        var split=str.split("@")
        return split[0]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentTictactoeBinding.inflate(inflater,container,false)
        val view = binding.root

        val viewmodel = ViewModelProvider(requireActivity()).get(TicTacToeViewModel::class.java) //Shared Viewmodel w/ GameHolder

        if(MyApplication.onlineMode){

            //Setup Listener to exit game if other person leaves? TODO: Ask Yonas what is going on here
            MyApplication.myRef.child("Users").child(SplitString(FirebaseAuth.getInstance().currentUser!!.email.toString())).child("Request").addChildEventListener(object :
                ChildEventListener {
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!viewmodel.exit) {
                        exitProcess(1)
                    }
                }
                //region
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
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
                //endregion
            })

            //Add Listener so that if something changes in a field, perform code. TODO: Once again, can probably happen in GameHolder.
            //TODO: GameHolder should just trigger the Fragment's "NetworkOnFieldUpdate" Function for modularity.
            MyApplication.myRef.child("data").child(MyApplication.code).child("Field").addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    var data = snapshot.key

                    //TODO: Optimize this, unnecessary duplicate code + should use the companion object enum
                    if(snapshot.value == "X") {
                        viewmodel.player1.add(data!!.toInt())
                        viewmodel.emptyCells.add(data!!.toInt())
                        MyApplication.myTurn = !MyApplication.isCodeMaker
                        //TODO: Trigger Observer instead by updating the viewmodel board!
                        //e.g. viewmodel.logic.liveboard/winner.value = [stuff]
                        //updateField(data!!.toInt(), snapshot.value.toString())
                    } else if(snapshot.value == "O"){
                        viewmodel.player2.add(data!!.toInt())
                        viewmodel.emptyCells.add(data!!.toInt())
                        MyApplication.myTurn = MyApplication.isCodeMaker    //Not negated

                        //updateField(data!!.toInt(), snapshot.value.toString())
                    }

                }
                //region
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    //TODO: Implement Reset?
                    //reset()
                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                //endregion
            })

        }

        fun updatePrompt(){
            if(viewmodel.logic.winner == null){
                if(viewmodel.logic.player == "X"){
                    binding.playerprompt.setText("Player 1's Turn:")}
                else{
                    binding.playerprompt.setText("Player 2's Turn:")
                }
            }
            else{
                if(viewmodel.logic.winner == 0){
                    binding.playerprompt.setText("It's a Draw.")
                }
                else{
                    binding.playerprompt.setText("Player "+viewmodel.logic.winner+" wins.")
                }
            }
        }

        viewmodel.logic.livewinner.observe(viewLifecycleOwner, ){
            updatePrompt()
        }

        viewmodel.logic.liveboard.observe(viewLifecycleOwner, {
            binding.topleft.setText(viewmodel.logic.board[0][0])
            binding.topmid.setText(viewmodel.logic.board[0][1])
            binding.topright.setText(viewmodel.logic.board[0][2])

            binding.midleft.setText(viewmodel.logic.board[1][0])
            binding.midmid.setText(viewmodel.logic.board[1][1])
            binding.midright.setText(viewmodel.logic.board[1][2])

            binding.botleft.setText(viewmodel.logic.board[2][0])
            binding.botmid.setText(viewmodel.logic.board[2][1])
            binding.botright.setText(viewmodel.logic.board[2][2])
            updatePrompt()
        })

        binding.topleft.setOnClickListener(){ viewmodel.click(0, 0) }
        binding.topmid.setOnClickListener(){ viewmodel.click(0, 1) }
        binding.topright.setOnClickListener(){ viewmodel.click(0, 2) }
        binding.midleft.setOnClickListener(){ viewmodel.click(1, 0) }
        binding.midmid.setOnClickListener(){ viewmodel.click(1, 1) }
        binding.midright.setOnClickListener(){ viewmodel.click(1, 2) }
        binding.botleft.setOnClickListener(){ viewmodel.click(2, 0) }
        binding.botmid.setOnClickListener(){ viewmodel.click(2, 1) }
        binding.botright.setOnClickListener(){ viewmodel.click(2, 2) }

        return view
    }

}