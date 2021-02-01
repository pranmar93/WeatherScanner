package com.example.mainapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mainapplication.fragment.LaterFragment
import com.example.mainapplication.fragment.TodayFragment
import com.example.mainapplication.fragment.TomorrowFragment

class ViewPagerAdapter(manager: FragmentManager, lifeCycle: Lifecycle) : FragmentStateAdapter(manager, lifeCycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> TodayFragment()
            1 -> TomorrowFragment()
            else -> LaterFragment()
        }
    }
}