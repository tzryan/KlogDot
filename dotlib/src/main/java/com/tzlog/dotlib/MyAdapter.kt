package com.tzlog.dotlib

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.Serializable
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider


/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/6/12 14:19
 * @Package: com.tzlog.dotlib
 * @Description:
 **/
class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder> {

    var mContext: Context
    var mDatas: List<IData>
    var mInflater: LayoutInflater
    var localCheck = false
    var maxSize = 3

    //这是构造方法
    constructor(context: Context, list: List<IData>, maxChooseSize: Int = 3) {
        this.mContext = context
        this.mDatas = list
        this.maxSize = maxChooseSize
        mInflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View = mInflater.inflate(R.layout.adapter_item_layout, parent,false)
        val holder: ViewHolder = ViewHolder(itemView)
        return holder
    }

    override fun getItemCount(): Int {
        if (mDatas.size > 0 && mDatas != null) {
            return mDatas.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemText.text = mDatas[position].file.name
        if(localCheck){
            holder.itemText.setOnClickListener {
                startIntent(mDatas[position].file)
            }
        }
        holder.checkBox.isChecked = mDatas[position].checked
        holder.checkBox.setOnCheckedChangeListener {
            buttonView, isChecked ->
            var _count = 0
            for(item in mDatas){
                if(item.checked)
                    _count ++
            }
            if(_count < maxSize){
                run {
                    mDatas[position].checked = isChecked
                }
            }else{
                if(isChecked){
                    //超过数量，用户选true，强制改false
                    holder.checkBox.isChecked = false
                    mDatas[position].checked = false
                    Toast.makeText(mContext,"最多选择${maxSize}个",Toast.LENGTH_SHORT).show()
                }else{
                    //超过数量，用户取消
                    mDatas[position].checked = false
                }
            }
        }
    }


    private fun startIntent(file:File){

        var kLogDataKey = "klogdot.fileProvider"
        try {
//            val appInfo = KLog.sAppContext?.packageManager?.getApplicationInfo(KLog.sAppContext?.packageName, GET_META_DATA)
//            kLogDataKey = appInfo?.metaData?.getString("kLogDataKey").toString()
            kLogDataKey = KLog.sAppContext?.packageName.toString() + ".fileProvider"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val intent = Intent(Intent.ACTION_VIEW)
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//            val contentUri  = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileProvider", file)
            val contentUri  = FileProvider.getUriForFile(mContext,  kLogDataKey, file)
//            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")//打开安装APP
            intent.setDataAndType(contentUri, "text/plain")
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")//打开安装App
            intent.setDataAndType(Uri.fromFile(file), "text/plain")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        mContext.startActivity(intent)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemText: TextView = itemView.findViewById(R.id.item_text)
        var checkBox: CheckBox = itemView.findViewById(R.id.item_checkbox)
    }
}

data class IData constructor(var file: File, var checked:Boolean) :Serializable
