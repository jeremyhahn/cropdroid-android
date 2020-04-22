package com.jeremyhahn.cropdroid.ui.condition

import com.jeremyhahn.cropdroid.model.ConditionConfig

interface ConditionDialogHandler {
    fun onConditionDialogApply(condition: ConditionConfig)
}