package com.igloosec.smartguard.next.agentmanager.entity;

import lombok.Data;

@Data
public class AgentJob {

    private String assetCd;

    private String jobType;

    private String checkFlag;   // WAS로부터 요청만 있어서 큐에만 등록된 상태면 0. 에이전트가 job을 정해진 checkCnt 만큼 요청해 갔다면 1.

    private int checkCnt;    // 에이전트가 job 가져갔으면 ++1.

    private String delayTime;   // 에이전트가 주기적으로 job request를 호출할 시간 조정.

    public AgentJob(String assetCd, String jobType, String checkFlag, int checkCnt, String delayTime) {

        this.assetCd = assetCd;
        this.jobType = jobType;
        this.checkFlag = checkFlag;
        this.checkCnt = checkCnt;
        this.delayTime = delayTime;
    }
}
