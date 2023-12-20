package com.igloosec.smartguard.next.agentmanager.api.etc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Alias("InstallFile")
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstallFile {
    private String swNm;
    private String bitType;
    private String installFileNm;
    private String installFileDir;
    private String checksumHash;
    private String agentType;
}
