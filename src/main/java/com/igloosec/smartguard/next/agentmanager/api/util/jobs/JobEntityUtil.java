package com.igloosec.smartguard.next.agentmanager.api.util.jobs;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagnosisReq;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;

public class JobEntityUtil {
    public JobEntity getDiagnosisJobEntity(DiagnosisReq req) {

        JobEntity jobEntity = new JobEntity();

        jobEntity.setJobType(req.getJobType());
        jobEntity.setAssetCd(req.getAssetCd());
        jobEntity.setSwType(req.getSwType());
        jobEntity.setSwNm(req.getSwNm());
        jobEntity.setSwInfo(req.getSwInfo());
        jobEntity.setSwDir(req.getSwDir());
        jobEntity.setSwUser(req.getSwUser());
        jobEntity.setSwEtc(req.getSwEtc());
        jobEntity.setAgentJobSDate(DateUtil.getCurrDateBySecond());

        return jobEntity;
    }
}
