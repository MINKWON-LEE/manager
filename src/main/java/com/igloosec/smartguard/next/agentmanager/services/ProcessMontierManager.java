package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.concurrents.OneTimeThread;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;

import com.igloosec.smartguard.next.agentmanager.utils.ErrCodeUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by osujin12 on 2016. 4. 12..
 */
@Service
public class ProcessMontierManager extends AbstractManager{

    private int schedTime = 0;

    private final int completedChkJob = 100;

    private final Dao dao;

    ProcessMontierManager(Dao dao) {
        this.dao = dao;
        initData();
    }

    public void doProcessMontier() {

        OneTimeThread worker = new OneTimeThread() {
            @Override
            public void task() throws Exception {
                JobEntity jobEntity;
                String jobDate;
                boolean isJob;

                while (true){

                    try {

                        // 0. 진단완료 또는 실패난 장비는 진단큐에서 제거 (매니저 모듈 멀티로 연동한 경우 동작)
                        if (INMEMORYDB.MultiServices) {
                            removceCompletedGetJobInfoList();
                            removeCompletedJobInfoList();
                        }

                        // 1. 진단 타임아웃 체크.
                        Set keyset = INMEMORYDB.RUNNINGDGJOBLIST.keySet();
                        for(Iterator iter = keyset.iterator(); iter.hasNext();){
                            String assetCd = iter.next().toString();

                            try {
                                RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(assetCd);
                                jobEntity = runnigJobEntity.getJobEntity();
                                jobDate = jobEntity.getSDate();
                                isJob = compareRunTime(jobDate, jobEntity.getJobType());

                                if (isJob){
                                    logger.debug("DG JobTime Out , DG JobQueue & JobEntity Delete : "+jobEntity.getAssetCd());

                                    //현재 실행 중인 진단 실행 큐에서 제거
                                    INMEMORYDB.removeDGJOBList(jobEntity);
                                    // 현재 실행 중인 자산별로 등록된 JOB을 제거
                                    INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

                                    if (INMEMORYDB.maxDGexec > 0 && INMEMORYDB.RUNNINGAGENTDGJOBLIST.size() > 0) {
                                        INMEMORYDB.RUNNINGAGENTDGJOBLIST.remove(jobEntity.getAssetCd());
                                    }

                                    logger.debug("(assetCd {}), RUNNINGDGJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                            jobEntity.getAssetCd(), INMEMORYDB.RUNNINGDGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

                                    if(runnigJobEntity != null) {
                                        new SnetException(dao, SnetCommonErrCode.ERR_0001, replaceJobType(jobEntity.getJobType()), jobEntity, "D");
                                    }
                                }
                              } catch (NullPointerException e){}

                        }

                        // 2. 장비정보 수집 타임아웃 체크.
                        keyset = INMEMORYDB.RUNNINGGSJOBLIST.keySet();
                        for(Iterator iter = keyset.iterator(); iter.hasNext();){

                            String assetCd = (String) iter.next();
                            RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(assetCd);
                            jobEntity = runnigJobEntity.getJobEntity();
                            jobDate = runnigJobEntity.getJobDate();
                            isJob = compareRunTime(jobDate, jobEntity.getJobType());

                            if (isJob){
                                logger.debug("GS JobTime Out , GS JobQueue & JobEntity Delete : "+runnigJobEntity.getMsg());

                                try {

                                    // 현재 실행 중인 장비 정보 수집 큐에서 제거
                                    INMEMORYDB.removeGSJOBList(jobEntity);
                                    // 현재 실행 중인 ssh 서비스 종료.
                                    INMEMORYDB.removeSshJobList(jobEntity);
                                    // 현재 실행 중인 자산별로 등록된 JOB을 제거
                                    INMEMORYDB.removeJobListPerAsset(assetCd, jobEntity.getJobType());

                                    logger.debug("(assetCd {}), RUNNINGGSJOBLIST size - {}, RUNNING_SSHJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                            assetCd, INMEMORYDB.RUNNINGGSJOBLIST.size(),
                                            INMEMORYDB.RUNNING_SSHJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

                                    new SnetException(dao, SnetCommonErrCode.ERR_0001, replaceJobType(jobEntity.getJobType()), jobEntity, "G");
                                } catch (Exception e){}
                            }
                        }

                        // 3. 네트워크 수집 및 진단 (온라인) 타임아웃 체크.
                        keyset = INMEMORYDB.RUNNINGNWJOBLIST.keySet();
                        for(Iterator iter = keyset.iterator(); iter.hasNext();){
                            String assetCd = (String) iter.next();
                            RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGNWJOBLIST.get(assetCd);
                            jobDate = runnigJobEntity.getJobDate();
                            jobEntity = runnigJobEntity.getJobEntity();
                            isJob = compareRunTime(jobDate, jobEntity.getJobType());
                            if (isJob){
                                logger.debug("NW JobTime Out , NW JobQueue & JobEntity Delete : "+ runnigJobEntity.getMsg());

                                try {

                                    // 현재 실행 중인 네트워크장비 큐에서 제거
                                    INMEMORYDB.removeNWJOBList(jobEntity);
                                    // 현재 실행 중인 ssh 서비스 종료.
                                    INMEMORYDB.removeSshJobList(jobEntity);
                                    // 현재 실행 중인 자산별로 등록된 JOB을 제거
                                    INMEMORYDB.removeJobListPerAsset(assetCd, jobEntity.getJobType());

                                    logger.debug("(assetCd {}), RUNNINGNWJOBLIST size - {}, RUNNING_SSHJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                            assetCd, INMEMORYDB.RUNNINGNWJOBLIST.size(),
                                            INMEMORYDB.RUNNING_SSHJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());


                                    if(runnigJobEntity != null) {
                                        String type = "";
                                        if (jobEntity.getJobType().equals(ManagerJobType.WM300.toString())) {
                                            type = "G";
                                        } else {
                                            type = "D";
                                        }
                                        new SnetException(dao, SnetCommonErrCode.ERR_0001, replaceJobType(jobEntity.getJobType()), jobEntity, type);
                                    }
                                } catch (Exception e){}
                            }
                        }

                        // 4. 에이전트 설치 타임아웃 체크.
                        keyset = INMEMORYDB.RUNNINGSETUPJOBLIST.keySet();
                        for(Iterator iter = keyset.iterator(); iter.hasNext();){
                            String assetCd = (String) iter.next();
                            RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGSETUPJOBLIST.get(assetCd);
                            jobEntity = runnigJobEntity.getJobEntity();
                            jobDate = runnigJobEntity.getJobDate();
                            isJob = compareRunTime(jobDate, jobEntity.getJobType());
                            if (isJob){
                                logger.debug("AGENTSETUPREQ JobTime Out , AGENTSETUPREQ Delete : "+runnigJobEntity.getMsg());

                                try {
                                    //현재 실행 중인 에이전트 설치 큐에서 제거.
                                    INMEMORYDB.removeSETUPJOBList(jobEntity);
                                    // 현재 실행 중인 ssh 서비스 종료.
                                    INMEMORYDB.removeSshJobList(jobEntity);
                                    // 현재 실행 중인 자산별로 등록된 JOB을 제거
                                    INMEMORYDB.removeJobListPerAsset(assetCd, jobEntity.getJobType());

                                    logger.debug("(assetCd {}), RUNNINGSETUPJOBLIST size - {}, RUNNING_SSHJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                            assetCd, INMEMORYDB.RUNNINGSETUPJOBLIST.size(),
                                            INMEMORYDB.RUNNING_SSHJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

                                    new SnetException(dao, SnetCommonErrCode.ERR_0001, replaceJobType(jobEntity.getJobType()), jobEntity, "I");
                                } catch (Exception e) {}
                            }
                        }

                        // 5. 에이전트 중지, 재시작 타임아웃 체크.
                        keyset = INMEMORYDB.RUNNINGCONTROLJOBLIST.keySet();
                        for(Iterator iter = keyset.iterator(); iter.hasNext();){
                            String assetCd = (String) iter.next();
                            RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGCONTROLJOBLIST.get(assetCd);
                            jobEntity = runnigJobEntity.getJobEntity();
                            jobDate = runnigJobEntity.getJobDate();
                            isJob = compareRunTime(jobDate, jobEntity.getJobType());
                            if (isJob){
                                logger.debug("AGENTCONTROLREQ JobTime Out , AGENTCONTROLREQ Delete : "+runnigJobEntity.getMsg());

                                try {
                                    // 현재 실행 중인 에이전트 제어 큐에서 제거.
                                    INMEMORYDB.removeCONTROLJOBList(jobEntity);
                                    // 현재 실행 중인 자산별로 등록된 JOB을 제거
                                    INMEMORYDB.removeJobListPerAsset(assetCd, jobEntity.getJobType());

                                    logger.debug("(assetCd {}), RUNNINGCONTROLJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                            assetCd, INMEMORYDB.RUNNINGCONTROLJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

                                    new SnetException(dao, SnetCommonErrCode.ERR_0001, replaceJobType(jobEntity.getJobType()), jobEntity, "C");
                                } catch (Exception e) {}
                            }
                        }

                        // 6. 로그파일 수집 타임아웃 체크.
                        keyset = INMEMORYDB.RUNNINGLOGJOBLIST.keySet();
                        for(Iterator iter = keyset.iterator(); iter.hasNext();){
                            String assetCd = (String) iter.next();
                            RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGLOGJOBLIST.get(assetCd);
                            jobEntity = runnigJobEntity.getJobEntity();
                            jobDate = runnigJobEntity.getJobDate();
                            isJob = compareRunTime(jobDate, jobEntity.getJobType());
                            if (isJob){
                                logger.debug("RUNNINGLOGJOBLIST JobTime Out , RUNNINGLOGJOBLIST Delete");

                                try {
                                    // 현재 실행 중인 에이전트 제어 큐에서 제거.
                                    INMEMORYDB.removeLOGJOBList(jobEntity);
                                    // 현재 실행 중인 자산별로 등록된 JOB을 제거
                                    INMEMORYDB.removeJobListPerAsset(assetCd, jobEntity.getJobType());

                                    logger.debug("(assetCd {}), RUNNINGLOGJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                            assetCd, INMEMORYDB.RUNNINGLOGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

                                    new SnetException(dao, SnetCommonErrCode.ERR_0001, replaceJobType(jobEntity.getJobType()), jobEntity, "C");
                                } catch (Exception e) {}
                            }
                        }

                        // 7. 오랫동안 Job Api를 호출하지 않은 장비는 JobAPI 자산 리스트에서 제거.
                        keyset = INMEMORYDB.AGENTSCHEDULEASSETCHECKLIST.keySet();
                        for(Iterator iter = keyset.iterator(); iter.hasNext();) {
                            String assetCd = (String) iter.next();
                            jobDate = INMEMORYDB.AGENTSCHEDULEASSETCHECKLIST.get(assetCd);
                            isJob = compareRunTime(jobDate, "jobApi");
                            if (isJob) {
                                logger.debug("AGENTSCHEDULEASSETCHECKLIST JobTime Out , AGENTSCHEDULEASSETCHECKLIST Delete : " + assetCd);
                                INMEMORYDB.removeJOBAPIList(assetCd);
                            }
                        }

                        // 8. 실행 중인 상태에 멈춰있는 오래된 진단 실행 중 상태인 것들을 타임 아웃 처리.
                        //    1시간에 한번씩만 agent_job_flag가 2, 6, 7 인거 4 로 업데이트
                        schedTime ++;
                        if (schedTime > 59) {
                            String oldJobs = dao.selectOldJobsFromJobHistory();
                            if (StringUtils.isNotEmpty(oldJobs)) {
                                updateOldJobHistory(false);
                            }
                            logger.debug("refresh old job flag. set schedTime of 0");
                            schedTime = 0;
                        }

                    } catch (Exception e ){
                        logger.error(CommonUtils.printError(e));
                    } finally {
                        try {
                            Thread.currentThread().sleep(60*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        worker.start();
    }

    private boolean compareRunTime(String startDate, String jobType){

        long compare = 0;
        long currentTime = DateUtil.getCurTimeInDate().getTime();
        long startTime = DateUtil.getMiliTime(startDate);
        long checkTime = (currentTime - startTime);

        if (jobType.equals(ManagerJobType.AJ100.toString())) {

            compare = INMEMORYDB.JobDGTimeOut;
        } else if (jobType.equals(ManagerJobType.AJ200.toString())) {

            compare = INMEMORYDB.JobGSTimeOut;
        } else if (jobType.equals(ManagerJobType.WM300.toString())
                || jobType.equals(ManagerJobType.WM302.toString())) {

            compare = INMEMORYDB.JobNWTimeOut;
        } else if (jobType.equals(ManagerJobType.AJ600.toString())) {

            compare = INMEMORYDB.JobSetupTimeOut;
        } else if (jobType.equals(ManagerJobType.AJ601.toString())
                || jobType.equals(ManagerJobType.AJ602.toString())
                || jobType.equals(ManagerJobType.AJ603.toString())) {

            compare = INMEMORYDB.JobControlTimeOut;
        } else if (jobType.equals(ManagerJobType.AJ300.toString())) {

            compare = INMEMORYDB.JobLogTimeOut;
        } else if (jobType.toLowerCase().equals("jobapi")) {

            compare = INMEMORYDB.JobApiTimeOut;
        }

        return (checkTime > compare);
    }

    private String replaceJobType(String JobType){
        String s = "";
        switch (JobType){
            case "AJ200":
                s = "asset information gathering";
                break;
            case "AJ100":
                s = "diagnosis";
                break;
            case "AJ600":
                s = "agent setup";
                break;
            case "AJ601":
                s = "agent restart";
                break;
            case "AJ602":
                s = "agent kill";
                break;
            case "AJ300":
                s = "agent logFile";
                break;
            default:
                s = "diagnosis";
                break;
        }

        return s;
    }

    private boolean removceCompletedGetJobInfoList() {
        boolean ret = false;

        //진단 맵(큐)를 assetCd 맵으로 변환.
        Map<Integer, JobEntity> assetCdMap = INMEMORYDB.RUNNINGGSJOBLIST.values().stream().collect(HashMap::new, (m1, m2) -> m1.put(m1.size(), m2.getJobEntity()), HashMap::putAll);

        logger.debug("check completed job`s size : " +  assetCdMap.size());

        int divide = (assetCdMap.size() / completedChkJob) + 1;
        for (int i=0; i < divide; i++) {
            List<JobEntity> assetCdList = getCheckingAssetCdList(i, assetCdMap);
            if (assetCdList.size() == 0) {
                break;
            }

            HashMap<String, Object> param = new HashMap<>();
            param.put("getReqs", assetCdList);

            try {
                List<AgentInfo> agentInfos = dao.selectRunningGetJobInfoList(param);

                for (AgentInfo agentInfo : agentInfos) {
                    if (agentInfo.getConnectLog().trim().equals("success")
                            || agentInfo.getConnectLog().trim().contains("timed out")
                            || agentInfo.getConnectLog().trim().contains("refused")
                            || agentInfo.getConnectLog().trim().contains("failed")) {

                        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(agentInfo.getAssetCd());
                        JobEntity jobEntity = runnigJobEntity.getJobEntity();

                        // 현재 실행 중인 장비 정보 수집 큐에서 제거
                        INMEMORYDB.removeGSJOBList(jobEntity);
                        // 현재 실행 중인 ssh 서비스 종료.
                        INMEMORYDB.removeSshJobList(jobEntity);
                        // 현재 실행 중인 자산별로 등록된 JOB을 제거
                        INMEMORYDB.removeJobListPerAsset(agentInfo.getAssetCd(), jobEntity.getJobType());

                        logger.debug("multiService - (assetCd {}), RUNNINGGSJOBLIST size - {}, RUNNING_SSHJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                agentInfo.getAssetCd(), INMEMORYDB.RUNNINGGSJOBLIST.size(),
                                INMEMORYDB.RUNNING_SSHJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

                    }
                }
                ret = true;
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        return ret;
    }

    private boolean removeCompletedJobInfoList() {
        boolean ret = false;

        //진단 맵(큐)를 assetCd 맵으로 변환.
        Map<Integer, JobEntity> assetCdMap = INMEMORYDB.RUNNINGDGJOBLIST.values().stream().collect(HashMap::new, (m1, m2) -> m1.put(m1.size(), m2.getJobEntity()), HashMap::putAll);

        logger.debug("check completed job`s size : " +  assetCdMap.size());

        int divide = (assetCdMap.size() / completedChkJob) + 1;
        for (int i=0; i < divide; i++) {
            List<JobEntity> assetCdList = getCheckingAssetCdList(i, assetCdMap);
            if (assetCdList.size() == 0) {
                break;
            }

            HashMap<String, Object> param = new HashMap<>();
            param.put("diagReqs", assetCdList);

            try {
                List<JobEntity>  jobEntities = dao.selectCompletedJobInfoList(param);

                for (JobEntity jobEntity : jobEntities) {
                    if (jobEntity.getAgentJobFlag() == 3 || jobEntity.getAgentJobFlag() == 4) {
                        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(jobEntity.getAssetCd());
                        JobEntity curJob = runnigJobEntity.getJobEntity();
                        if (jobEntity.getSwType().equals(curJob.getSwType())
                                && jobEntity.getSwNm().equals(curJob.getSwNm())
                                && jobEntity.getSwInfo().equals(curJob.getSwInfo())
                                && jobEntity.getSwDir().equals(curJob.getSwDir())
                                && jobEntity.getSwUser().equals(curJob.getSwUser())
                                && jobEntity.getSwEtc().equals(curJob.getSwEtc())) {

                            //현재 실행 중인 진단 실행 큐에서 제거
                            INMEMORYDB.removeDGJOBList(jobEntity);
                            // 현재 실행 중인 자산별로 등록된 JOB을 제거
                            INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

                            if (INMEMORYDB.maxDGexec > 0 && INMEMORYDB.RUNNINGAGENTDGJOBLIST.size() > 0) {
                                INMEMORYDB.RUNNINGAGENTDGJOBLIST.remove(jobEntity.getAssetCd());
                            }

                            logger.debug("multiService - removed completed JobInfo from (assetCd {}), RUNNINGDGJOBLIST size - {}, JOBLISTPERASSET size - {}",
                                    jobEntity.getAssetCd(), INMEMORYDB.RUNNINGDGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
                        }
                    }
                }
                ret = true;
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        return ret;
    }

    private List<JobEntity> getCheckingAssetCdList(int checked, Map<Integer, JobEntity> assetCdMap) {

        int min, max;
        min = checked * completedChkJob;
        if ((min + completedChkJob) > assetCdMap.size()) {
            max = min + assetCdMap.size();
        } else {
            max = min + completedChkJob;
        }

        List<JobEntity> assetCdList = new ArrayList<>();
        for (int i = min; i < max; i++) {
            if (assetCdMap.get(i) != null) {
                assetCdList.add(assetCdMap.get(i));
            }
        }

        return assetCdList;
    }

    private boolean updateOldJobHistory(boolean init) {
        boolean ret = false;

        try {
            String desc = ErrCodeUtil.parseMessage(SnetCommonErrCode.ERR_0001.getMessage(), replaceJobType(ManagerJobType.AJ100.toString()));
            if (init) {
                dao.updateAllOldJobHistory(desc);
            } else {
                dao.updateOldJobHistory(desc);
            }
            ret = true;
        } catch (SnetException ex) {
            logger.error(ex.getMessage());
        }

        return ret;
    }

    private void initData() {
        try {
            String oldJobs = dao.selectAllOldJobsFromJobHistory();
            if (StringUtils.isNotEmpty(oldJobs)) {
                updateOldJobHistory(true);
            }
            logger.debug("init : refresh old job flag.");
        } catch (Exception e ){
            logger.error(CommonUtils.printError(e));
        }

    }
}




