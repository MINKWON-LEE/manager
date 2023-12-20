package com.igloosec.smartguard.next.agentmanager.api.asset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDel {
    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;    // AJ150 : jobDel

    private String swType;

    private String swNm;

    private String swInfo;

    private String swDir;

    private String swUser;

    private String swEtc;

    private String managerCd;

    private String agentUserStime;

    private String agentUserEtime;
}
