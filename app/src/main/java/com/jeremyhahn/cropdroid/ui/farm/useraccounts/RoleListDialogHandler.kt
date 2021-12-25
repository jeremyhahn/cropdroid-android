package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import com.jeremyhahn.cropdroid.model.RoleConfig
import com.jeremyhahn.cropdroid.model.UserConfig

interface RoleListDialogHandler {
    fun onRoleSelection(user: UserConfig, role: RoleConfig)
}