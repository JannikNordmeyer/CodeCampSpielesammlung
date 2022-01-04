package com.example.testapplication

import android.app.Application

class MyApplication : Application() {
    companion object {
        var globalSelectedGame = GameNames.NONE
    }
}