package com.example.balistupexample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.balistupexample.HeldObject.held0
import com.example.balistupexample.HeldObject.held1
import com.example.balistupexample.HeldObject.held100
import com.example.balistupexample.HeldObject.held2
import com.example.balistupexample.HeldObject.held3
import com.example.balistupexample.HeldObject.held4
import com.example.balistupexample.HeldObject.held5
import com.example.balistupexample.HeldObject.held6
import com.example.balistupexample.databinding.ActivityMainBinding
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.ZoneOffset.UTC
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/*
    LifeCycle 정리
    bband = 인터넷 서버 연결
    bcast = 수신기 웹 서버 연결
- HELD의 전달 내용에 따라서 인터넷 서버 또는 수신기 웹서버를 통해서 동작 시킬 수 있다.

    HELD. @clearAppContextCacheDate
 - 지정된 날짜 및 시간 이전에 생성된 APP ContextCache의 모든 파일을 제거.

 * 주기적으로 HELD가 들어오면 리스트업을 하고 주기적으로 BA List check를 하면서 BA의 동작에 대한 처리를 하도록 한다.
 * 각 동작에 대한 구분을 별도로 처리 하는게 좋을 것으로 보인다.
 * valid until에 따른 동작 처리도 필요 하므로 BA List check 함수를 만들어서 관리 하는 측면이 좋을 것으로 보인다.
 * 고로 기능은 BA ListUp(), BA ListCheck()의 기능을 분리 하는게 좋아 보인다.

    제외 조건 및 동작 조건
    목록제외
    1. requiredCapabilities 에 지원되지 않으면 제외 -> 추후에 구현 진행 예정.
    2. validFrom이 현재 UTC보다 미래의 값이면 제외
    3. validUntil이 현재 UTC보다 이전 값이면 제외.

    만약. valid from 없는 경우 바로 시작. valid until이 없는 경우 무기한 이다. (A331참고)
    Load
    1. default가 정의되지 않으면 기본 값은 false이다. default가 없으면 어느것도 로드 될 수 없음. (여러개 이상) (O)
    2. 여러 목록이 존재 해도 default가 true이면 해당 항목이 로드될 후보. (O)
    3. 목록에 정확히 한개의 항목이 있는 경우. (default없어도 됨) (O)

    UnLoad
    1. 항목이 비어 있는 경우 BA가 로드 되어 있다면 현재 페이지 언로드 해야 함. (O)
    2. BA가 로드 중 BA가 validUntil속성에 도달한 경우 (O)
    3. HELD가 SLS에 더이 상 존재 하지 않거나 유효하지 않은 경우 (당연히..항목이 비어 있겠지? - 1번과 같을듯...)

    no reload
    1. 새로운 목록에 appId & appContexId가 현재 로드된 BA와 같은 경우 (O)

    no load
    1. 항목이 둘 이상이고 어느 후보도 default가 표시되지 않으면 제외 한다.

    new load
    1. BA가 현재 로드되어 있는 상태에서 BA가 리스트에 항목과 다른경우 현재 BA를 언로드 하고 리스트에 있는 항목을 로드 한다.
    2. 현재 로드된 BA가 없고 항목의 새로운 BA를 로드 한다.
* */

class MainActivity : AppCompatActivity() {
    data class BAInfo(var appContextId: String?, var appId: String?, var requiredCapabilities: String?,
                      var validFrom: String?, var validUntil: String?, var default: String?,
                      var bbandUrl: String?, var bcastPageUrl: String?, var bcastPackageUrl: String?)

    private var baList: ArrayList<BAInfo> = ArrayList()

    private val TAG = "SHLEE"
    private var isBaRunning = false
    private var isBaDefault = false //현재 BA의 리스트의 후보 항목들의 default가 없는 경우 체크.

    private var mCurrentContextId: String? = null
    private var mCurrentAppId: String? = null

    private var mCurrentBaInfo: BAInfo? = null

    private lateinit var binding: ActivityMainBinding
    private var mHandler: Handler? = null

    private val MSG_HANDLE_RECEIVE_HELD = 0
    private val MSG_HANDLE_CHECK_LIST = 1
    private val MSG_HANDLE_PRINT_LIST = 2

    private val appId = "http://kids.pbs.org/a1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCurBaInfo.setOnClickListener {
            Log.i(TAG, "현재 동작중인 BA의 정보 요청")
            Log.i(TAG, "$mCurrentBaInfo")
        }
        binding.btnCurBaList.setOnClickListener {
            printBaList()
        }
        binding.btnHeld0.setOnClickListener {
            baList.clear()
            parseHeld(held0)
        }
        binding.btnHeld1.setOnClickListener {
            baList.clear()
            parseHeld(held1)
        }
        binding.btnHeld2.setOnClickListener {
            baList.clear()
            parseHeld(held2)
        }
        binding.btnHeld3.setOnClickListener {
            baList.clear()
            parseHeld(held3)
        }
        binding.btnHeld4.setOnClickListener {
            baList.clear()
            parseHeld(held4)
        }
        binding.btnHeld5.setOnClickListener {
            baList.clear()
            parseHeld(held5)
        }
        binding.btnHeld6.setOnClickListener {
            baList.clear()
            parseHeld(held6)
        }
        binding.btnHeld100.setOnClickListener {
            baList.clear()
            parseHeld(held100)
        }
        //후보 목록에서 appid를 검색 후 appContextId를 검색 한다.
        binding.btnAppId1.setOnClickListener {
            Log.i(TAG, "AppId변경.(Launch APP)")
            changeCurrentBA("http://kids.pbs.org/a3")
        }

        if(mHandler == null) {
            mHandler = Handler(Looper.getMainLooper()) {
                when(it.what) {
                    MSG_HANDLE_RECEIVE_HELD -> {
                        Log.i(TAG, "새로운 held가 들어 왔습니다.")
                        baList.clear()
                        parseHeld(held1)
                    }
                    MSG_HANDLE_CHECK_LIST -> {
                        baListMgr()
                    }
                    MSG_HANDLE_PRINT_LIST -> {
                        printBaList()
                    }
                }
                true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart()")

        mHandler?.sendEmptyMessage(MSG_HANDLE_RECEIVE_HELD)
    }

    private fun printBaList() {
        Log.i(TAG, "BA List size = ${baList.size}")
        baList.forEach { Log.i(TAG, "$it") }
    }

    //목록제외 조건 적용 후 나머지 리스트업한다.
    //BA후보군으로 관리 하도록 파싱 후 파싱 데이터 List에 추가 한다.

    @Synchronized
    private fun parseHeld(held: String) {
        Log.i(TAG,"held = [$held]")
        var bcastEntryPackageUrl: String?
        var bcastEntryPageUrl: String?
        var bbandEntryPageUrl: String?
        var appContextId: String?
        var appId: String?
        var validFrom: String?
        var validUntil: String?
        var default: String?
        var requiredCapabilities: String?

        try {
            val inputStream: InputStream = ByteArrayInputStream(held.toByteArray())
            val parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()
            parser.setInput(InputStreamReader(inputStream))
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_DOCUMENT, XmlPullParser.END_TAG, XmlPullParser.TEXT -> {
                    }

                    XmlPullParser.START_TAG -> {
                        val startTag = parser.name

                        Log.i(TAG, "startTag = $startTag")
                        if (startTag == "HTMLEntryPackage") {
                            bcastEntryPackageUrl =
                                parser.getAttributeValue(null, "bcastEntryPackageUrl")
                            bcastEntryPageUrl = parser.getAttributeValue(null, "bcastEntryPageUrl")
                            appContextId = parser.getAttributeValue(null, "appContextId")
                            appId = parser.getAttributeValue(null, "appId")
                            validFrom = parser.getAttributeValue(null, "validFrom")
                            validUntil = parser.getAttributeValue(null, "validUntil")
                            default = parser.getAttributeValue(null, "default")
                            bbandEntryPageUrl = parser.getAttributeValue(null, "bbandEntryPageUrl")
                            requiredCapabilities = parser.getAttributeValue(null, "requiredCapabilities")


//                            Log.i(TAG, "bcastEntryPackageUrl:$bcastEntryPackageUrl, bcastEntryPageUrl:$bcastEntryPageUrl, " +
//                                    "appContextId:$appContextId, appId:$appId, " +
//                                    "validFrom:$validFrom, validUntil:$validUntil, default:$default, bbandEntryPageUrl:$bbandEntryPageUrl, " +
//                                    "requiredCapabilities:$requiredCapabilities")

                            //if(requiredCapabilities != null) {
                                Log.i(TAG, "requiredCapability = [$requiredCapabilities]")
                            baList.add(
                                BAInfo(
                                    appContextId, appId, requiredCapabilities, validFrom, validUntil,
                                    default, bbandEntryPageUrl, bcastEntryPageUrl, bcastEntryPackageUrl
                                )
                            )

                            //}
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.d(TAG, "error:")
            e.printStackTrace()
        }

        Log.i(TAG, "파싱 종료.")

        mHandler?.sendEmptyMessage(MSG_HANDLE_CHECK_LIST)
    }

    @Synchronized
    private fun baListMgr() {
        isBaDefault = false
        var validFromToMills: Long = 0
        var validUntilToMills: Long = 0

        var currentDateMS: Long = getMillisFromUtcDatetime(getUtcDatetimeAsDate())
        Log.i(TAG, "getUtcDatetimeAsDate = [$currentDateMS]")
        currentDateMS += 32400000 // UTC+9

        mCurrentAppId = mCurrentBaInfo?.appId
        mCurrentContextId = mCurrentBaInfo?.appContextId

        if(baList.isNotEmpty()) {
            Log.i(TAG, "ba List가 존재 함. size[${baList.size}]")

            //시간체크 후 시간이 초과한 데이터는 BA List에서 제거 한다.
            printBaList()
            baList.removeIf {ba ->
                var ret = false

                if(ba.validFrom != null) {
                    validFromToMills = getMillisFromUtcDatetime(ba.validFrom!!)
                    Log.i(TAG, "validFromToMills = $validFromToMills")
                    //ret = true
                }
                if(ba.validUntil != null) {
                    validUntilToMills = getMillisFromUtcDatetime(ba.validUntil!!)
                    Log.i(TAG, "validUntilToMills = $validUntilToMills(${ba.validUntil}), +9적용: [${validUntilToMills + 32400000}]")
                    //ret = true
                }

                if (ba.validFrom != null && ba.validUntil != null) {
                    Log.i(TAG, "시간정보가 존재 함.curr[$currentDateMS]")
                    ret = currentDateMS !in (validFromToMills + 1) until validUntilToMills
                    Log.i(TAG, "ret = $ret")
                }

                ret
            }

            if(isBaRunning) {
                Log.i(TAG, "현재 동작 중인 BA가 존재 함.")
                if(baList.size == 0){
                    Log.i(TAG, "BA 리스트가 없음. 로드되어 있는 BA종료")
                    isBaRunning = false
                    mCurrentBaInfo = null
                    return
                }

                //app&appContextId를 현재BA의 Id와 비교
                run loop@{
                    baList.forEach {
                        if(it.appId == mCurrentAppId && it.appContextId == mCurrentContextId) {
                            Log.i(TAG, "새로운 BA를 ReLoad하지 않음. 선택된 목록의 정보만 변경") //정보? 무엇을?
                            //Todo. 선택된 BA정보 업데이트
                            return@loop
                        } else {
                            //현재 시간정보와 현재 동작중인 BA의 validUtil보다 크면 BA를 종료 한다.
                            val currentDateToMs = getMillisFromUtcDatetime(getUtcDatetimeAsDate())
                            mCurrentBaInfo?.validUntil?.let {
                                Log.i(TAG, "mCurrentBaInfo?.validUntil = ${mCurrentBaInfo?.validUntil}")
                                validUntilToMills = getMillisFromUtcDatetime(mCurrentBaInfo?.validUntil!!)

                                if(currentDateToMs > validUntilToMills) {
                                    Log.i(TAG, "동작 시간을 초과 하였습니다. BA를 종료 합니다.")
                                    //todo BA 종료 기능 추가.
                                    mCurrentBaInfo = null
                                    isBaRunning = false
                                    return@loop
                                }
                            }

                            //현재 동작중인 BA정보와 List의 BA정보가 다르면 새로운 BA를 동작 시킨다.
                            //이미 다른 appId가 존재 함을 확인 하였고 default가 true면 해당 BA를 로드 하면 된다.
                            if(it.default == "true") {
                                Log.i(TAG, "현재 BA와 값이 다르며 Default가 설정된 BA가 존재 함. 새롭게 실행.")
                                isBaRunning = true
                                baSelectMgr(
                                    it.appContextId, it.appId, it.requiredCapabilities, it.validFrom,
                                    it.validUntil, it.default, it.bbandUrl, it.bcastPageUrl, it.bcastPackageUrl
                                )
                                return@loop
                            }
                        }
                    }

                    Log.i(TAG, "Default가 ture인 데이터가 존재 하지 않음. 하지만 BA내용은 다름. 첫번째 BA를 로드 한다.")
                    isBaRunning = true
                    baSelectMgr(
                        baList[0].appContextId, baList[0].appId, baList[0].requiredCapabilities, baList[0].validFrom,
                        baList[0].validUntil, baList[0].default, baList[0].bbandUrl, baList[0].bcastPageUrl, baList[0].bcastPackageUrl
                    )
                }
            } else {    //BA가 동작 중이지 않을 때
                Log.i(TAG, "현재 동작 중인 BA가 존재하지 않음")

                //baList가 1개 초과인경우
                if(baList.size > 1) {
                    Log.i(TAG, "BA후보 목록이 1개 이상입니다.")
                    for(ba in baList) {
                        //ba의 default의 설정 여부 체크.
                        if(ba.default != null) {
                            if(ba.default == "true") {
                                Log.i(TAG, "BA default is true. BA를 동작 합니다.")
                                //todo BA로드 기능 추가.
                                baSelectMgr(
                                    ba.appContextId, ba.appId, ba.requiredCapabilities, ba.validFrom,
                                    ba.validUntil, ba.default, ba.bbandUrl, ba.bcastPageUrl, ba.bcastPackageUrl
                                )
                            }
                            isBaDefault = true
                        }
                    }

                    if(!isBaDefault) {
                        Log.i(TAG, "최종적으로 BA의 Default설정이 없습니다. 제일 첫번째 BA항목을 로드 합니다.")
                        //todo 첫번째 BA항목을 로드 한다.
                        mCurrentBaInfo = BAInfo(
                            baList[0].appContextId, baList[0].appId, baList[0].requiredCapabilities, baList[0].validFrom,
                            baList[0].validUntil, baList[0].default, baList[0].bbandUrl, baList[0].bcastPageUrl, baList[0].bcastPackageUrl
                        )
                        isBaRunning = true
                    }
                } else {
                    Log.i(TAG, "BA List가 1개 입니다. BA를 로드 합니다.size = ${baList.size}")
                    //todo BA Load기능 추가.
                    mCurrentBaInfo = BAInfo(
                        baList[0].appContextId, baList[0].appId, baList[0].requiredCapabilities, baList[0].validFrom,
                        baList[0].validUntil, baList[0].default, baList[0].bbandUrl, baList[0].bcastPageUrl, baList[0].bcastPackageUrl
                    )
                    isBaRunning = true
                }
            }
        } else {
            Log.i(TAG, "ba List가 존재 하지 않음.")

            if(isBaRunning) {
                Log.i(TAG, "BA를 UnLoad합니다.")
                isBaRunning = false
                mCurrentBaInfo = null
            } else {
                Log.i(TAG, "현재 실행 중인 BA가 없습니다.")
            }
        }

        mHandler?.sendEmptyMessageDelayed(MSG_HANDLE_CHECK_LIST, 5000) //3초마다 주기적으로 List를 체크 하도록 한다.
    }

    @Synchronized
    private fun baSelectMgr(appContextId: String?, appId: String?, requiredCapabilities: String?,
                            validFrom: String?, validUntil: String?, default: String?, bband: String?,
                            bcastPageUrl: String?, bcastPackageUrl: String?) {
        mCurrentBaInfo = BAInfo(
            appContextId, appId, requiredCapabilities, validFrom, validUntil, default, bband,
            bcastPageUrl, bcastPackageUrl
        )
    }

    //appId는 필수 (Launch APP)
    //appId가 같은데 같은 appId가 n개 이상이면 appContextId를 추가로 비교 한다. (appId와 같은지)
    //appId가 아예 없으면 동작 하면 안됨!(A/344 p186참고)
    private fun changeCurrentBA(appId: String) {
        //현재 동작중인 BA의 appid와 전달받은 appid가 다를 때 아래 동작을 수행한다.
        if(mCurrentAppId != appId) {
            Log.i(TAG, "현재 동작중인 BA의 appId와 전달 받은 appId가 다름.")
            for(baIdx in baList.indices) {
                if(baList[baIdx].appId == appId) {
                    Log.i(TAG, "요청한 appId가 존재 한다.")
                    if(baList[baIdx].appContextId == appId) {
                        Log.i(TAG, "appContextId가 appId와 같음. 동작.")
                        baSelectMgr(
                            baList[baIdx].appContextId, baList[baIdx].appId, baList[baIdx].requiredCapabilities, baList[baIdx].validFrom,
                            baList[baIdx].validUntil, baList[baIdx].default, baList[baIdx].bbandUrl, baList[baIdx].bcastPageUrl, baList[baIdx].bcastPackageUrl
                        )
                        break
                    } else {
                        Log.i(TAG, "appContextId가 appId와 다르다.")
                        baSelectMgr(
                            baList[baIdx].appContextId, baList[baIdx].appId, baList[baIdx].requiredCapabilities, baList[baIdx].validFrom,
                            baList[baIdx].validUntil, baList[baIdx].default, baList[baIdx].bbandUrl, baList[baIdx].bcastPageUrl, baList[baIdx].bcastPackageUrl
                        )
                    }
                }
            }
        } else Log.i(TAG, "BA가 서로 같음( BA change하지 않는다. )")
    }

    private fun getMillisFromUtcDatetime(dateStr: String): Long {
        val dateFormat1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val dateFormat2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return try {
            dateFormat1.parse(dateStr)?.time ?: throw IllegalArgumentException("Invalid date format")
        } catch (e: ParseException) {
            dateFormat2.parse(dateStr)?.time ?: throw IllegalArgumentException("Invalid date format")
        }
    }

    private fun getUtcDatetimeAsDate(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
}