package com.freejeff.pull2refreshlayout

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.freejeff.pull2refreshlayout.lib.IRefreshHeader

class MyRefreshHeader(context: Context) : LinearLayout(context), IRefreshHeader {
    var tv: TextView
    var progressBar: ProgressBar
    private var ivArrow: ImageView

    override fun onPrepare() {
        tv.text = "Pull To Refresh"
    }

    override fun onMove(currentY: Int, headerHeight: Int) {
        Log.d("onMove","$currentY");
        if (Math.abs(currentY) < headerHeight) {
            ivArrow.setImageResource(R.mipmap.arrow_down)
            tv.text = "Pull To Refresh"
        } else {
            ivArrow.setImageResource(R.mipmap.arrow_up)
            tv.text = "Release To Refresh"
        }
    }

    override fun onRefresh() {
        ivArrow.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        tv.text = "Refreshing"
    }

    override fun onRefreshFinish(): Long {
        progressBar.visibility = View.GONE
        ivArrow.visibility = View.GONE
        tv.text = "Refresh Finished"
        setBackgroundColor(Color.YELLOW)
        return 1000
    }

    override fun onReset() {
        tv.text = "Pull To Refresh"
        progressBar.visibility = View.GONE
        ivArrow.visibility = View.VISIBLE
        ivArrow.setImageResource(R.mipmap.arrow_down)
    }

    init {
        View.inflate(context, R.layout.layout_header, this)
        tv = findViewById(R.id.tv)
        progressBar = findViewById(R.id.progressBar)
        ivArrow = findViewById(R.id.ivArrow)
    }
}