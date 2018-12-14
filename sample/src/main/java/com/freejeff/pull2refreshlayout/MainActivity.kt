package com.freejeff.pull2refreshlayout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.Toast
import com.freejeff.pull2refreshlayout.lib.IRefreshListener
import com.freejeff.pull2refreshlayout.lib.Pull2RefreshLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.btn)
        btn.setOnClickListener { Toast.makeText(this,"Hello",Toast.LENGTH_SHORT).show() }
        val refreshLayout = findViewById<Pull2RefreshLayout>(R.id.refreshLayout)
        refreshLayout.refreshListener = object:IRefreshListener{
            override fun onRefresh() {
                Handler().postDelayed({refreshLayout.finishRefresh()},2000)
            }
        }
    }
}
