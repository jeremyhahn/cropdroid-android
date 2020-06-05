package com.jeremyhahn.cropdroid.config

import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.Farm

interface ConfigSubject {
    fun register(o: ConfigObserver)
    fun unregister(o :ConfigObserver)
    fun updateObservers(farmConfig: Farm)
}

interface ConfigObserver {
    //fun update(farmConfig: Farm)
    fun updateConfig(controller: Controller)
    //fun updateMetrics(controller: Controller)
}