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
@Alias("SnetAssetGetHistoryModel")
public class SnetAssetGetHistoryModel {
    private String assetCd;
    private String getDay;
    private String getType;
    private String userId;
    private String userNm;
    private String branchNm;
    private String teamNm;
    private String osNm;
    private String hostNm;
    private String ipAddress;
    private float auditRate;
    private float auditRateFirewall;
    private String auditDay;
    private String hostGrade;
    private String vmYn;
    private String ismsYn;
    private String infraYn;
    private String autoAuditYn;
    private String assetRmk;
    private String govFlag;
    private String agentCd;
    private String agentVersion;
    private int agentStatus;
    private int agentType;
    private String masterUpdateDate;
}
