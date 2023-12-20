package com.igloosec.smartguard.next.agentmanager.api.asset;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.*;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.util.jobs.JobEntityInitData;
import com.igloosec.smartguard.next.agentmanager.api.util.jobs.JobEntityUtil;
import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.*;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.services.*;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import com.igloosec.smartguard.next.agentmanager.utils.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class AssetManagerService {

    private Dao dao;
    private ConfigGlobalService configGlobalService;
    private OptionProperties optionProperties;
    private JobHandleManager jobHandleManager;
    private AgentVersionManager agentVersionManager;
    private AssetUpdateManager assetUpdateManager;
    private NotificationListener notificationListener;
    private DiagnosisManager diagnosisManager;
    private AgentLogManager agentLogManager;
    private HandledProcessManager handledProcessManager;

    public AssetManagerService(Dao dao, ConfigGlobalService configGlobalService,
                               OptionProperties optionProperties, JobHandleManager jobHandleManager,
                               AgentVersionManager agentVersionManager, AssetUpdateManager assetUpdateManager,
                               NotificationListener notificationListener, DiagnosisManager diagnosisManager,
                               AgentLogManager agentLogManager, HandledProcessManager handledProcessManager) {

        this.dao = dao;
        this.configGlobalService = configGlobalService;
        this.optionProperties = optionProperties;
        this.jobHandleManager = jobHandleManager;
        this.agentVersionManager = agentVersionManager;
        this.assetUpdateManager = assetUpdateManager;
        this.notificationListener = notificationListener;
        this.diagnosisManager = diagnosisManager;
        this.agentLogManager = agentLogManager;
        this.handledProcessManager = handledProcessManager;
    }

    /**
     * 파일 다운로드
     */
    public Resource downloadAuditFile(String assetCd, String agentCd, String jobType, String auditFileCd) throws Exception {
        Resource resource = null;
        JobEntity jobEntity = null;

        try {
            // 1. 장비 정보 수집
            if (jobType.equals(ManagerJobType.AJ200.toString()) ) {
                RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(assetCd);
                if (runnigJobEntity != null) {
                    jobEntity = runnigJobEntity.getJobEntity();
                    String dnFile = assetUpdateManager.doAssetUpdate_V3(jobEntity);
                    File file = new File(dnFile);
                    resource = new UrlResource(file.toURI());
                }
            // 2. 진단 실행 또는 긴급 진단 실행.
            } else if (jobType.equals(ManagerJobType.AJ100.toString()) || jobType.equals(ManagerJobType.AJ101.toString())) {
                RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(assetCd);
                if (runnigJobEntity != null) {
                    jobEntity = runnigJobEntity.getJobEntity();
                    String dnFile = diagnosisManager.doDiagnosisService_V3(jobEntity);
                    if (dnFile != null && !dnFile.isEmpty()) {
                        File file = new File(dnFile);
                        resource = new UrlResource(file.toURI());
                    }
                }
                // 3. 에이전트 최신 파일 다운로드
            } else if (jobType.equals(ManagerJobType.AJ603.toString())) {
                RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGCONTROLJOBLIST.get(assetCd);
                if (runnigJobEntity != null) {
                    jobEntity = runnigJobEntity.getJobEntity();
                    if (jobEntity.getJobType().equals(ManagerJobType.AJ603.toString())) {
                        File patchFiles = agentVersionManager.doForcedUpdateAgent_v3(jobEntity.getAgentInfo());
                        if (patchFiles != null) {
                            resource = new UrlResource(patchFiles.toURI());
                        }
                        notificationListener.arrangeControlJobList(jobEntity);
                    }
                } else {
                    AgentInfo ai = jobHandleManager.initAgentInfoUpdate(assetCd);

                    if (ai != null) {
                        if (!ai.getAgentVersion().equals(INMEMORYDB.latestAgentVersion)) {
                            File patchFiles = agentVersionManager.doUpateAgent_v3(ai);
                            if (patchFiles != null) {
                                resource = new UrlResource(patchFiles.toURI());
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.debug(ex.getMessage());

            if (jobType.equals(ManagerJobType.AJ200.toString()) ) {
                if (jobEntity == null) {
                    jobEntity = new JobEntity();
                }
                jobEntity.setAssetCd(assetCd);
                AgentInfo ai = new AgentInfo();
                ai.setAssetCd(assetCd);
                jobEntity.setAgentInfo(ai);
                if (INMEMORYDB.useLog4JChecker) {
                    INMEMORYDB.deleteLog4JFiles(jobEntity.getSOTP());
                }
                notificationListener.arrangeJobList(jobEntity);
                throw new SnetException(dao, ex.getMessage(), jobEntity, "G");
            } else if (jobType.equals(ManagerJobType.AJ100.toString()) ) {
                if( jobEntity != null ) {
                    notificationListener.arrangeJobList(jobEntity);
                    throw new SnetException(dao, ex.getMessage(), jobEntity, "D");
                } else {
                    throw new Exception("please check the assetCd again.");
                }
            } else if (jobType.equals(ManagerJobType.AJ101.toString()) ) {
                if( jobEntity != null ) {
                    notificationListener.arrangeJobList(jobEntity);
                    throw new SnetException(dao, ex.getMessage(), jobEntity, "E");
                } else {
                    throw new Exception("please check the assetCd again.");
                }
            }
        }

        if ((resource == null) || !resource.exists()) {
            checkOldJobRequest(jobType, assetCd, agentCd);

            log.error("file not found");
            return null;
        }

        return resource;
    }

    /**
     * 파일 업로드
     */
    public JobEntity uploadResultFile(ResultFile getFile, String uploadPath) throws Exception {

        String errMsg = "";
        JobEntity jobEntity = null;
        RunnigJobEntity runnigJobEntity = null;
        String jobType = getFile.getJobType();
        String assetCd = getFile.getAssetCd();
        String agentCd = getFile.getAgentCd();
        List<MultipartFile> files = getFile.getFiles();

        log.info("assetCd({}) - starting upload Result File. jobType - {}", assetCd, jobType);

        // 1. 장비 정보 수집
        if (jobType.equals(ManagerJobType.AJ200.toString()) ) {
            runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(assetCd);
            if (runnigJobEntity != null) {
                jobEntity = runnigJobEntity.getJobEntity();
            }

        // 2. 에이전트 설치 전 장비정보 수집.
        } if (jobType.equals(ManagerJobType.AJ201.toString()) ) {
            if (INMEMORYDB.RUNNINGDGJOBLIST.get(assetCd) != null) {
                errMsg = "["+assetCd+"] Diagnosis Running...";
            } else {
               if (INMEMORYDB.RUNNINGGSJOBLIST.containsKey(assetCd)) {
                   errMsg = "["+assetCd+"] is already Running for Gs...";
               } else {
                   log.info("Unrequested get script execute result. : {} - {} - {}", getFile.getAssetCd(), getFile.getJobType(), getFile.getFiles().size());
                   JobEntityInitData jobEntityInitData = new JobEntityInitData(getFile);
                   jobEntity = jobEntityInitData.getJobEntity();
               }
            }

        // 3. 진단 실행 or 긴급 진단 실행.
        } else if (jobType.equals(ManagerJobType.AJ100.toString())
                || jobType.equals(ManagerJobType.AJ101.toString())
                || jobType.equals(ManagerJobType.AJ154.toString())) {

            runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(assetCd);
            if (runnigJobEntity != null) {
                jobEntity = runnigJobEntity.getJobEntity();
            }

        // 4. 에이전트 로그 수집
        } else if (jobType.equals(ManagerJobType.AJ300.toString())) {
            runnigJobEntity = INMEMORYDB.RUNNINGLOGJOBLIST.get(assetCd);
            if (runnigJobEntity != null) {
                jobEntity = runnigJobEntity.getJobEntity();
            }
        }

        if (runnigJobEntity == null && jobEntity == null) {
            if (errMsg.isEmpty()) {
                errMsg = "assetCd (" + assetCd + ")" + "is empty.";
            }

            checkOldJobRequest(jobType, assetCd, agentCd);

            log.error(errMsg);
            throw new Exception(errMsg);
        }

        for (MultipartFile file : files) {
            File dest = new File(uploadPath + File.separator + file.getOriginalFilename());
            if (jobType.equals(ManagerJobType.AJ201.toString())) {
                String fileNm = file.getOriginalFilename();
                fileNm = fileNm.substring(0, fileNm.lastIndexOf(INMEMORYDB.GSRESULTTYPE));
                jobEntity.setFileName(fileNm);
                log.debug("AJ201 upload FileName : {}", fileNm);
                file.transferTo(dest);
            } else {
                if (file.getOriginalFilename().equals(jobEntity.getFileName() + INMEMORYDB.DES)) {
                    file.transferTo(dest);
                } else {
                    String msg = "jobEntity - fileName : " + jobEntity.getFileName() + ", upload fileName : " + file.getOriginalFilename();
                    log.error(msg);
                    notificationListener.arrangeJobList(jobEntity);
                    if (jobType.equals(ManagerJobType.AJ200.toString()) ) {
                        throw new SnetException(dao, msg, jobEntity, "G");
                    } else if (jobType.equals(ManagerJobType.AJ100.toString()) || jobType.equals(ManagerJobType.AJ101.toString()) ) {
                        INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.SEND);
                        throw new SnetException(dao, msg, jobEntity, "D");
                    }
                }
            }
            log.info("file uploaded successfully. path - " + dest.toURI());
        }

        return jobEntity;
    }

    /**
     * 파일 업,다운로드 되거나 진단과정
     * 업데이트시 RUNNINGDGJOBLIST에는 존재하지 않고
     * SNET_AGENT_JOB_HISTORY테이블의 agent_job_flag가 2인 상태이면서, 요청시간이 오래된 JOB이라면
     * SNET_AGENT_JOB_HISTORY테이블의 agent_job_flag를 4로 업데이트 해준다.
      */
    private void checkOldJobRequest(String jobType, String assetCd, String agentCd) throws Exception {
        log.debug("start checking old job if agent_job_flag is 2 and agent_job_rdate is out of date.");

        HashMap<String, String> params = new HashMap<>();
        params.put("assetCd", assetCd);
        params.put("agentCd", agentCd);
        params.put("agentJobDesc", "old job request.");

        if (jobType.equals(ManagerJobType.AJ100.toString())) {
            dao.updateAgentJobHistoryCheckOld(params);
        } else if (jobType.equals(ManagerJobType.AJ200.toString())) {
            dao.updateAgentGetJobHistoryCheckOld(params);
        }
    }

    /*
       스크립트실행 결과 파일 처리
     */
    public void handleResultFile(String assetCd, String jobType, String agentCd, String uploadPath, JobEntity jobEntity) throws Exception {

        log.info("assetCd({}) - starting handling this Result File. jobType - {}", assetCd, jobType);
        if (jobEntity == null) {
            String msg = "assetCd( "+ assetCd + ") - the job to be handled cannot exist. jobType - " + jobType;
            log.error(msg);
            throw new Exception(msg);
        }

        // 1.장비 정보 수집 결과 파일 처리
        if (jobType.equals(ManagerJobType.AJ200.toString()) ) {

            try {
                assetUpdateManager.recvGetscriptResult(jobEntity);
                notificationListener.arrangeGsJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
            } catch (Exception ex) {
                log.error(CommonUtils.printError(ex));
                notificationListener.arrangeGsJobList(jobEntity);
                CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());
                if(jobEntity.isAgentGetprog()){
                    throw new SnetException(dao, ex.getMessage(), jobEntity, "G");
                }else {
                    notificationListener.arrangeDgJobList(jobEntity);
                    throw new SnetException(dao, ex.getMessage(), jobEntity, "D");
                }
            }
        // 2. 에이전트 설치 전 장비 정보 수집 결과 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ201.toString())) {
            assetUpdateManager.doGSBFAGENTresult_v3(jobEntity);
        // 3. 진단 실행 결과 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ100.toString())) {

            // 복호화 후 파싱에에 나는 예외는 recvDGscriptResult 함수 안에서 SnetException 처리
            diagnosisManager.recvDGscriptResult(jobEntity);
            notificationListener.arrangeDgJobList(jobEntity);
            CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());

        // 4. 긴급 진단 실행 결과 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ101.toString())) {

            // 복호화 후 파싱에에 나는 예외는 recvEventDGscriptResult 함수 안에서 SnetException 처리
            diagnosisManager.recvEventDGscriptResult(jobEntity);
            notificationListener.arrangeDgJobList(jobEntity);
            CommonUtils.deleteFiles(uploadPath, jobEntity.getFileName(), jobEntity.getAssetCd());

        // 5. 에이전트 로그 파일 처리
        } else if (jobType.equals(ManagerJobType.AJ300.toString())) {
            try {
                agentLogManager.handleAgentLogFile(jobEntity);
                notificationListener.arrangeLogJobList(jobEntity);
            } catch (Exception ex) {
                notificationListener.arrangeLogJobList(jobEntity);
                INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
                throw new Exception("failed to handle AgentLog file.");
            }
        }
    }

    /**
     *  현재 에이전트 처리 상태 업데이트 -  수집, 진단
     */
    public void updateHandledStatus(HandledProcess handledProcess) throws Exception {
        String jobType = handledProcess.getJobType();
        log.info("assetCd({}) - starting updating this process. jobType - {}", handledProcess.getAssetCd(), jobType);

        JobEntity jobEntity = handledProcessManager.updateHandledProcess(handledProcess);

        if (jobEntity == null) {
            String msg = "assetCd( " + handledProcess.getAssetCd() + ") - the job to update status cannot exist. jobType - " + jobType;
            log.error(msg);
            checkOldJobRequest(jobType, handledProcess.getAssetCd(), handledProcess.getAgentCd());

            throw new Exception(msg);
        }
    }

    // agentManager 서버에서 ssh 동작이 하나라도 실행 중이면 ssh 요청 실패로 끝낸다.
    // 하나의 에이전트에 수집 요청을 하면 그 수집요청이 완료될 때까지 더이상의 수집 요청은 받지 않는다. phil
    public JobEntity createJobEntityAssetGet(String assetCd, String jobType, String personManagerCd) throws Exception {
        log.debug("start get process.");

        JobEntity jobEntity = null;
        try {
            AgentInfo ai = jobHandleManager.initAgentInfoForBF(assetCd);

            if (ai != null) {
                jobEntity = jobHandleManager.initJobEntity(ai, personManagerCd, "N");
                if(jobEntity != null) {
                    if (jobEntity.getAgentInfo().getAgentRegiFlag() == 2) {
                        log.info("this asset is already running agent service.");
                    }
                } else {
                    throw new Exception("failed to create the jobEntity.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
            if (jobEntity == null) {
                jobEntity = new JobEntity();
            }
            jobEntity.setAssetCd(assetCd);
            AgentInfo ai = new AgentInfo();
            ai.setAssetCd(assetCd);
            jobEntity.setAgentInfo(ai);
            throw new SnetException(dao, ex.getMessage(), jobEntity, "G");
        }

        log.debug("get request succeed {} in inserting data into RUNNINGGSJOBLIST. get job list size - {}, JOBLISTPERASSET size - {}"
                , assetCd, INMEMORYDB.RUNNINGGSJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
        return jobEntity;
    }

    /**
     * 진단 실행 JOB 생성
     */
    public ApiResult createJobEntityAssetOrgDiagS(List<DiagnosisReq> diagReqS) throws Exception {

        //Config 값 동기화.
        configGlobalService.init();

        // 1. 개별 select
        JobEntity jobEntity = null;
        JobEntityUtil jobEntityUtil = new JobEntityUtil();

        // 추후 삭제 필요.. 포스트맨 테스트를 위해서 존재함.
        if(optionProperties.getDiagnosis().equals("true")) {
            log.debug("===========dignosis request for debug Test===============");
            for (DiagnosisReq req : diagReqS) {
                JobEntity jobEntityTest = jobEntityUtil.getDiagnosisJobEntity(req);
                jobEntityTest.setAgentJobFlag(1);
                jobEntityTest.setAgentJobDesc("");
                dao.updateAgentJobHistory(jobEntityTest);
                //현재 실행 중인 진단 실행 큐에서 제거
                INMEMORYDB.removeDGJOBList(jobEntityTest);
                // 현재 실행 중인 자산별로 등록된 JOB을 제거
                INMEMORYDB.removeJobListPerAsset(jobEntityTest.getAssetCd(), jobEntityTest.getJobType());
                log.debug("(assetCd {}), RUNNINGDGJOBLIST size - {}, JOBLISTPERASSET size - {}",
                        jobEntityTest.getAssetCd(), INMEMORYDB.RUNNINGDGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
            }
        }

        for (DiagnosisReq req : diagReqS) {

            try {
                jobEntity = jobHandleManager.initJobEntity(req);
                if (jobEntity != null) {
                    INMEMORYDB.createRunningDgJobList(dao, jobEntity);
                    log.debug("succeed in inserting item aeestCd({}) into RUNNINGDGJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
                            jobEntity.getAssetCd(), INMEMORYDB.RUNNINGDGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
                }
            } catch (Exception ex) {
                log.error("please check the assetCd({})", req.getAssetCd());
            }
        }

//            // 2. 전체 select
//            List<JobEntity> jobEntities = jobHandleManager.initJobEntity(diagReqS);
//            if (jobEntities != null && jobEntities.size() >0) {
//                for(JobEntity jobEntity : jobEntities) {
//                    jobHandleManager.insertDgJobList(jobEntity);
//                }
//            }

        return ApiResult.builder().build();
    }

    /**
     * 긴급진단 실행 JOB 생성
     */
    public ApiResult createEvnetJobEntityAssetDiagS(List<DiagnosisReq> diagReqS) {

        JobEntity jobEntity = null;

        for (DiagnosisReq req : diagReqS) {

            try {

                jobEntity = jobHandleManager.initEventJobEntity(req.getAssetCd(), req.getPrgId(), req.getSwNm());
                if (jobEntity != null) {
                    INMEMORYDB.createRunningDgJobList(dao, jobEntity);
                    log.debug("succeed in inserting item aeestCd({}) into RUNNINGDGJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
                            jobEntity.getAssetCd(), INMEMORYDB.RUNNINGDGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
                }
            } catch (Exception ex) {
                log.error("please check the assetCd({})", req.getAssetCd());
            }
        }

        return ApiResult.builder().build();
    }


    public ApiResult deleteJobEntityAssetDiagS(List<JobDel> daigDelS) {

        JobEntity jobEntity;
        for (JobDel jobDel : daigDelS) {
            RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(jobDel.getAssetCd());
            if (runnigJobEntity != null) {
                jobEntity = runnigJobEntity.getJobEntity();
                if (jobEntity.getSwType().equals(jobDel.getSwType()) && jobEntity.getSwType().equals(jobDel.getSwType())
                        && jobEntity.getSwNm().equals(jobDel.getSwNm()) && jobEntity.getSwInfo().equals(jobDel.getSwInfo())) {
                    if ((jobDel.getManagerCd() == null)
                            || jobDel.getManagerCd().isEmpty()) {
                        notificationListener.arrangeDgJobList(jobEntity);
                    } else {
                        CopyOnWriteArrayList<AgentJob> curJobList = INMEMORYDB.JOBLISTPERASSET.get(jobDel.getAssetCd());
                        if ((curJobList != null) && (curJobList.size() > 0 )) {
                            for (AgentJob ai : curJobList) {
                                if (ai.getJobType().equals(ManagerJobType.AJ100.toString())
                                        && (ai.getCheckCnt() == INMEMORYDB.agentJobCheckCnt)) {
                                    notificationListener.arrangeDgJobList(jobEntity);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return ApiResult.builder().build();
    }

    public ApiResult makeDownloadableDiagInfo(List<DiagInfoReq> daigInfoReqS) {

        for (DiagInfoReq req : daigInfoReqS) {

            JobEntityInitData initData = new JobEntityInitData(req);
            JobEntity args = initData.getJobEntity();

            try {
                JobEntity jobEntity = dao.selectDiagInfo(args);

                String desFileOrg = jobEntity.getDiagInfoFilePathDes();
                String desFile = desFileOrg.substring(0, desFileOrg.lastIndexOf("."));      // 진단 백업경로에서 확장자 제외한 풀패스
                String desFIleNm = desFile.substring(desFile.lastIndexOf(File.separator) + 1);   // 진단 백업경로에서 확장자 제외한 파일이름만.
                jobEntity.setFileName(desFile);

                jobEntity.setFileType(INMEMORYDB.ZIP);
                jobEntity.setJobType(args.getJobType());
                String zipFileNme = desFile + INMEMORYDB.ZIP;
                String tomcatDnDir = INMEMORYDB.DIAG_INFO_FILE_TOMCAT_DIR + desFIleNm;

                // 1. 기존 파일 복호화 후 압축해제
                new AESCryptography().decryptionFile(jobEntity);
                new ZipUtil(zipFileNme, tomcatDnDir).unzip();

                // 2. WAS에서 다운로드 가능한 패스워드 없는 zip파일 생성
                ArrayList<File> zipfiles = new ArrayList<File>();
                File diagFolder = new File(tomcatDnDir);
                File[] cfgFileList = diagFolder.listFiles();
                for (File file : cfgFileList) {
                    if (!file.isDirectory()) {
                        zipfiles.add(file);
                    }
                }

                new ZipUtil().makeZipWithoutPwd(zipfiles, jobEntity.getDiagInfoFilePath());

                CommonUtils.deleteDirectory(new File(tomcatDnDir));

                //백업디렉터리에서 압축해제한 zip파일 삭제
                CommonUtils.deleteFile(zipFileNme);

                jobEntity.setDiagInfoFlag(2);   // 복호화 요청 완료.
                dao.updateDiagInfo(jobEntity);

            } catch (Exception ex) {
                String msg = "please run diagnosis again. cause the File is empty or Decryption failed.";
                log.error("assetCd : " + args.getAssetCd() + msg);
                args.setDiagInfoFlag(3);
                args.setDiagInfoJobDesc(msg);
                try {
                    dao.updateDiagInfo(args);
                } catch (Exception fi) {
                    log.error(args.getAssetCd() + " : " + fi.getMessage());
                }
            }
        }

        return ApiResult.builder().build();
    }

    public ApiResult FailedRunedSshService(JobEntity jobEntity) throws Exception {

        String msg = "failed : assetcd(" + jobEntity.getAssetCd() + ") is running ssh service.";
        jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
        jobEntity.setAgentJobFlag(4);
        jobEntity.setAgentJobDesc(msg);
        dao.updateAgentGetJobHistory(jobEntity);

        log.debug(msg);

        return ApiResult.builder().result(400)
                .message(msg).build();
    }
}
