package com.jeremyhahn.cropdroid.model

import java.util.*
import kotlin.collections.ArrayList

data class Workflow(var id: Long, val farmId: Long, var name: String,
                    val conditions: List<Condition>, val schedules: List<Schedule>,
                    var steps: List<WorkflowStep>, val lastCompleted: Calendar?) {

    constructor(id: Long) : this(0, 0, "", ArrayList(), ArrayList(), ArrayList(), null) {
        this.id = id
    }
    constructor(name: String) : this(0, 0, "", ArrayList(), ArrayList(), ArrayList(), null) {
        this.name = name
    }
}

