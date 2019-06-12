package com.tzlog.dot

import androidx.collection.SimpleArrayMap
import java.io.Console

/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/6/5 15:10
 * @Package: com.tzlog.dot
 * @Description:
 **/
object Test02 {

    @JvmStatic
    fun main(args: Array<String>) {

    }

    internal class UserA {

        var tools: Tools

        init {
            tools = Tools()
            setFactoryInt()
            tools.testPut(123)
            setFactoryString()
            tools.testPut("Hello world")

            val cls: Class<*>? = null
        }

        fun setFactoryInt() {
            tools.addInterface(object : IFactory<Int> {
                override fun format(t: Int): String {
                    return "--int--" + t!!.toString() + "工厂格式化"
                }


            })
        }

        fun setFactoryString() {
            tools.addInterface(object : IFactory<String> {
                override fun format(ss: String): String {
                    return "--string--" + ss + "工厂格式化"
                }
            })
        }
    }


    internal class Tools {

        private var map: SimpleArrayMap<Any, IFactory<*>>? = null
        private var interfac :IFactory<*>? = null

        init {
            map = SimpleArrayMap()
        }

        fun addInterface(interfac: IFactory<*>) {
            this.interfac = interfac
        }

        fun testPut(key: Any) {
            if (interfac != null) {
                map?.put(key, interfac)
                testGet(key)
            }
        }

        fun testGet(key: Any) {
            val iFormatter = map?.get(key) as IFactory<Any>
            val ss = iFormatter!!.format(key)
            println(ss)
        }
    }

    internal interface IFactory<T> {
        fun format(t: T): String
    }
}