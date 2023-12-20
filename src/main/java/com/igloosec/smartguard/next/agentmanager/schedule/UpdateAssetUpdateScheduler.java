/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.schedule
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 4. 5.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.schedule;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.GetReq;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.schedule.model.BatchGet;
import com.igloosec.smartguard.next.agentmanager.services.JobHandleManager;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hyeon-sik Jung
 *
 */
@Component
public class UpdateAssetUpdateScheduler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JobHandleManager jobHandleManager;
	private Dao dao;

	public UpdateAssetUpdateScheduler(Dao dao, JobHandleManager jobHandleManager) {
		this.dao = dao;
		this.jobHandleManager = jobHandleManager;
	}

	@Scheduled(cron = "${smartguard.v3.schedule.get}")
	public void updateAssetInfomation(){
		try {

			String day = DateUtil.getFormatString("d");
			String month = DateUtil.getFormatString("M");
			List<GetReq> queueJob = new ArrayList<>();
			List<BatchGet> batchGetS = (List<BatchGet>) dao.selectBatchGet();

			if (batchGetS != null && batchGetS.size() > 0) {
				for (BatchGet bg : batchGetS) {
					RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(bg.getAssetCd());
					if (runnigJobEntity != null) {
						logger.debug("batchGetS : this information of asset is already running.");
						continue;
					} else {

						// month, year 특정일에따른 스케쥴링 기능
						GetReq getReq = new GetReq();
						getReq.setJobType(ManagerJobType.AJ200.toString());
						getReq.setAssetCd(bg.getAssetCd());
						getReq.setPersonManagerCd(bg.getManagerCd());  // personManagerCd  (ex. T1sgmanager00000)
						getReq.setPersonManagerNm(bg.getUserNm());
						getReq.setManagerCd(bg.getCreateUserId());     //  user_id (ex. sgmanager)
						if (bg.getGetType().equals("D")) {
							queueJob.add(getReq);
							logger.debug("batchGetS : queueJob is added. <1> - " + getReq.getAssetCd());
						} else if (bg.getGetType().equals("M")) {
							List<String> dayList = Arrays.asList(StringUtil.split(bg.getGetDayList(), ","));
							if (dayList.contains(day)) {
								queueJob.add(getReq);
								logger.debug("batchGetS : queueJob is added. <2> - " + getReq.getAssetCd());
							}
						} else if (bg.getGetType().equals("Y")) {
							//특정월 기준 로직 추가
							List<String> monthList = Arrays.asList(StringUtil.split(bg.getGetMonthList(), ","));
							if (monthList.contains(month)) {
								List<String> dayList = Arrays.asList(StringUtil.split(bg.getGetDayList(), ","));
								if (dayList.contains(day)) {
									queueJob.add(getReq);
									logger.debug("batchGetS : queueJob is added. <3> - " + getReq.getAssetCd());
								}
							}
						}
					}
				}

				if (queueJob.size() > 0) {
					logger.debug("queueJob size -  <4> " + queueJob.size());
					jobHandleManager.initAgentInfoForBF(queueJob, "", INMEMORYDB.autoGet);
				}
			}
		} catch (Exception e) {
			logger.error("Update AssetUpdate Exception :: {}", e.getMessage(), e.fillInStackTrace());
		}
	}


}
