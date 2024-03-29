package com.tzlog.dot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
    }

    fun click01(v :View){
        KLog.storage("天地间案例开发",JSON)
    }

    fun click02(v :View){
        KLog.storage("看了容不得你吧v","记录内容click02","kjaskldjasklfjlksdsdvsfvbdfs",this)
    }

    fun click03(v :View){
        KLog.storage("我的脚二分v","科目一",XML)
    }

    fun click04(v :View){
        KLog.storage("板块或标题名称A","二级标题A",JSON)
        KLog.storage(true,"板块或标题名称B","二级标题B","三级标题B",JSON)
    }

    fun click05(v :View){
        KLogChooseActivity.enterForResult(this,true)
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
                    //list[i]中String为日志文件的全路径
                    Toast.makeText(this@MainActivity ,"选中了"+list.size+"个Log日志文件",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
