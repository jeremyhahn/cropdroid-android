package com.jeremyhahn.cropdroid.ui.schedule

import com.jeremyhahn.cropdroid.model.Schedule

interface ScheduleSelectionListener {
    fun onScheduleSelected(schedule: Schedule)
}