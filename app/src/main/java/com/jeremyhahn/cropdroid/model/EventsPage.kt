package com.jeremyhahn.cropdroid.model


data class EventsPage(var events: ArrayList<EventLog>, var page: Int, var size: Int, var count: Int, var start: Int, var end: Int)