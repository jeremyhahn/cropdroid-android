package com.jeremyhahn.cropdroid.model

data class RecurrenceRule(var frequency: String, var interval: Int, var count: Int, var days: List<String>) {
    constructor() : this("", 0, 0, ArrayList<String>())
}