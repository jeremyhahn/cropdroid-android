package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import com.jeremyhahn.cropdroid.model.UserConfig

interface UserAccountsListener {
    fun onUserClick(position : Int)
    fun getUsers() : ArrayList<UserConfig>
    fun size() : Int
    fun clear()
    fun showRoleDialog(user: UserConfig)
//    fun createUser(user: UserConfig)
    fun deleteUser(user: UserConfig, position: Int)
    fun showSetPasswordDialog(user: UserConfig)
}