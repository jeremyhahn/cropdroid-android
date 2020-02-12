package com.jeremyhahn.cropdroid

import androidx.fragment.app.FragmentPagerAdapter
import android.content.Context;
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class TabAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> {
                //  val homeFragment: HomeFragment = HomeFragment()
                return RoomFragment()
            }
            1 -> {
                return ReservoirFragment()
            }
            2 -> {
                // val movieFragment = MovieFragment()
                return DoserFragment()
            }
            else -> return null
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}