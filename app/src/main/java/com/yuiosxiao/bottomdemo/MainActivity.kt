package com.yuiosxiao.bottomdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBottomBar()
    }

    private fun initBottomBar() {
        bottom_bar
            .setContainer(R.id.fl_content)
            .setTitleSize(12)
            .setIconHeight(22)
            .setIconWidth(22)
            .setTitleBeforeAndAfterColor("#999999", "#666666")
            .addItem(FragmentA::class.java, "首页", R.mipmap.tab_home_unselect, R.mipmap.tab_home_select)
            .addItem(FragmentB::class.java, "我的", R.mipmap.tab_circle_unselect, R.mipmap.tab_circle_select)
            .build()
        bottom_bar.setClickListen {
            println(it)
        }

        bottom_bar.currentIndex = 1
        bottom_bar.setCurrentIndex(1)
    }
}