package com.igloosec.smartguard.next.agentmanager.entity;

import java.util.Date;

public class AgentMaster {
    private String agentCd;
    private String agentRegiFlag;
    private String agentRegiDesc;
    private int agentType;
    private Date agentInstallDate;
    private String agentInstallUserNm;
    private Date agentStartDate;
    private String agentUseStime;
    private String agentUseEtime;
    private String agentVersion;

    public String getAgentCd() {
        return agentCd;
    }

    public void setAgentCd(String agentCd) {
        this.agentCd = agentCd;
    }

    public String getAgentRegiFlag() {
        return agentRegiFlag;
    }

    public void setAgentRegiFlag(String agentRegiFlag) {
        this.agentRegiFlag = agentRegiFlag;
    }

    public String getAgentRegiDesc() {
        return agentRegiDesc;
    }

    public void setAgentRegiDesc(String agentRegiDesc) {
        this.agentRegiDesc = agentRegiDesc;
    }

    public int getAgentType() {
        return agentType;
    }

    public void setAgentType(int agentType) {
        this.agentType = agentType;
    }

    public Date getAgentInstallDate() {
        return agentInstallDate;
    }

    public void setAgentInstallDate(Date agentInstallDate) {
        this.agentInstallDate = agentInstallDate;
    }

    public String getAgentInstallUserNm() {
        return agentInstallUserNm;
    }

    public void setAgentInstallUserNm(String agentInstallUserNm) {
        this.agentInstallUserNm = agentInstallUserNm;
    }

    public Date getAgentStartDate() {
        return agentStartDate;
    }

    public void setAgentStartDate(Date agentStartDate) {
        this.agentStartDate = agentStartDate;
    }

    public String getAgentUseStime() {
        return agentUseStime;
    }

    public void setAgentUseStime(String agentUseStime) {
        this.agentUseStime = agentUseStime;
    }

    public String getAgentUseEtime() {
        return agentUseEtime;
    }

    public void setAgentUseEtime(String agentUseEtime) {
        this.agentUseEtime = agentUseEtime;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }
}
