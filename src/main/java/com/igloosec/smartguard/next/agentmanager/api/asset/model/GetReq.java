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
public class GetReq implements IReq {
    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;    // AJ200 : gscrptExecReq

    private String personManagerCd;

    private String personManagerNm;

    private String managerCd;

    private String managerNm;

    private String swNm;

    private String getSeq;  // 한 장비에 대해 장비 정보 수집 요청이 여러개 있을 경우 대비

}
