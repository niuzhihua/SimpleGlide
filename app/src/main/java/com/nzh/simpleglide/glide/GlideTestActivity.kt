package com.nzh.simpleglide.glide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.nzh.myglide.MyGlide
import com.nzh.myglide.lifecycle.request.RequestOption
import com.nzh.simpleglide.R
import kotlinx.android.synthetic.main.activity_glide_test.*


class GlideTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_glide_test)

    }


    fun show(view: View) {
        val modelIfeng = "http://p0.ifengimg.com/ifeng/index/20150921/ifengLogo.png"
        val option = RequestOption()
        option.errorId = R.mipmap.icon_failure
        option.placeHolderId = R.mipmap.icon_loading
//        MyGlide.with(this).load(modelIfeng).apply(option).into(image)


        val modelBaidu = "https://www.baidu.com/img/PCfb_5bf082d29588c07f842ccde3f97243ea.png"
//        MyGlide.with(this).load(modelBaidu).apply(option).into(image2)

        val option2 = RequestOption()
        option2.overrideHeight = 80
        option2.overrideHeight = 80
        val modelBaidu2 = "https://www.baidu.com/img/PCfb_5bf082d29588c07f842ccde3f97243ea.png"
        MyGlide.with(this).load(modelBaidu2).apply(option2).into(image3)
    }
}