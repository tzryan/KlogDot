package com.tzlog.dotlib

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.activity_chooselog.*

/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/6/11 11:34
 * @Package: com.tzlog.dotlib
 * @Description:
 **/
class KLogChooseActivity : AppCompatActivity() {

    companion object{
        fun enter(ctx: Context){
            var i = Intent(ctx,KLogChooseActivity::class.java)
            ctx.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_chooselog)
        setSupportActionBar(top_toolbar)
        initData()
    }

    fun initData(){
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setTitle(R.string.toolsbar_title)
        }
    }
}