package com.tzlog.dotlib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_chooselog.*
import java.io.File

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

        const val REQUEST_CODE = 1001
        const val RESULT_CODE = 1002
        const val DATA_CODE = "KLogChooseData"

        fun enterForResult(ctx: Activity){
            var i = Intent(ctx,KLogChooseActivity::class.java)
            ctx.startActivityForResult(i,REQUEST_CODE)
        }
    }

    val listdata = arrayListOf<IData>()
    var mAdapter: MyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooselog)
        initData()

    }

    fun initData(){

        var dirs = File(KLog.getDirPath())
        if(dirs.exists() && dirs.isDirectory){
            var list = dirs.listFiles()
            list?.run {
                for (f in list){
                    listdata.add(IData(f,false))
                }
            }
        }
        //   /storage/emulated/0/Android/data/com.tzlog.dot/cache/log/KLog-2019-06-12H18.txt
//        for (i in 30 downTo 0 step 1) {
//            //for循环从30开始降到0，每次减1
//        }
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter =  MyAdapter(this,listdata)
        mRecyclerView.adapter = mAdapter
        tv_cancel.setOnClickListener { v->
            setResult(RESULT_CODE,null)
            finish()
        }
        tv_sure.setOnClickListener {
            mAdapter?.run {
                val _list = arrayListOf<String>()
                for (i in this.mDatas.size-1 downTo 0 step 1){
                    if(this.mDatas[i].checked){
                        _list.add(this.mDatas[i].file.absolutePath)
                    }
                }
                val i = Intent()
                i.putExtra(DATA_CODE,_list)
                setResult(RESULT_CODE,i)
            }
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CODE,null)
        super.onBackPressed()
    }

}