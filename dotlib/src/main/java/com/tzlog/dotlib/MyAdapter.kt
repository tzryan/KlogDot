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
    //这是构造方法
    constructor(context: Context, list: List<IData>) {
        this.mContext = context
        this.mDatas = list
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
        holder.checkBox.isChecked = mDatas[position].checked
        holder.checkBox.setOnCheckedChangeListener {
            buttonView, isChecked ->
            run {
                mDatas[position].checked = isChecked
            }
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemText: TextView = itemView.findViewById(R.id.item_text)
        var checkBox: CheckBox = itemView.findViewById(R.id.item_checkbox)
    }
}

data class IData constructor(var file: File, var checked:Boolean) :Serializable
