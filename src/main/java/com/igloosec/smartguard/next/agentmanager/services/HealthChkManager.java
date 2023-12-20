package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.AgentStatus;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by osujin12 on 2016. 3. 4..
 */
@Slf4j
@Service("healthChkManager")
public class HealthChkManager {
	
    @Autowired
    private Dao dao;

	public void updateAgentStatus_v3(String assetCd, String agentCd, String version, Integer status) throws Exception {
		AgentStatus agentStatus = new AgentStatus();
		agentStatus.setAssetCd(assetCd);
		agentStatus.setAgentCd(agentCd);
		agentStatus.setAgentStatus(status);
		dao.insertAgentStatus_v3(agentStatus);

		// Set agent version info
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setAssetCd(assetCd);
		agentInfo.setAgentCd(agentCd);

		String subVer = "";

		if(version.contains("PRODUCT")){
			//1.0.1.PRODUCT
			subVer = version.substring(0, version.indexOf("PRODUCT") - 1);
		}else if(version.contains("201")){
			subVer = version.substring(4, 12);
		}else if(version.contains("rc")){
			subVer = version.substring(0, 5);
		}else{
			subVer = version;
			if (subVer.length() > 10) {
				subVer = version.substring(0, 9);
				log.debug("subver`s length is" + subVer.length());
			}
		}

		log.debug("Agent Version [[ {} ]] , Convert ==> {}",version	,subVer);
		agentInfo.setAgentVersion(subVer);
		agentInfo.setAgentType(2);

		String curAgentVer = dao.selectCurrentAgentVersion(agentInfo.getAgentCd());
		if (!curAgentVer.equals(agentInfo.getAgentVersion())) {
			if (!curAgentVer.trim().equals("20160101")) {
				agentInfo.setAgentOldVersion(curAgentVer);
			}

			try {
				dao.updateAgentVersion(agentInfo);
				dao.updateAssetGetHistoryAgentInfo(agentInfo);
			}catch (Exception e){
				log.error(e.getMessage());
			}
		}
	}
}
