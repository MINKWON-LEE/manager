package com.igloosec.smartguard.next.agentmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("SnetNotificationDataModel")
public class SnetNotificationDataModel {
    private long notiSeq;
    private String assetCd;
    private String swType;
    private String swNm;
    private String swInfo;
    private String swUser;
    private String swDir;
    private String swEtc;
    private String auditDay;
    private double auditRate;
    private String assetUserId;
    private String diagnosisCd;
    private String hostNm;
    private String ipAddress;
}
