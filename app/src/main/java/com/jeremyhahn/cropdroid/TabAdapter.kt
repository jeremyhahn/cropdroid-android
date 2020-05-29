package com.jeremyhahn.cropdroid

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jeremyhahn.cropdroid.ui.doser.DoserFragment
import com.jeremyhahn.cropdroid.ui.events.EventListFragment
import com.jeremyhahn.cropdroid.ui.reservoir.ReservoirFragment
import com.jeremyhahn.cropdroid.ui.room.RoomFragment

class TabAdapter(manager: FragmentManager, tabs:  ArrayList<String>) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabCount: Int
    private val tabs: List<String>
    private val fragments: List<Fragment>

    init {
        this.tabs = tabs
        this.tabCount = tabs.size
        this.fragments = ArrayList(tabCount)
        fragments.add(RoomFragment())
        fragments.add(ReservoirFragment())
        fragments.add(DoserFragment())
        fragments.add(EventListFragment())
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return this.tabs[position]
    }

    override fun getCount(): Int {
        return this.tabCount
    }
}
