package com.tzlog.dotlib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.activity_chooselog.*
import kotlinx.android.synthetic.main.adapter_footer.*
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

    companion object {

        const val REQUEST_CODE = 1001
        const val RESULT_CODE = 1002
        const val DATA_CODE = "KLogChooseData"
        const val LOCAL_CHECK_CODE = "localCheck"

        /**
         * @param localCheck 是否支持点击,本地查看
         * **/
        fun enterForResult(ctx: Activity, localCheck: Boolean = false) {
            val i = Intent(ctx, KLogChooseActivity::class.java)
            i.putExtra(LOCAL_CHECK_CODE, localCheck)
            ctx.startActivityForResult(i, REQUEST_CODE)
        }
    }

    val listdata = arrayListOf<IData>()
    var mAdapter: LogAdapter<IData, BaseViewHolder>? = null
    var localCheck = false
    var tv_no_data: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooselog)
        initExtraData()
        initData()
    }

    private fun initExtraData() {
        localCheck = intent.getBooleanExtra(LOCAL_CHECK_CODE, false)
    }

    @SuppressLint("SetTextI18n")
    fun initData() {
        val dirs = File(KLog.getDirPath())
        if (dirs.exists() && dirs.isDirectory) {
            val list = dirs.listFiles()
            list?.run {
                for (i: Int in (list.size - 1) downTo 0 step 1) {
                    listdata.add(IData(list[i], false))
                }
            }
        }
        //   /storage/emulated/0/Android/data/com.tzlog.dot/cache/log/KLog-2019-06-12H18.txt
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = LogAdapter(R.layout.adapter_item_layout, listdata)
        mAdapter?.localCheck = localCheck
        mRecyclerView.adapter = mAdapter

        registerAdapterClick()

        tv_cancel.setOnClickListener { v ->
            setResult(RESULT_CODE, null)
            finish()
        }
        tv_sure.setOnClickListener {
            mAdapter?.run {
                val _list = arrayListOf<String>()
                for (item in this.data) {
                    if (item.checked) {
                        _list.add((item).file.absolutePath)
                    }
                }
                if (_list.size <= 0) {
                    Toast.makeText(this@KLogChooseActivity, "请选择上传日志", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val i = Intent()
                i.putExtra(DATA_CODE, _list)
                setResult(RESULT_CODE, i)
            }
            finish()
        }
        if (listdata.size <= 0) {
            tv_no_data?.text = "暂无日志~"
        } else {
            val time = if (KLog.config.mSaveDays < 1)
                "1天内"
            else
                "${KLog.config.mSaveDays.toInt()}天"
            tv_no_data?.text = "只保存最近${time}的日志~"
        }
    }


    @SuppressLint("InflateParams")
    private fun registerAdapterClick() {

        val footer = LayoutInflater.from(this).inflate(R.layout.adapter_footer, null)
        tv_no_data = footer.findViewById(R.id.tv_no_data)
        mAdapter?.addFooterView(footer)

        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.item_text -> {
                    val iData = (adapter.getItem(position) as IData)
                    if (iData.checked) {
                        (adapter.data[position] as IData).checked = false
                        mAdapter?.notifyDataSetChanged()
                    } else {
                        var _num = 0
                        for (item in adapter.data) {
                            if ((item as IData).checked) {
                                _num++
                            }
                        }
                        if (_num >= KLog.config.maxChooseSize) {
                            Toast.makeText(view.context, "最多选择${KLog.config.maxChooseSize}个", Toast.LENGTH_SHORT).show()
                        } else {
                            (adapter.data[position] as IData).checked = true
                            mAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        mAdapter?.setOnItemChildLongClickListener { adapter, view, position ->
            when (view?.id) {
                R.id.item_text -> {
                    val iData = (adapter?.getItem(position) as IData)
                    startIntent(iData.file)
                }
            }
            true
        }

    }

    private fun startIntent(file: File) {
        //先判断是否是txt文件，否则打不开
        val intent_file: File?
        intent_file = if (!file.name.endsWith(".txt", true)) {
            val newNameFile = File(file.absolutePath + ".txt")
            file.renameTo(newNameFile)
            newNameFile
        } else {
            file
        }

        var kLogDataKey = "klogdot.fileProvider"
        try {
            kLogDataKey = KLog.sAppContext?.packageName.toString() + ".fileProvider"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val intent = Intent(Intent.ACTION_VIEW)
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(this, kLogDataKey, intent_file)
            //intent.setDataAndType(contentUri, "application/vnd.android.package-archive")//打开安装APP
            intent.setDataAndType(contentUri, "text/plain")
        } else {
            intent.setDataAndType(Uri.fromFile(intent_file), "text/plain")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        this.startActivity(intent)
    }

    override fun onBackPressed() {
        setResult(RESULT_CODE, null)
        super.onBackPressed()
    }

}