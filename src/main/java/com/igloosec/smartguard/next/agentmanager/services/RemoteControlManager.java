package com.igloosec.smartguard.next.agentmanager.services;

import com.google.gson.Gson;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.jcraft.jsch.JSchException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

@Service
public class RemoteControlManager extends AbstractManager {

	@Autowired
    private ConnectionManager connectionManager;
	
	public boolean runRelayAgentUP(JobEntity jobEntity) throws Exception {

		try {
			logger.debug("Run Relay Agent Send");
			String cmds = INMEMORYDB.absolutePath(INMEMORYDB.RUN_RELAY_AGENT_SCRIPT, jobEntity.getAgentInfo().getOsType());
			return doRunUpDown_NoSU(jobEntity,cmds);
		}
	     catch (Exception e) {
		logger.error(CommonUtils.printError(e));
		throw new Exception(e.getMessage());
	}
			
	}
	
	public boolean runRelayAgentDOWN(JobEntity jobEntity) throws Exception {

		try {
			logger.debug("Run Relay Agent Send");
			String cmds = INMEMORYDB.absolutePath(INMEMORYDB.RUN_RELAY_AGENT_SCRIPT, jobEntity.getAgentInfo().getOsType());
			return doRunUpDown_NoSU(jobEntity,cmds);
		}
	     catch (Exception e) {
		logger.error(CommonUtils.printError(e));
		throw new Exception(e.getMessage());
	}
			
	}

	public boolean runAgentUP(JobEntity jobEntity) throws Exception {

		try {
			logger.debug("Run Agent Send");
			String cmds = INMEMORYDB.absolutePath(INMEMORYDB.RUN_AGENT_SCRIPT, jobEntity.getAgentInfo().getOsType());
			return doRunUpDown(jobEntity,cmds);
		}
	     catch (Exception e) {
		logger.error(CommonUtils.printError(e));
		throw new Exception(e.getMessage());
	}
			
	}
	
	//TO RUN Relay Module with Access Account --CURRENTLY Root Account Run
	public boolean doRunUpDown_NoSU(JobEntity jobEntity, String cmds) throws Exception {
		boolean isJobDone = false;
		String inStream = "";
		
		try {
			
			AgentInfo ai = jobEntity.getAgentInfo();
			String accessType = ai.getChannelType();

			/**
			 * channelType = ""; 'S' = 'SSH & SFTP', 'TF' = 'TELNET & FTP' , 'T' =
			 * 'TELNET' only , 'F' = 'FTP' only
			 **/

			logger.debug("accessType :: " + accessType);

			
			logger.debug("cmds : " + cmds);

			//Send Execute Command VIA TELNET or SSH  && read Input And Save into A file.
			if (accessType.startsWith("S")) {
				
				inStream = connectionManager.runRemoteScriptViaSSH(ai,cmds);
				logger.debug(inStream);

				isJobDone = !(inStream.contains("[CONNECTION_FAILURE]") || inStream.contains("[COMMAND_FAILURE]"));
				
			}
			else if(accessType.startsWith("T")){
				inStream = connectionManager.runRemoteScriptViaTelnet(ai, cmds);
				logger.debug(inStream);
				isJobDone = !(inStream.contains("FAILURE"));

			}else{
				logger.debug("CANNOT CONNECT TELNET SERVICE.");
			}

			if (!isJobDone){
				throw new Exception(inStream);
			}

			//TODO: save to File inStream

			return isJobDone;
		} catch (Exception e) {
			logger.error(CommonUtils.printError(e));
			throw new Exception("접속이 실패하였습니다.(접속정보를 확인해 주세요.) 사유 : "+inStream);
		}
				
	}
	
	
	
	
	
	public boolean doRunUpDown(JobEntity jobEntity , String cmds) throws Exception {
		boolean isJobDone = false;
		String inStream = "";

		try {
			logger.debug("Run Process Send");

			AgentInfo ai = jobEntity.getAgentInfo();
			String accessType = ai.getChannelType();

			/**
			 * channelType = ""; 'S' = 'SSH & SFTP', 'TF' = 'TELNET & FTP' , 'T' =
			 * 'TELNET' only , 'F' = 'FTP' only
			 **/

			logger.debug("accessType :: " + accessType);

			logger.debug("cmds : " + cmds);

			//Send Execute Command VIA TELNET or SSH  && read Input And Save into A file.
			if (accessType.startsWith("S")) {
				
				inStream = connectionManager.runRemoteScriptViaSSH_Su(ai,cmds);
				logger.debug(inStream);

				isJobDone = !(inStream.contains("[CONNECTION_FAILURE]") || inStream.contains("[COMMAND_FAILURE]"));
				
			}
			else if(accessType.startsWith("T")){
				inStream = connectionManager.runRemoteScriptViaTelnet_Su(ai,ai.getUserIdRoot(),ai.getPasswordRoot(), cmds);
				isJobDone = !(inStream.contains("FAILURE"));
				logger.debug(inStream);

			}else{
				logger.debug("CANNOT CONNECT TELNET SERVICE.");
			}

			if (!isJobDone){
				throw new Exception(inStream);
			}

			//TODO: save to File inStream

			return isJobDone;
		} catch (Exception e) {
			logger.error(CommonUtils.printError(e));
			throw new Exception("접속이 실패하였습니다.(접속정보를 확인해 주세요.) 사유 : "+inStream);
//			throw new Exception("AgentUP Fail, socket is not established");
		}
	}

	public boolean runProcessUP(JobEntity jobEntity) throws Exception {
		boolean isJobDone = false;
		String inStream = "";

		try {
			logger.debug("Run Process Send");


			AgentInfo ai = jobEntity.getAgentInfo();
			String accessType = ai.getChannelType();

			/**
			 * channelType = ""; 'S' = 'SSH & SFTP', 'TF' = 'TELNET & FTP' , 'T' =
			 * 'TELNET' only , 'F' = 'FTP' only
			 **/

			logger.debug("accessType :: " + accessType);


			String script = "";
			String langSet = "LANG=C;export LANG;cd /usr/local/snet/agent/bin;";
			if(jobEntity.getJobType().equals(ManagerJobFactory.RELAYUP)){
				script =  langSet+INMEMORYDB.RUN_RELAY_SCRIPT;
			}else if(jobEntity.getJobType().equals(ManagerJobFactory.AGNTUP2)){
				script =  langSet+INMEMORYDB.RUN_AGENT2_SCRIPT;
			} else if(jobEntity.getJobType().equals(ManagerJobFactory.AGNTUP)){
				script = langSet+INMEMORYDB.RUN_AGENT_SCRIPT;
			} else if (jobEntity.getJobType().equals(ManagerJobFactory.KILLAGENT)){
				script = langSet+INMEMORYDB.KILL_AGENT_SCRIPT;
			}

			HashMap<String, String> uploadResult = null;
			String uploadPath = "relay/";
			if (jobEntity.getRelay2AgentInfo() != null){
				Gson gson = new Gson();
				String jobEntityJson = gson.toJson(jobEntity);
				BufferedWriter out = new BufferedWriter(new FileWriter("/usr/local/snetManager/manager/libs/" + jobEntity.getAssetCd() + ".json"));
				out.write(jobEntityJson);
				out.close();
				File[] files = new File[1];
				files[0] = new File("/usr/local/snetManager/manager/libs/"  + jobEntity.getAssetCd() + ".json");
				
	    		uploadResult = connectionManager.uploadViaSFTP_Proxy(jobEntity.getRelay2AgentInfo(),jobEntity.getRelayAgentInfo(), uploadPath, files);

	    		CommonUtils.deleteFile(files[0].getAbsolutePath());
	    		
	    		if(!"true".equalsIgnoreCase(uploadResult.get("RESULT"))){
					INMEMORYDB.deleteShFile(jobEntity, INMEMORYDB.SEND);
					throw new JSchException(uploadResult.get("ERROR"));
				}
			}
		    
			String cmds = INMEMORYDB.absolutePath(script, jobEntity.getAgentInfo().getOsType());
			logger.debug("cmds : " + cmds);

			//Send Execute Command VIA TELNET or SSH  && read Input And Save into A file.
			if (accessType.startsWith("S")) {
				
				inStream = connectionManager.runRemoteScriptViaSSH_Su(ai,cmds);
				logger.debug(inStream);

				isJobDone = !(inStream.contains("[CONNECTION_FAILURE]") || inStream.contains("[COMMAND_FAILURE]"));
				
			}
			else if(accessType.startsWith("T")){
				inStream = connectionManager.runRemoteScriptViaTelnet_Su(ai,ai.getUserIdRoot(),ai.getPasswordRoot(), cmds);
				logger.debug(inStream);
				isJobDone = !(inStream.contains("FAILURE"));

			}else if(accessType.startsWith("P")){
			  	if (jobEntity.getRelay2AgentInfo() != null){
			  		ai = jobEntity.getRelay2AgentInfo();
			  		cmds = "cd "+uploadPath+";touch /usr/local/snet/agent/knownhosts;/usr/local/snet/agent/jre/bin/java -cp " + uploadPath + "/usr/local/snet/agent/libs/SupportMultiRelay.jar:/usr/local/snet/agent/libs/* com.igloosec.smartguard.multirelay.SupportMain " + jobEntity.getAssetCd() + ".json " + uploadPath;
			  	}
				//relay 서버 접속 할 때
				if (ai.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
					inStream = connectionManager.runRemoteScriptViaSSH(ai, cmds);
				} else{
					inStream = connectionManager.runRemoteScriptViaSSH_Su_sshToolsProxy(ai,jobEntity.getRelayAgentInfo(), cmds);
				}
				isJobDone = !(inStream.contains("[CONNECTION_FAILURE]") || inStream.contains("[COMMAND_FAILURE]"));
			}else{
				logger.debug("CANNOT CONNECT TELNET SERVICE.");
			}

			if (!isJobDone){
				throw new Exception(inStream);
			}

			//TODO: save to File inStream

			return isJobDone;
		} catch (Exception e) {
			logger.error(CommonUtils.printError(e));
			throw new Exception("접속이 실패하였습니다.(접속정보를 확인해 주세요.) 사유 : "+inStream);
//			throw new Exception("AgentUP Fail, socket is not established");
		}
	}
	
	
	
}
