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
public class NwDgManualReq {
    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;       // WM200 : srvManualGscrptFin, WM201 : multiSrvManualGscrptFin  (수집과 같은 jobType을 사용)

    private String personManagerCd;

    private String personManagerNm;

    private String managerCd;

    private String managerNm;

    @NotEmpty(message = "swNm is required.")
    private String swNm;         // vendorCd

    @NotEmpty(message = "fileNm is required.")
    private String fileNm;       // configfileName

    @NotEmpty(message = "message is required.")
    private String message;      // auditType "AUDIT" 일 때 진단

    private String swInfo;       // single 일 때 사용

    private String nwUrlCd;      // zip 일 때 사용

    private String getSeq;
}
