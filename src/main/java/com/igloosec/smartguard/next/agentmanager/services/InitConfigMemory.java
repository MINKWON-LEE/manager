/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 4. 12.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;

import com.sk.snet.manipulates.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hyeon-sik Jung
 *
 */
@Slf4j
@Service
@Transactional
public class InitConfigMemory {

	private Dao dao;
	private ConfigGlobalService configGlobalService;

	public InitConfigMemory(Dao dao, ConfigGlobalService configGlobalService) {
		this.dao = dao;
		this.configGlobalService = configGlobalService;
	}
	
	public void initMemory(){
		configGlobalService.init();

		log.debug("ConfigGlobal Gov Flag : " +  ConfigGlobalManager.getConfigGlobalValue("DefaultGovFlag"));
	}

	public void resetProperties() {
		String cryptoCheck = ConfigGlobalManager.getConfigGlobalValue("isDiagsCrypto");
		if(cryptoCheck != null){
			if(cryptoCheck.equals("true")){
				EncryptUtil.isCryptoCheck(true);
				log.debug("isCryptoCheck ...... false");
			} else {
				EncryptUtil.isCryptoCheck(true);
				log.debug("isCryptoCheck ...... false");
			}
		} else {
			EncryptUtil.isCryptoCheck(false);
			log.debug("isCryptoCheck ......<default> false");
		}

		String maxDgExec = ConfigGlobalManager.getConfigGlobalValue("MaxDgExec");
		if (maxDgExec != null) {
			INMEMORYDB.maxDGexec = Integer.parseInt(maxDgExec);
			log.debug("current maxDGexec`s size : " + maxDgExec);
		} else {
			INMEMORYDB.maxDGexec = 0;
			log.debug("current maxDGexec`s size <default> : " + INMEMORYDB.maxDGexec);
		}

		String maxDgReq = ConfigGlobalManager.getConfigGlobalValue("MaxDgReq");
		if (maxDgReq != null) {
			INMEMORYDB.maxDGreq = Integer.parseInt(maxDgReq);
			log.debug("current maxDGreq`s size : " + maxDgReq);
		} else {
			INMEMORYDB.maxDGreq = 0;
			log.debug("current maxDGreq`s size <default> : " + INMEMORYDB.maxDGreq);
		}

		String nwRunDg = ConfigGlobalManager.getConfigGlobalValue("NwRunDg");
		if (nwRunDg != null) {
			INMEMORYDB.nwRunDg = nwRunDg;
			log.debug("current nwRunDg : " + nwRunDg);
		} else {
			INMEMORYDB.nwRunDg = "20";
			log.debug("current nwRunDg <default> : " + INMEMORYDB.nwRunDg);
		}

		String dgWaitTime = ConfigGlobalManager.getConfigGlobalValue("DgWaitTime");
		if (dgWaitTime != null) {
			INMEMORYDB.dgWaitTime = dgWaitTime;
			log.debug("current dgWaitTime : " + dgWaitTime);
		} else {
			INMEMORYDB.dgWaitTime = "20";
			log.debug("current dgWaitTime <default> : " + INMEMORYDB.dgWaitTime);
		}

		String agentCPUMax = ConfigGlobalManager.getConfigGlobalValue("AgentCPUMax");
		if (agentCPUMax != null) {
			INMEMORYDB.agentCPUMax = agentCPUMax;
			log.debug("current agentCPUMax : " + agentCPUMax);
		} else {
			INMEMORYDB.agentCPUMax = "95";
			log.debug("current agentCPUMax <default> : " + INMEMORYDB.agentCPUMax);
		}

		String agentMemoryMax = ConfigGlobalManager.getConfigGlobalValue("AgentMemoryMax");
		if (agentMemoryMax != null) {
			INMEMORYDB.agentMemoryMax = agentMemoryMax;
			log.debug("current agentMemoryMax : " + agentMemoryMax);
		} else {
			INMEMORYDB.agentMemoryMax = "95";
			log.debug("current agentMemoryMax <default> : " + INMEMORYDB.agentMemoryMax);
		}

		String agentJobCheckCnt = ConfigGlobalManager.getConfigGlobalValue("AgentJobCheckCnt");
		if (agentJobCheckCnt != null) {
			INMEMORYDB.agentJobCheckCnt = Integer.parseInt(agentJobCheckCnt);
			log.debug("current agentJobCheckCnt : " + agentJobCheckCnt);
		} else {
			INMEMORYDB.agentJobCheckCnt = 3;
			log.debug("current agentJobCheckCnt <default> : " + INMEMORYDB.agentJobCheckCnt);
		}

		String jobReqTime = ConfigGlobalManager.getConfigGlobalValue("JobReqTime");
		if (jobReqTime != null) {
			INMEMORYDB.DefaultJobSchedule = Integer.parseInt(jobReqTime) * 1000 * 60;
			log.debug("current defaultJobSchedule : " + jobReqTime);
		} else {
			INMEMORYDB.DefaultJobSchedule = 1000*60*5;
			log.debug("current defaultJobSchedule <default> : " + INMEMORYDB.DefaultJobSchedule);
		}

		String emptyJobReqTime = ConfigGlobalManager.getConfigGlobalValue("EmptyJobReqTime");
		if (emptyJobReqTime != null) {
			INMEMORYDB.EmptyJobSchedule = Integer.parseInt(emptyJobReqTime) * 1000 * 60;
			log.debug("current emptyjobReqTime : " + emptyJobReqTime);
		} else {
			INMEMORYDB.EmptyJobSchedule = 1000*60*60;
			log.debug("current emptyjobReqTime <default> : " + INMEMORYDB.EmptyJobSchedule);
		}

		String fastDelayTime = ConfigGlobalManager.getConfigGlobalValue("FastDelayTime");
		if (fastDelayTime != null) {
			INMEMORYDB.fastDelayTime = Integer.parseInt(fastDelayTime) * 1000 * 60;
			log.debug("current fastDelayTime : " + fastDelayTime);
		} else {
			INMEMORYDB.fastDelayTime = INMEMORYDB.DefaultJobSchedule;
			log.debug("current fastDelayTime <default> : " + INMEMORYDB.fastDelayTime);
		}

		String fastDelayUse = ConfigGlobalManager.getConfigGlobalValue("FastDelayUse");
		if(StringUtils.isNotEmpty(fastDelayUse)) {
			if(!fastDelayUse.toLowerCase().equals("0")){
				INMEMORYDB.fastDelayUse = true;
				log.debug("fastDelayUse ...... true");
			} else {
				INMEMORYDB.fastDelayUse = false;
				log.debug("fastDelayUse ...... false");
			}
		} else {
			INMEMORYDB.fastDelayUse = false;
			log.debug("fastDelayUse ...... <default> false");
		}

		String agentResourceUse = ConfigGlobalManager.getConfigGlobalValue("AgentResourceUse");
		if(StringUtils.isNotEmpty(agentResourceUse)) {
			if(!agentResourceUse.toLowerCase().equals("0")){
				INMEMORYDB.agentResourceUse = true;
				log.debug("agentResourceUse ...... true");
			} else {
				INMEMORYDB.agentResourceUse = false;
				log.debug("agentResourceUse ...... false");
			}
		} else {
			INMEMORYDB.agentResourceUse = false;
			log.debug("agentResourceUse ...... <default> false");
		}

		String agentResourceTime = ConfigGlobalManager.getConfigGlobalValue("AgentResourceTime");
		if (agentResourceTime != null) {
			INMEMORYDB.agentResourceTime = Integer.parseInt(agentResourceTime);
			log.debug("current agentResourceTIme : " + fastDelayTime);
		} else {
			INMEMORYDB.agentResourceTime = 5;
			log.debug("current agentResourceTIme <default> : " + INMEMORYDB.agentResourceTime);
		}

		String execWaitTime = ConfigGlobalManager.getConfigGlobalValue("ExecWaitTime");
		if (execWaitTime != null) {
			INMEMORYDB.EXEC_WAIT_TIME = execWaitTime;
			log.debug("current EXEC_WAIT_TIME : " + execWaitTime);
		} else {
			INMEMORYDB.EXEC_WAIT_TIME = "5";
			log.debug("current EXEC_WAIT_TIME <default> : " + INMEMORYDB.EXEC_WAIT_TIME);
		}

		String diagInfoNotUse = ConfigGlobalManager.getConfigGlobalValue("DiagInfoNotUse");
		if(StringUtils.isNotEmpty(diagInfoNotUse)) {
			if(!diagInfoNotUse.toLowerCase().equals("0")){
				INMEMORYDB.diagInfoNotUse = true;
				log.debug("diagInfoNotUse ...... true");
			} else {
				INMEMORYDB.diagInfoNotUse = false;
				log.debug("diagInfoNotUse ...... false");
			}
		} else {
			INMEMORYDB.diagInfoNotUse = false;
			log.debug("diagInfoNotUse ...... <default> false");
		}

		String useLog4JChecker = ConfigGlobalManager.getConfigGlobalValue("UseLog4JChecker");
		if(StringUtils.isNotEmpty(useLog4JChecker)) {
			if(!useLog4JChecker.toLowerCase().equals("0")){
				INMEMORYDB.useLog4JChecker = true;
				log.debug("useLog4JChecker ...... true");
			} else {
				INMEMORYDB.useLog4JChecker = false;
				log.debug("useLog4JChecker ...... false");
			}
		} else {
			INMEMORYDB.useLog4JChecker = false;
			log.debug("useLog4JChecker ...... <default> false");
		}

		String useLog4JgetCnt = ConfigGlobalManager.getConfigGlobalValue("UseLog4JgetCnt");
		if(StringUtils.isNotEmpty(useLog4JgetCnt)) {
			INMEMORYDB.useLog4JgetCnt = Integer.parseInt(useLog4JgetCnt);
			log.debug("useLog4JgetCnt ...... " + useLog4JgetCnt);
		} else {
			INMEMORYDB.useLog4JgetCnt = 0;
			log.debug("useLog4JgetCnt ...... <default> 0");
		}

		String useLog4JFile = ConfigGlobalManager.getConfigGlobalValue("UseLog4JFile");
		if(StringUtils.isNotEmpty(useLog4JFile)) {
			if(!useLog4JFile.toLowerCase().equals("0")){
				INMEMORYDB.useLog4JFile = true;
				log.debug("useLog4JFile ...... true");
			} else {
				INMEMORYDB.useLog4JFile = false;
				log.debug("useLog4JFile ...... false");
			}
		} else {
			INMEMORYDB.useLog4JFile = false;
			log.debug("useLog4JFile ...... <default> false");
		}

		String useLog4JPath = ConfigGlobalManager.getConfigGlobalValue("UseLog4JPath");
		if(StringUtils.isNotEmpty(useLog4JPath)) {
			INMEMORYDB.useLog4JPath = useLog4JPath;
			log.debug("useLog4JPath ...... " + useLog4JPath);
		} else {
			INMEMORYDB.useLog4JPath = "";
			log.debug("useLog4JPath ..... Empty");
		}

		String GetOptUse = ConfigGlobalManager.getConfigGlobalValue("GetOptUse");
		if(StringUtils.isNotEmpty(GetOptUse) && !GetOptUse.equals("0") && !GetOptUse.toLowerCase().equals("n")) {
			INMEMORYDB.GetOptUse = 1;
			log.debug("GetHddUse ...... " + GetOptUse);
		} else {
			INMEMORYDB.GetOptUse = 0;
			log.debug("GetHddUse ..... Empty");
		}

		String GetDisableFunc = ConfigGlobalManager.getConfigGlobalValue("GetDisableFunc");
		if(StringUtils.isNotEmpty(GetDisableFunc)) {
			INMEMORYDB.GetDisableFunc = GetDisableFunc;
			log.debug("GetDisableFunc ...... " + GetDisableFunc);
		} else {
			INMEMORYDB.GetDisableFunc = "";
			log.debug("GetDisableFunc ..... Empty");
		}

		String useDiagOption = ConfigGlobalManager.getConfigGlobalValue("DiagOption");
		if(StringUtils.isNotEmpty(useDiagOption) && !useDiagOption.equals("0") && !useDiagOption.toLowerCase().equals("n")) {
			INMEMORYDB.useDiagOption = true;
			log.debug("useDiagOption ...... " + useDiagOption);
		} else {
			INMEMORYDB.useDiagOption = false;
			log.debug("useDiagOption ..... Empty");
		}
	}
}
