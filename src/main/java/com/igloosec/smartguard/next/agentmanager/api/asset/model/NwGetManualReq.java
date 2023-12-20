package com.igloosec.smartguard.next.agentmanager.api.asset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NwGetManualReq {

    @NotEmpty(message = "assetCd is required.")
    private String assetCd;

    @NotEmpty(message = "jobType is required.")
    private String jobType;         // WM200 : srvManualGscrptFin, WM201 : multiSrvManualGscrptFin

    private String personManagerCd;

    private String personManagerNm;

    @NotEmpty(message = "swNm is required.")
    private String swNm;         // vendorCd

    @NotEmpty(message = "fileNm is required.")
    private String fileNm;       // configfileName

    @NotEmpty(message = "message is required.")
    private String message;      // auditType "REGI" 일 때 수집

    private String swInfo;       // single 일 때 사용

    private String nwUrlCd;      // zip 일 때 사용

    private String getSeq;

    private String managerNm;

    private String managerCd;

    public NwDgManualReq toNwDgManualReq() {
        
        NwDgManualReq nwDgManualReq = new NwDgManualReq();
        nwDgManualReq.setAssetCd(this.assetCd);
        nwDgManualReq.setJobType(this.jobType);
        nwDgManualReq.setPersonManagerCd(this.personManagerCd);
        nwDgManualReq.setPersonManagerNm(this.personManagerNm);
        nwDgManualReq.setSwNm(this.swNm);
        nwDgManualReq.setFileNm(this.fileNm);
        nwDgManualReq.setMessage(this.message);
        nwDgManualReq.setSwInfo(this.swInfo);
        nwDgManualReq.setNwUrlCd(this.nwUrlCd);
        nwDgManualReq.setGetSeq(this.getSeq);
        nwDgManualReq.setManagerCd(this.managerCd);
        nwDgManualReq.setManagerNm(this.managerNm);


        return nwDgManualReq;
    }
}
