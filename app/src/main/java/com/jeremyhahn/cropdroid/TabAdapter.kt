package com.jeremyhahn.cropdroid

import androidx.fragment.app.FragmentPagerAdapter
import android.content.Context;
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

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