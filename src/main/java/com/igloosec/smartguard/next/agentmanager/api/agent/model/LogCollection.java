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
public class LogCollection {
    @NotEmpty(message = "assetCd is required.")
    private String assetCd;     // 로그 파일 수집 요청, 로그 파일 업로드

    @NotEmpty(message = "jobType is required.")
    private String jobType;     // 로그 파일 수집 요청
}
