package com.example.testapplication
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class ArithmeticsGameLogic (){

    var liveExpr = MutableLiveData<Triple<Int, Int, Char>>()
    var liveScore = MutableLiveData<Int>().apply { value = 0 }

    var operators = arrayListOf('+', '-', '×', '÷')

    var operand1 = 0
    var operand2 = 0
    var operator = '+'

    var expressions = arrayListOf<Triple<Int, Int, Char>>()
    var exprCounter = 0

    fun networkOnFieldUpdate(data : String?){}

    fun start() {

        var i = 0
        while(i < 38){

            operator = operators[(0..3).random()]

            if(operator == '+'){
                operand1 = (0..150).random()
                operand2 = (0..150).random()
                liveExpr.value = Triple(operand1, operand2, operator)
            }
            if(operator == '-'){
                var a = (0..150).random()
                var b = (0..150).random()
                operand1 = maxOf(a, b)
                operand2 = minOf(a, b)
                liveExpr.value = Triple(operand1, operand2, operator)
            }
            if(operator == '×'){
                operand1 = (0..20).random()
                operand2 = (0..20).random()
                liveExpr.value = Triple(operand1, operand2, operator)
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

            }

            expressions.add(Triple(operand1, operand2, operator))
            i++
        }

        cycle()
    }
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
            if(result == operand1 / operand2){
                liveScore.value = liveScore.value!!.plus(1)
            }
        }

        cycle()


    }

    fun cycle(){

        liveExpr.value = expressions[exprCounter]
        operand1 = expressions[exprCounter].first
        operand2 = expressions[exprCounter].second
        operator = expressions[exprCounter].third
        exprCounter++


    }

}