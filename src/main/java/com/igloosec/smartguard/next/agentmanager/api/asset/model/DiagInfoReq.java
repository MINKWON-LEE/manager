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
public class DiagInfoReq {

    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;           // AJ202 환경 수집 파일 복호화

    private String swType;
    private String swNm;
    private String swInfo;
    private String swDir;
    private String swUser;
    private String swEtc;
}
