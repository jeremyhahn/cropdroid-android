package com.jeremyhahn.cropdroid.model

data class Reservoir(var mem : Int, var waterTemp : Double,
       var PH : Double, var EC : Double, var TDS : Double, var SAL : Double, var SG : Double,
       var DO_mgL : Double, var DO_PER: Double, var ORP : Double,
       var envTemp : Double, var envHumidity : Double, var envHeatIndex : Double,
       var upperFloat : Int, var lowerFloat : Int)
