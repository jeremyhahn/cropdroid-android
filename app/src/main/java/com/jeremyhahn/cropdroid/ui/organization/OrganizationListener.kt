package com.jeremyhahn.cropdroid.ui.organization

import com.jeremyhahn.cropdroid.model.Farm
import com.jeremyhahn.cropdroid.model.Organization

interface OrganizationListener {
    fun onOrganizationClick(position : Int)
    fun getOrganizations() : ArrayList<Organization>
    fun size() : Int
    fun clear()
    fun showContextMenu(position: Int)
}