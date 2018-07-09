package com.iwallic.app.navigator


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.iwallic.app.R

class FindFragment : Fragment() {
    companion object {
        val TAG: String = FindFragment::class.java.simpleName
        fun newInstance() = FindFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_find, container, false)
        return view
    }
}
