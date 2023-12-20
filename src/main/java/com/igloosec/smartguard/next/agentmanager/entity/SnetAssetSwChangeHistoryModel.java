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
@Alias("SnetAssetSwChangeHistoryModel")
public class SnetAssetSwChangeHistoryModel {
    private String assetCd;
    private String swType;
    private String swNm;
    private String swInfo;
    private String swUser;
    private String swDir;
    private String swEtc;
    private String auditDay;
    private float auditRate;
    private float auditRateFirewall;
    private String auditFileCd;
    private String swOperator;
    private String changeType;
    private String getDay;
    private String userRegi;

    public String toString(){
        return assetCd + swType + swNm + swInfo + swDir + swUser + swEtc;
    }
}
