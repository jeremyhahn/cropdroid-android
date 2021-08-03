package com.jeremyhahn.cropdroid.ui.workflow

import com.jeremyhahn.cropdroid.model.WorkflowStep

interface NewWorkflowStepDialogHandler {
    fun onWorkflowStepDialogApply(workflowStep: WorkflowStep)
}