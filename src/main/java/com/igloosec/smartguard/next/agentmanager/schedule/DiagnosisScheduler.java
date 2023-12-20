/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.schedule
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 4. 5.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.schedule;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagnosisReq;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;

import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.services.JobHandleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DiagnosisScheduler {

	private Dao dao;
	private JobHandleManager jobHandleManager;

	public DiagnosisScheduler(Dao dao, JobHandleManager jobHandleManager) {
		this.dao = dao;
		this.jobHandleManager = jobHandleManager;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Scheduled(cron = "${smartguard.v3.schedule.diagnosis}")
	public void diagnosisSchedule(){
		try {
			
			List<DiagnosisReq> queueJob = (List<DiagnosisReq>) dao.selectDiagnosisJob();

			if(queueJob!=null){
				for(DiagnosisReq dr : queueJob){
					RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(dr.getAssetCd());
					if (runnigJobEntity != null) {
						outputLog(runnigJobEntity, dr);
						continue;
					}

					JobEntity jobEntity = jobHandleManager.initJobEntity(dr);
					if (jobEntity != null) {
						INMEMORYDB.createRunningDgJobList(dao, jobEntity);
						log.debug("succeed in inserting item aeestCd({}) into RUNNINGDGJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
								jobEntity.getAssetCd(), INMEMORYDB.RUNNINGDGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
					}
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void outputLog(RunnigJobEntity runnigJobEntity, DiagnosisReq dr) {
		JobEntity jobEntity = runnigJobEntity.getJobEntity();
		if (jobEntity.getSwType().equals(dr.getSwType()) &&
				jobEntity.getSwNm().equals(dr.getSwNm()) &&
				jobEntity.getSwInfo().equals(dr.getSwInfo()) &&
				jobEntity.getSwDir().equals(dr.getSwDir()) &&
				jobEntity.getSwUser().equals(dr.getSwUser()) &&
				jobEntity.getSwEtc().equals(dr.getSwEtc())) {
			log.debug("this diagnosis of software is already running. swType : {}, swNm : {}, swInfo : {}",
					jobEntity.getSwType(), jobEntity.getSwNm(), jobEntity.getSwInfo());
		} else {
			log.debug("other diagnosis of this software in this asset is already running. " +
							"current swType : {}, swNm : {}, swInfo : {}" +
							" - skipped software swType : {}, swNm : {}, swInfo : {}",
					jobEntity.getSwType(), jobEntity.getSwNm(), jobEntity.getSwInfo(),
					dr.getSwType(), dr.getSwNm(), dr.getSwInfo());
		}
	}
}
