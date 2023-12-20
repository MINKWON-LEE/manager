package com.igloosec.smartguard.next.agentmanager.api.agent.model;

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
public class ControlAgentReq {

    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    private String agentCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;     // AJ600 : agentSetupReq (설치), AJ601 : restartAgent (재시작), AJ602 : killAgent (중지), AJ603 : agentUpdate(업데이트)
}
