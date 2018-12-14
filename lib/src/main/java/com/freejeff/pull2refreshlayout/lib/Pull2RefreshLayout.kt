package com.freejeff.pull2refreshlayout.lib

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.view.NestedScrollingParent
import android.support.v4.view.NestedScrollingParent2
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Scroller
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.reflect.Constructor

class Pull2RefreshLayout : ViewGroup, NestedScrollingParent {


    private lateinit var refreshHeaderView: View
    private var targetView: View? = null
    private var mScroller: Scroller = Scroller(context)
    private var lastY: Int = 0
    var refreshListener: IRefreshListener? = null
    private var isRefreshing = false
    private var refreshState: RefreshState = RefreshState.STATE_INITIAL


    companion object {
        const val DEFAULT_DAMP = 0.5f
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        parseAttr(context, attributeSet)
        addView(refreshHeaderView)
    }


    private fun parseAttr(context: Context, attributeSet: AttributeSet?) {
        attributeSet?.let {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.Pull2RefreshLayout)
            val headerClassName = typedArray.getString(R.styleable.Pull2RefreshLayout_refreshHeader)
            if (headerClassName == null) {
                refreshHeaderView = DefaultRefreshHeader(context)
                return
            }
            val headerClass: Class<out View> = Class.forName(headerClassName) as Class<out View>
            val constructor: Constructor<out View> = headerClass.getConstructor(Context::class.java)
            refreshHeaderView = constructor.newInstance(context)
            if (refreshHeaderView !is IRefreshHeader) {
                throw IllegalStateException("refresh header must implementation IRefreshHeader")
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val headerWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth - paddingLeft - paddingRight, MeasureSpec.EXACTLY)
        val headerheightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight - paddingTop - paddingBottom, MeasureSpec.AT_MOST)
        refreshHeaderView.measure(headerWidthMeasureSpec, headerheightMeasureSpec)
        targetView?.let {
            measureChild(targetView, widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun addView(child: View?, params: LayoutParams?) {
        super.addView(child, params)
        if (targetView == null && childCount > 1) {
            targetView = getChildAt(1)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        refreshHeaderView.layout(
                0,
                -refreshHeaderView.measuredHeight,
                refreshHeaderView.measuredWidth,
                0)
        targetView?.let {
            it.layout(
                    paddingLeft,
                    paddingTop,
                    paddingLeft + it.measuredWidth,
                    paddingTop + it.measuredHeight)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isRefreshing) return false
        val maxScrollHeight = refreshHeaderView.height + getOverScrollHeight()
        val refreshHeader = refreshHeaderView as IRefreshHeader
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                if (event.y == 0f) {
                    refreshHeader.onPrepare()
                }
                lastY = event.y.toInt()
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                var deltaY = (event.y - lastY)
                var damp = Math.abs(scrollY.toFloat()) / maxScrollHeight.toFloat() * DEFAULT_DAMP
                damp = DEFAULT_DAMP
                Log.e("TAG", "$deltaY - ${deltaY * damp}")
                deltaY -= deltaY * damp
                var destY = scrollY - deltaY.toInt()
                if (destY >= 0) {
                    destY = 0
                } else if (Math.abs(destY) > maxScrollHeight) {
                    destY = -maxScrollHeight.toInt()
                }
                scrollTo(0, destY)
                refreshHeader.onMove(destY, refreshHeaderView.height)
                lastY = event.y.toInt()
            }

            MotionEvent.ACTION_UP -> {
                if (scrollY < 0) {
                    if (scrollY <= -refreshHeaderView.height) {
                        smoothScrollTo(0, -refreshHeaderView.height, 500)
                        doRefresh()
                    } else {
                        smoothScrollBack()
                    }
                }
            }


        }
        return false
    }

    private fun setRefreshState(refreshState: RefreshState) {
        val refreshHeader = refreshHeaderView as IRefreshHeader
        when (refreshState) {
            RefreshState.STATE_INITIAL -> {

            }

            RefreshState.STATE_PREPARE -> {

                refreshHeader.onPrepare()
            }

            RefreshState.STATE_PULLING -> {

            }

            RefreshState.STATE_HOLDING -> {

            }

            RefreshState.STATE_REFRESHING -> {

            }

            RefreshState.STATE_RETURNING -> {

            }
        }
        this.refreshState = refreshState
    }

    private fun doRefresh() {
        refreshListener?.onRefresh()
        (refreshHeaderView as IRefreshHeader).onRefresh()
        isRefreshing = true
    }

    fun finishRefresh() {
        if (!mScroller.isFinished) mScroller.abortAnimation()
        (refreshHeaderView as IRefreshHeader).onRefreshFinish()
        isRefreshing = false
        smoothScrollBack()
    }

    private fun getOverScrollHeight() = refreshHeaderView.height * 0.75


    private fun smoothScrollBack() {
        smoothScrollTo(0, 0, 1000)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (scrollY == 0 && isRefreshing) {
            (refreshHeaderView as IRefreshHeader).onReset()
        }
    }


    private fun smoothScrollTo(destX: Int, destY: Int, duration: Int) {
        if (!mScroller.isFinished) mScroller.abortAnimation()
        val deltaX = destX - scrollX;
        val deltaY = destY - scrollY;
        mScroller.startScroll(scrollX, scrollY, deltaX, deltaY, duration)
        invalidate()
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            postInvalidate()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val child = targetView
                if (child != null && child is ScrollView && child.scrollY != 0) {
                    return false
                }
                return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }


}