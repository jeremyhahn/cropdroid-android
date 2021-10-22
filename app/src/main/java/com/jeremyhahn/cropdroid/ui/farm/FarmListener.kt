package com.jeremyhahn.cropdroid.ui.farm

import com.jeremyhahn.cropdroid.model.Farm

interface FarmListener {
    fun onFarmClick(position : Int)
//    fun createFarm(orgId: Long)
//    fun deleteFarm(farmId : Long)
    fun getFarms() : ArrayList<Farm>
    fun size() : Int
    fun clear()
    fun showContextMenu(position: Int)
}