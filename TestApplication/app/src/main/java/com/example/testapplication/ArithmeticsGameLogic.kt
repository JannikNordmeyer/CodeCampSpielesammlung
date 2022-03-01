package com.example.testapplication
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

class ArithmeticsGameLogic (){

    var liveExpr = MutableLiveData<Triple<Int, Int, Char>>()
    var liveScore = MutableLiveData<Int>().apply { value = 0 }

    var operators = arrayListOf<Char>('+', '-', '×', '÷')

    var operand1 = 0
    var operand2 = 0
    var operator = '+'

    fun networkOnFieldUpdate(data : String?){}

    fun enter(result: Int?) {

        if(result == null){
            cycle()
            return
        }
        if(operator == '+'){
            if(result == operand1 + operand2){
                liveScore.value = liveScore.value!!.plus(1)
            }
        }
        if(operator == '-'){
            if(result == operand1 - operand2){
                liveScore.value = liveScore.value!!.plus(1)
            }
        }
        if(operator == '×'){
            if(result == operand1 * operand2){
                liveScore.value = liveScore.value!!.plus(1)
            }
        }
        if(operator == '÷'){
            if(result == operand1 * operand2){
                liveScore.value = liveScore.value!!.plus(1)
            }
        }

        cycle()


    }

    fun cycle(){

        operator = operators[(0..3).random()]

        if(operator == '+'){
            operand1 = (0..150).random()
            operand2 = (0..150).random()
            liveExpr.value = Triple(operand1, operand2, operator)
            return
        }
        if(operator == '-'){
            var a = (0..150).random()
            var b = (0..150).random()
            operand1 = maxOf(a, b)
            operand2 = minOf(a, b)
            liveExpr.value = Triple(operand1, operand2, operator)
            return
        }
        if(operator == '×'){
            operand1 = (0..20).random()
            operand2 = (0..20).random()
            liveExpr.value = Triple(operand1, operand2, operator)
            return
        }
        if(operator == '÷'){
            operand1 = (10..25).random()
            var i = 1
            var divisors = arrayListOf<Int>()
            while(i < operand1){
                if(operand1 % i == 0){
                    divisors.add(i)
                }
                i++
            }
            operand2 = divisors[(0..divisors.size-1).random()]
            liveExpr.value = Triple(operand1, operand2, operator)
            return

        }


    }

}