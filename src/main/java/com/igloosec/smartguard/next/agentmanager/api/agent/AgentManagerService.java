package com.igloosec.smartguard.next.agentmanager.api.agent;

import com.igloosec.smartguard.next.agentmanager.api.agent.model.ControlAgentReq;
import com.igloosec.smartguard.next.agentmanager.api.agent.model.LogCollection;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiJobResult;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.*;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.memory.NotiType;
import com.igloosec.smartguard.next.agentmanager.property.AgentContextProperties;
import com.igloosec.smartguard.next.agentmanager.services.*;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;

import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class AgentManagerService {

    private Dao dao;
    private JobHandleManager jobHandleManager;
    private AgentVersionManager agentVersionManager;
    private HealthChkManager healthChkManager;
    private AgentContextProperties agentContextProperties;
    private AgentLogManager agentLogManager;
    private NotificationListener notificationlistener;
    private AgentJobScheduleManager agentJobScheduleManager;
    private AgentResourceManager agentResourceManager;

    public AgentManagerService(Dao dao, JobHandleManager jobHandleManager,
                               AgentVersionManager agentVersionManager, HealthChkManager healthChkManager,
                               AgentContextProperties agentContextProperties, AgentLogManager agentLogManager,
                               NotificationListener notificationlistener, AgentJobScheduleManager agentJobScheduleManager,
                               AgentResourceManager agentResourceManager) {
        this.dao = dao;
        this.jobHandleManager = jobHandleManager;
        this.agentVersionManager = agentVersionManager;
        this.healthChkManager = healthChkManager;
        this.agentContextProperties = agentContextProperties;
        this.agentLogManager = agentLogManager;
        this.notificationlistener = notificationlistener;
        this.agentJobScheduleManager = agentJobScheduleManager;
        this.agentResourceManager = agentResourceManager;
    }

    public ApiResult getAuthInfo(String ip, String hostName, String os, String assetCd) throws Exception {

        AgentInfo ai;
        if (StringUtil.isEmpty(assetCd)) {
            // ai = jobHandleManager.initAgentInfoAuth(ip, hostName, os);
            throw new Exception("assetCd does not exist.");
        } else {
            ai = jobHandleManager.initAgentInfoAuth(assetCd);
        }

        if (StringUtils.isEmpty(ai.getAgentCd())) {
            throw new Exception("agentCd does not exist.");
        }

        return ApiResult.builder()
                .timestamp(DateUtil.getCurrDateBySecondFmt2())
                .assetCd(ai.getAssetCd())
                .agentCd(ai.getAgentCd())
                .ipAddress(ai.getConnectIpAddress())
                .build();
    }

    public ApiResult<ApiJobResult> getJobsAgent(String version, String assetCd, String agentCd,
                                                String cpuUseRate, String memTotal, String memFree,
                                                String memUse, String memUseRate) throws Exception {

        String msg = "";

        // UI에서 삭제된 에이전트 처리 - 에이전트 중지
        if (StringUtils.isEmpty(agentCd)) {
            msg = "agentCd is empty. please check auth API. assetCd - " + assetCd;
            return returnDefaultJobApiResult(assetCd, agentCd, msg);
        }

        int agentExist = dao.selectAgentMasterCd(agentCd);
        if (agentExist == 0) {

            msg = "do not get this agentCd from snet." + agentCd;
            return ApiResult.<ApiJobResult>builder()
                    .message(msg)
                    .timestamp(DateUtil.getCurrDateBySecondFmt2())
                    .delaytime(Integer.toString(INMEMORYDB.EmptyJobSchedule))
                    .assetCd(assetCd)
                    .agentCd(agentCd)
                    .build();
        }

        if( INMEMORYDB.agentResourceUse ) {
            agentResourceManager.insertAgentResource(assetCd, cpuUseRate, memTotal, memFree, memUse, memUseRate);
        }

        healthChkManager.updateAgentStatus_v3(assetCd, agentCd,version, 1);
        boolean isUpdate = agentVersionManager.chkOldVersion(version);
        // 에이전트가 하위 버전이면 에이전트 업데이트 부터 실행.
        if (isUpdate) {
            String useDiagSudo = dao.selectUseDiagSudo(assetCd);
            if (StringUtils.isEmpty(useDiagSudo)) {
                useDiagSudo = "N";
            }
            ApiJobResult apiJobResult = ApiJobResult.builder().
                    version(INMEMORYDB.latestAgentVersion).
                    fileNm(agentContextProperties.getAgentPatchFiles() + INMEMORYDB.ZIP).
                    useDiagSudo(useDiagSudo).build();

            return ApiResult.<ApiJobResult>builder()
                    .resultData(apiJobResult)
                    .timestamp(DateUtil.getCurrDateBySecondFmt2())
                    .delaytime(agentJobScheduleManager.getDelayTime(assetCd, agentCd))   //to do 에이전트별 delay 시간 조정 필요
                    .assetCd(assetCd)
                    .agentCd(agentCd)
                    .jobType(ManagerJobType.AJ603.toString())
                    .build();
        }

        boolean checkPolling = false;
        AgentJob agentJob = null;
        CopyOnWriteArrayList<AgentJob> curJobList = INMEMORYDB.JOBLISTPERASSET.get(assetCd);
        if ((curJobList != null) && (curJobList.size() > 0 )) {
            for (AgentJob ai : curJobList) {
                if (ai.getCheckFlag().equals("0")) {
                    agentJob = ai;
                    agentJob.setDelayTime(agentJobScheduleManager.getDelayTime(assetCd, agentCd));
                    break;
                } else {
                    checkPolling = true;
                }
            }
        }

        if ( agentJob == null ) {
            if (checkPolling) {
                msg = "agent already got the job. assetCd - " + assetCd;
                return returnDefaultJobApiResult(assetCd, agentCd, msg);
            } else {
                msg = "job is empty. assetCd - " + assetCd;
                return returnDefaultJobApiResult(assetCd, agentCd, msg);
            }
        }

        ApiResult<ApiJobResult> apiResult = null;
        String jobType = agentJob.getJobType();

            // 1. 진단 실행 요청
        if(jobType.equals(ManagerJobType.AJ100.toString())) {

            apiResult = getJobsAssetDiagnosis(agentJob);

            // 2. 에이전트 상태관리 요청 (중지/ 재시작 / 업데이트)
        } else if (jobType.equals(ManagerJobType.AJ601.toString())
                || jobType.equals(ManagerJobType.AJ602.toString())
                || jobType.equals(ManagerJobType.AJ603.toString())) {

            apiResult = getJobsAgentControl(agentJob);

            // 3. 장비 정보 수집 요청
        } else if (jobType.equals(ManagerJobType.AJ200.toString())) {

            apiResult = getJobsAssetGet(agentJob);

            // 4. 로그 파일 수집 요청
        } else if (jobType.equals(ManagerJobType.AJ300.toString())) {

            apiResult = getJobsAgentLog(agentJob);

            // 5. 긴급 진단 실행 요청
        } else if (jobType.equals(ManagerJobType.AJ101.toString())) {

            apiResult = getJobsAssetEventDiagnosis(agentJob);

        } else {
            msg = "jobType is required. please check the jopType.";
            log.error(msg);
            apiResult = ApiResult.<ApiJobResult>builder().result(400).message(msg).build();
        }

        if (apiResult.getResult() == 200) {
            // AgentJob 의 checkCnt가 1000이면 agent에서 job을 계속 가져갈 수 있다.
            if( agentJob.getCheckCnt() != 1000) {
                // 에이전트가 5회 Job을 가져갔으면 더 이상 못 가져간다.
                if (agentJob.getCheckCnt() <= 1) {
                    agentJob.setCheckFlag("1");
                } else {
                    int cnt = agentJob.getCheckCnt();
                    cnt -= 1;
                    agentJob.setCheckCnt(cnt);
                }
            }
        }

        return apiResult;
    }

    public ApiResult<ApiJobResult> returnDefaultJobApiResult(String assetCd, String agentCd, String msg) {

        return ApiResult.<ApiJobResult>builder()
                .message(msg)
                .timestamp(DateUtil.getCurrDateBySecondFmt2())
                .delaytime(agentJobScheduleManager.getDelayTime(assetCd, agentCd))
                .assetCd(assetCd)
                .agentCd(agentCd)
                .build();
    }

    public ApiResult<ApiJobResult> getJobsAssetGet(AgentJob agentJob) {

        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(agentJob.getAssetCd());
        if (runnigJobEntity != null) {
            JobEntity jobEntity = runnigJobEntity.getJobEntity();
            try {
                String auditFileNm = jobEntity.getAuditFileName();

                // 에이전트가 job을 가져갔을 때로 snet_connect_master의 connect_log를 AN003으로 업데이트
                String msg = NotiType.getStatusLog(ManagerJobType.AJ200.toString(), NotiType.AN003.toString(), "");
                jobEntity.getAgentInfo().setConnectLog(msg + " [" + DateUtil.getCurrDateBySecondFmt() + "]");
                dao.updateConnectMaster(jobEntity);
                // to do 장비 정보 업데이트 이력
                jobEntity.setAgentJobFlag(2);
                jobEntity.setAgentJobDesc(msg);
                dao.updateAgentGetJobHistory(jobEntity);

                ApiJobResult apiJobResult= ApiJobResult.builder()
                        .fileNm(jobEntity.getFileName())
                        .auditFileNm(auditFileNm)
                        .useDiagSudo(jobEntity.getAgentInfo().getUseDiagSudo())
                        .auditFileCd(jobEntity.getAuditFileCd())
                        .agentCpuMax(INMEMORYDB.agentCPUMax)
                        .agentMemMax(INMEMORYDB.agentMemoryMax)
                        .dgWaitTime(INMEMORYDB.dgWaitTime)
                        .build();

                return ApiResult.<ApiJobResult>builder().resultData(apiJobResult)
                        .timestamp(DateUtil.getCurrDateBySecondFmt2())
                        .delaytime(agentJob.getDelayTime())
                        .assetCd(jobEntity.getAssetCd())
                        .agentCd(jobEntity.getAgentInfo().getAgentCd())
                        .managerCd(jobEntity.getManagerCode())
                        .jobType(jobEntity.getJobType()).build();

            } catch (Exception ex) {
                log.error(ex.getMessage());
                return ApiResult.<ApiJobResult>builder().result(500).message("Failed.").build();
            }
        } else {
            return ApiResult.<ApiJobResult>builder().result(404).message("not found.").build();
        }
    }

    public ApiResult<ApiJobResult> getJobsAssetDiagnosis(AgentJob agentJob) {

        // SG2019-850
        // SKT의 경우 한개라도 진단 실행 중인 장비가 있다면 진단 실행 업무를 전달하지 않는다.
        // INMEMORYDB.maxDGexe이 1로 셋팅되어 있음.
        if ((INMEMORYDB.maxDGexec > 0)
                && (INMEMORYDB.RUNNINGAGENTDGJOBLIST.size() == INMEMORYDB.maxDGexec)
                &&(!INMEMORYDB.RUNNINGAGENTDGJOBLIST.containsKey(agentJob.getAssetCd())))  {
            String msg = "job is empty. assetCd - " + agentJob.getAssetCd();
            return ApiResult.<ApiJobResult>builder().message(msg).build();
        }

        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(agentJob.getAssetCd());
        if (runnigJobEntity != null) {
            JobEntity jobEntity = runnigJobEntity.getJobEntity();

            try {
                // 긴급 진단인지 확인.
                if (jobEntity.getEventFlag().equals("Y")) {
                    return getJobsAssetEventDiagnosis(agentJob);
                }

                // 진단 요청이 여러개 있을 때 request time 을 짧게 가져간다.
                if (INMEMORYDB.fastDelayUse) {
                    if (dao.getAssetDiagnosisJobCount(agentJob.getAssetCd()) > 1) {
                        agentJob.setDelayTime(Integer.toString(INMEMORYDB.fastDelayTime));
                    }
                }

                // 수집 / 진단 분리시 현재 서버에서 진단 실행 중이라면
                // job을 empty로 전달.. 서버 진단이 완료 된 뒤에 다음 진단 요청을 에이전트에 전달한다.
                if (jobEntity.isDiagInfoHandling()) {
                    String msg = "processing diagnosis from server currently, thus agent`s job is forced empty.";
                    log.debug(msg);
                    return ApiResult.<ApiJobResult>builder()
                            .message(msg)
                            .timestamp(DateUtil.getCurrDateBySecondFmt2())
                            .delaytime(agentJob.getDelayTime())
                            .assetCd(jobEntity.getAssetCd())
                            .agentCd(jobEntity.getAgentInfo().getAgentCd())
                            .build();
                }

                // 에이전트가 job을 가져갔을 때로 agentJobFlag를 2로 변경.
                jobEntity.setAgentJobFlag(2);
                dao.updateAgentJobHistory(jobEntity);

                if (INMEMORYDB.maxDGexec > 0) {
                    INMEMORYDB.RUNNINGAGENTDGJOBLIST.put(jobEntity.getAssetCd(), DateUtil.getCurrDateBySecond());
                }

                String tempTiagInfoNotUse = "N";
                if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse) {
                    tempTiagInfoNotUse = "Y";
                }

                ApiJobResult apiJobResult = ApiJobResult.builder()
                        .fileNm(jobEntity.getFileName())
                        .auditFileNm(jobEntity.getAuditFileName())
                        .useDiagSudo(jobEntity.getAgentInfo().getUseDiagSudo())
                        .auditFileCd(jobEntity.getAuditFileCd())
                        .agentCpuMax(INMEMORYDB.agentCPUMax)
                        .agentMemMax(INMEMORYDB.agentMemoryMax)
                        .swType(jobEntity.getSwType())
                        .dgWaitTime(INMEMORYDB.dgWaitTime)
                        .diagInfoUse(tempTiagInfoNotUse)
                        .build();

                return ApiResult.<ApiJobResult>builder().resultData(apiJobResult)
                        .timestamp(DateUtil.getCurrDateBySecondFmt2())
                        .delaytime(agentJob.getDelayTime())
                        .assetCd(jobEntity.getAssetCd())
                        .agentCd(jobEntity.getAgentInfo().getAgentCd())
                        .managerCd(jobEntity.getManagerCode())
                        .jobType(jobEntity.getJobType()).build();

            } catch (Exception ex) {
                log.error(ex.getMessage());
                return ApiResult.<ApiJobResult>builder().result(500).message("Failed.").build();
            }
        }
        return ApiResult.<ApiJobResult>builder().result(404).message("not found.").build();
    }

    public ApiResult<ApiJobResult> getJobsAssetEventDiagnosis(AgentJob agentJob) {
        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(agentJob.getAssetCd());
        if (runnigJobEntity != null) {
            JobEntity jobEntity = runnigJobEntity.getJobEntity();

            try {
                jobEntity.setAgentJobFlag(1);
                dao.updateEventAgentJobHistory(jobEntity);

                ApiJobResult apiJobResult = ApiJobResult.builder()
                        .fileNm(jobEntity.getFileName())
                        .auditFileNm(jobEntity.getAuditFileName())
                        .useDiagSudo(jobEntity.getAgentInfo().getUseDiagSudo())
                        .auditFileCd(jobEntity.getAuditFileCd())
                        .agentCpuMax(INMEMORYDB.agentCPUMax)
                        .agentMemMax(INMEMORYDB.agentMemoryMax)
                        .swType(jobEntity.getSwType())
                        .dgWaitTime(INMEMORYDB.dgWaitTime)
                        .build();

                return ApiResult.<ApiJobResult>builder().resultData(apiJobResult)
                        .timestamp(DateUtil.getCurrDateBySecondFmt2())
                        .delaytime(agentJob.getDelayTime())
                        .assetCd(jobEntity.getAssetCd())
                        .agentCd(jobEntity.getAgentInfo().getAgentCd())
                        .managerCd(jobEntity.getManagerCode())
                        .jobType(jobEntity.getJobType()).build();

            } catch (Exception ex) {
            log.error(ex.getMessage());
            return ApiResult.<ApiJobResult>builder().result(500).message("Failed.").build();
            }
        }
        return ApiResult.<ApiJobResult>builder().result(404).message("not found.").build();
    }

    public ApiResult<ApiJobResult> getJobsAgentControl(AgentJob agentJob) {

        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGCONTROLJOBLIST.get(agentJob.getAssetCd());
        if (runnigJobEntity != null) {
            JobEntity jobEntity = runnigJobEntity.getJobEntity();

            try {

                if (jobEntity.getJobType().equals(ManagerJobType.AJ603.toString())) {
                    log.debug("forced update agent.");
                    String useDiagSudo = dao.selectUseDiagSudo(jobEntity.getAssetCd());
                    if (StringUtils.isEmpty(useDiagSudo)) {
                        useDiagSudo = "N";
                    }

                    ApiJobResult apiJobResult = ApiJobResult.builder().
                            version(INMEMORYDB.latestAgentVersion).
                            fileNm(agentContextProperties.getAgentPatchFiles() + INMEMORYDB.ZIP).
                            useDiagSudo(useDiagSudo).build();

                    return ApiResult.<ApiJobResult>builder()
                            .resultData(apiJobResult)
                            .timestamp(DateUtil.getCurrDateBySecondFmt2())
                            .delaytime(agentJobScheduleManager.getDelayTime(jobEntity.getAssetCd(), jobEntity.getAgentCd()))   //to do 에이전트별 delay 시간 조정 필요
                            .assetCd(jobEntity.getAssetCd())
                            .agentCd(jobEntity.getAgentCd())
                            .jobType(ManagerJobType.AJ603.toString())
                            .build();

                } else {
                    notificationlistener.arrangeControlJobList(jobEntity);
                }

                return ApiResult.<ApiJobResult>builder()
                        .timestamp(DateUtil.getCurrDateBySecondFmt2())
                        .delaytime(agentJob.getDelayTime())
                        .assetCd(jobEntity.getAssetCd())
                        .agentCd(jobEntity.getAgentInfo().getAgentCd())
                        .jobType(jobEntity.getJobType()).build();

            } catch (Exception ex) {
                log.error(ex.getMessage());
                return ApiResult.<ApiJobResult>builder().result(500).message("Failed.").build();
            }
        }
        return ApiResult.<ApiJobResult>builder().result(404).message("not found.").build();
    }

    public ApiResult<ApiJobResult> getJobsAgentLog(AgentJob agentJob) {

        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGLOGJOBLIST.get(agentJob.getAssetCd());
        if (runnigJobEntity != null) {
            JobEntity jobEntity = runnigJobEntity.getJobEntity();

            ApiJobResult apiJobResult = ApiJobResult.builder()
                    .fileNm(jobEntity.getFileName()).build();

            return ApiResult.<ApiJobResult>builder().resultData(apiJobResult)
                    .timestamp(DateUtil.getCurrDateBySecondFmt2())
                    .delaytime(agentJob.getDelayTime())
                    .assetCd(jobEntity.getAssetCd())
                    .agentCd(jobEntity.getAgentInfo().getAgentCd())
                    .jobType(jobEntity.getJobType()).build();
        }

        return ApiResult.<ApiJobResult>builder().result(404).message("not found.").build();
    }

    public ApiResult createJobEntityLogCollectionS(List<LogCollection> logCollectionS) throws Exception {

        String jobType = logCollectionS.get(0).getJobType();
        if (jobType.equals(ManagerJobType.AJ300.toString())) {
            agentLogManager.createJobEntities(logCollectionS);
        } else {
            throw new Exception("please check jobType again.");
        }

        return ApiResult.builder().build();
    }

    public ApiResult controlAgent(List<ControlAgentReq> controlAgentReqS) throws Exception {

        try {
            jobHandleManager.initAgentInfoMulti(controlAgentReqS);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }

        return ApiResult.builder().build();
    }
}
