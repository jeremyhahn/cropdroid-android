package com.jeremyhahn.cropdroid

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jeremyhahn.cropdroid.ui.doser.DoserFragment
import com.jeremyhahn.cropdroid.ui.events.EventListFragment
import com.jeremyhahn.cropdroid.ui.reservoir.ReservoirFragment
import com.jeremyhahn.cropdroid.ui.room.RoomFragment

class TabAdapter(manager: FragmentManager, tabCount: Int) : FragmentPagerAdapter(manager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var tabCount = 0
    var fragments = ArrayList<Fragment>(4)

    init {
        this.tabCount = tabCount
        fragments.add(RoomFragment())
        fragments.add(ReservoirFragment())
        fragments.add(DoserFragment())
        fragments.add(EventListFragment())
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return fragments[0]
            }
            1 -> {
                return fragments[1]
            }
            2 -> {
                return fragments[2]
            }
            3 -> {
                return fragments[3]
            }
            else -> return fragments[0]
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}
