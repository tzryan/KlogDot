# KlogDot
#使用方法：
###在根目录的 build.gradle添加
```allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" }
        maven { url "https://github.com/tzryan/KlogDot/raw/master"}
    }
}
```
### 在app目录下的build.gradle添加如下
```android
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation 'com.tzlog.dotlib:TzLogDotLib:1.0.0@aar'
}
```

###init初始化方法
```private fun initLog() {
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
```
###调用方法
```KLog.storage("保险业务",JSON)
   KLog.i("保险业务",JSON)
   KLog.iTag("学车业务","科目一",XML)
```

