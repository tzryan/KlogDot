package com.tzlog.dotlib

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/8/6 14:25
 * @Package: com.tzlog.dotlib
 * @Description:
 **/
class LogAdapter<T, K : BaseViewHolder> : BaseQuickAdapter<T, K> {

    constructor(layoutResId: Int, list: List<T>) : super(layoutResId, list)

    var localCheck = false

    override fun convert(helper: K, item: T) {
        val iData: IData = item as IData
        helper.setText(R.id.item_text, "${iData.file.name}-${readFileSize(iData.file.length())}")

        if (localCheck) {
            helper.addOnLongClickListener(R.id.item_text)
        }
        helper.addOnClickListener(R.id.item_text)

        helper.getView<CheckBox>(R.id.item_checkbox).isChecked = iData.checked
    }

    private fun readFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / 1024.0.pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }
}

