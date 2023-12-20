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
public class DiagnosisReq {
    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;

    private String swType;   //긴급 진단에선 사용 X
    private String swNm;
    private String swInfo;
    private String swDir;
    private String swUser;
    private String swEtc;
    private String managerCd;
    private String agentUseStime;
    private String agentUseEtime;
    private String auditSpeed;
    private String prgId;   // 긴급 진단시에만 사용

    private String diagInfoUse;  // 수집 진단 분리
}
