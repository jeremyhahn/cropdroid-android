package com.jeremyhahn.cropdroid.ui.workflow

import com.jeremyhahn.cropdroid.model.Workflow

interface NewWorkflowDialogHandler {
    fun onWorkflowDialogApply(workflow: Workflow)
}