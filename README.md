A344(BA) : https://www.atsc.org/wp-content/uploads/2024/04/A344-2024-04-Interactive-Content.pdf

SLS(HELD) : https://www.atsc.org/wp-content/uploads/2024/04/A331-2024-04-Signaling-Delivery-Sync-FEC.pdf

ATSC3.0 A344/Interactive Content의 라이플 사이클에 관한 테스트 코드 이다.

HELD 수신시 HELD의 각 정보를 토대로 동작되는 BA 후보군을 추리고 조건에 맞게 동작되도록 처리한다.
* 주의사항
 - 시간 체크 할 때 UTC기준으로 처리를 하게 되는데. 현재 시간정보를 가져 올때는 일반 UTC타임으로 가져 오므로 설정된 시간의 -9로 되어 있다.
 - 현재 시간(UTC) + 9 (3240000) 으로 해서 시간 계산을 하도록 처리 하자.

LaunchApp 동작 조건
- 성공시 별도의 응답은 하지 않으며 실패시에만 에러 코드로 응답해야 한다.
- error code
  -23 : appId가 held에 없는 경우
  -25 : appId가 존재, 사용못하거나 Bcast전용
  -26 : appId가 존재, bband전용 이며 네트워크 연결이 되어 있지 않음.
  -27 : 수신기가 필수로 요구되는 기능을 지원하지 않음.
