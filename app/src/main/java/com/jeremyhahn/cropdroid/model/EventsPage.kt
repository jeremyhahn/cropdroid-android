package com.jeremyhahn.cropdroid.model

data class EventsPage(
    var events: ArrayList<EventLog>,
    var page: Int,
    var pageSize: Int,
    var hasMore: Boolean)