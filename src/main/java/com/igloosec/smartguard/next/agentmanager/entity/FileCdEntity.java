/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.FileCdEntity.java
 * @author : Oh su jin
 * created at : 2016. 7. 29.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.entity;

public class FileCdEntity {

    /**
     * 상용 진단파일 코드 일괄 수정을 위한 Entity
     */
    String fileCd;
    String assetCd;
    String swType;


    public String getSwType() {
        return swType;
    }

    public void setSwType(String swType) {
        this.swType = swType;
    }

    public String getFileCd() {
        return fileCd;
    }

    public void setFileCd(String fileCd) {
        this.fileCd = fileCd;
    }

    public String getAssetCd() {
        return assetCd;
    }

    public void setAssetCd(String assetCd) {
        this.assetCd = assetCd;
    }
}
