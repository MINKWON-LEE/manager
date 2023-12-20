package com.igloosec.smartguard.next.agentmanager.entity;

import lombok.Data;

@Data
public class ConfigAuditItemOption {

    private String auditFileCd;

    private String diagnosisCd;

    private String optionKey;

    private String optionDefaultValue;

    private String optionValue;

    private String description;

    private String numYn;

    private String optionReason;
}
