package com.jeremyhahn.cropdroid.ui.condition

import com.jeremyhahn.cropdroid.model.Condition

interface ConditionSelectionListener {
    fun onConditionSelected(schedule: Condition)
}