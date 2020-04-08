package com.jeremyhahn.cropdroid.model

import java.util.*

data class Schedule(val id: Int, var channelId: Int, var startDate: Calendar, var endDate: Calendar?, var frequency: Int,
        var interval: Int, var count: Int, var days: ArrayList<String>) {

        constructor() : this(0, 0, Calendar.getInstance(), null, 0, 0, 0, ArrayList())
}