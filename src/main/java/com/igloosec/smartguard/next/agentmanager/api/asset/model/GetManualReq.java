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
public class GetManualReq {
    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;   // WM100 : manualGscrptFin (dat 파일업로드), WM101 : multiManualGscrptFin (zip 파일 업로드)

    @NotEmpty(message = "fileNm is required.")
    private String fileNm;

    private String managerCd; // single 파일일떄만 사용.

    private String swUrlCd;   // zip파일일 때만 사용.

    private String getSeq;
}
