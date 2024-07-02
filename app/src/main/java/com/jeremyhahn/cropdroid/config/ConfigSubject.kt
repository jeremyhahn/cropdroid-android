package com.jeremyhahn.cropdroid.config

import com.jeremyhahn.cropdroid.model.*

interface ConfigObserver {
    fun setConfig(controller: Controller)
    fun setState(state: ControllerState)
    fun setStateDelta(delta: ControllerStateDelta)
}