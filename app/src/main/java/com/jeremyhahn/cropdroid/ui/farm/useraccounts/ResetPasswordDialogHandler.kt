package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import com.jeremyhahn.cropdroid.model.UserConfig

interface ResetPasswordDialogHandler {
    fun onResetPassword(user: UserConfig)
}