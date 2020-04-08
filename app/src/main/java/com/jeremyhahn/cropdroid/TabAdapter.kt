package com.jeremyhahn.cropdroid

import androidx.fragment.app.FragmentPagerAdapter
import android.content.Context;
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.jeremyhahn.cropdroid.ui.doser.DoserFragment
import com.jeremyhahn.cropdroid.ui.events.EventListFragment
import com.jeremyhahn.cropdroid.ui.reservoir.ReservoirFragment
import com.jeremyhahn.cropdroid.ui.room.RoomFragment

class TabAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return RoomFragment()
            }
            1 -> {
                return ReservoirFragment()
            }
            2 -> {
                return DoserFragment()
            }
            3 -> {
                return EventListFragment()
            }
            else -> return RoomFragment()
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}