package com.igloosec.smartguard.next.agentmanager.entity;

import lombok.Data;

@Data
public class ConfigAuditItem {

    private String auditFileCd;
    private String diagnosisCd;
    private int    diagnosisType;
    private String swNm;
    private String itemGrpNm;
    private String itemNm;
    private String itemGrade;
    private String itemStandard;
    private String itemCounterMeasure;
    private String itemCounterMeasureDetail;
    private String diagnosisFunction;
    private String createDate;
    private String updateDate;
    private int    weightTot;
    private int    resultTot;
    private String itemReason;
}
