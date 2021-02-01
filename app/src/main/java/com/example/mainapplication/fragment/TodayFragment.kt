package com.example.mainapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mainapplication.R
import com.example.mainapplication.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_recycler_view.view.*

class TodayFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_recycler_view, container, false)

        root.recyclerView.adapter = (requireActivity() as MainActivity).getAdapter(this)
        return root
    }
}