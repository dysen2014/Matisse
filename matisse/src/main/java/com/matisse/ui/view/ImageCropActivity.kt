package com.matisse.ui.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.gyf.barlibrary.ImmersionBar
import com.matisse.R
import com.matisse.entity.ConstValue
import com.matisse.internal.entity.SelectionSpec
import com.matisse.ui.BaseActivity
import com.matisse.utils.BitmapUtils
import com.matisse.utils.Platform
import com.matisse.utils.StatusBarUtil
import com.matisse.utils.UIUtils
import com.matisse.widget.CropImageView
import com.matisse.widget.IncapableDialog
import kotlinx.android.synthetic.main.activity_crop.*
import kotlinx.android.synthetic.main.include_view_navigation.*
import java.io.File

class ImageCropActivity : BaseActivity(), View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener {

    private lateinit var cv_crop_image: CropImageView

    private var mBitmap: Bitmap? = null
    private var mIsSaveRectangle: Boolean = false
    private var mOutputX: Int = 0
    private var mOutputY: Int = 0
    private lateinit var mSpec: SelectionSpec
    private lateinit var imagePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        mSpec = SelectionSpec.getInstance()
        setTheme(mSpec.themeId)
        super.onCreate(savedInstanceState)

        baseSetContentView(R.layout.activity_crop)

        if (Platform.isClassExists("com.gyf.barlibrary.ImmersionBar")) {
            ImmersionBar.with(this).titleBar(toolbar)?.statusBarDarkFont(mSpec.isDarkStatus)?.init()
        }

        if (mSpec.needOrientationRestriction()) {
            requestedOrientation = mSpec.orientation
        }

        imagePath = intent.getStringExtra(ConstValue.EXTRA_RESULT_SELECTION_PATH)

        initView()
        initCropFun()
    }

    private fun initView() {

        cv_crop_image = findViewById(R.id.cv_crop_image)
        button_complete.setOnClickListener(this)
        button_back.setOnClickListener(this)
    }

    private fun initCropFun() {
        mOutputX = mSpec.cropOutPutX
        mOutputY = mSpec.cropOutPutY
        mIsSaveRectangle = mSpec.isCropSaveRectangle

        val cropFocusNormalWidth = UIUtils.getScreenWidth(this) - UIUtils.dp2px(this, 50f).toInt()
        val cropFocusNormalHeight = UIUtils.getScreenHeight(this) - UIUtils.dp2px(this, 400f).toInt()

        val cropWidth = if (mSpec.cropFocusWidth in 1..(cropFocusNormalWidth - 1))
            mSpec.cropFocusWidth else cropFocusNormalWidth

        val cropHeight = if (mSpec.cropFocusHeight in 1..(cropFocusNormalHeight - 1))
            mSpec.cropFocusHeight else cropFocusNormalHeight

        cv_crop_image.setFocusStyle(mSpec.cropStyle)
        cv_crop_image.setFocusWidth(cropWidth)
        cv_crop_image.setFocusHeight(cropHeight)
        cv_crop_image.setOnBitmapSaveCompleteListener(this)

        //缩放图片
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        val displayMetrics = resources.displayMetrics
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels)
        options.inJustDecodeBounds = false
        mBitmap = BitmapFactory.decodeFile(imagePath, options)
        mBitmap?.let {
            val rotateBitmap = cv_crop_image.rotate(it, BitmapUtils.getBitmapDegree(imagePath).toFloat())
            // 设置默认旋转角度
            cv_crop_image.setImageBitmap(rotateBitmap)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            inSampleSize = if (width > height) {
                width / reqWidth
            } else {
                height / reqHeight
            }
        }
        return inSampleSize
    }

    override fun onBitmapSaveSuccess(file: File) {
        val intent = Intent()
        intent.putExtra(ConstValue.EXTRA_RESULT_BUNDLE, file.absolutePath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBitmapSaveError(file: File) {
        val incapableDialog = IncapableDialog.newInstance("",
                getString(R.string.error_crop))
        incapableDialog.show(supportFragmentManager, IncapableDialog::class.java.name)
    }

    override fun onClick(v: View?) {
        when (v) {
            button_complete -> cv_crop_image.saveBitmapToFile(getCropCacheFolder(this), mOutputX, mOutputY, mIsSaveRectangle)
            button_back -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun getCropCacheFolder(context: Context): File {
        return if (mSpec.cropCacheFolder != null && mSpec.cropCacheFolder?.exists() == true && mSpec.cropCacheFolder?.isDirectory == true) {
            mSpec.cropCacheFolder!!
        } else {
            File(context.cacheDir.toString() + "/Matisse/cropTemp/")
        }
    }

    override fun onDestroy() {
        if (Platform.isClassExists("com.gyf.barlibrary.ImmersionBar")) {
            ImmersionBar.with(this).destroy()
        }
        cv_crop_image.setOnBitmapSaveCompleteListener(null)
        if (null != mBitmap && !mBitmap?.isRecycled!!) {
            mBitmap?.recycle()
            mBitmap = null
        }
        super.onDestroy()
    }
}