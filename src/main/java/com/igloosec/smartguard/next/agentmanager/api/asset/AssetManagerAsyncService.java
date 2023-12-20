package com.igloosec.smartguard.next.agentmanager.api.asset;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagInfoReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagnosisReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.IReq;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.services.AgentLogManager;
import com.igloosec.smartguard.next.agentmanager.services.AssetUpdateManager;
import com.igloosec.smartguard.next.agentmanager.services.DiagnosisManager;
import com.igloosec.smartguard.next.agentmanager.services.JobHandleManager;
import com.igloosec.smartguard.next.agentmanager.services.NotificationListener;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EnableAsync
@Slf4j
@Service
public class AssetManagerAsyncService {

    private Dao dao;
    private JobHandleManager jobHandleManager;
    private AssetUpdateManager assetUpdateManager;
    private NotificationListener notificationListener;
    private DiagnosisManager diagnosisManager;
    private AgentLogManager agentLogManager;
    private AssetManagerService assetManagerService;
    private OptionProperties optionProperties;

    public AssetManagerAsyncService(Dao dao, JobHandleManager jobHandleManager,
                                    AssetUpdateManager assetUpdateManager, NotificationListener notificationListener,
                                    DiagnosisManager diagnosisManager, AgentLogManager agentLogManager,
                                    AssetManagerService assetManagerService, OptionProperties optionProperties) {
        this.dao = dao;
        this.jobHandleManager = jobHandleManager;
        this.assetUpdateManager = assetUpdateManager;
        this.notificationListener = notificationListener;
        this.diagnosisManager = diagnosisManager;
        this.agentLogManager = agentLogManager;
        this.assetManagerService = assetManagerService;
        this.optionProperties = optionProperties;
    }

    @Async
    public ApiResult assetGet(JobEntity jobEntity) throws Exception {
        try {
            INMEMORYDB.createRunningGsJobList(jobEntity);
            log.debug("succeed in inserting item aeestCd({}) into RUNNINGGSJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
                    jobEntity.getAssetCd(), INMEMORYDB.RUNNINGGSJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

            // 현재 실행 중인 ssh 서비스 저장.
            INMEMORYDB.createSshMem(jobEntity);
            log.debug("succeed in inserting item aeestCd({}) into RUNNING_SSHJOBLIST size - {}",
                    jobEntity.getAssetCd(), INMEMORYDB.RUNNING_SSHJOBLIST.size());

            assetUpdateManager.doAssetUpdateBFA(jobEntity);

            notificationListener.arrangeGsJobList(jobEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            notificationListener.arrangeGsJobList(jobEntity);
            throw new SnetException(dao, ex.getMessage(), jobEntity, "G");
        }

        return ApiResult.builder().build();
    }

    @Async
    public CompletableFuture<ApiResult> createJobEntityAssetGetS(List<? extends IReq> getReqS, String managerCd) throws Exception {
        log.info("starting createJobEntityAssetDiagS. jobType - {}", getReqS.get(0).getJobType());

        return CompletableFuture.completedFuture(this.createJobEntityAssetGetSForAsync(getReqS, managerCd))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                } );
    }

    public ApiResult createJobEntityAssetGetSForAsync(List<? extends IReq> getReqS, String managerCd) throws Exception {

        String jobType = getReqS.get(0).getJobType();
        log.debug("start get list process. jobType - " + jobType);
        try {
            if (jobType.equals(ManagerJobType.AJ200.toString())) {
                jobHandleManager.initAgentInfoForBF(getReqS, managerCd, "N");
            } else if (jobType.equals(ManagerJobType.WM300.toString()) || jobType.equals(ManagerJobType.WM302.toString())) {
                jobHandleManager.initAgentInfoForBFNw(getReqS, managerCd);
            } else {
                throw new Exception("jobType is empty!.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return ApiResult.builder().build();
    }

    @Async
    public CompletableFuture<ApiResult> createJobEntityAssetDiagS(List<DiagnosisReq> diagReqS) throws Exception {
        log.info("starting createJobEntityAssetDiagS. jobType - {}", diagReqS.get(0).getJobType());

        return CompletableFuture.completedFuture(this.createJobEntityAssetDiagSForAsync(diagReqS))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                } );
    }

    public ApiResult createJobEntityAssetDiagSForAsync(List<DiagnosisReq> diagReqS) throws Exception {
        String jobType = diagReqS.get(0).getJobType();
        log.debug("start diagnosis list process. jobType - " + jobType);

        ApiResult apiResult = null;

        if (jobType.equals(ManagerJobType.AJ100.toString())) {
            apiResult = assetManagerService.createJobEntityAssetOrgDiagS(diagReqS);
        } else if (jobType.equals(ManagerJobType.AJ101.toString())) {
            apiResult = assetManagerService.createEvnetJobEntityAssetDiagS(diagReqS);
        }

        return apiResult;
    }

    /*
       스크립트실행 결과 파일 처리
     */
    @Async
    public CompletableFuture<ApiResult> handleResultFile(String assetCd, String jobType, String agentCd, String uploadPath, JobEntity jobEntity) throws Exception {

        log.info("assetCd({}) - starting handling this Result File. jobType - {}", assetCd, jobType);
        if (jobEntity == null) {
            String msg = "assetCd( "+ assetCd + ") - the job to be handled cannot exist. jobType - " + jobType;
            log.error(msg);
            throw new Exception(msg);
        }

        return CompletableFuture.completedFuture(this.handleResultFileForAsync(assetCd, jobType, uploadPath, jobEntity))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                } );
    }

    private ApiResult handleResultFileForAsync(String assetCd, String jobType, String uploadPath, JobEntity jobEntity) throws Exception {
        // 수집/진단 실행 완료 후 des 파일 삭제
        String desFile = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() + ".des";

        // 1.장비 정보 수집 결과 파일 처리
        if (jobType.equals(ManagerJobType.AJ200.toString()) ) {
            try {
                assetUpdateManager.recvGetscriptResult(jobEntity);
                notificationListener.arrangeGsJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
                if(optionProperties.getDiagnosis().equals("false")) {
                    CommonUtils.deleteFile(desFile);
                }
            } catch (Exception ex) {
                log.error(CommonUtils.printError(ex));
                notificationListener.arrangeGsJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
                if(optionProperties.getDiagnosis().equals("false")) {
                    CommonUtils.deleteFile(desFile);
                }
                if (jobEntity.isAgentGetprog()) {
                    throw new SnetException(dao, ex.getMessage(), jobEntity, "G");
                } else {
                    notificationListener.arrangeDgJobList(jobEntity);
                    throw new SnetException(dao, ex.getMessage(), jobEntity, "D");
                }
            }
        // 2. 에이전트 설치 전 장비 정보 수집 결과 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ201.toString())) {
            assetUpdateManager.doGSBFAGENTresult_v3(jobEntity);
        // 3. 진단 실행 결과 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ100.toString())) {
            try {
                if (jobEntity.getDiagInfoUse().toLowerCase().equals("y") && !INMEMORYDB.diagInfoNotUse){
                    diagnosisManager.recvSRVDGscriptResult(jobEntity);
                } else {
                    diagnosisManager.recvDGscriptResult(jobEntity);
                }
                notificationListener.arrangeDgJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
                if(optionProperties.getDiagnosis().equals("false")) {
                    INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.SEND);
                    CommonUtils.deleteFile(desFile);
                }
            } catch (Exception ex) {
                notificationListener.arrangeDgJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
                if(optionProperties.getDiagnosis().equals("false")) {
                    INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.SEND);
                    CommonUtils.deleteFile(desFile);
                }
                throw new SnetException(dao, ex.getMessage(), jobEntity, "D");
            }

        // 4. 긴급 진단 실행 결과 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ101.toString())) {
            try {
                diagnosisManager.recvEventDGscriptResult(jobEntity);
                notificationListener.arrangeDgJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
            } catch (Exception ex) {
                notificationListener.arrangeDgJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
                throw new SnetException(dao, ex.getMessage(), jobEntity, "D");
            }

        // 5. 에이전트 로그 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ300.toString())) {
            try {
                agentLogManager.handleAgentLogFile(jobEntity);
                notificationListener.arrangeLogJobList(jobEntity);
            } catch (Exception ex) {
                notificationListener.arrangeLogJobList(jobEntity);
                throw new Exception("failed to handle AgentLog file.");
            }

        // 6. 에이전트 진단 실패 후에도 로그만 업데이트
        // 로그파일 없어도 진단 실패 과정에 영향없음. 이후 에이전트에서 AN054 호출 가능.
        } else if (jobType.equals(ManagerJobType.AJ154.toString())) {
            try {
                diagnosisManager.recvDGLastLog(jobEntity);
            } catch (Exception ex) {
                throw new Exception("failed to handle Agent Diagnosis Log file.");
            }

        }

        return ApiResult.builder().build();
    }

    @Async
    public CompletableFuture<ApiResult> makeDownloadableDiagInfoForAsync(List<DiagInfoReq> daigInfoReqS) throws Exception {
        log.info("starting createJobEntityAssetDiagS. jobType - {}", daigInfoReqS.get(0).getJobType());

        return CompletableFuture.completedFuture(this.makeDownloadableDiagInfo(daigInfoReqS))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                } );
    }

    public ApiResult makeDownloadableDiagInfo(List<DiagInfoReq> daigInfoReqS) throws Exception {
        String jobType = daigInfoReqS.get(0).getJobType();
        log.debug("start to make diag info file downloadable. jobType - " + jobType);

        ApiResult apiResult = null;

        apiResult = assetManagerService.makeDownloadableDiagInfo(daigInfoReqS);

        return apiResult;
    }

}
