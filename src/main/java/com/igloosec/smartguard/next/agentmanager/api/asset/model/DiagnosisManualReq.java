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
public class DiagnosisManualReq {
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;           // WM102 : manualDgFin (xml 파일업로드) WM103 : multiManualDgFin (zip 파일 업로드)

    @NotEmpty(message = "fileNm is required.")
    private String fileNm;

    private String swType;            // single파일 일 때만 사용
    private String swNm;              // single파일 일 때만 사용
    private String swInfo;            // single파일 일 때만 사용
    private String swDir;             // single파일 일 때만 사용
    private String swUser;            // single파일 일 때만 사용
    private String swEtc;             // single파일 일 때만 사용
    private String ipAddress;         // single파일 일 때만 사용
    private String hostNm;            // single파일 일 때만 사용
    private String swUrlCd;           // zip파일 일 때만 사용
}
