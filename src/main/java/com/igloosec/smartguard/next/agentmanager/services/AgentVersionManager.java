package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.SocketClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("agentVersionManager")
public class AgentVersionManager {

	private DeployManager deployManager;
	private Dao dao;

	public AgentVersionManager(DeployManager deployManager, Dao dao) {
		this.dao = dao;
		this.deployManager = deployManager;
	}

	/**
	 * @return : 시스템 설정 값을 확인하여 에이전트 자동 업데이트 시간 체크 여부
	 * @Date : 2018.1.03
	 * @version : 1.0
	 * @author : LeeSangJun ( jun927@igloosec.com )
	 */
	public boolean checkTimeUpdateAgent() {
		try {

			log.debug("checkTimeUpdateAgent !!");
			String sAgentUpdateMin = "";
			String sAgentUpdateSec = "";
			String eAgentUpdateMin = "";
			String eAgentUpdateSec = "";
			String agentUpdateUse = "";

			sAgentUpdateMin = ConfigGlobalManager.getConfigGlobalValue("SAgentUpdateMin");
			sAgentUpdateSec = ConfigGlobalManager.getConfigGlobalValue("SAgentUpdateSec");
			eAgentUpdateMin = ConfigGlobalManager.getConfigGlobalValue("EAgentUpdateMin");
			eAgentUpdateSec = ConfigGlobalManager.getConfigGlobalValue("EAgentUpdateSec");
			agentUpdateUse = ConfigGlobalManager.getConfigGlobalValue("AgentUpdateUse");

			//에이전트 업데이트 사용 여부 체크 로직 추가
			//2018.08.28
			if(agentUpdateUse != null){
				if(agentUpdateUse.equals("0")){
					log.debug("Do not use agent updates.");
					return false;
				}
			}else{
				log.debug("Do not use agent updates.");
				return false;
			}

			if (sAgentUpdateMin == null || sAgentUpdateSec == null || eAgentUpdateMin == null || eAgentUpdateSec == null) {
				return false;
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
			Date currentTime = new Date(System.currentTimeMillis());

			int sAgentUpdateTime = Integer.parseInt(sAgentUpdateMin + sAgentUpdateSec + "00");
			int eAgentUpdateTime = Integer.parseInt(eAgentUpdateMin + eAgentUpdateSec + "00");
			int CurrentAgentTime = Integer.parseInt(dateFormat.format(currentTime));

			log.debug("sAgentUpdateTime : {} , eAgentUpdateTime : {}  , CurrentAgentTime : {} ", sAgentUpdateTime, eAgentUpdateTime, CurrentAgentTime);

			if (CurrentAgentTime > sAgentUpdateTime) {
				if (eAgentUpdateTime > CurrentAgentTime) {
					log.debug("The set update time..");
					return true;
				}
			}
		} catch (Exception e) {
			log.error(CommonUtils.printError(e));
		}
		return false;
	}

	public File doUpateAgent_v3(AgentInfo ai) {

		File patchFile = null;
		if (checkTimeUpdateAgent()) {
			log.debug("Start Send Latest AGENT.");
			patchFile = deployManager.getOutboundUpdateFiles(ai);
			log.debug("sending.....");
		} else {
			log.debug("Update time is Not..");
		}

		return patchFile;
	}

	public File doForcedUpdateAgent_v3(AgentInfo ai) {

		log.debug("Start Send Latest AGENT.");

		return deployManager.getOutboundUpdateFiles(ai);
	}

	//에이전트 버전 확인
	public boolean chkOldVersion(String verStr) {
		if (!checkTimeUpdateAgent()) {
			log.debug("Update time is Not..");
			return false;
		}

		int lv1, lv2, lv3, lv4;
		int av1, av2, av3, av4;
		verStr = verStr.trim();
		String latestVer = INMEMORYDB.latestAgentVersion;
		log.debug("latestVer:" + latestVer);
		log.debug("verStr:" + verStr);
		String[] lvparsed = latestVer.split("\\.");
		String[] avparsed = verStr.split("\\.");

		if (avparsed.length < 4)
			return true;

//		if (verStr.contains("P") || verStr.contains("p") || verStr.contains("V") || verStr.contains("v")) {
//			return true;
//		}

//		log.debug("L.3.1:" + lvparsed[0] + " L.3.2:" + lvparsed[1] + " L.3.3:" + lvparsed[2]);
//		log.debug("a.3.1:" + avparsed[1] + " a.3.2:" + avparsed[2] + " a.3.3:" + avparsed[3]);

		if (lvparsed[0] != null && lvparsed[1] != null && lvparsed[2] != null && lvparsed[3] != null
				&& avparsed[0] != null && avparsed[1] != null && avparsed[2] != null && avparsed[3] != null) {
			try {
				lv1 = Integer.parseInt(lvparsed[0]);
				lv2 = Integer.parseInt(lvparsed[1]);
				lv3 = Integer.parseInt(lvparsed[2]);
				lv4 = Integer.parseInt(lvparsed[3]);

				// downgrade를 위해.
				if (lv1 < 2) {
					return true;
				}

				// 에이전트 버전 체크 후 업데이트시 에이전트에서 올라오는 버전 숫자 4자리 미만이면
				// 옛날 에이전트로 보고 업데이트
				// 에이전트는 무조건 버전 네마디 숫자이어야 됨.
				try {
					av1 = Integer.parseInt(avparsed[0]);
					av2 = Integer.parseInt(avparsed[1]);
					av3 = Integer.parseInt(avparsed[2]);
					av4 = Integer.parseInt(avparsed[3]);
				} catch (Exception ex) {
					log.error("agent version is too old :  " +
							" latestVer:" + latestVer + " verStr:" + verStr
							+ "ERR MSG :" + ex.getMessage() + "\n");
					return true;
				}


				if (lv1 > av1) {
					log.debug("lv1>av1");
					return true;
				} else if (lv2 > av2) {
					log.debug("lv2>av2");
					return true;
				} else if (lv3 > av3) {
					log.debug("lv3>av3");
					return true;
				} else if (lv2 < av2) {
					if (av2 > 100000){
						return true;
					}else{
						return false;
					}
				} else if (lv4 > av4) {
					log.debug("lv4>av4");
					return true;
				} else {
					log.info("AGENT VERSION IS UP TO DATE.!\n");
					return false;
				}
			} catch (Exception e) {
				log.error("Version Context ERROR[1]  " +
						" latestVer:" + latestVer + " verStr:" + verStr
						+ "ERR MSG :" + e.getMessage() + "\n");
				return false;
			}

		} else {
			log.error("Version Context Format ERROR[2]  " +
					" latestVer:" + latestVer + " verStr:" + verStr
					+ "\n");

			return true;
		}
	}

	public String getAgentLatestVersionStr(){

		String versionFile = INMEMORYDB.MODULES_VERSION;
		log.debug("MODULES_VERSION File Path : " + versionFile);
		String latestVer = "";


		File file = new File(versionFile);
		if(file.exists()){
			latestVer = readAgentVerFile(file);
		} else {
		    log.debug("MODULES_VERSION File is Not Exist.");
        }

		INMEMORYDB.latestAgentVersion = latestVer;

		return latestVer;
	}

	private String readAgentVerFile(File f){

		String lineStr = "";
		int lineCnt = 0;

		try {
			BufferedReader input = new BufferedReader(new FileReader(f));

			try{
				String line = null;
				while( (line = input.readLine()) != null)
				{
					if(line.startsWith("NeAgent=")){
					    log.debug("MODULES.VERSION : " + line);
						lineStr = line.replaceAll("NeAgent=", "");
						break;
					}
				}
				input.close();
			}catch(IOException ex){
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return lineStr;
	}
	  
	public String doSENDCMDService(JobEntity jobEntity, String msg) throws Exception {
				  
		String ret = null;
		String cmd = CommonUtils.toMux(msg);

		try {

			SocketClient socketClient = new SocketClient(jobEntity);
			socketClient.sendCMD(cmd,jobEntity);

//	                String[] header = socketClient.reciveHeader(jobEntity);
			ret = socketClient.receiveStream(cmd);
			socketClient.closeSocket();

		}catch (Exception e){
			log.error(CommonUtils.printError(e));
//	        throw new Exception(e.getMessage());
//	        throw new Exception(SnetCommonErrCode.ERR_0004.getMessage());
		}
			return ret;
	}
	  	  
	 @SuppressWarnings({ "unused", "resource" })
	public static void main(String args[]){
		  
		  ApplicationContext ctx = new ClassPathXmlApplicationContext(
					"classpath:applicationContext.xml");
		  INMEMORYDB.MONITER_PORT="9877";

		  JobHandleManager jobHandleManager = (JobHandleManager)ctx.getBean("jobHandleManager");
		  AgentVersionManager agentVersionManager = (AgentVersionManager)ctx.getBean("agentVersionManager");
		  
		  String assetCd="";
		  if(args != null && args.length >0){
			  assetCd = args[0];
			  System.out.println("assetCd : "+ assetCd);
		  }
		  
		  
		  AgentInfo ai = null;
			try {
//				ai = jobHandleManager.initAgentInfo(assetCd);
			} catch (Exception e) {
				e.printStackTrace();
				ai = null;
			}
		  
			agentVersionManager.doUpateAgent_v3(ai);
	  }
}
