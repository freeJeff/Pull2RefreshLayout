package com.freejeff.pull2refreshlayout.lib

import android.content.Context
import android.widget.LinearLayout

class DefaultRefreshHeader(context: Context): LinearLayout(context),IRefreshHeader {
    override fun onPrepare() {
    }

    override fun onMove(currentY: Int, headerHeight: Int) {
    }

    override fun onRefresh() {
    }

    override fun onRefreshFinish() {
    }

    override fun onReset() {
    }

}