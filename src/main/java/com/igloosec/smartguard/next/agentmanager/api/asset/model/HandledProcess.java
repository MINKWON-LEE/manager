package com.igloosec.smartguard.next.agentmanager.api.asset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HandledProcess {
    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "agentCd is required.")
    private String agentCd;

    private String auditFileCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;

    @NotEmpty(message = "notiType is required.")
    private String notiType;      // AN001 : 파일 해쉬 체크 완료, AN002 : (긴급)진단 스크립트 실행 완료                 //파일 다운로드 요청시 사용안함.
                                  // AN051 : 파일 해쉬 체크 실패, AN052 : 스크립트 실행 실패, AN060 :  2.0에서 ERRORFIN (sudo sh 스크립트했을 때 에러 메시지), AN053 : 재시작/중지 실패

    private String message;       // 2.0에서 ERRORFIN (sudo sh 스크립트했을 때 에러 메시지)
}
