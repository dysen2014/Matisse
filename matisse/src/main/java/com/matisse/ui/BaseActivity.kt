package com.matisse.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.matisse.R
import com.matisse.utils.StatusBarUtil

/**
 * Describe :
 * Created by Leo on 2018/10/22 on 10:39.
 */
abstract class BaseActivity : AppCompatActivity() {

    var vContent : LinearLayout? = null
    var llBg : LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getLayoutId())
//        StatusBarUtil.setTransparent(this)
    }

    protected fun getLayoutId(): Int {
        return R.layout.activity_base
    }

    /**
     * set screen view
     *
     * @param layoutResID
     */
    fun baseSetContentView(layoutResID: Int) {
        vContent = findViewById(R.id.v_content) //v_content是在基类布局文件中预定义的layout区域
        val layoutParams = LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        //通过LayoutInflater填充基类的layout区域
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(layoutResID, null)
        vContent?.addView(v, layoutParams)
    }

    /**
     * 修改状态栏的夜色
     *
     * @param colorId
     */
    protected fun setStatusColor(colorId: Int) {

        if (llBg == null)
            llBg = findViewById(R.id.ll_bg)
        llBg?.setBackgroundColor(colorId)
        StatusBarUtil.setTransparent(this)
    }

    protected fun setStatusBG(drawableId: Int) {

        if (llBg == null)
            llBg = findViewById(R.id.ll_bg)
        llBg?.setBackgroundResource(drawableId)
        StatusBarUtil.setTransparent(this)
    }
}

