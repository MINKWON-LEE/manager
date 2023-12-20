package com.igloosec.smartguard.next.agentmanager.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiJobResult {

    private String auditFileCd;

    private String fileNm;

    private String auditFileNm;

    private String agentCpuMax;

    private String agentMemMax;

    private String useDiagSudo;

    private String version;

    private String swType;

    private String dgWaitTime;

    private String diagInfoUse;
}
