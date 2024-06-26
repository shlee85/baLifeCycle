package com.example.balistupexample

object HeldObject {
    //단일 -> 단일 실행 되어야 한다.
    //bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html"
    //bband전용
    var held0 = """
    <HELD> 
        <HTMLEntryPackage appId="http://kids.pbs.org/a3" appContextId="http://kids.pbs.org"
         bbandEntryPageUrl="http://kids.pbs.org/a6/index.html"/>
    </HELD>
""".trimIndent()

    //다중=유효한 시간정보가 2개(나머지 유효하지 않음), default없음. -> 맨처음 유효한 데이터가 실행 되어야 함.
    var held1 = """
    <HELD>
        <HTMLEntryPackage appId="http://kids.pbs.org/a1" appContextId="http://kids.pbs.org/alt"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html" validFrom="2024-05-
        12T09:30:47Z" validUntil="2024-07-17T12:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a2" appContextId="http://kids.pbs.org/alt"
        bbandEntryPageUrl="http://kids.pbs.org/a5/index.html" validFrom="2024-05-08T04:30:47Z"
        validUntil="2024-05-18T23:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a3" appContextId="http://kids.pbs.org/alt"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html"
        bbandEntryPageUrl="http://kids.pbs.org/a6/index.html" validFrom="2024-05-07T12:30:47Z"
        validUntil="2024-05-17T13:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a3" appContextId="http://kids.pbs.org/a3"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html"
        bbandEntryPageUrl="http://kids.pbs.org/a6/index.html" validFrom="2024-05-08T02:30:47Z"
        validUntil="2024-05-19T16:07:20Z" requiredCapabilities="050E 058E |"/>
    </HELD>
""".trimIndent()

    //다중 = 유효한 시간정보가 2개, default=true 한개 있는 경우  ->  default가 true인 held데이터가 우선 동작. (validUntil만 초과하지 않으면 해당 값 유효)
    //bcastEntryPackageUrl="app" bcastEntryPageUrl="p2/index.html"
    var held2 = """
    <HELD>
        <HTMLEntryPackage appId="http://kids.pbs.org/a12" appContextId="http://kids.pbs.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" validUntil="2024-05-
        09T09:30:47Z" default="true"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a22" appContextId="http://kid.pbs.org"
        bcastEntryPackageUrl="app" requiredCapabilities="050E 058E |"
        bcastEntryPageUrl="p1a/index.html" validFrom="2024-05-17T08:00:47Z" validUntil="2024-05-10T09:30:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a3" appContextId="http://kids.pbs.org"
        bbandEntryPageUrl="http://kids.pbs.org/a6/index.html"
        validFrom="2024-05-13T09:30:47Z" validUntil="2024-07-17T12:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a42" appContextId="http://kids.pbs.org/alt"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html" validFrom="2024-05-
        08T12:30:47Z" validUntil="2024-05-10T19:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a5" appContextId="http://kids.pbs.org/alt"
        bbandEntryPageUrl="http://kids.pbs.org/a5/index.html" validFrom="2016-07-17T09:30:47Z"
        validUntil="2016-07-17T12:00:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a6" appContextId="http://kids.pbs.org/alt"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p2a/index.html"
        bbandEntryPageUrl="http://kids.pbs.org/a6/index.html" validFrom="2016-07-17T12:30:47Z"
        validUntil="2016-07-17T13:00:47Z"/>
    </HELD>
""".trimIndent()

    //다중= default 한개만 true -> default가 true인 데이터가 동작
    var held3 = """
    <HELD>
        <HTMLEntryPackage appId="http://kids.pbs.org/a13" appContextId="http://kids.pbs.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a23" appContextId="http://kids.pbs2.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" default="true"/>
    </HELD>
    """.trimIndent()

    //다중= default 없고, 유효한 시간정보가 한개의 데이터만 있을 때 -> 유효한 시간정보 데이터가 동작
    var held4 = """
    <HELD>
        <HTMLEntryPackage appId="http://kids.pbs.org/a14" appContextId="http://kids.pbs.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a24" appContextId="http://kids.pbs2.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" validFrom="2024-05-08T05:00:47Z" 
        validUntil="2024-05-09T20:30:47Z"/>
    </HELD>
    """.trimIndent()

    //다중= default없고, 유효한 시간정보가 모두 들어 있을 때 -> 첫번째 데이터가 동작
    var held5 = """
    <HELD>
        <HTMLEntryPackage appId="http://kids.pbs.org/a15" appContextId="http://kids.pbs.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" validFrom="2024-05-08T04:00:47Z" 
        validUntil="2024-05-09T21:30:47Z"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a25" appContextId="http://kids.pbs2.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" validFrom="2024-05-08T05:00:47Z" 
        validUntil="2024-05-09T20:30:47Z"/>
    </HELD>
    """.trimIndent()

    //한개만 유효함 하지만 default는 유효하지 않은 데이터만 가지고 있음. -> 시간이 유효한 데이터가 동작
    var held6 = """
    <HELD>
        <HTMLEntryPackage appId="http://kids.pbs.org/a16" appContextId="http://kids.pbs.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" validFrom="2024-05-08T04:00:47Z" 
        validUntil="2024-05-08T10:30:47Z" default="true"/>
        <HTMLEntryPackage appId="http://kids.pbs.org/a26" appContextId="http://kids.pbs2.org"
        bcastEntryPackageUrl="app" bcastEntryPageUrl="p1/index.html" validFrom="2024-05-08T05:00:47Z" 
        validUntil="2024-05-09T20:30:47Z"/>
    </HELD>
    """.trimIndent()

    //빈 held데이터 -> 현재 BA종료
    var held100 = """"""
}