package com.tzlog.dot;

import androidx.collection.SimpleArrayMap;

/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/6/5 14:57
 * @Package: com.tzlog.dot
 * @Description:
 **/
public class Test {

    public static void main(String[] s){
        new UserA();

        Class cls = null;
    }

    static class UserA{

        Tools tools;
        UserA(){
            tools = new Tools();
            setFactoryInt();
            tools.testPut(123);
            setFactoryString();
            tools.testPut("Hello world");
        }

        void setFactoryInt(){
            tools.addInterface(new IFactory<Integer>(){
                @Override
                public String format(Integer integer) {
                    return "--int--"+integer.toString() +"工厂格式化";
                }
            });
        }

        void setFactoryString(){
            tools.addInterface(new IFactory<String>(){
                @Override
                public String format(String ss) {
                    return "--string--"+ss +"工厂格式化";
                }
            });
        }
    }


    static class Tools{

        private SimpleArrayMap<Object, IFactory> map;
        private IFactory interfac = null;

        Tools(){
            map = new SimpleArrayMap<>();
        }

        void addInterface(IFactory interfac){
            this.interfac = interfac;
        }

        void testPut(Object key){
            if(interfac!=null){
                map.put(key,interfac);
                testGet(key);
            }
        }

        void testGet(Object key){
            IFactory iFormatter = map.get(key);
            String ss = iFormatter.format(key);
            //这里要接收，规定的格式
            System.out.println(ss);
        }
    }

    interface IFactory<T>{
        String format(T t);
    }
}
