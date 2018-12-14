package com.freejeff.pull2refreshlayout.lib

interface IRefreshHeader {

    fun onPrepare()

    fun onMove(currentY:Int,headerHeight:Int)

    fun onRefresh()

    fun onRefreshFinish()

    fun onReset()

}