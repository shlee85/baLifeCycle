package com.example.balistupexample

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.text.ParseException
import java.text.SimpleDateFormat
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
    private var held = """
    <HELD>
        <HTMLEntryPackage appId="http://kids.pbs.org/a1" appContextId="http://kids.pbs.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" validUntil="2016-07-
        17T09:30:47Z" default="true"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a2" appContextId="http://kid.pbs.org"
        bcastEntryPackageUrl="app" requiredCapabilities="050E 058E |"
        bcastEntryPageUrl="p1a/index.html" validFrom="2016-07-17T08:00:47Z" validUntil="2016-07-17T09:30:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a3" appContextId="http://kids.pbs.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2/index.html" validFrom="2016-07-
        17T09:30:47Z" validUntil="2016-07-17T12:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a4" appContextId="http://kids.pbs.org/alt"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html" validFrom="2016-07-
        17T09:30:47Z" validUntil="2016-07-17T12:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a5" appContextId="http://kids.pbs.org/alt"
        bbandEntryPageUrl="http://kids.pbs.org/a5/index.html" validFrom="2016-07-17T09:30:47Z"
        validUntil="2016-07-17T12:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a6" appContextId="http://kids.pbs.org/alt"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html"
        bbandEntryPageUrl="http://kids.pbs.org/a6/index.html" validFrom="2016-07-17T12:30:47Z"
        validUntil="2016-07-17T13:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a7" appContextId="http://kids.pbs.org/alt"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html"
        bbandEntryPageUrl="http://kids.pbs.org/a6/index.html" validFrom="2024-05-07T02:30:47Z"
        validUntil="2024-05-07T20:00:47Z" requiredCapabilities="050E 058E |"/>
    </HELD>
""".trimIndent()

    data class BAInfo(var appContextId: String?, var appId: String?, var requiredCapabilities: String?,
                      var validFrom: String?, var validUntil: String?, var default: String?,
                      var bbandUrl: String?, var bcastPageUrl: String?, var bcastPackageUrl: String?)

    private var baList: ArrayList<BAInfo> = ArrayList()

    private val TAG = "SHLEE"
    private var isBaRunning = false
    private var mCurrentContextId: String? = null
    private var mCurrentAppId: String? = null
    private lateinit var mCurrentBaInfo: BAInfo
    private var validFromToMills: Long = 0
    private var validUntilToMills: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //현재 동작중인 BA의 app 및 appContextId를 임시로 만든다.
        mCurrentAppId = "http://kids.pbs.org/a2"
        mCurrentContextId = "http://kids.pbs.org"

        //테스트를 위한 현재 동작중인 BA설정을 한다.
        mCurrentBaInfo = BAInfo(
            mCurrentContextId, mCurrentAppId, null, "2024-05-07T02:30:47Z",
            "2024-05-07T05:30:47Z", null, null, null, null
        )

        Log.i(TAG, "현재 동작중인 BA 정보 = [$mCurrentBaInfo]")

        baListUp(held)
        printBaList()

        baListMgr()
    }

    private fun printBaList() {
        baList.forEach { Log.i(TAG, "$it") }
    }

    //목록제외 조건 적용 후 나머지 리스트업한다.
    //BA후보군으로 관리 하도록 파싱 후 파싱 데이터 List에 추가 한다.

    @Synchronized
    private fun baListUp(held: String) {
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

        val currentDateMS: Long = getMillisFromUtcDatetime(getUtcDatetimeAsDate())
        Log.i(TAG, "getUtcDatetimeAsDate = [$currentDateMS]")

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
    }

    @Synchronized
    private fun baListMgr() {
        if(baList.isNotEmpty()) {
            //현재 시간 기준 vaildFrom과 valildUntil체크 (mills)
            //string to mills로 변환하여 비교 한다.
            /*
            var validFromToMills: Long = 0
            var validUntilToMills: Long = 0
            if(validFrom != null) {
                Log.i(TAG, "validFrom = $validFrom")
                validFromToMills = getMillisFromUtcDatetime(validFrom)
                Log.i(TAG, "validFromToMills:[$validFromToMills]")
            }

            if(validUntil != null) {
                validUntilToMills = getMillisFromUtcDatetime(validUntil)
                Log.i(TAG, "validUntilToMills:[$validUntilToMills]")
            }

            if(validFrom != null && validUntil != null) {
                Log.i(TAG, "[$validFromToMills][$validUntilToMills][$currentDateMS]")
                if(currentDateMS in (validFromToMills + 1)..<validUntilToMills) {
                    Log.i(TAG, "조건 충족함 리스트 업.")

                } else {
                    Log.i(TAG, "조건 충족하지 않음!")
                }
            }
             */
            Log.i(TAG, "ba List가 존재 함.")

            if(isBaRunning) {
                Log.i(TAG, "현재 동작 중인 BA가 존재 함.")

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
                            mCurrentBaInfo.validUntil.let {
                                validUntilToMills = getMillisFromUtcDatetime(mCurrentBaInfo.validUntil!!)

                                if(currentDateToMs > validUntilToMills) {
                                    Log.i(TAG, "동작 시간을 초과 하였습니다. BA를 종료 합니다.")
                                }
                            }
                        }
                    }
                }


            } else {
                Log.i(TAG, "현재 동작 중인 BA가 존재하지 않음")

            }
        } else {
            Log.i(TAG, "ba List가 존재 하지 않음.")

            if(isBaRunning) {
                Log.i(TAG, "BA를 Un Load합니다.")
            } else {
                Log.i(TAG, "현재 실행 중인 BA가 없습니다.")
            }
        }
    }

    private fun baSelectMgr() {

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