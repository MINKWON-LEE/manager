package com.igloosec.smartguard.next.agentmanager.api.asset.model;

public interface IReq {

    String getAssetCd();
    void setAssetCd(String AssetCd);

    String getJobType();
    void setJobType(String jobType);

    String getManagerCd();
    void setManagerCd(String managerCd);

    String getManagerNm();
    void setManagerNm(String managerNm);

    String getPersonManagerCd();
    void setPersonManagerCd(String personManagerCd);

    String getPersonManagerNm();
    void setPersonManagerNm(String personManagerNm);

    String getSwNm();
    void setSwNm(String swNm);

    String getGetSeq();
    void setGetSeq(String getSeq);
}
