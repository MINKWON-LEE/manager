package com.igloosec.smartguard.next.agentmanager.entity;

import lombok.Data;

@Data
public class ConfigAuditItemExcept {

    private String auditFileCd;
    private String diagnosisCd;
    private int    diagnosisType;
    private String createDate;
    private String createUserId;
}
