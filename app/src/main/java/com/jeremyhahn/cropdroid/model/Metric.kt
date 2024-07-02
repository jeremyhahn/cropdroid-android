package com.jeremyhahn.cropdroid.model

data class Metric(var id: Long, var controllerId: Long, var datatype: Int, var key: String, var name: String, var enable: Boolean, var notify: Boolean,
   var unit: String, var alarmLow: Double, var alarmHigh: Double, var value: Double) {

   fun isEnabled() : Boolean {
      return enable
   }
}