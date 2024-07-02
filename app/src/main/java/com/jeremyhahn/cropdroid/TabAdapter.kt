package com.jeremyhahn.cropdroid

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerFragment
import java.util.concurrent.ConcurrentHashMap

class TabAdapter(manager: FragmentManager, tabs: ArrayList<String>,
   fragments: ConcurrentHashMap<String, ControllerFragment>) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabCount: Int
    private val tabs: List<String>
    private var fragments: ConcurrentHashMap<String, ControllerFragment>

    init {
        this.tabs = tabs
        this.tabCount = tabs.size
        this.fragments = fragments
    }

    override fun getItem(position: Int): Fragment {
        return fragments[tabs[position].toLowerCase()]!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return this.tabs[position]
    }

    override fun getCount(): Int {
        return this.tabCount
    }
}
