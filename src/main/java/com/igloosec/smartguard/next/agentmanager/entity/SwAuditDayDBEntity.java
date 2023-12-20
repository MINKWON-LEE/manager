/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.SwAuditDayDBEntity.java
 * company : Mobigen
 *
 * @author : Je Joong Lee
 * created at : 2016. 2. 26.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.entity;

import org.apache.commons.lang.StringUtils;

public class SwAuditDayDBEntity extends BaseDBEntity {

    private String swType;
    private String swNm;
    private String swInfo;
    private String swOperator;
    private String auditFileCd;
    private String auditDay;
    private String osKind; //window,linux.....
    private String cveCode;
    private int cveCodeCnt;
    private String cveUpdateDate;
    private String assetName;

    public String getOsKind() {
        return osKind;
    }

    public void setOsKind(String osKind) {
        this.osKind = osKind;
    }

    /*
     * 2016.11.17
     * WEB, WAS, DB 멀티 진단을 위한 추가 인자
     */
    private String swDir = "-";
    private String swUser = "-";
    private String swEtc = "-";

    /*
     * 20160328 snet_asset_master govflag에 따른 파일 분류
     */
    private int fileType;

    private String containerId = "-";  // 클라우드 자산 Container ID

    private String containerNm = "-";  // 클라우드 자산 Container Name

    private String pod = "-";  // 클라우드 자산 Pod

    private String nameSpace = "-";  // 클라우드 자산 NameSpace

    private String temp = "";
    /**
     * @return the swType
     */
    public String getSwType() {
        return swType;
    }

    /**
     * @param swType
     *            the swType to set
     */
    public void setSwType(String swType) {
        this.swType = swType;
    }

    /**
     * @return the swNm
     */
    public String getSwNm() {
        return swNm;
    }

    /**
     * @param swNm
     *            the swNm to set
     */
    public void setSwNm(String swNm) {
        this.swNm = swNm;
    }

    /**
     * @return the swInfo
     */
    public String getSwInfo() {
        return swInfo;
    }

    /**
     * @param swInfo
     *            the swInfo to set
     */
    public void setSwInfo(String swInfo) {
        this.swInfo = swInfo;
    }

    /**
     * @return the auditFileCd
     */
    public String getAuditFileCd() {
        return auditFileCd;
    }

    /**
     * @param auditFileCd
     *            the auditFileCd to set
     */
    public void setAuditFileCd(String auditFileCd) {
        this.auditFileCd = auditFileCd;
    }

    /**
     * @return the auditDay
     */
    public String getAuditDay() {
        return auditDay;
    }

    /**
     * @param auditDay
     *            the auditDay to set
     */
    public void setAuditDay(String auditDay) {
        this.auditDay = auditDay;
    }

    /**
     * @return the fileType
     */
    public int getFileType() {
        return fileType;
    }

    /**
     * @param fileType the fileType to set
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
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

    public String getSwOperator() {
        return swOperator;
    }

    public void setSwOperator(String swOperator) {
        this.swOperator = swOperator;
    }

    public String getCveCode() {
        return cveCode;
    }

    public void setCveCode(String cveCode) {
        this.cveCode = cveCode;
    }

    public int getCveCodeCnt() {
        return cveCodeCnt;
    }

    public void setCveCodeCnt(int cveCodeCnt) {
        this.cveCodeCnt = cveCodeCnt;
    }

    public String getCveUpdateDate() {
        return cveUpdateDate;
    }

    public void setCveUpdateDate(String cveUpdateDate) {
        this.cveUpdateDate = cveUpdateDate;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = StringUtils.defaultIfEmpty(this.containerId, "-");
        this.containerId = containerId;
    }

    public String getContainerNm() {
        return containerNm;
    }

    public void setContainerNm(String containerNm) {
        this.containerNm = StringUtils.defaultIfEmpty(this.containerNm, "-");
        this.containerNm = containerNm;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = StringUtils.defaultIfEmpty(this.pod, "-");
        this.pod = pod;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = StringUtils.defaultIfEmpty(this.nameSpace, "-");
        this.nameSpace = nameSpace;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "SwAuditDayDBEntity [swType=" + swType + ", swNm=" + swNm
                + ", swInfo=" + swInfo + ", auditFileCd=" + auditFileCd
                + ", auditDay=" + auditDay + ", swOperator=" + swOperator + "]";
    }
}
