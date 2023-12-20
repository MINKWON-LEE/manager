/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.UpdateAssetMasterManager.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 17.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.google.common.collect.Lists;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AssetMasterDBEntity;
import com.igloosec.smartguard.next.agentmanager.entity.AssetMasterStatusDBEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;

import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("updateAssetMasterManager")
public class UpdateAssetMasterManager extends AbstractManager {

	@Autowired
	private Dao dao;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Transactional
	public void updateSgwStatus() throws SnetException {
		// AUDIT_CONFIG_SG in --> sgw_regi = 1
		dao.updateAssetMasterSgwRegiIn();

		// AUDIT_CONFIG_SG not in --> sgw_regi =0
		dao.updateAssetMasterSgwRegiNotIn();
		
		logger.debug("updateSgwStatus finished!!");
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void taskUpdateAliveStatus() throws SnetException {
		List<AssetMasterStatusDBEntity> assetMasterList = (List<AssetMasterStatusDBEntity>) dao.selectAssetMasterAll();

		if(assetMasterList.size() > 0){
			int size = (int) Math.ceil(assetMasterList.size() / (double) 5);
			
			List<List<AssetMasterStatusDBEntity>> partitionList = Lists.partition(assetMasterList, size);
			
			// AUDIT_CONFIG_SG에 없을 경우 alive_chk = 0
			dao.updateAssetMasterAliveChkNotIn();
			
			// Multiple thread
			for(List<AssetMasterStatusDBEntity> entity : partitionList){
				// statusTaskManager.setAssetMasterList(entity);
				taskExecutor.execute(new AliveCheckTask(entity));
			}
			
			for (;;) {
				int count = taskExecutor.getActiveCount();
				// System.out.println("Active Threads : " + count);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (count == 0) {
					taskExecutor.shutdown();
					break;
				}
			}
		}
	}

	private class AliveCheckTask implements Runnable {

		private List<AssetMasterStatusDBEntity> assetMasterList;

		public AliveCheckTask(List<AssetMasterStatusDBEntity> assetMasterList) {
			this.assetMasterList = assetMasterList;
		}

		public void run() {
			List<AssetMasterDBEntity> updateList = new ArrayList<>();
			// AUDIT_CONFIG_SG에 있는 경우만 alive check
			for(AssetMasterStatusDBEntity entity : assetMasterList){
				AssetMasterDBEntity updateEntity = new AssetMasterDBEntity();
				
					boolean isAlive = false;
					updateEntity.setAssetCd(entity.getAssetCd());
					
					String ip = entity.getMasterIp();
					List<Integer> ports = new ArrayList<>();
					if(entity.getSsh()!=null)
						ports.add(Integer.parseInt(entity.getSsh()));
					if(entity.getTelnet()!=null)
						ports.add(Integer.parseInt(entity.getTelnet()));
					if(entity.getFtp()!=null)
						ports.add(Integer.parseInt(entity.getFtp()));
					if(entity.getSftp()!=null)
						ports.add(Integer.parseInt(entity.getSftp()));
					if(entity.getWindow()!=null)
						ports.add(Integer.parseInt(entity.getWindow()));
					for(Integer port : ports){
						if(CommonUtils.isOpenPort(ip, port)){
							isAlive=true;
							break;
						}
					}
					
					if(isAlive){
						if(entity.getAliveChk()!= 1){
							updateEntity.setAliveChk(1);
							updateList.add(updateEntity);
							logger.debug("is Alive Check asset_cd :: {} , check :: {}", entity.getAssetCd(), isAlive);
						}
					}
					else{
						if(entity.getAliveChk()!=0){
							updateEntity.setAliveChk(0);
							updateList.add(updateEntity);
							logger.debug("is Alive Check asset_cd :: {} , check :: {}", entity.getAssetCd(), isAlive);
						}
					}
			}
			try {
				dao.updateAssetMasterAliveChk(updateList);
			} catch (SnetException e) {
				e.printStackTrace();
			}
		}
	}
}