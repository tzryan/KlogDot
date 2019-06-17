package com.tzlog.dot

import android.app.Application
import com.tzlog.dotlib.KLog
import java.util.ArrayList

/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/6/17 13:16
 * @Package: com.tzlog.dot
 * @Description:
 **/
class MyApp :Application(){

    override fun onCreate() {
        super.onCreate()
        KLog.init(this)
            .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
            .setGlobalTag("车轮驾考通")// 设置log全局标签，默认为空
            .setFilePrefix("KLog")// 当文件前缀为空时，默认为"KLog"，即写入文件为"tlog-MM-dd.txt"
            .setSingleTagSwitch(false)// 一条日志仅输出一条，默认开，为美化 AS 3.1.0 的 Logcat
            .setSaveDays(3f)//0.0007f 设置日志可保留天数，默认为 -1 表示无限时长  (1分钟=0.00069f)
            // 新增 ArrayList 格式化器，默认已支持 Array, Throwable, Bundle, Intent 的格式化输出
            .addFormatter(object : KLog.IFormatter<ArrayList<*>> {
                override fun format(t: ArrayList<*>?): String {
                    return "KLog Formatter ArrayList { $t }"
                }
            })
    }
}