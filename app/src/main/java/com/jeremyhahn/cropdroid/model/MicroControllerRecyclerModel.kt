package com.jeremyhahn.cropdroid.model

data class MicroControllerRecyclerModel(val type : Int, val metric: Metric?, val channel: Channel?) {

    companion object {
        const val METRIC_TYPE = 0
        const val CHANNEL_TYPE = 1
    }
}