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
        tv.text = "Pull To Refresh"
    }

    override fun onMove(currentY: Int, headerHeight: Int) {
        if(Math.abs(currentY)<headerHeight){
            tv.text = "Pull To Refresh"
        }else{
            tv.text = "Release To Refresh"
        }
    }

    override fun onRefresh() {
        tv.text = "Refreshing"
    }

    override fun onRefreshFinish() {
        tv.text = "Refresh Finished"
    }

    override fun onReset() {
        tv.text = "Pull To Refresh"
    }

    init {
        View.inflate(context,R.layout.layout_header,this)
        tv = findViewById(R.id.tv)
    }
}