package com.igloosec.smartguard.next.agentmanager.api.etc.model;

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
public class CheckSumReq {
    @NotEmpty(message = "jobType is required.")
    private String jobType;    // WM400 : batchUpdateCheckSum, WM401 : batchUpdateSetupFileCheckSum

    @NotEmpty(message = "fileType is required.")
    private String fileType;   // jobType이 WM400일 때 scripts, jobType이 WM401일 때 installer

    private String special;
}
