package com.freejeff.pull2refreshlayout

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.freejeff.pull2refreshlayout.lib.IRefreshHeader

class MyRefreshHeader(context: Context): LinearLayout(context),IRefreshHeader {
    lateinit var tv:TextView

    override fun onPrepare() {
        Log.d("STATE","onPrepare")
        tv.text = "Wait Refresh"
    }

    override fun onMove(currentY: Int, headerHeight: Int) {
//        Log.d("STATE","$currentY -- $headerHeight")
    }

    override fun onRefresh() {
        Log.d("STATE","onRefresh")
        tv.text = "Refreshing"
    }

    override fun onRefreshFinish() {
        Log.d("STATE","onRefreshFinish")
        tv.text = "refresh finish"
    }

    override fun onReset() {
        Log.d("STATE","onReset")
        tv.text = "Wait Refresh"
    }

    init {
        View.inflate(context,R.layout.layout_header,this)
        tv = findViewById(R.id.tv)
    }
}