package com.example.testapplication
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class ArithmeticsGameLogic (viewmodel: ArithmeticsViewModel){

    var liveExpr = MutableLiveData<Triple<Int, Int, Char>>()
    var liveScore = MutableLiveData<Int>().apply { value = 0 }

    var operators = arrayListOf('+', '-', '×', '÷')

    var operand1 = 0
    var operand2 = 0
    var operator = '+'

    private val TAG = ArithmeticsGameLogic::class.java.simpleName

    var expressions = arrayListOf<Triple<Int, Int, Char>>()
    var exprCounter = 0

    var viewmodel = viewmodel

    fun networkOnFieldUpdate(data : String?) {
    }

    fun start() {

        Log.d("fug","##################### START AUFGERUFEN #####################")

        var i = 0
        expressions.clear()
        exprCounter = 0
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

    fun reset(){
        viewmodel.score = 0
        liveScore.value = viewmodel.score
    }

    fun enter(result: String?) {
        Log.d(TAG,"##################### ENTER ###########################")
        Log.d(TAG,"INPUT STRING: "+result)
        var true_result = result?.toIntOrNull()
        Log.d(TAG,"POST CONVERSION STRING: "+true_result)

        if(true_result == null){
            Log.d(TAG,"empty bitch")
            viewmodel.score -= 1
            liveScore.value = viewmodel.score
            cycle()
            return
        }
        var mult = 0
        if(operator == '+'){
            if(true_result == operand1 + operand2){
                mult = 1
            }
            else mult = -1
        }
        if(operator == '-'){
            if(true_result == operand1 - operand2){
                mult = 1
            }
            else mult = -1
        }
        if(operator == '×'){
            if(true_result == operand1 * operand2){
                mult = 1
            }
            else mult = -1
        }
        if(operator == '÷'){
            if(true_result == operand1 / operand2){
                mult = 1
            }
            else mult = -1
        }
        Log.d(TAG,"#####################################################################################################")
        Log.d(TAG,mult.toString())
        viewmodel.score += 1*mult
        Log.d(TAG, viewmodel.score.toString())
        liveScore.value = viewmodel.score
        cycle()
    }

    fun cycle(){

        liveExpr.value = expressions[exprCounter]
        liveScore.value = viewmodel.score
        operand1 = expressions[exprCounter].first
        operand2 = expressions[exprCounter].second
        operator = expressions[exprCounter].third
        exprCounter++


    }

}