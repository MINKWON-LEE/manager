package com.igloosec.smartguard.next.agentmanager.entity;

import org.springframework.web.multipart.MultipartFile;

public class DiagnosisResult {
    private MultipartFile file;
    private String agentCd;
    private String assetCd;
    private String swType;
    private String swNm;
    private String swInfo;
    private String swDir;
    private String swUser;
    private String swEtc;

    private String ipAddress;
    private String hostNm;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostNm() {
        return hostNm;
    }

    public void setHostNm(String hostNm) {
        this.hostNm = hostNm;
    }
}
