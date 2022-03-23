package com.example.testapplication
import android.util.Log
import androidx.lifecycle.MutableLiveData

class ArithmeticsGameLogic (viewmodel: ArithmeticsViewModel){

    var liveExpr = MutableLiveData<Triple<Int, Int, Char>>()
    var liveScore = MutableLiveData<Int>().apply { value = 0 }

    var operators = arrayListOf('+', '-', '×', '÷')

    var operand1 = 0
    var operand2 = 0
    var operator = '+'

    var expressions = arrayListOf<Triple<Int, Int, Char>>()
    var exprCounter = 0

    var viewmodel = viewmodel

    fun networkOnFieldUpdate(data : String?) {
    }

    fun start() {

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

    fun enter(result: String?) {

        var trueResult = result?.toIntOrNull()

        if(trueResult == null){
            viewmodel.score -= 1
            liveScore.value = viewmodel.score
            cycle()
            return
        }
        var mult = 0
        if(operator == '+'){
            if(trueResult == operand1 + operand2){
                mult = 1
            }
            else mult = -1
        }
        if(operator == '-'){
            if(trueResult == operand1 - operand2){
                mult = 1
            }
            else mult = -1
        }
        if(operator == '×'){
            if(trueResult == operand1 * operand2){
                mult = 1
            }
            else mult = -1
        }
        if(operator == '÷'){
            if(trueResult == operand1 / operand2){
                mult = 1
            }
            else mult = -1
        }
        viewmodel.score += 1*mult
        liveScore.value = viewmodel.score
        cycle()
    }

    private fun cycle(){

        liveExpr.value = expressions[exprCounter]
        liveScore.value = viewmodel.score
        operand1 = expressions[exprCounter].first
        operand2 = expressions[exprCounter].second
        operator = expressions[exprCounter].third
        exprCounter++

    }

}