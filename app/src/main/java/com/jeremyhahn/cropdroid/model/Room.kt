package com.jeremyhahn.cropdroid.model

data class Room (val mem: Int,
     val tempF0: Double, val tempC0: Double, val humidity0: Double, val heatIndex0: Double,
     val tempF1: Double, val tempC1: Double, val humidity1: Double, val heatIndex1: Double,
     val tempF2: Double, val tempC2: Double, val humidity2: Double, val heatIndex2: Double,
     val vpd: Double, val pod0: Double, val pod1: Double, val co2: Double,
     val water0: Int, val water1: Int, val photo :Int)