package com.tzlog.dotlib

import android.app.Application
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.collection.SimpleArrayMap
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.reflect.ParameterizedType
import java.net.UnknownHostException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import kotlin.annotation.Retention

/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/6/4 16:48
 * @Package: com.tzlog.dotlib
 * @Description:
 **/
class KLog private constructor() {

    @IntDef(V, D, I, W, E, A)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TYPE


    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }

    class Config {
        var mDefaultDir: String? = null// The default storage directory of log.
        var mDir: String? = null       // The storage directory of log.
        var mFilePrefix = "util"// The file prefix of log.
        var mLogSwitch = true  // The switch of log.
        var mLog2ConsoleSwitch = true  // The logcat's switch of log.
        var mGlobalTag: String? = null  // The global tag of log.
        var mTagIsSpace = true  // The global tag is space.
        var mLogHeadSwitch = true  // The head's switch of log.
        var mLog2FileSwitch = true // The file's switch of log.
        var mLogBorderSwitch = true  // The border's switch of log.
        var mSingleTagSwitch = true  // The single tag of log.
        var mConsoleFilter = V     // The console's filter of log.
        var mFileFilter = V     // The file's filter of log.
        var mStackDeep = 1     // The stack's deep of log.
        var mStackOffset = 0     // The stack's offset of log.
        var mSaveDays :Float = -1.0f    // The save days of log.
        var maxChooseSize = 3

        init {
            if (mDefaultDir == null) {
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && sAppContext!!.externalCacheDir != null)
                    mDefaultDir = sAppContext!!.externalCacheDir.toString() + FILE_SEP + "klog" + FILE_SEP
                else {
                    mDefaultDir = sAppContext!!.cacheDir.toString() + FILE_SEP + "klog" + FILE_SEP
                }
            }
        }

        fun setMaxChooseSize(maxChooseSize: Int): Config{
            this.maxChooseSize = maxChooseSize
            return sConfig as Config
        }

        fun setLogSwitch(logSwitch: Boolean): Config {
            mLogSwitch = logSwitch
            return this
        }

        fun setConsoleSwitch(consoleSwitch: Boolean): Config {
            mLog2ConsoleSwitch = consoleSwitch
            return this
        }

        fun setGlobalTag(tag: String?): Config {
            if (isSpace(tag)) {
                mGlobalTag = ""
                mTagIsSpace = true
            } else {
                mGlobalTag = tag
                mTagIsSpace = false
            }
            return this
        }

        fun setLogHeadSwitch(logHeadSwitch: Boolean): Config {
            mLogHeadSwitch = logHeadSwitch
            return this
        }

        fun setLog2FileSwitch(log2FileSwitch: Boolean): Config {
            mLog2FileSwitch = log2FileSwitch
            return this
        }

        fun setDir(dir: String): Config {
            if (isSpace(dir)) {
                mDir = null
            } else {
                mDir = if (dir.endsWith(FILE_SEP.toString())) dir else dir + FILE_SEP
            }
            return this
        }

        fun setDir(dir: File?): Config {
            mDir = if (dir == null) null else dir.absolutePath + FILE_SEP
            return this
        }

        fun setFilePrefix(filePrefix: String): Config {
            if (isSpace(filePrefix)) {
                mFilePrefix = "util"
            } else {
                mFilePrefix = filePrefix
            }
            return this
        }

        fun setBorderSwitch(borderSwitch: Boolean): Config {
            mLogBorderSwitch = borderSwitch
            return this
        }

        fun setSingleTagSwitch(singleTagSwitch: Boolean): Config {
            mSingleTagSwitch = singleTagSwitch
            return this
        }

        fun setConsoleFilter(@TYPE consoleFilter: Int): Config {
            mConsoleFilter = consoleFilter
            return this
        }

        fun setFileFilter(@TYPE fileFilter: Int): Config {
            mFileFilter = fileFilter
            return this
        }


        fun setStackDeep(@androidx.annotation.IntRange(from = 1) stackDeep: Int): Config {
            mStackDeep = stackDeep
            return this
        }

        fun setStackOffset(@androidx.annotation.IntRange(from = 0) stackOffset: Int): Config {
            mStackOffset = stackOffset
            return this
        }

        /**
         * @param saveDays
         * 至少1分钟以上（0.00069天）
         * 最大不得超过30天
         * */
        fun setSaveDays(@androidx.annotation.FloatRange(from = 0.00069 , to = 30.0) saveDays: Float): Config {
            mSaveDays = saveDays
            return this
        }

        fun <T> addFormatter(iFormatter: IFormatter<T>?): Config {
            if (iFormatter != null) {
                I_FORMATTER_MAP.put(getTypeClassFromInterface(iFormatter), iFormatter)
            }
            return this
        }

        override fun toString(): String {
            return ("switch: " + mLogSwitch
                    + LINE_SEP + "console: " + mLog2ConsoleSwitch
                    + LINE_SEP + "tag: " + (if (mTagIsSpace) "null" else mGlobalTag)
                    + LINE_SEP + "head: " + mLogHeadSwitch
                    + LINE_SEP + "file: " + mLog2FileSwitch
                    + LINE_SEP + "dir: " + (if (mDir == null) mDefaultDir else mDir)
                    + LINE_SEP + "filePrefix: " + mFilePrefix
                    + LINE_SEP + "border: " + mLogBorderSwitch
                    + LINE_SEP + "singleTag: " + mSingleTagSwitch
                    + LINE_SEP + "consoleFilter: " + T[mConsoleFilter - V]
                    + LINE_SEP + "fileFilter: " + T[mFileFilter - V]
                    + LINE_SEP + "stackDeep: " + mStackDeep
                    + LINE_SEP + "stackOffset: " + mStackOffset
                    + LINE_SEP + "saveDays: " + mSaveDays
                    + LINE_SEP + "formatter: " + I_FORMATTER_MAP)
        }
    }

    interface IFormatter<T> {
        fun format(t: T?): String
    }

    private class TagHead internal constructor(
        internal var tag: String,
        internal var consoleHead: Array<String?>,
        internal var fileHead: String
    )

    object LogFormatter {
        fun formatJson(json: String): String {
            var json = json
            try {
                if (json.startsWith("{")) {
                    json = JSONObject(json).toString(4)
                } else if (json.startsWith("[")) {
                    json = JSONArray(json).toString(4)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return json
        }

        fun formatXml(xml: String): String {
            var xml = xml
            try {
                val xmlInput = StreamSource(StringReader(xml))
                val xmlOutput = StreamResult(StringWriter())
                val transformer = TransformerFactory.newInstance().newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
                transformer.transform(xmlInput, xmlOutput)
                xml = xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">$LINE_SEP")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return xml
        }

        fun array2String(`object`: Any): String {
            if (`object` is Array<*>) {
                return Arrays.deepToString(`object`)
            } else if (`object` is BooleanArray) {
                return Arrays.toString(`object`)
            } else if (`object` is ByteArray) {
                return Arrays.toString(`object`)
            } else if (`object` is CharArray) {
                return Arrays.toString(`object`)
            } else if (`object` is DoubleArray) {
                return Arrays.toString(`object`)
            } else if (`object` is FloatArray) {
                return Arrays.toString(`object`)
            } else if (`object` is IntArray) {
                return Arrays.toString(`object`)
            } else if (`object` is LongArray) {
                return Arrays.toString(`object`)
            } else if (`object` is ShortArray) {
                return Arrays.toString(`object`)
            }
            throw IllegalArgumentException("Array has incompatible type: " + `object`.javaClass)
        }

        fun throwable2String(e: Throwable): String {
            var t: Throwable? = e
            while (t != null) {
                if (t is UnknownHostException) {
                    return ""
                }
                t = t.cause
            }
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            var cause: Throwable? = e.cause
            while (cause != null) {
                cause.printStackTrace(pw)
                cause = cause.cause
            }
            pw.flush()
            return sw.toString()
        }

        fun bundle2String(bundle: Bundle): String {
            val iterator = bundle.keySet().iterator()
            if (!iterator.hasNext()) {
                return "Bundle {}"
            }
            val sb = StringBuilder(128)
            sb.append("Bundle { ")
            while (true) {
                val key = iterator.next()
                val value = bundle.get(key)
                sb.append(key).append('=')
                if (value != null && value is Bundle) {
                    sb.append(if (value === bundle) "(this Bundle)" else bundle2String(value))
                } else {
                    sb.append(formatObject(value))
                }
                if (!iterator.hasNext()) return sb.append(" }").toString()
                sb.append(',').append(' ')
            }
        }

        fun intent2String(intent: Intent): String {
            val sb = StringBuilder(128)
            sb.append("Intent { ")
            var first = true
            val mAction = intent.action
            if (mAction != null) {
                sb.append("act=").append(mAction)
                first = false
            }
            val mCategories = intent.categories
            if (mCategories != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("cat=[")
                var firstCategory = true
                for (c in mCategories) {
                    if (!firstCategory) {
                        sb.append(',')
                    }
                    sb.append(c)
                    firstCategory = false
                }
                sb.append("]")
            }
            val mData = intent.data
            if (mData != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("dat=").append(mData)
            }
            val mType = intent.type
            if (mType != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("typ=").append(mType)
            }
            val mFlags = intent.flags
            if (mFlags != 0) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("flg=0x").append(Integer.toHexString(mFlags))
            }
            val mPackage = intent.getPackage()
            if (mPackage != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("pkg=").append(mPackage)
            }
            val mComponent = intent.component
            if (mComponent != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("cmp=").append(mComponent.flattenToShortString())
            }
            val mSourceBounds = intent.sourceBounds
            if (mSourceBounds != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("bnds=").append(mSourceBounds.toShortString())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                val mClipData = intent.clipData
                if (mClipData != null) {
                    if (!first) {
                        sb.append(' ')
                    }
                    first = false
                    clipData2String(mClipData, sb)
                }
            }
            val mExtras = intent.extras
            if (mExtras != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("extras={")
                sb.append(bundle2String(mExtras))
                sb.append('}')
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                val mSelector = intent.selector
                if (mSelector != null) {
                    if (!first) {
                        sb.append(' ')
                    }
                    first = false
                    sb.append("sel={")
                    sb.append(if (mSelector === intent) "(this Intent)" else intent2String(mSelector))
                    sb.append("}")
                }
            }
            sb.append(" }")
            return sb.toString()
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        private fun clipData2String(clipData: ClipData, sb: StringBuilder) {
            val item = clipData.getItemAt(0)
            if (item == null) {
                sb.append("ClipData.Item {}")
                return
            }
            sb.append("ClipData.Item { ")
            val mHtmlText = item.htmlText
            if (mHtmlText != null) {
                sb.append("H:")
                sb.append(mHtmlText)
                sb.append("}")
                return
            }
            val mText = item.text
            if (mText != null) {
                sb.append("T:")
                sb.append(mText)
                sb.append("}")
                return
            }
            val uri = item.uri
            if (uri != null) {
                sb.append("U:").append(uri)
                sb.append("}")
                return
            }
            val intent = item.intent
            if (intent != null) {
                sb.append("I:")
                sb.append(intent2String(intent))
                sb.append("}")
                return
            }
            sb.append("NULL")
            sb.append("}")
        }
    }

    companion object {

        const val V = Log.VERBOSE
        const val D = Log.DEBUG
        const val I = Log.INFO
        const val W = Log.WARN
        const val E = Log.ERROR
        const val A = Log.ASSERT

        private val T = charArrayOf('V', 'D', 'I', 'W', 'E', 'A')

        private val FILE = 0x10
        private val JSON = 0x20
        private val XML = 0x30

        private val FILE_SEP = System.getProperty("file.separator")
        private val LINE_SEP = System.getProperty("line.separator")
        private val TOP_CORNER = "┌"
        private val MIDDLE_CORNER = "├"
        private val LEFT_BORDER = "│ "
        private val BOTTOM_CORNER = "└"
        private val SIDE_DIVIDER = "────────────────────────────────────────────────────────"
        private val MIDDLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
        private val TOP_BORDER = TOP_CORNER + SIDE_DIVIDER + SIDE_DIVIDER
        private val MIDDLE_BORDER = MIDDLE_CORNER + MIDDLE_DIVIDER + MIDDLE_DIVIDER
        private val BOTTOM_BORDER = BOTTOM_CORNER + SIDE_DIVIDER + SIDE_DIVIDER
        private val MAX_LEN = 3000
        private val NOTHING = "log nothing"
        private val NULL = "null"
        private val ARGS = "args"
        private val PLACEHOLDER = " "

        private val SDF_THREAD_LOCAL = ThreadLocal<SimpleDateFormat>()

        private val EXECUTOR = Executors.newSingleThreadExecutor()

        private val I_FORMATTER_MAP :SimpleArrayMap<Class<*>, IFormatter<*>> = SimpleArrayMap()
        //SimpleArrayMap<Any, Any>()
        //<Class, IFormatter>

        var sAppContext: Application? = null
        private var sConfig: Config? = null
        private var show_logcat: Boolean = false

        fun init(context: Application): Config {
            sAppContext = context
            if (sConfig == null) {
                sConfig = Config()
            }
            return sConfig as Config
        }

        val config: Config
            get() {
                if (sConfig == null) throw NullPointerException("U should init first.")
                return sConfig as Config
            }

        /**
         * @param showLogcat 是否显示在Logcat
         * @param blockName 版块名称，功能名称
         * @param contents 需要记录的内容
         * */
        fun storage(showLogcat: Boolean = false, blockName :String = "", vararg contents: Any){
            show_logcat = showLogcat
            if(TextUtils.isEmpty(blockName)){
                log(I, sConfig!!.mGlobalTag, *contents)
            }else{
                log(I, blockName, *contents)
            }
        }

        fun storage(blockName :String = "", vararg contents: Any){
            show_logcat = false
            if(TextUtils.isEmpty(blockName)){
                log(I, sConfig!!.mGlobalTag, *contents)
            }else{
                log(I, blockName, *contents)
            }
        }

        private fun v(vararg contents: Any) {
            log(V, sConfig!!.mGlobalTag, *contents)
        }

        private fun vTag(tag: String, vararg contents: Any) {
            log(V, tag, *contents)
        }

        private fun d(vararg contents: Any) {
            log(D, sConfig!!.mGlobalTag, *contents)
        }

        private fun dTag(tag: String, vararg contents: Any) {
            log(D, tag, *contents)
        }

        private fun i(vararg contents: Any) {
            log(I, sConfig!!.mGlobalTag, *contents)
        }

        private fun iTag(tag: String, vararg contents: Any) {
            log(I, tag, *contents)
        }

        private fun w(vararg contents: Any) {
            log(W, sConfig!!.mGlobalTag, *contents)
        }

        private fun wTag(tag: String, vararg contents: Any) {
            log(W, tag, *contents)
        }

        private fun e(vararg contents: Any) {
            log(E, sConfig!!.mGlobalTag, *contents)
        }

        private fun eTag(tag: String, vararg contents: Any) {
            log(E, tag, *contents)
        }

        private fun a(vararg contents: Any) {
            log(A, sConfig!!.mGlobalTag, *contents)
        }

        private fun aTag(tag: String, vararg contents: Any) {
            log(A, tag, *contents)
        }

        private fun file(content: Any) {
            log(FILE or D, sConfig!!.mGlobalTag, content)
        }

        private fun file(@TYPE type: Int, content: Any) {
            log(FILE or type, sConfig!!.mGlobalTag, content)
        }

        private fun file(tag: String, content: Any) {
            log(FILE or D, tag, content)
        }

        private fun file(@TYPE type: Int, tag: String, content: Any) {
            log(FILE or type, tag, content)
        }

        private fun json(content: String) {
            log(JSON or D, sConfig!!.mGlobalTag, content)
        }

        private fun json(@TYPE type: Int, content: String) {
            log(JSON or type, sConfig!!.mGlobalTag, content)
        }

        private fun json(tag: String, content: String) {
            log(JSON or D, tag, content)
        }

        private fun json(@TYPE type: Int, tag: String, content: String) {
            log(JSON or type, tag, content)
        }

        private fun xml(content: String) {
            log(XML or D, sConfig!!.mGlobalTag, content)
        }

        private fun xml(@TYPE type: Int, content: String) {
            log(XML or type, sConfig!!.mGlobalTag, content)
        }

        private fun xml(tag: String, content: String) {
            log(XML or D, tag, content)
        }

        private fun xml(@TYPE type: Int, tag: String, content: String) {
            log(XML or type, tag, content)
        }

        private fun log(type: Int, tag: String?, vararg contents: Any) {
            if (!sConfig!!.mLogSwitch || !sConfig!!.mLog2ConsoleSwitch && !sConfig!!.mLog2FileSwitch)
                return
            val type_low = type and 0x0f
            val type_high = type and 0xf0
            if (type_low < sConfig!!.mConsoleFilter && type_low < sConfig!!.mFileFilter) return
            val tagHead = processTagAndHead(tag)
            val body = processBody(type_high, *contents)
            if(show_logcat){
                if (sConfig!!.mLog2ConsoleSwitch && type_low >= sConfig!!.mConsoleFilter && type_high != FILE) {
                    print2Console(type_low, tagHead.tag, tagHead.consoleHead, body)
                }
            }
            if ((sConfig!!.mLog2FileSwitch || type_high == FILE) && type_low >= sConfig!!.mFileFilter) {
                print2File(type_low, tagHead.tag, tagHead.fileHead + body)
            }
        }

        private fun processTagAndHead(tag: String?): TagHead {
            var tag = tag
            if (!sConfig!!.mTagIsSpace && !sConfig!!.mLogHeadSwitch) {
                tag = sConfig!!.mGlobalTag
            } else {
                val stackTrace = Throwable().stackTrace
                val stackIndex = 3 + sConfig!!.mStackOffset
                if (stackIndex >= stackTrace.size) {
                    val targetElement = stackTrace[3]
                    val fileName = getFileName(targetElement)
                    if (sConfig!!.mTagIsSpace && isSpace(tag)) {
                        val index = fileName.indexOf('.')// Use proguard may not find '.'.
                        tag = if (index == -1) fileName else fileName.substring(0, index)
                    }
                    return TagHead(tag!!, emptyArray(), ": ")
                }
                var targetElement = stackTrace[stackIndex]
                val fileName = getFileName(targetElement)
                if (sConfig!!.mTagIsSpace && isSpace(tag)) {
                    val index = fileName.indexOf('.')// Use proguard may not find '.'.
                    tag = if (index == -1) fileName else fileName.substring(0, index)
                }
                if (sConfig!!.mLogHeadSwitch) {
                    val tName = Thread.currentThread().name
                    val head = Formatter()
                        .format(
                            "%s, %s.%s(%s:%d)",
                            tName,
                            targetElement.className,
                            targetElement.methodName,
                            fileName,
                            targetElement.lineNumber
                        )
                        .toString()
                    val fileHead = " [$head]: "
                    if (sConfig!!.mStackDeep <= 1) {
                        return TagHead(tag!!, arrayOf(head), fileHead)
                    } else {
                        val consoleHead = arrayOfNulls<String>(
                            Math.min(
                                sConfig!!.mStackDeep,
                                stackTrace.size - stackIndex
                            )
                        )
                        consoleHead[0] = head
                        val spaceLen = tName.length + 2
                        val space = Formatter().format("%" + spaceLen + "s", "").toString()
                        var i = 1
                        val len = consoleHead.size
                        while (i < len) {
                            targetElement = stackTrace[i + stackIndex]
                            consoleHead[i] = Formatter()
                                .format(
                                    "%s%s.%s(%s:%d)",
                                    space,
                                    targetElement.className,
                                    targetElement.methodName,
                                    getFileName(targetElement),
                                    targetElement.lineNumber
                                )
                                .toString()
                            ++i
                        }
                        return TagHead(tag!!, consoleHead, fileHead)
                    }
                }
            }
            return TagHead(tag!!, emptyArray(), ": ")
        }

        private fun getFileName(targetElement: StackTraceElement): String {
            val fileName = targetElement.fileName
            if (fileName != null) return fileName
            // If name of file is null, should add
            // "-keepattributes SourceFile,LineNumberTable" in proguard file.
            var className = targetElement.className
            val classNameInfo = className.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (classNameInfo.size > 0) {
                className = classNameInfo[classNameInfo.size - 1]
            }
            val index = className.indexOf('$')
            if (index != -1) {
                className = className.substring(0, index)
            }
            return "$className.java"
        }

        private fun processBody(type: Int, vararg contents: Any): String {
            var body = NULL
            if (contents != null) {
                if (contents.size == 1) {
                    body = formatObject(type, contents[0])
                } else {
                    val sb = StringBuilder()
                    var i = 0
                    val len = contents.size
                    while (i < len) {
                        val content = contents[i]
                        sb.append(ARGS)
                            .append("[")
                            .append(i)
                            .append("]")
                            .append(" = ")
                            .append(formatObject(content))
                            .append(LINE_SEP)
                        ++i
                    }
                    body = sb.toString()
                }
            }
            return if (body.isEmpty()) NOTHING else body
        }

        private fun formatObject(type: Int, `object`: Any?): String {
            if (`object` == null) return NULL
            if (type == JSON) return LogFormatter.formatJson(`object`.toString())
            return if (type == XML) LogFormatter.formatXml(`object`.toString()) else formatObject(`object`)
        }

        private fun formatObject(`object`: Any?): String {
            if (`object` == null) return NULL
            if (!I_FORMATTER_MAP.isEmpty) {
                val iFormatter = I_FORMATTER_MAP.get(getClassFromObject(`object`))
                iFormatter?.let {
                    return (it as IFormatter<Any>).format(`object`)
                }

            }
            if (`object`.javaClass.isArray) return LogFormatter.array2String(`object`)
            if (`object` is Throwable) return LogFormatter.throwable2String(`object`)
            if (`object` is Bundle) return LogFormatter.bundle2String((`object` as Bundle?)!!)
            return if (`object` is Intent) LogFormatter.intent2String((`object` as Intent?)!!) else `object`.toString()
        }

        private fun print2Console(
            type: Int,
            tag: String,
            head: Array<String?>,
            msg: String
        ) {
            if (sConfig!!.mSingleTagSwitch) {
                printSingleTagMsg(type, tag, processSingleTagMsg(type, tag, head, msg))
            } else {
                printBorder(type, tag, true)
                printHead(type, tag, head)
                printMsg(type, tag, msg)
                printBorder(type, tag, false)
            }
        }

        private fun printBorder(type: Int, tag: String, isTop: Boolean) {
            if (sConfig!!.mLogBorderSwitch) {
                Log.println(type, tag, if (isTop) TOP_BORDER else BOTTOM_BORDER)
            }
        }

        private fun printHead(type: Int, tag: String, head: Array<String?>) {
            if (head != null) {
                for (aHead in head) {
                    Log.println(type, tag, if (sConfig!!.mLogBorderSwitch) LEFT_BORDER + aHead else aHead)
                }
                if (sConfig!!.mLogBorderSwitch) Log.println(type, tag, MIDDLE_BORDER)
            }
        }

        private fun printMsg(type: Int, tag: String, msg: String) {
            val len = msg.length
            val countOfSub = len / MAX_LEN
            if (countOfSub > 0) {
                var index = 0
                for (i in 0 until countOfSub) {
                    printSubMsg(type, tag, msg.substring(index, index + MAX_LEN))
                    index += MAX_LEN
                }
                if (index != len) {
                    printSubMsg(type, tag, msg.substring(index, len))
                }
            } else {
                printSubMsg(type, tag, msg)
            }
        }

        private fun printSubMsg(type: Int, tag: String, msg: String) {
            if (!sConfig!!.mLogBorderSwitch) {
                Log.println(type, tag, msg)
                return
            }
            val sb = StringBuilder()
            val lines = msg.split(LINE_SEP!!.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            for (line in lines) {
                Log.println(type, tag, LEFT_BORDER + line)
            }
        }

        private fun processSingleTagMsg(
            type: Int,
            tag: String,
            head: Array<String?>,
            msg: String
        ): String {
            val sb = StringBuilder()
            sb.append(PLACEHOLDER).append(LINE_SEP)
            if (sConfig!!.mLogBorderSwitch) {
                sb.append(TOP_BORDER).append(LINE_SEP)
                if (head != null) {
                    for (aHead in head) {
                        sb.append(LEFT_BORDER).append(aHead).append(LINE_SEP)
                    }
                    sb.append(MIDDLE_BORDER).append(LINE_SEP)
                }
                for (line in msg.split(LINE_SEP!!.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
                    sb.append(LEFT_BORDER).append(line).append(LINE_SEP)
                }
                sb.append(BOTTOM_BORDER)
            } else {
                if (head != null) {
                    for (aHead in head) {
                        sb.append(aHead).append(LINE_SEP)
                    }
                }
                sb.append(msg)
            }
            return sb.toString()
        }

        private fun printSingleTagMsg(type: Int, tag: String, msg: String?) {
            var countOfSub = -1
            var len = -1
            msg?.let {
                len = msg.length
                countOfSub = len / MAX_LEN
            }

            if (countOfSub > 0) {
                if (sConfig!!.mLogBorderSwitch) {
                    Log.println(type, tag, msg?.substring(0, MAX_LEN) + LINE_SEP + BOTTOM_BORDER)
                    var index = MAX_LEN
                    for (i in 1 until countOfSub) {
                        Log.println(
                            type, tag, PLACEHOLDER + LINE_SEP + TOP_BORDER + LINE_SEP
                                    + LEFT_BORDER + msg?.substring(index, index + MAX_LEN)
                                    + LINE_SEP + BOTTOM_BORDER
                        )
                        index += MAX_LEN
                    }
                    if (index != len) {
                        Log.println(
                            type, tag, PLACEHOLDER + LINE_SEP + TOP_BORDER + LINE_SEP
                                    + LEFT_BORDER + msg?.substring(index, len)
                        )
                    }
                } else {
                    Log.println(type, tag, msg?.substring(0, MAX_LEN))
                    var index = MAX_LEN
                    for (i in 1 until countOfSub) {
                        Log.println(
                            type, tag,
                            PLACEHOLDER + LINE_SEP + msg?.substring(index, index + MAX_LEN)
                        )
                        index += MAX_LEN
                    }
                    if (index != len) {
                        Log.println(type, tag, PLACEHOLDER + LINE_SEP + msg?.substring(index, len))
                    }
                }
            } else {
                Log.println(type, tag, msg)
            }
        }

        fun getDirPath(): String{
            return (
            (if (sConfig!!.mDir == null)
                sConfig!!.mDefaultDir
            else
                sConfig!!.mDir)
                    + "" )
//                    + sConfig!!.mFilePrefix )
        }

        private fun print2File(type: Int, tag: String, msg: String) {
            val now = Date(System.currentTimeMillis())
            val format = sdf.format(now)
            val date = format.substring(0, 10)
            val time = format.substring(11)
            val fullPath = ((if (sConfig!!.mDir == null) sConfig!!.mDefaultDir else sConfig!!.mDir)
                    + sConfig!!.mFilePrefix + "-" + date + "H" + (time.substring(0,2)) + ".txt")
//                    + sConfig!!.mFilePrefix + "-" + date + ".txt")
            if (!createOrExistsFile(fullPath)) {
                Log.e("KLog", "create $fullPath failed!")
                return
            }
            val sb = StringBuilder()
            sb.append(time)
                .append(T[type - V])
                .append("/")
                .append(tag)
                .append(msg)
                .append(LINE_SEP)
            val content = sb.toString()
            input2File(content, fullPath)
        }

        private val sdf: SimpleDateFormat
            get() {
                var simpleDateFormat: SimpleDateFormat? = SDF_THREAD_LOCAL.get()
                if (simpleDateFormat == null) {
                    simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    SDF_THREAD_LOCAL.set(simpleDateFormat)
                }
                return simpleDateFormat
            }

        private fun createOrExistsFile(filePath: String): Boolean {
            val file = File(filePath)
            if (file.exists()) return file.isFile
            if (!createOrExistsDir(file.parentFile)) return false
            try {
                deleteDueLogs(filePath)
                val isCreate = file.createNewFile()
                if (isCreate) {
                    printDeviceInfo(filePath)
                }
                return isCreate
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

        }

        private fun deleteDueLogs(filePath: String) {
            val file = File(filePath)
            val parentFile = file.parentFile
            val files =
                parentFile.listFiles { dir, name -> name.matches(("^" + sConfig!!.mFilePrefix + "-[0-9]{4}-[0-9]{2}-[0-9]{2}H[0-9]{2}.txt$").toRegex()) }
//                parentFile.listFiles { dir, name -> name.matches(("^" + sConfig!!.mFilePrefix + "-[0-9]{4}-[0-9]{2}-[0-9]{2}.txt$").toRegex()) }
            files?.run {
                if (files.isEmpty()) return@deleteDueLogs
                val length = filePath.length
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                try {
                    val curDay = filePath.substring(length - 14, length - 4)
                    val dueMillis = sdf.parse(curDay).time - sConfig!!.mSaveDays * 86400000L
                    for (aFile in files) {
                        val name = aFile.name
                        val l = name.length
                        val logDay = name.substring(l - 14, l - 4)
                        if (sdf.parse(logDay).time <= dueMillis) {
                            EXECUTOR.execute {
                                val delete = aFile.delete()
                                if (!delete) {
                                    Log.e("KLog", "delete $aFile failed!")
                                }
                            }
                        }
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }

        private fun printDeviceInfo(filePath: String) {
            var versionName = ""
            var versionCode = 0
            try {
                val pi = sAppContext!!
                    .packageManager
                    .getPackageInfo(sAppContext!!.packageName, 0)
                if (pi != null) {
                    versionName = pi.versionName
                    versionCode = pi.versionCode
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val time = filePath.substring(filePath.length - 14, filePath.length - 4)
            val head = "************* Log Head ****************" +
                    "\nDate of Log        : " + time +
                    "\nDevice Manufacturer: " + Build.MANUFACTURER +
                    "\nDevice Model       : " + Build.MODEL +
                    "\nAndroid Version    : " + Build.VERSION.RELEASE +
                    "\nAndroid SDK        : " + Build.VERSION.SDK_INT +
                    "\nApp VersionName    : " + versionName +
                    "\nApp VersionCode    : " + versionCode +
                    "\n************* Log Head ****************\n\n"
            input2File(head, filePath)
        }

        private fun createOrExistsDir(file: File?): Boolean {
            return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
        }

        private fun isSpace(s: String?): Boolean {
            if (s == null) return true
            var i = 0
            val len = s.length
            while (i < len) {
                if (!Character.isWhitespace(s[i])) {
                    return false
                }
                ++i
            }
            return true
        }

        private fun input2File(input: String, filePath: String) {
            EXECUTOR.execute {
                var bw: BufferedWriter? = null
                try {
                    bw = BufferedWriter(FileWriter(filePath, true))
                    bw.write(input)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("KLog", "log to $filePath failed!")
                } finally {
                    try {
                        bw?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

        private fun <T> getTypeClassFromInterface(callback: IFormatter<T>?): Class<*>? {
            if (callback == null) return null
            val mySuperClass = callback.javaClass.genericInterfaces[0]
            var type = (mySuperClass as ParameterizedType).actualTypeArguments[0]
            while (type is ParameterizedType) {
                type = type.rawType
            }
            var className = type.toString()
            if (className.startsWith("class ")) {
                className = className.substring(6)
            } else if (className.startsWith("interface ")) {
                className = className.substring(10)
            }
            try {
                return Class.forName(className)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }

            return null
        }

        private fun getClassFromObject(obj: Any): Class<*> {
            val objClass = obj.javaClass
            val genericInterfaces = objClass.genericInterfaces
            if (genericInterfaces.size == 1) {
                var type = genericInterfaces[0]
                while (type is ParameterizedType) {
                    type = (type as ParameterizedType).rawType
                }
                var className = type.toString()
                if (className.startsWith("class ")) {
                    className = className.substring(6)
                } else if (className.startsWith("interface ")) {
                    className = className.substring(10)
                }
                try {
                    return Class.forName(className)
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }

            }
            return objClass
        }
    }
}