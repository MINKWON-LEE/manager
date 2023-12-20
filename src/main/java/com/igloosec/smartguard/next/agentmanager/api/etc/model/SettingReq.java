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
public class SettingReq {
    @NotEmpty(message = "jobType is required.")
    private String jobType;    // WM400 : batchUpdateCheckSum, WM401 : batchUpdateSetupFileCheckSum

    private String option;
}
