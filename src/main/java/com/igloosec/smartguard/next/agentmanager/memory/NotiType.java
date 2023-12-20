package com.igloosec.smartguard.next.agentmanager.memory;

import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;

/**
 * 에이전트가 수집 / 진단 기능 수행시 현재 상태를 업데이트.
 */
public enum NotiType {
    AN001("completedFileHash"),                    // 진단(수집)스크립트 다운로드 및 파일 해쉬 체크 완료
    AN002("completedScriptExec"),                  // 수집 / 진단 스크립트 실행 완료
    AN003("startGetInfo"),                         // 수집 JOB 요청 정보를 에이전트가 가져감
    AN051("failedFileHash"),                       // 스크립트 파일 해쉬 체크(혹은 압축 해제) 실패
    AN052("failedScriptExec"),                     // 에이전트 스크립트 실행 중 실패 (스크립트 에러)
    AN053("failedRestartStop"),                    // 재시작 / 중지 실패
    AN054("failedDownloadScript"),                 // 스크립트 파일 다운로드 실패
    AN055("failedLackOfResource"),                 // 에이전트 진단 실행 중 자원문제로 인한 진단실패
    AN056("failedTimedOut"),                       // 에이전트 진단 실행 대기 시간 초과로 인한 진단 실패
    AN057("getNotHashMatch"),                      // 수집스크립트 해쉬값 불일치로 인한 수집/진단 실패                // ERR_0036
    AN058("diagNotHashMatch"),                     // 진단스크립트 해쉬값 불일치로 인한 수집/진단 실패                // ERR_0037.
    AN059("failedAgentUpdate"),                    // 에이전트 업데이트 실패
    AN060("errorFin"),                             // 2.0에서 ERRORFIN (sudo sh 스크립트했을 때 에러 메시지)
    AN061("timeout"),                              // 수집, 진단, 재시작, 에이전트 업데이트, 로그 수집 등등 타임아웃으로 인한 진단 실패   // ERR_0001
    AN062("malfromedXMLOnline"),                   // 진단 결과 파일 XML 확인 필요 (온라인)                            // ERR_0033
    AN063("malfromedXMLManual"),                   // 진단 결과 파일 XML 확인 필요 (수동업로드)                        // ERR_0034
    AN064("failedFileDecryption"),                 // 서버에 수집/진단 스크립트가 존재하는지 확인 필요                 // ERR_0006
    AN065("failedStdDiag"),                        // 화면에서 진단 기준 정상적으로 선택했는지 확인 필요               // ERR_0012
    AN066("malformedDAT"),                         // 수집 결과 파일 형식 오류                                         // ERR_0013, ERR_0020
    AN067("malformedXML"),                         // 진단 결과 파일 형식 오류                                         // ERR_0015
    AN068("malformedDATFromAgent"),                // 에이전트에서 통해 올라온 수집 결과 파일 복호화 실패              // ERR_0019
    AN069("failedBackUpDAT"),                      // 수집 결과파일 파싱 후 서버에서 백업 과정에서 오류                // ERR_0021
    AN070("checkedManagerCd"),                     // SU 계정이 DB에 하나라도 있는지 확인                              // ERR_0023
    AN071("errorSQL"),                             // DB 쿼리 실행 중 오류                                             // ERR_0024, ERR_0026     // 진단 결과 파일의 result 값이 error등2글자이상이라든지..
    AN072("checkIPHOSTNMdat"),                     // 수집 결과 파일에 ip, hostname 오류                               // ERR_0027
    AN073("malformedNWcfg"),                       // 네트워크 설정 파일 재확인 필요                                         // ERR_0035
    AN074("diffXMLandDB"),                         // 디비에 저장되어 있는 자산정보와 진단 결과파일의 자산 정보가 다를 때    // ERR_0038
    AN075("notFoundAgentInfoDB"),                  // DB 진단 실행 요청시 해당 자산코드에 대한 에이전트 정보를 찾을 수 없음  // ERR_0040
    AN076("notFoundAgentInfo"),                    // 진단 실행 요청시 해당 자산코드에 대한 에이전트 정보를 찾을 수 없음     // ERR_0041
    AN077("failedCreateJobEntity");                // 진단 실행 Job 생성 실패

    private final String STATUS_PREFIX = "status_";
    private String status;

    NotiType(String jobType) {
        this.status = jobType;
    }

    public String getStatus() { return STATUS_PREFIX + status.toUpperCase(); }

    public String getValue() { return status; }

    public boolean isEquals(String status) {
        return this.getStatus().equals(status);
    }

    public static String getStatusLog(String jobType, String status, String msg) {
        String scriptType ="", logMsg = "";
        if (jobType.equals(ManagerJobType.AJ200.toString())) {
            scriptType = "for asset information gathering.";
        } else if (jobType.equals(ManagerJobType.AJ100.toString())) {
            scriptType = "for diagnosis.";
        } else if (jobType.equals(ManagerJobType.AJ101.toString())) {
            scriptType = "for EVENT diagnosis.";
        } else if (jobType.equals(ManagerJobType.AJ601.toString()) || jobType.equals(ManagerJobType.AJ602.toString())) {
            scriptType = "for controlling the agent.";
        }

        if (status.equals(NotiType.AN001.toString())) {
            logMsg = " : checked file`s hash successfully " + scriptType;
        } else if (status.equals(AN002.toString())) {
            logMsg = " : executed script file successfully " + scriptType;
        } else if (status.equals(AN003.toString())) {
            logMsg = " : agent gets job request " + scriptType;
        } else if (status.equals(AN051.toString())) {
            logMsg = " : failed to check file`s hash " + scriptType;
        } else if (status.equals(AN052.toString())) {
            logMsg = " : failed to execute script file " + scriptType;
        } else if (status.equals(AN053.toString()) && jobType.equals(ManagerJobType.AJ601.toString())) {
            logMsg = " : failed to restart the agent " + scriptType;
        } else if (status.equals(AN053.toString()) && jobType.equals(ManagerJobType.AJ602.toString())) {
            logMsg = " : failed to stop the agent " + scriptType;
        } else if (status.equals(AN054.toString())) {
            logMsg = " : failed to download script file " + scriptType;
        } else if (status.equals(AN055.toString())) {
            logMsg = " : lack of resource " + scriptType;
        } else if (status.equals(AN056.toString())) {
            logMsg = " : the waiting time has exceeded " + scriptType;
        } else if (status.equals(AN059.toString())) {
            logMsg = " : failed to update the agent " + scriptType;
        } else if (status.equals(AN060.toString())) {
            if (msg != null) {
                String[] args = msg.split("\\%");
                if (args.length >= 3) {
                    logMsg = SnetCommonErrCode.ERR_0039.getMessage(args[1], args[2]);
                } else {
                    logMsg = SnetCommonErrCode.ERR_0039.getMessage(msg);
                }
            }
        }
        return status + logMsg;
    }
}
