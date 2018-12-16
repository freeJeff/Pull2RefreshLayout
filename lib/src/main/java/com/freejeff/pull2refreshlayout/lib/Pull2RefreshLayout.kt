package com.freejeff.pull2refreshlayout.lib

import android.content.Context
import android.support.v4.view.NestedScrollingParent
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Scroller
import java.lang.IllegalStateException
import java.lang.reflect.Constructor

class Pull2RefreshLayout : ViewGroup, NestedScrollingParent {


    private lateinit var refreshHeaderView: View
    private var targetView: View? = null
    private var mScroller: Scroller = Scroller(context)
    private var lastY: Int = 0
    var refreshListener: IRefreshListener? = null
    private var isRefreshing = false
    var refreshState: RefreshState = RefreshState.INITIAL


    companion object {
        const val DEFAULT_DAMP = 0.7f
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
        val maxScrollHeight = refreshHeaderView.height + getOverScrollHeight()
        val refreshHeader = refreshHeaderView as IRefreshHeader
//        Log.d("onTouch","${event.actionMasked}  ${refreshState.name}")
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                lastY = event.y.toInt()
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                if (scrollY < 0 && Math.abs(scrollY) < refreshHeaderView.height) {
                    refreshState = RefreshState.PULLING
                    Log.d("STATE", "PULLING")
                } else if (Math.abs(scrollY) >= refreshHeaderView.height) {
                    refreshState = RefreshState.HOLDING
                    Log.d("STATE", "HOLDING")
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val currentScrollY = scrollY
                var deltaY = (event.y - lastY)
                val damp = DEFAULT_DAMP
                deltaY -= deltaY * damp
                var destY = currentScrollY - deltaY.toInt()
                if (destY >= 0) {
                    destY = 0
                }
//                else if (Math.abs(destY) > maxScrollHeight) {
//                    destY = -maxScrollHeight.toInt()
//                }
                scrollTo(0, destY)
                if (refreshState == RefreshState.INITIAL) {
                    refreshState = RefreshState.PULLING
                    Log.d("STATE", "PULLIng")
                    refreshHeader.onPrepare()
                } else if (Math.abs(destY) < refreshHeaderView.height && refreshState != RefreshState.PULLING) {
                    refreshState = RefreshState.PULLING
                    Log.d("STATE", "PULLIng")
                } else if (refreshState == RefreshState.PULLING && Math.abs(destY) >= refreshHeaderView.height) {
                    refreshState = RefreshState.HOLDING
                    Log.d("STATE", "HOLDING")
                }
                lastY = event.y.toInt()
            }

            MotionEvent.ACTION_UP -> {
                if (scrollY < 0) {
                    if (scrollY <= -refreshHeaderView.height && refreshState == RefreshState.HOLDING) {
                        refreshState = RefreshState.RELEASE_BACK
                        Log.d("STATE", "RELEASE_BACK")
                        smoothScrollTo(-refreshHeaderView.height, 200)
                    } else {
                        refreshState = RefreshState.RETURNING
                        Log.d("STATE", "RETURNING")
                        smoothScrollBack()
                    }
                }
            }


        }
        return false
    }


    private fun doRefresh() {
        (refreshHeaderView as IRefreshHeader).onRefresh()
        refreshListener?.onRefresh()
    }

    fun finishRefresh() {
        if (!mScroller.isFinished) mScroller.abortAnimation()
        (refreshHeaderView as IRefreshHeader).onRefreshFinish()
        smoothScrollBack()
    }

    private fun getOverScrollHeight() = refreshHeaderView.height * 0.75


    private fun smoothScrollBack() {
        refreshState = RefreshState.RETURNING
        Log.d("STATE", "RETURNING")
        smoothScrollTo(0, 800)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (t > 0) return
        val refreshHeader = refreshHeaderView as IRefreshHeader
        refreshHeader.onMove(t, refreshHeaderView.height)
        if (t == -refreshHeaderView.height && refreshState == RefreshState.RELEASE_BACK) {
            doRefresh()
        } else if (t == 0 && (refreshState == RefreshState.RETURNING || refreshState == RefreshState.PULLING)) {
            refreshState = RefreshState.INITIAL
            Log.d("STATE", "INITIAL")
            refreshHeader.onReset()
        }
    }


    private fun smoothScrollTo(destY: Int, duration: Int) {
        if (!mScroller.isFinished) mScroller.abortAnimation()
        val deltaY = destY - scrollY;
        mScroller.startScroll(scrollX, scrollY, 0, deltaY, duration)
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