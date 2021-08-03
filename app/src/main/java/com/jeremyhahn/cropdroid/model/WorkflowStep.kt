package com.jeremyhahn.cropdroid.model

data class WorkflowStep(val id: Long, val workflowId: Long, val deviceId: Long, var deviceType: String,
                        val channelId: Long, var channelName: String, val webhook: String, val duration: Int,
                        val wait: Int, val state: Int, var text: String) {

    //constructor() : this(0, 0, 0, "", 0,"","",0, 0, 0, "")

    constructor(id: Long, workflowId: Long) : this(id, workflowId, 0, "", 0,"","",0, 0, 0, "")

    constructor(id: Long, workflowId: Long, deviceId: Long, channelId: Long, webhook: String, duration: Int, wait: Int, state: Int) :
            this(id, workflowId, 0, "", 0,"","",0, 0, 0, "")
}