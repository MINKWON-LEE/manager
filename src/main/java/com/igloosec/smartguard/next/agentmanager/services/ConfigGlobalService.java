package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ConfigGlobalService {

	Logger logger = LoggerFactory.getLogger(ConfigGlobalService.class);

	@Autowired
	private Dao dao;

	public void init() {
		try {
			//ConfigGlobal 인스턴스 생성
			List<Map> configList = dao.selectSnetConfigGlobalList();
			ConfigGlobalManager.getInstance(configList, true);
		}catch (Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		}

	}
}
