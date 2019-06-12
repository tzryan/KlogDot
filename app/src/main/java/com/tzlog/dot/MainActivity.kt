package com.tzlog.dot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tzlog.dotlib.KLog
import com.tzlog.dotlib.KLogChooseActivity
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val JSON =
        "{\"tools\": [{ \"name\":\"css format\" , \"site\":\"http://tools.w3cschool.cn/code/css\" },{ \"name\":\"JSON format\" , \"site\":\"http://tools.w3cschool.cn/code/JSON\" },{ \"name\":\"pwd check\" , \"site\":\"http://tools.w3cschool.cn/password/my_password_safe\" }]}"
    private val XML =
        "<books><book><author>Jack Herrington</author><title>PHP Hacks</title><publisher>O'Reilly</publisher></book><book><author>Jack Herrington</author><title>Podcasting Hacks</title><publisher>O'Reilly</publisher></book></books>"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initLog()
    }

    private fun initLog() {
        KLog.init(this)
            .setLogSwitch(BuildConfig.DEBUG)// 设置log总开关，包括输出到控制台和文件，默认开
            .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
            .setGlobalTag(null)// 设置log全局标签，默认为空
            // 当全局标签不为空时，我们输出的log全部为该tag，
            // 为空时，如果传入的tag为空那就显示类名，否则显示tag
            .setLogHeadSwitch(true)// 设置log头信息开关，默认为开
            .setLog2FileSwitch(true)// 打印log时是否存到文件的开关，默认关
            .setDir("")// 当自定义路径为空时，写入应用的/cache/log/目录中
            .setFilePrefix("KLog")// 当文件前缀为空时，默认为"KLog"，即写入文件为"tlog-MM-dd.txt"
            .setBorderSwitch(true)// 输出日志是否带边框开关，默认开
            .setSingleTagSwitch(true)// 一条日志仅输出一条，默认开，为美化 AS 3.1.0 的 Logcat
            .setConsoleFilter(KLog.V)// log的控制台过滤器，和logcat过滤器同理，默认Verbose.
            .setFileFilter(KLog.V)// log文件过滤器，和logcat过滤器同理，默认Verbose
            .setStackDeep(1)// log 栈深度，默认为 1
            .setStackOffset(0)// 设置栈偏移，比如二次封装的话就需要设置，默认为 0
            .setSaveDays(5)// 设置日志可保留天数，默认为 -1 表示无限时长
            // 新增 ArrayList 格式化器，默认已支持 Array, Throwable, Bundle, Intent 的格式化输出
            .addFormatter(object : KLog.IFormatter<ArrayList<*>> {
                override fun format(t: ArrayList<*>?): String {
                    return "ALog Formatter ArrayList { $t }"
                }
            })
    }

    fun click01(v :View){
        KLog.storage("保险业务",JSON)
    }

    fun click02(v :View){
        KLog.storage("学车业务","记录内容click02")
    }

    fun click03(v :View){

        KLog.i("学车业务","科目一",XML)
    }

    fun click04(v :View){
        KLog.json("会员业务",JSON)
    }

    fun click05(v :View){
        KLogChooseActivity.enterForResult(this)
//        for (i:Int in 0..30){
//            KLog.file("输入字符到文件中")
//            KLog.file(KLog.I,"输入info类型的字符到文件中")
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == KLogChooseActivity.REQUEST_CODE && resultCode == KLogChooseActivity.RESULT_CODE){
            data?.run {
                var list = this.getStringArrayListExtra(KLogChooseActivity.DATA_CODE)
                list?.let {

                }
            }
        }
    }


}
