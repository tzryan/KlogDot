package com.tzlog.dotlib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_chooselog.*
import java.io.File
import java.text.DecimalFormat

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
        const val LOCAL_CHECK_CODE = "localCheck"

        fun enterForResult(ctx: Activity,localCheck:Boolean = false){
            var i = Intent(ctx,KLogChooseActivity::class.java)
            i.putExtra(LOCAL_CHECK_CODE,localCheck)
            ctx.startActivityForResult(i,REQUEST_CODE)
        }
    }

    val listdata = arrayListOf<IData>()
    var mAdapter: MyAdapter? = null
    var localCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooselog)
        initExtraData()
        initData()

    }

    private fun initExtraData() {
        localCheck = intent.getBooleanExtra(LOCAL_CHECK_CODE,false)
    }

    @SuppressLint("SetTextI18n")
    fun initData(){

        var dirs = File(KLog.getDirPath())
        if(dirs.exists() && dirs.isDirectory){
            var list = dirs.listFiles()
            list?.run {
//                for (f in list){
                for ( i : Int  in (list.size-1) downTo 0 step 1){
                    listdata.add(IData(list[i],false))
                }
            }
        }
        //   /storage/emulated/0/Android/data/com.tzlog.dot/cache/log/KLog-2019-06-12H18.txt
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter =  MyAdapter(this,listdata,KLog.config.maxChooseSize)
        mAdapter?.localCheck = localCheck
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
                if(_list.size <= 0){
                    Toast.makeText(this@KLogChooseActivity,"请选择上传日志",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val i = Intent()
                i.putExtra(DATA_CODE,_list)
                setResult(RESULT_CODE,i)
            }
            finish()
        }
        if(listdata.size <= 0){
            tv_no_data.visibility = View.VISIBLE
            scroll_view.visibility = View.GONE
        }else{
            tv_no_data.visibility = View.GONE
            scroll_view.visibility = View.VISIBLE
        }
        var time = if(KLog.config.mSaveDays < 1)
            "1天内"
        else
            "${KLog.config.mSaveDays.toInt()}天"
        tv_tip.text = "只保存最近${time}的日志~"
    }

    override fun onBackPressed() {
        setResult(RESULT_CODE,null)
        super.onBackPressed()
    }

}