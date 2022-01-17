package com.example.testapplication

import android.app.Application

//Globale Kontroll Variablen. Sollten (Hoffe ich) nicht bei Application Death sterben ähnlich wie Zeug im Viewmodel.
class MyApplication : Application() {
    companion object {
        var globalSelectedGame = GameNames.NONE
        var isCodeMaker = true;
        var code = "null"
        var codeFound = false
        var checkTemp = true
        var keyValue : String = "null"
    }
}