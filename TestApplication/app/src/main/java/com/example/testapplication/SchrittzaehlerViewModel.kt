package com.example.testapplication

import androidx.lifecycle.ViewModel

class SchrittzaehlerViewModel: ViewModel() {

    var logic = SchrittzaehlerLogic()

    var score = 0

}