package com.igloosec.smartguard.next.agentmanager.entity;

public class AgentJobHistory {
    private String agentCd;
    private String assetCd;
    private String swType;
    private String swNm;
    private String swInfo;
    private String agentJobRdate;
    private String userId;
    private int svcType;
    private String auditFileCd;
    private int agentJobFlag;
    private String agentJobSdate;
    private String agentJobEdate;
    private String agentJobDesc;
    private String swDir;
    private String swUser;
    private String swEtc;
    private String statusLog;

    public String getAgentCd() {
        return agentCd;
    }

    public void setAgentCd(String agentCd) {
        this.agentCd = agentCd;
    }

    public String getAssetCd() {
        return assetCd;
    }

    public void setAssetCd(String assetCd) {
        this.assetCd = assetCd;
    }

    public String getSwType() {
        return swType;
    }

    public void setSwType(String swType) {
        this.swType = swType;
    }

    public String getSwNm() {
        return swNm;
    }

    public void setSwNm(String swNm) {
        this.swNm = swNm;
    }

    public String getSwInfo() {
        return swInfo;
    }

    public void setSwInfo(String swInfo) {
        this.swInfo = swInfo;
    }

    public String getAgentJobRdate() {
        return agentJobRdate;
    }

    public void setAgentJobRdate(String agentJobRdate) {
        this.agentJobRdate = agentJobRdate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getSvcType() {
        return svcType;
    }

    public void setSvcType(int svcType) {
        this.svcType = svcType;
    }

    public String getAuditFileCd() {
        return auditFileCd;
    }

    public void setAuditFileCd(String auditFileCd) {
        this.auditFileCd = auditFileCd;
    }

    public int getAgentJobFlag() {
        return agentJobFlag;
    }

    public void setAgentJobFlag(int agentJobFlag) {
        this.agentJobFlag = agentJobFlag;
    }

    public String getAgentJobSdate() {
        return agentJobSdate;
    }

    public void setAgentJobSdate(String agentJobSdate) {
        this.agentJobSdate = agentJobSdate;
    }

    public String getAgentJobEdate() {
        return agentJobEdate;
    }

    public void setAgentJobEdate(String agentJobEdate) {
        this.agentJobEdate = agentJobEdate;
    }

    public String getAgentJobDesc() {
        return agentJobDesc;
    }

    public void setAgentJobDesc(String agentJobDesc) {
        this.agentJobDesc = agentJobDesc;
    }

    public String getSwDir() {
        return swDir;
    }

    public void setSwDir(String swDir) {
        this.swDir = swDir;
    }

    public String getSwUser() {
        return swUser;
    }

    public void setSwUser(String swUser) {
        this.swUser = swUser;
    }

    public String getSwEtc() {
        return swEtc;
    }

    public void setSwEtc(String swEtc) {
        this.swEtc = swEtc;
    }

    public String getStatusLog() {
        return statusLog;
    }

    public void setStatusLog(String statusLog) {
        this.statusLog = statusLog;
    }

    private int fileType;
    private String fileNm;
    private String checksumHash;

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getFileNm() {
        return fileNm;
    }

    public void setFileNm(String fileNm) {
        this.fileNm = fileNm;
    }

    public String getChecksumHash() {
        return checksumHash;
    }

    public void setChecksumHash(String checksumHash) {
        this.checksumHash = checksumHash;
    }
}
