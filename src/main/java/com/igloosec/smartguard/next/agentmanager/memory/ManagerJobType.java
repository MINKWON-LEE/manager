package com.igloosec.smartguard.next.agentmanager.memory;

public enum ManagerJobType {
    AJ100("dgExecReq"),                          // 진단실행 (= dgFile, = dgFin)
    AJ154("dgExecLog"),                          // 진단실행시 스크립트 오류등의 사유로 로그파일만 업로드 하는 경우
    AJ101("eventDgExecReq"),                     // 긴급진단실행
    AJ150("jobDel"),                             // 진단실행 취소 요청
    AJ200("gscrptExecReq"),                      // 장비정보 수집 요청 (= gscrptFile, = gscrptFin)
    AJ201("gsBfAgent"),                          // Agent 설치전 GET SCRIPT 실행. 수동 설치시 에이전트 설치전 에이전트가 실행.
    AJ202("gsDiagInfo"),                         // 수집 진단 분리시 환경 수집 파일 복호화 및 UI에서 다운로드 가능하게 zip파일 생성
    AJ300("agentLogs"),                          // 에이전트 로그 수집 요청
    AJ400("runCmd"),                             // 실시간 커맨드 실행 요청
    AJ500("ipUpd"),                              // 매니저 서버 IP변경 요청
    AJ600("agentSetupReq"),                      // 에이전트 설치 요청
    AJ601("restartAgent"),                       // 에이전트 재시작 요청
    AJ602("killAgent"),                          // 에이전트 중지 요청
    AJ603("agentUpdate"),                        // 에이전트 업데이트 요청
    AJ800("errorFin"),                           // job 수행 중 실시간 에러 처리
    WM100("manualGscrptFin"),                    // 장비정보 수집 요청 수동 (single file, .dat)
    WM101("multiManualGscrptFin"),               // 장비정보 수집 요청 수동 (multi files, .zip)
    WM102("manualDgFin"),                        // 진단실행 수동 (single file, .xml)
    WM103("multiManualDgFin"),                   // 진단실행 수동 (multi files, .zip)
    WM200("srvManualGscrptFin"),                 // 네트워크 장비정보 수집 요청 혹은 네트워크 장비 진단실행 수동 (single file, ex. cfg,,,)
    WM201("multiSrvManualGscrptFin"),            // 네트워크 장비정보 수집 요청 혹은 네트워크 장비 진단실행 수동 (multi files, ex. zip)
    WM202("srvManualDgFin"),                     // 네트워크 장비 진단실행 수동 (수동으로 네트워크 장비 config 파일을 업로드하여 .xml 파일을 만들어 진단한다.)
    WM300("networkExeReq"),                      // 네트워크 장비정보 수집 요청 자동
    WM301("srvGscrptFin"),                       // 네트워크 장비정보 수집 요청 자동 (수동으로 네트워크 장비 config 파일을 업로드하여 .dat 파일을 만들어 장비정보 등록을 한다.)
    WM302("networkDgExecReq"),                   // 네트워크 진단실행 요청 자동
    WM303("srvDgFin"),                           // 네트워크 장비 진단실행 자동 (수동으로 네트워크 장비 config 파일을 업로드하여 .dat 파일을 만들어 장비정보 등록을 한다.)
    WM400("batchUpdateCheckSum"),                // 수집, 진단 스크립트 업데이트 요청
    WM401("batchUpdateSetupFileCheckSum"),       // 설치파일 해쉬값 업데이트 요청
    WM500("memoryReload");                       // configGlobal 메모리 리셋

    private final String CALLER_PREFIX = "CALLER_";
    private String jobType;

    ManagerJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getCallerType() { return CALLER_PREFIX + jobType.toUpperCase(); }

    public String getValue() { return jobType; }

    public boolean isEquals(String caller) {
        return this.getCallerType().equals(caller);
    }
}
