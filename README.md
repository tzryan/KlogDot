# KlogDot
##使用场景
```
在日常开发中，我们经常回遇到用户反馈哪里哪里出了问题，但是用户又无法正确表达刚才的操作或忘记了操作，甚至比较复杂
的程序逻辑，就算用户告知了你如何操作的，也无法找出错误代码，这时可以用传统的PC端常用的方式“log打点”，记录下用户
操作以及重要代码运行时的日志，可以选在在合适的时机上传给服务器，也可以让用户自主选择相应时间段的日志上传
主要功能：（1）KLog.storage("示例标题名称A","二级标题A",JSON) 打点记录
        （2）KLogChooseActivity.enterForResult(this,true) 吊起用户选择日志的UI界面
        （3）KLog.storage(true，"示例标题名称A","二级标题A",JSON) 打点记录，同时在logcat中也会有log.i输出
```

#使用方法：
###在根目录的 build.gradle添加
```
allprojects {
    repositories {
        jcenter()
        maven { url "https://github.com/tzryan/KlogDot/raw/master"}
    }
}
```
### 在app目录下的build.gradle添加如下
```
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation 'com.tzlog.dotlib:TzLogDotLib:1.0.32@aar'
}
```

###init初始化方法
```
KLog.init(this).setLogSwitch(BuildConfig.DEBUG)// 设置log总开关，包括输出到控制台和文件，默认开
               .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
               .setGlobalTag("车轮驾考通")// 设置log全局标签，默认为空
               // 当全局标签不为空时，我们输出的log全部为该tag，
               // 为空时，如果传入的tag为空那就显示类名，否则显示tag
               .setLogHeadSwitch(true)// 设置log头信息开关，默认为开
               .setLog2FileSwitch(true)// 打印log时是否存到文件的开关，默认关
               .setDir("")// 当自定义路径为空时，写入应用的/cache/log/目录中
               .setFilePrefix("KLog")// 当文件前缀为空时，默认为"KLog"，即写入文件为"tlog-MM-dd.txt"
               .setBorderSwitch(true)// 输出日志是否带边框开关，默认开
               .setSingleTagSwitch(true)// 一条日志仅输出一条，默认开，为美化 AS 3.1.0 的 Logcat
               .setConsoleFilter(KLog.V)// log的控制台过滤器，和logcat过f滤器同理，默认Verbose.
               .setFileFilter(KLog.V)// log文件过滤器，和logcat过滤器同理，默认Verbose
               .setStackDeep(1)// log 栈深度，默认为 1
               .setStackOffset(0)// 设置栈偏移，比如二次封装的话就需要设置，默认为 0
               .setSaveDays(3f)//0.0007f 设置日志可保留天数，默认为 -1 表示无限时长  (1分钟=0.00069f)
               // 新增 ArrayList 格式化器，默认已支持 Array, Throwable, Bundle, Intent 的格式化输出
               .addFormatter(object : KLog.IFormatter<ArrayList<*>> {
                   override fun format(t: ArrayList<*>?): String {
                       return "ALog Formatter ArrayList { $t }"
                   }
               })
```
###调用方法
```
KLog.storage("板块或标题名称A","二级标题A",JSON)
KLog.storage(true,"板块或标题名称B","二级标题B","三级标题B",JSON)//true为存储在file的同时，输出在logcat中
KLogChooseActivity.enterForResult(this,true)//调起选择Log日志文件的UI
KLog.getDirPath() //获取日志的存储目录Path
...
```
###选中数据的回调方法
```
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
```

### 适配android-Q （不适配可能导致7.0手机上查看日志时回崩溃）
```
1.请在你自己的项目res目录下新建一个xml文件夹，在此文件夹中新建filepaths.xml,内容如下：
            <?xml version="1.0" encoding="utf-8"?>
            <paths>
                <external-path path="Android/data/你的包名/" name="files_root" />
                <external-path path="." name="external_storage_root" />
            </paths>
            
2.在你自己项目的AndroidManifest.xml文件中添加，注意下面的authorities并不是包名，只是一个命名，无需修改
            <meta-data
                    android:name="com.google.android.actions"
                    android:resource="@xml/filepaths"/>
            <provider
                    android:name="com.tzlog.dotlib.KLogProvider"
                    android:authorities="com.tzlog.dotlib.fileProvider"
                    android:grantUriPermissions="true"
                    android:exported="false">
                <meta-data
                        android:name="android.support.FILE_PROVIDER_PATHS"
                        android:resource="@xml/filepaths" />
            </provider>
```


