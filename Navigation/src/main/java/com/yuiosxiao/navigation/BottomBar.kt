package com.yuiosxiao.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class BottomBar: View {

    private var containerId: Int = 0
    private val fragmentClassList = ArrayList<Class<*>>()
    private val titleList = ArrayList<String>()
    private val iconResBeforeList = ArrayList<Int>()
    private val iconResAfterList = ArrayList<Int>()

    private val fragmentList = ArrayList<Fragment>()

    private var itemCount: Int = 0

    private val paint = Paint()

    private val iconBitmapBeforeList = ArrayList<Bitmap?>()

    private val iconBitmapAfterList = ArrayList<Bitmap?>()
    private val iconRectList = ArrayList<Rect>()

    var currentIndex: Int = 0
    private var firstCheckedIndex: Int = 0

    private var titleColorBefore = Color.parseColor("#999999")
    private var titleColorAfter = Color.parseColor("#ff5d5e")

    private var titleSizeInDp = 10
    private var iconWidth = 20
    private var iconHeight = 20
    private var titleIconMargin = 5

    private var titleBaseLine: Int = 0
    private val titleXList = ArrayList<Int>()

    private var parentItemWidth: Int = 0

    private var target = -1

    private var currentFragment: Fragment? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private lateinit var listen: (Int) -> Unit

    fun setClickListen(listen:(Int) -> Unit) {
        this.listen = listen
    }

    fun setContainer(containerId: Int): BottomBar {
        this.containerId = containerId
        return this
    }
    /**
     * 设置选择前和选择后的标题颜色
     */
    fun setTitleBeforeAndAfterColor(beforeResCode: String, afterResCode: String): BottomBar {
        titleColorBefore = Color.parseColor(beforeResCode)
        titleColorAfter = Color.parseColor(afterResCode)
        return this
    }
    /**
     * 设置标题大小
     */
    fun setTitleSize(titleSizeInDp: Int): BottomBar {
        this.titleSizeInDp = titleSizeInDp
        return this
    }
    /**
     * 设置图片宽度
     */
    fun setIconWidth(iconWidth: Int): BottomBar {
        this.iconWidth = iconWidth
        return this
    }
    /**
     * 设置标题和图片间的距离
     */
    fun setTitleIconMargin(titleIconMargin: Int): BottomBar {
        this.titleIconMargin = titleIconMargin
        return this
    }
    /**
     * 设置图片高度
     */
    fun setIconHeight(iconHeight: Int): BottomBar {
        this.iconHeight = iconHeight
        return this
    }

    fun addItem(fragmentClss: Class<*>,
                title: String,
                iconResBefore: Int,
                iconResAfter: Int): BottomBar {
        fragmentClassList.add(fragmentClss)
        titleList.add(title)
        iconResBeforeList.add(iconResBefore)
        iconResAfterList.add(iconResAfter)
        return this
    }

    fun setFirstChecked(firstCheckedIndex: Int): BottomBar {
        this.firstCheckedIndex = firstCheckedIndex
        return this
    }

    fun build() {
        itemCount = fragmentClassList.size
        for (i in 0 until itemCount) {
            val beforeBitmap = getBitmap(iconResBeforeList[i])
            iconBitmapBeforeList.add(beforeBitmap)

            val afterBitmap = getBitmap(iconResAfterList[i])
            iconBitmapAfterList.add(afterBitmap)

            val rect = Rect()
            iconRectList.add(rect)

            val clx = fragmentClassList[i]

            var fragment: Fragment? = null
            try {
                fragment = clx.newInstance() as Fragment
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            fragmentList.add(fragment!!)
        }
        currentIndex = firstCheckedIndex
        switchFragment(currentIndex)

        invalidate()
    }

    private fun getBitmap(resId: Int): Bitmap? {
        if (resId == 0) return null
        val bitmapDrawable = ContextCompat.getDrawable(context, resId) as BitmapDrawable
        return bitmapDrawable.bitmap
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initParam()
    }

    private fun initParam() {
        if (itemCount != 0) {
            parentItemWidth = width / itemCount
            val parentItemHeight = height
            //图标边长
            val iconWidth = dp2px(this.iconWidth.toFloat())
            val iconHeight = dp2px(this.iconHeight.toFloat())
            //布标文字margin
            val textIconMargin = dp2px(titleIconMargin.toFloat() / 2)
            //标题高度
            val titleSize = dp2px(titleSizeInDp.toFloat())
            paint.textSize = titleSize.toFloat()
            val rect = Rect()
            paint.getTextBounds(titleList[0], 0, titleList[0].length, rect)
            val titleHeight = rect.height()

            val iconTop = (parentItemHeight - iconHeight - textIconMargin - titleHeight) / 2
            titleBaseLine = parentItemHeight - iconTop

            val firstRectX = (parentItemWidth - iconWidth) / 2
            for (i in 0 until itemCount) {
                val rectX = i * parentItemWidth + firstRectX
                val temp = iconRectList[i]

                temp.left = rectX
                temp.top = iconTop
                temp.right = rectX + iconWidth
                temp.bottom = iconTop + iconHeight
            }
            //标题
            for (i in 0 until itemCount) {
                val title = titleList[i]
                paint.getTextBounds(title, 0, title.length, rect)
                titleXList.add((parentItemWidth - rect.width()) / 2 + parentItemWidth * i)
            }
        }
    }

    private fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)//这里让view自身替我们画背景，如果指定的话
        if (itemCount != 0) {
            //画背景
            paint.isAntiAlias = false
            for (i in 0 until itemCount) {
                var bitmap: Bitmap? = null
                bitmap = if (i == currentIndex) {
                    iconBitmapAfterList[i]
                } else {
                    iconBitmapBeforeList[i]
                }
                if (bitmap == null) continue
                val rect = iconRectList[i]
                canvas?.drawBitmap(bitmap, null, rect, paint) //null代表bitmap全部画出
            }
            //画文字
            paint.isAntiAlias = true
            for (i in 0 until itemCount) {
                val title = titleList[i]
                if (i == currentIndex) {
                    paint.color = titleColorAfter
                } else {
                    paint.color = titleColorBefore
                }
                val x = titleXList[i]
                canvas?.drawText(title, x.toFloat(), titleBaseLine.toFloat(), paint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> target = withinWhichArea(event.x.toInt())
            MotionEvent.ACTION_UP -> {
                if (event.y >= 0 && target == withinWhichArea(event.x.toInt())) {
                    listen(target)
                    switchFragment(target)
                    currentIndex = target
                    invalidate()
                }
                target = -1
            }
        }
        return true
    }

    private fun withinWhichArea(x: Int): Int {
        return x / parentItemWidth
    }

    private fun switchFragment(whichFragment: Int) {
        val fragment = fragmentList[whichFragment]
        val frameLayoutId = containerId
        if (fragment != null) {
            val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            if (fragment.isAdded) {
                if (currentFragment != null) {
                    transaction.hide(currentFragment!!).show(fragment)
                } else {
                    transaction.show(fragment)
                }
            } else {
                if (currentFragment != null) {
                    transaction.hide(currentFragment!!).add(frameLayoutId, fragment)
                } else {
                    transaction.add(frameLayoutId, fragment)
                }
            }
            currentFragment = fragment
            transaction.commitAllowingStateLoss()
        }
    }
}