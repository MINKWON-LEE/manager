package com.igloosec.smartguard.next.agentmanager.api.util.jobs;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.*;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import lombok.Data;

import java.util.Random;

@Data
public class JobEntityInitData {


    private JobEntity jobEntity;
    private String assetCd;
    private String jobType;   // WM100 : manualGscrptFin (dat 파일업로드), WM101 : multiManualGscrptFin (zip 파일 업로드)
    private String fileNm;
    private String managerCd; // single 파일일떄만 사용.
    private String swUrlCd;   // zip파일일 때만 사용.

    // MANUALGSCRPTFIN, MULTIMANUALGSCRPTFIN
    public JobEntityInitData(GetManualReq gmr) {
        jobEntity = new JobEntity();

        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAssetCd(gmr.getAssetCd());
        jobEntity.setAgentInfo(agentInfo);

        jobEntity.setAssetCd(gmr.getAssetCd());
        jobEntity.setJobType(ManagerJobType.WM100.toString());
        jobEntity.setFileName(gmr.getFileNm());
        jobEntity.setManagerCode(gmr.getManagerCd());
        jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);
        jobEntity.setAgentJobSDate(DateUtil.getCurrDateBySecond());

        int otp = new Random().nextInt(900000)+100000;
        String S_OTP = String.valueOf(otp);
        jobEntity.setCOTP(S_OTP);

//        jobEntity.setSwType("NW");
//        jobEntity.setSwNm(parsedMsg[3]);
//        jobEntity.setSwInfo(parsedMsg[6]);
//        jobEntity.setSwDir("-");
//        jobEntity.setSwUser("-");
//        jobEntity.setSwEtc("-");
    }

    // MANUALDGFIN, MULTIMANUALDGFIN
    public JobEntityInitData(DiagnosisManualReq dmr) {
        jobEntity = new JobEntity();

        jobEntity.setJobType(dmr.getJobType());
        jobEntity.setFileName(dmr.getFileNm());
        jobEntity.setFileType(INMEMORYDB.DGRESULTTYPE);

        if (dmr.getAssetCd() != null && !dmr.getAssetCd().isEmpty()) {
            jobEntity.setAssetCd(dmr.getAssetCd());
            jobEntity.setSwType(dmr.getSwType());
            jobEntity.setSwNm(dmr.getSwNm());
            jobEntity.setSwInfo(dmr.getSwInfo());
            jobEntity.setSwDir(dmr.getSwDir());
            jobEntity.setSwUser(dmr.getSwUser());
            jobEntity.setSwEtc(dmr.getSwEtc());
            jobEntity.setIpAddres(dmr.getIpAddress());
            jobEntity.setHostNm(dmr.getHostNm());
        }
    }

    /**
     * SRVMANUALGSCRPTFIN, MULTISRVMANUALGSCRPTFIN
     * message(AuditType)이 REGI
     * NwGetManualReq의 filenm(configfileName), message(AuditType), nwUrlCd 은 여기서 처리 안함.
     */

    public JobEntityInitData(NwGetManualReq ngmr) {
        jobEntity = new JobEntity();

        AgentInfo ai = new AgentInfo();
        ai.setAssetCd(ngmr.getAssetCd());
        jobEntity.setAgentInfo(ai);

        jobEntity.setJobType(ManagerJobType.WM100.toString());
        jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);
        jobEntity.setAssetCd(ngmr.getAssetCd());
        jobEntity.setManagerCode(ngmr.getPersonManagerCd());
        jobEntity.setSwNm(ngmr.getSwNm());  // vendorCd
        jobEntity.setSwInfo(ngmr.getSwInfo());

        int otp = new Random().nextInt(900000)+100000;
        String S_OTP = String.valueOf(otp);
        jobEntity.setCOTP(S_OTP);
        jobEntity.setSwType("NW");
        jobEntity.setSwDir("-");
        jobEntity.setSwUser("-");
        jobEntity.setSwEtc("-");
    }

    /**
     * SRVMANUALGSCRPTFIN, MULTISRVMANUALGSCRPTFIN
     * message(AuditType)이 AUDIT
     * NwGetManualReq의 filenm(configfileName)과 message(AuditType)은 여기서 처리 안함.
     */
    public JobEntityInitData(NwDgManualReq ndmr) {
        jobEntity = new JobEntity();

        AgentInfo ai = new AgentInfo();
        ai.setAssetCd(ndmr.getAssetCd());
        jobEntity.setAgentInfo(ai);

        jobEntity.setJobType(ManagerJobType.WM202.toString());
        jobEntity.setFileName(ndmr.getFileNm());  // configfileName
        jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);
        jobEntity.setAssetCd(ndmr.getAssetCd());
        jobEntity.setManagerCode(ndmr.getPersonManagerCd());
        jobEntity.setSwNm(ndmr.getSwNm());  // vendorCd
        jobEntity.setSwInfo(ndmr.getSwInfo());

        int otp = new Random().nextInt(900000)+100000;
        String S_OTP = String.valueOf(otp);
        jobEntity.setCOTP(S_OTP);
        jobEntity.setSwType("NW");
        jobEntity.setSwDir("-");
        jobEntity.setSwUser("-");
        jobEntity.setSwEtc("-");
    }

    /**
     * WM300 (NETWORKEXEREQ), WM302 (NETWORKDGEXECREQ)
     */
    public JobEntityInitData(IReq iReq) {
        jobEntity = new JobEntity();

        AgentInfo ai = new AgentInfo();
        ai.setAssetCd(iReq.getAssetCd());
        jobEntity.setAgentInfo(ai);

        jobEntity.setJobType(iReq.getJobType());
        jobEntity.setAssetCd(iReq.getAssetCd());
        int otp = new Random().nextInt(900000)+100000;
        String S_OTP = String.valueOf(otp);

        jobEntity.setFileName(iReq.getAssetCd() +"_"+S_OTP);
        jobEntity.setCOTP(S_OTP);
        jobEntity.setFileType(INMEMORYDB.CFG);

        jobEntity.setSwNm(iReq.getSwNm());
        jobEntity.setManagerCode(iReq.getPersonManagerCd());

        if (iReq.getJobType().equals(ManagerJobType.WM302.toString())) {
            NwDgReq dgReq = (NwDgReq) iReq;
            jobEntity.setManagerCode(dgReq.getManagerCd());
            jobEntity.setSwType(dgReq.getSwType());
            jobEntity.setSwInfo(dgReq.getSwInfo());
            jobEntity.setSwDir(dgReq.getSwDir());
            jobEntity.setSwUser(dgReq.getSwUser());
            jobEntity.setSwEtc(dgReq.getSwEtc());
            jobEntity.setAgentJobSDate(DateUtil.getCurrDateBySecond());
        }
    }

    /**
     * AJ201 (GSBFAGENT)
     */
    public JobEntityInitData(ResultFile resFile) {
        jobEntity = new JobEntity();

        AgentInfo ai = new AgentInfo();
        ai.setAssetCd(resFile.getAssetCd());

        jobEntity = new JobEntity();
        jobEntity.setAgentInfo(ai);
        jobEntity.setJobType(ManagerJobType.AJ201.toString());
        jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);

        int otp = new Random().nextInt(900000)+100000;
        String S_OTP = String.valueOf(otp);
        jobEntity.setCOTP(S_OTP);

        jobEntity.setAgentManualSetup(true);
    }

    /**
     * AJ202 (GSDIAGINDO)
     */
    public JobEntityInitData(DiagInfoReq diReq) {
        jobEntity = new JobEntity();

        jobEntity.setJobType(diReq.getJobType());
        jobEntity.setAssetCd(diReq.getAssetCd());
        jobEntity.setSwType(diReq.getSwType());
        jobEntity.setSwNm(diReq.getSwNm());
        jobEntity.setSwInfo(diReq.getSwInfo());
        jobEntity.setSwDir(diReq.getSwDir());
        jobEntity.setSwUser(diReq.getSwUser());
        jobEntity.setSwEtc(diReq.getSwEtc());
    }
}
