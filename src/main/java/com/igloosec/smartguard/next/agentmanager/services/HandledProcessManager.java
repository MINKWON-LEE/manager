package com.igloosec.smartguard.next.agentmanager.services;


import com.igloosec.smartguard.next.agentmanager.api.asset.model.HandledProcess;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.memory.NotiType;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HandledProcessManager {

    private Dao dao;
    private NotificationListener notificationListener;

    public HandledProcessManager(Dao dao, NotificationListener notificationListener) {
        this.dao = dao;
        this.notificationListener = notificationListener;
    }

    public JobEntity updateHandledProcess(HandledProcess handledProcess) throws Exception  {
        String jobType = handledProcess.getJobType();
        log.info("assetCd({}) - starting updating this process. jobType - {}", handledProcess.getAssetCd(), jobType);

        JobEntity jobEntity = null;

        try {
            /* 1.장비 정보 수집 에이전트 처리 상태 업로드 */
            if (jobType.equals(ManagerJobType.AJ200.toString())) {
                jobEntity = updateAssetGet(handledProcess);
                /* 2.진단 실행 에이전트 처리 상태 업로드 */
            } else if (jobType.equals(ManagerJobType.AJ100.toString())) {
                jobEntity = updateAssetDiagnosis(handledProcess);
                /* 3. 긴급 진단 실행 에이전트 처리 상태 업로드 */
            } else if (jobType.equals(ManagerJobType.AJ101.toString())) {
                jobEntity = updateAssetEventDiagnosis(handledProcess);
            } else if (jobType.equals(ManagerJobType.AJ601.toString())
                    || jobType.equals(ManagerJobType.AJ602.toString())) {
                jobEntity = updateAgentControl(handledProcess);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            jobEntity = null;
        }

        return jobEntity;
    }

    private JobEntity updateAssetGet(HandledProcess handledProcess) throws Exception {
        JobEntity jobEntity = null;

        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(handledProcess.getAssetCd());
        if (runnigJobEntity != null) {
            jobEntity = runnigJobEntity.getJobEntity();
            if (jobEntity != null) {
                String noti = handledProcess.getNotiType();
                String msg = NotiType.getStatusLog(ManagerJobType.AJ200.toString(), noti, handledProcess.getMessage());
                jobEntity.getAgentInfo().setConnectLog(msg + " [" + DateUtil.getCurrDateBySecondFmt() + "]");
                dao.updateConnectMaster(jobEntity);

                // 실패시 JOB에서 삭제
                if (isFailedNotiType(noti)) {
                    jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                    jobEntity.setAgentJobFlag(4);
                    dao.updateAgentGetJobHistory(jobEntity);
                    notificationListener.arrangeGsJobList(jobEntity);
                } else {
                    jobEntity.setAgentJobDesc(msg);
                    dao.updateAgentGetJobHistory(jobEntity);
                }
            }
        }

        return jobEntity;
    }

    private JobEntity updateAssetDiagnosis(HandledProcess handledProcess) throws Exception {

        JobEntity jobEntity = null;

        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(handledProcess.getAssetCd());
        if (runnigJobEntity != null) {
            jobEntity = runnigJobEntity.getJobEntity();
            if (jobEntity != null) {

                if (jobEntity.getEventFlag().equals("Y")) {
                    try {
                        return updateAssetEventDiagnosis(handledProcess);
                    } catch (Exception ex) {
                        return null;
                    }
                }

                String noti = handledProcess.getNotiType();
                String msg = NotiType.getStatusLog(ManagerJobType.AJ100.toString(), noti, handledProcess.getMessage());
                jobEntity.setAgentJobDesc(msg);
                if (handledProcess.getNotiType().equals(NotiType.AN001.toString())) {
                    jobEntity.setAgentJobFlag(6);   // 파일 hash 체크 완료
                } else if (handledProcess.getNotiType().equals(NotiType.AN002.toString())) {
                    jobEntity.setAgentJobFlag(7);   // 스크립트 실행 완료
                } else if (handledProcess.getNotiType().equals(NotiType.AN051.toString())
                            || handledProcess.getNotiType().equals(NotiType.AN054.toString())) {
                    jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                    jobEntity.setAgentJobFlag(4);   // 파일 hash 체크 실패
                } else if (handledProcess.getNotiType().equals(NotiType.AN052.toString())
                            || handledProcess.getNotiType().equals(NotiType.AN055.toString())
                            || handledProcess.getNotiType().equals(NotiType.AN056.toString())) {
                    jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                    jobEntity.setAgentJobFlag(4);   // 스크립트 실행 실패
                } else if (handledProcess.getNotiType().equals(NotiType.AN060.toString())) {
                    jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
                    jobEntity.setAgentJobFlag(4);   // sudo sh 실패
                }

                dao.updateAgentJobHistory(jobEntity);

                // 실패시 JOB에서 삭제
                if (isFailedNotiType(noti)) {
                    INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.SEND);
                    notificationListener.arrangeDgJobList(jobEntity);
                }
            }
        }

        return jobEntity;
    }

    private JobEntity updateAssetEventDiagnosis(HandledProcess handledProcess) throws Exception {

        JobEntity jobEntity = null;

        RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(handledProcess.getAssetCd());
        if (runnigJobEntity != null) {
            jobEntity = runnigJobEntity.getJobEntity();
            if (jobEntity != null) {
                String noti = handledProcess.getNotiType();
                String msg = NotiType.getStatusLog(ManagerJobType.AJ101.toString(), noti, "");
                jobEntity.setAgentJobDesc(msg);

                if (jobEntity.getEventFlag().equals("Y")) {
                    if (handledProcess.getNotiType().equals(NotiType.AN001.toString())) {
                        jobEntity.setAgentJobFlag(5);   // 파일 hash 체크 완료
                    } else if (handledProcess.getNotiType().equals(NotiType.AN002.toString())) {
                        jobEntity.setAgentJobFlag(6);   // 스크립트 실행 완료
                    } else if (handledProcess.getNotiType().equals(NotiType.AN051.toString())
                                || handledProcess.getNotiType().equals(NotiType.AN054.toString())) {
                        jobEntity.setAgentJobFlag(3);   // 파일 hash 체크 실패
                    } else if (handledProcess.getNotiType().equals(NotiType.AN052.toString())
                                || handledProcess.getNotiType().equals(NotiType.AN055.toString())
                                || handledProcess.getNotiType().equals(NotiType.AN056.toString())) {
                        jobEntity.setAgentJobFlag(3);   // 스크립트 실행 실패
                    } else if (handledProcess.getNotiType().equals(NotiType.AN060.toString())) {
                        jobEntity.setAgentJobFlag(3);   // sudo sh 실패
                    }
                    dao.updateEventAgentJobHistory(jobEntity);

                    // 실패시 JOB에서 삭제
                    if (isFailedNotiType(noti)) {
                        notificationListener.arrangeDgJobList(jobEntity);
                    }
                }
            }
        }

        return jobEntity;
    }

    private JobEntity updateAssetSrvDiagnosis(HandledProcess handledProcess) throws Exception {

        JobEntity jobEntity = null;

        return jobEntity;
    }

    private JobEntity updateAgentControl(HandledProcess handledProcess) throws Exception  {

        JobEntity jobEntity = new JobEntity();
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentCd(handledProcess.getAgentCd());
        agentInfo.setAssetCd(handledProcess.getAssetCd());
        jobEntity.setJobType(handledProcess.getJobType());
        jobEntity.setAssetCd(handledProcess.getAssetCd());
        jobEntity.setAgentInfo(agentInfo);

        String noti = handledProcess.getNotiType();
        String msg = NotiType.getStatusLog(handledProcess.getJobType(), noti, handledProcess.getMessage());
        jobEntity.getAgentInfo().setConnectLog(msg + " [" + DateUtil.getCurrDateBySecondFmt() + "]");
        dao.updateConnectMaster(jobEntity);

        // 실패시 JOB에서 삭제
        if (isFailedNotiType(noti)) {
            notificationListener.arrangeControlJobList(jobEntity);
        }

        return jobEntity;
    }

    private boolean isFailedNotiType(String noti) {

        boolean bRet = false;
        if (noti.equals(NotiType.AN051.toString())
                || noti.equals(NotiType.AN052.toString())
                || noti.equals(NotiType.AN053.toString())
                || noti.equals(NotiType.AN054.toString())
                || noti.equals(NotiType.AN055.toString())
                || noti.equals(NotiType.AN056.toString())
                || noti.equals(NotiType.AN059.toString())
                || noti.equals(NotiType.AN060.toString())) {

            bRet = true;
        }

        return bRet;
    }
}
