/**
/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.DeployManager.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 4.
 * description : 
 *  		* Run Initial Get-Script & Parse & DB insert
 *  		* Call PackagingManager to Make Set-up File
 *  		* Transfer Set-up File
 *  		* Execute Set-up script
 *  		* Make SSL Key(KeyTool)
 *  		* Public Key Share
 *  		* OTP Test
 *  		* Agent Process Start(OPTIONAL) 
 *  		* Agent Status Update(To DB)
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.google.gson.Gson;
import com.igloosec.smartguard.next.agentmanager.config.DBConfig;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;

import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.property.AgentContextProperties;
import com.igloosec.smartguard.next.agentmanager.property.FileStorageProperties;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import com.igloosec.smartguard.next.agentmanager.utils.SSHHandler;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

@EnableConfigurationProperties({AgentContextProperties.class, FileStorageProperties.class})
@Service("deployManager")
public class DeployManager extends AbstractManager{

	private Dao dao;
	private JobHandleManager jobHandleManager;
	private ConnectionManager connectionManager;
	private AgentContextProperties agentContextProperties;
	private FileStorageProperties fileStorageProperties;

	public DeployManager(Dao dao, JobHandleManager jobHandleManager,
						 ConnectionManager connectionManager, AgentContextProperties agentContextProperties,
						 FileStorageProperties fileStorageProperties) {
		this.dao = dao;
		this.jobHandleManager = jobHandleManager;
		this.connectionManager = connectionManager;
		this.agentContextProperties = agentContextProperties;
		this.fileStorageProperties = fileStorageProperties;
	}
	
	
	@SuppressWarnings("unused")
	public String chkJreEnv(AgentInfo ai) throws Exception {
		boolean isUploaded = false;
		String retStr = "NOT-TESTED";
		//1.SEND 해당 OS 에 맞는 chkJreEnv.sh 전송
		//2.Stream 으로 결과 접수
		//3.결과에 맞는 INSTALL FILE 선별(getOutboundFiles) 할 수 있도록 OS&BIT Type return
		
		
		File[] files = getOutboundFiles(ai, "CHKJRE");
		
		HashMap<String, String> tResult = transferFile(ai,files);
		
		if(tResult == null || !"true".equalsIgnoreCase(tResult.get("RESULT"))){
			retStr = "NOT-TESTED";
			return retStr;
		}
		
		String cmd = "";
		String sResult = sendExec(ai, cmd);
		
		//verifyJreEnv();
		
		return retStr;
	}
	
	public String sendExec(AgentInfo ai, String cmd){
		String inStream = "";
		String accessType = ai.getChannelType();
		boolean isWin = false;
		if(ai.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) isWin=true;
		try{
		
		if(accessType.startsWith("S") && isWin )
		{
			inStream = connectionManager.runRemoteScriptViaSSH(ai, cmd);
		}
		else
		if(accessType.startsWith("S") && !isWin )	
		{
			inStream = connectionManager.runRemoteScriptViaSSH_Su(ai, cmd);
		}
		else
		if(accessType.startsWith("T") && isWin )	
		{
			inStream = connectionManager.runRemoteScriptViaTelnet(ai, cmd);
		}
		else
		if(accessType.startsWith("T") && !isWin )	
		{
			inStream = connectionManager.runRemoteScriptViaTelnet_Su(ai ,ai.getUserIdRoot(), ai.getPasswordRoot(), cmd);
		}
		else
		{
		logger.info("NO TELNET/SSH SUPPORTED. UNABLE TO TEST JREENV.accessType=["
						+ accessType + "]");	
		}
		
		}catch(Exception e){
			logger.info("ERROR RUNNING JREENV CHECK PROGRAM. UNABLE TO TEST JREENV.accessType=["
					+ accessType + "]");	
			logger.error(e.getMessage());			
		}
		return inStream;
	}
	
	public HashMap<String, String> transferFile(AgentInfo ai , File[] files) throws Exception {
		HashMap<String, String> uploadResult = null;
		String uploadPath = INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR;
		String accessType = ai.getChannelType();
		
		 	if(accessType.endsWith("H") && accessType.length() > 1 )// TELNET & FTP  
		    {				
				uploadResult = connectionManager.uploadViaSFTP(ai, uploadPath , files);
			}
		    else 
		    if(accessType.endsWith("F") && accessType.length() > 1 )// TELNET & FTP
		    {	
				uploadResult = connectionManager.uploadViaFTP(ai, uploadPath, files);
		    }else {
				logger.error("NO FTP/SFTP SUPPORTED. UNABLE TO TEST JREENV. accessType=["
						+ accessType + "]");
			}
		 return uploadResult;
	}
	
	
	public boolean patchJar(AgentInfo ai) throws Exception {
	
		String patchType = "jar";

		return installAgent(ai,null, patchType);
	}
	
	public boolean installAgent(AgentInfo ai) throws Exception {
		return installAgent(ai,null, null);
	}

	public boolean installAgent(AgentInfo ai,AgentInfo relayInfo) throws Exception {
		return installAgent(ai,relayInfo, null);
	}
	
	public boolean installAgent(JobEntity jobEntity) throws Exception {
		return uploadAndRunSh_MultiRelaySupport(jobEntity);
	}

	/**
	 *  uploadPath => FTP Access ID's Home directory/[SETUP_RECV_FILE_AGENT_DIR]
	 *
	 **/	
	public boolean installAgent(AgentInfo ai , AgentInfo relayAgent, String patchType) throws Exception {

		AgentInfo agentInfo = ai;
		String uploadPath = INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR;
		String accessType = agentInfo.getChannelType();
//		String accessType = "PR";
        
		String logStr = "";
		if(patchType == null){
			logStr = "INSTALLATION";
		}
		else{
			logStr = "PATCH";
		}
		
		//PEM
		if("RSA".equals(agentInfo.getLoginType())){
			
		}
		
		
		/**
		 * channelType = ""; 'SH' = 'SSH & SFTP', 'TF' = 'TELNET & FTP' , 'T' =
		 * 'TELNET' only , 'F' = 'FTP' only
		 **/

		switch(accessType){
		case "SH": 
			return uploadAndRunSh_SH(agentInfo,uploadPath,patchType);			
		case "SF": 
			return uploadAndRunSh_SF(agentInfo,uploadPath,patchType);
		case "H": 
			return uploadAndRunSh_SFTP(agentInfo,uploadPath,patchType);
		case "F":
			return uploadAndRunSh_FTP(agentInfo,uploadPath,patchType);
		case "TF": 
			return uploadAndRunSh_TF(agentInfo,uploadPath,patchType);
		case "TH": 
			return uploadAndRunSh_TH(agentInfo,uploadPath,patchType);
		case "PR":
			return uploadAndRunSh_PR(agentInfo,relayAgent,uploadPath,patchType);
		default: 
			agentInfo.setSetupStatus("UNABLE TO PROCESS REMOTE "+logStr+" SINCE FTP/TELENT/SSH SERVICE IS NOT SUPPORTED. accessType=["
					+ accessType + "]");	
			agentInfo.setAgentRegiFlag(3);
			logger.error("UNABLE TO PROCESS REMOTE "+logStr+" SINCE FTP/TELENT/SSH SERVICE IS NOT SUPPORTED. accessType=["
					+ accessType + "]");
			return false;	
		}
	}
		
	public boolean uploadAndRunSh_SH(AgentInfo agentInfo, String uploadPath, String patchType)
			throws Exception {

		boolean isUploaded = uploadAndRunSh_SFTP(agentInfo, uploadPath,patchType);

		if (!isUploaded) {
			return false;
		}

		// verifyPromptViaSSH(agentInfo);

		return runInstallViaSSH(agentInfo);

	}
	
	public boolean uploadAndRunSh_SH_PEM(AgentInfo agentInfo, String uploadPath, String patchType)
			throws Exception {

		boolean isUploaded = uploadAndRunSh_SFTP(agentInfo, uploadPath,patchType);

		if (!isUploaded) {
			return false;
		}

		// verifyPromptViaSSH(agentInfo);

		return runInstallViaSSH(agentInfo);

	}
	
	@SuppressWarnings("unused")
	public boolean uploadAndRunSh_SF(AgentInfo agentInfo, String uploadPath, String patchType) throws Exception {

			boolean isUploaded = uploadAndRunSh_FTP(agentInfo,uploadPath, patchType);
			String lastUploadedPath = "";
			if(!isUploaded){return false;}

			lastUploadedPath = "."+ AgentInfo.slash +INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR+ AgentInfo.slash;

		return runInstallViaSSH(agentInfo);
		}

	public boolean uploadAndRunSh_TH(AgentInfo agentInfo, String uploadPath, String patchType) throws Exception {

		boolean isUploaded = uploadAndRunSh_SFTP(agentInfo,uploadPath,patchType);

		if(!isUploaded){return false;}

		return runInstallViaTelnet(agentInfo);

	}

	public boolean uploadAndRunSh_PR(AgentInfo agentInfo, AgentInfo relayInfo, String uploadPath, String patchType) throws Exception {

		boolean isUploaded = uploadAndRunSh_SFTPProxy(agentInfo,relayInfo,uploadPath,patchType);

		if(!isUploaded){return false;}

		return runInstallViaSSHProxy(agentInfo,relayInfo);

	}
	
	public boolean uploadAndRunSh_MultiRelaySupport(JobEntity jobEntity) throws Exception {

		boolean isUploaded = uploadAndRunSh_SFTPProxy(jobEntity);

		if(!isUploaded){return false;}

		return runSupportMultiRelaySSHProxy(jobEntity);

	}

	void verifyPromptViaSSH(AgentInfo ai) throws Exception {
		String[] prompts = connectionManager.verifyRootPromptViaSSH(ai);
		logger.info("[VERIFIED ROOT PROMPT] : '"+prompts[0]+"'");
		logger.info("[VERIFIED CONNECT PROMPT] : '"+prompts[1]+"'");
		ai.setPromptUserIdOs(prompts[1]);
		ai.setPromptUserIdRoot(prompts[0]);		
		
	}
	void verifyPromptViaTelnet(AgentInfo ai) throws Exception {
		String conPrompt = connectionManager.verifyPromptViaTelnet(ai);
		String rootPrompt = connectionManager.verifyRootPromptViaTelnet(ai);
		logger.info("[VERIFIED ROOT PROMPT] : '"+rootPrompt+"'");
		logger.info("[VERIFIED CONNECT PROMPT] : '"+conPrompt+"'");
		ai.setPromptUserIdOs(conPrompt);
		ai.setPromptUserIdRoot(rootPrompt);
	}
	
	@SuppressWarnings("unused")
	public boolean uploadAndRunSh_TF(AgentInfo agentInfo, String uploadPath, String patchType) throws Exception {
		
		boolean isUploaded = uploadAndRunSh_FTP(agentInfo,uploadPath,patchType);
		String lastUploadedPath = "";
		if(!isUploaded){return false;}
		
//		String homePath = connectionManager.getHomeDirViaTelnet(agentInfo);
//		if(!"".equals(homePath)){
//			lastUploadedPath = homePath+INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR+agentInfo.slash;
//			logger.debug("FIND LAST UPLOADED PATH :"+ lastUploadedPath);
//			agentInfo.setLastUploadedPath(lastUploadedPath);
//		}
		lastUploadedPath = "."+ AgentInfo.slash +INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR+ AgentInfo.slash;
		
		 
//		      verifyPromptViaTelnet(agentInfo);

		return runInstallViaTelnet(agentInfo);
	}
	
	public boolean uploadAndRunSh_SFTP(AgentInfo agentInfo, String uploadPath, String patchType) throws Exception {
		String RESULT = ""; //"true"/"false"
		String UPLOADPATH = ""; //"Uploaded Path"/""
		String ERROR = ""; //"Error message"/""
		
		File[] files = getOutboundFiles(agentInfo, patchType);
		
		HashMap<String, String> resultMap = connectionManager.uploadViaSFTP(agentInfo,uploadPath,files);
		RESULT = resultMap.get("RESULT");
		UPLOADPATH = resultMap.get("UPLOADPATH");
		ERROR = resultMap.get("ERROR");
		
		String errMsg = resultMap.get("ERROR");
		logger.debug("SFTP resultMap.get(ERROR) MSG ::"+errMsg);
		if (errMsg!= null &&errMsg.contains("[SFTP-ERROR]")){
			logger.debug("sftp Login Fail try to Ssh Tools Login.");
			resultMap = connectionManager.uploadViaSFTP_SshTools(agentInfo, uploadPath , files);
			
			RESULT = resultMap.get("RESULT");
			UPLOADPATH = resultMap.get("UPLOADPATH");
			ERROR = resultMap.get("ERROR");
			
		}
		
		if(!"true".equalsIgnoreCase(RESULT)){
			agentInfo.setSetupStatus(ERROR);
			agentInfo.setAgentRegiFlag(3);			
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP FILE IS UPLOADED SUCCESSFULLY VIA SFTP.");	
			agentInfo.setAgentRegiFlag(1);
			agentInfo.setLastUploadedPath(UPLOADPATH);
			agentInfo.setSetupShellFile(files[1].getName());
			return true;
		}
	}

	public boolean uploadAndRunSh_SFTPProxy(AgentInfo agentInfo, AgentInfo relayInfo, String uploadPath, String patchType) throws Exception {
		String RESULT = ""; //"true"/"false"
		String UPLOADPATH = ""; //"Uploaded Path"/""
		String ERROR = ""; //"Error message"/""

		File[] files = getOutboundFiles(agentInfo, patchType);

		HashMap<String, String> resultMap = connectionManager.uploadViaSFTP_Proxy(agentInfo,relayInfo,uploadPath,files);
		RESULT = resultMap.get("RESULT");
		UPLOADPATH = resultMap.get("UPLOADPATH");
		ERROR = resultMap.get("ERROR");

		if(!"true".equalsIgnoreCase(RESULT)){
			agentInfo.setSetupStatus(ERROR);
			agentInfo.setAgentRegiFlag(3);
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP FILE IS UPLOADED SUCCESSFULLY VIA SFTP.");
			agentInfo.setAgentRegiFlag(1);
			agentInfo.setLastUploadedPath(UPLOADPATH);
			agentInfo.setSetupShellFile(files[1].getName());
			return true;
		}
	}
	
	public boolean uploadAndRunSh_SFTPProxy(JobEntity jobEntity) throws Exception {
		String RESULT = ""; //"true"/"false"
		String UPLOADPATH = ""; //"Uploaded Path"/""
		String ERROR = ""; //"Error message"/""
		AgentInfo agentInfo = jobEntity.getRelay2AgentInfo();
		
		File[] files = getOutboundFiles(jobEntity.getAgentInfo(), null);
		File[] newFiles = new File[files.length + 1];
		Gson jobEntityJson = new Gson();
		String jsonStr = jobEntityJson.toJson(jobEntity).toString();
		BufferedWriter out = new BufferedWriter(new FileWriter("/usr/local/snetManager/manager/libs/" + jobEntity.getAssetCd() + ".json"));
		out.write(jsonStr);
		out.close();
		
		for(int i = 0; i < files.length; i++){
			logger.debug("files " + i + " : " + files[i].getName());
		}
		
		newFiles[0] = new File("/usr/local/snetManager/manager/libs/"  + jobEntity.getAssetCd() + ".json");
		for(int i = 0; i < files.length; i++){
			newFiles[i + 1] = files[i];
		}
		
		for(int i = 0; i < newFiles.length; i++){
			logger.debug("newFiles " + i + " : " + newFiles[i].getName());
		}
		
		HashMap<String, String> resultMap = connectionManager.uploadViaSFTP_Proxy(agentInfo,jobEntity.getRelayAgentInfo(),INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR,newFiles);
		RESULT = resultMap.get("RESULT");
		UPLOADPATH = resultMap.get("UPLOADPATH");
		ERROR = resultMap.get("ERROR");
		CommonUtils.deleteFile(newFiles[0].getAbsolutePath());
		
		if(!"true".equalsIgnoreCase(RESULT)){
			agentInfo.setSetupStatus(ERROR);
			agentInfo.setAgentRegiFlag(3);
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP FILE IS UPLOADED SUCCESSFULLY VIA SFTP.");
			agentInfo.setAgentRegiFlag(1);
			agentInfo.setLastUploadedPath(UPLOADPATH);
			agentInfo.setSetupShellFile(files[1].getName());
			return true;
		}
	}

	public boolean uploadAndRunSh_FTP(AgentInfo agentInfo, String uploadPath, String patchType){
		String RESULT = ""; //"true"/"false"
		String UPLOADPATH = ""; //"Uploaded Path"/""
		String ERROR = ""; //"Error message"/""
		
		File[] files = getOutboundFiles(agentInfo, patchType);
				
		HashMap<String, String> resultMap = connectionManager.uploadViaFTP(agentInfo,uploadPath,files);
		RESULT = resultMap.get("RESULT");
		UPLOADPATH = resultMap.get("UPLOADPATH");
		ERROR = resultMap.get("ERROR");

		if(!"true".equalsIgnoreCase(RESULT)){
			agentInfo.setSetupStatus(ERROR);
			agentInfo.setAgentRegiFlag(3);
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP FILE IS UPLOADED SUCCESSFULLY VIA FTP.");	
			agentInfo.setAgentRegiFlag(1);
			agentInfo.setLastUploadedPath(UPLOADPATH);
			agentInfo.setSetupShellFile(files[1].getName());
			return true;
		}	
		
	}
	
	public boolean runInstallViaTelnet(AgentInfo agentInfo) throws Exception {

		String uPath = agentInfo.getLastUploadedPath();
		if (agentInfo.getLastUploadedPath().equals("/" + INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR + "/") && agentInfo.getUserIdOs().equals("root")) {
			uPath = "/" + agentInfo.getUserIdOs() + "/" + INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR + "/";
		}
		String cmds  = "cd "+ uPath +";chmod 775 ./*;"+uPath+agentInfo.getBfaShellFile();
		cmds += ";touch "+ fileStorageProperties.getInitAgentDir() + agentInfo.getAssetCd() + "\n";
		String result = connectionManager.runRemoteScriptViaTelnet_Su(agentInfo,agentInfo.getUserIdRoot(), agentInfo.getPasswordRoot(),cmds);
		
		if(result.toUpperCase().contains("ERROR") || result.toUpperCase().contains("FAILURE")){
			agentInfo.setAgentRegiFlag(3);
			agentInfo.setSetupStatus(result);
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP DONE SUCCESSFULLY VIA TELNET.");	
			agentInfo.setAgentRegiFlag(2);
			return true;
		}
		
	}
	
	public boolean runInstallViaSSH(AgentInfo agentInfo) throws Exception {
		
//		logger.info("Clean Up SetUp Directory.");
//		cleanUpSetupDir(agentInfo);		
		
		String runSh = agentInfo.getSetupShellFile();
		String cmds = "";
		if(agentInfo.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
			cmds = agentInfo.getLastUploadedPath()+"\\"+runSh+" "+agentInfo.getAgentInstallPath()+"\n";
		}
		else{
			if (agentInfo.getUseDiagSudo() != null && agentInfo.getUseDiagSudo().equals("Y")) {
				cmds  = "cd "+agentInfo.getLastUploadedPath()+";chmod 775 ./*;sudo sh ./"+runSh+" " + agentInfo.getUserIdOs() + " " + agentInfo.getAgentInstallPath();
				cmds += ";touch " + fileStorageProperties.getInitAgentDir() + agentInfo.getAssetCd() + "\n";
			} else {
				cmds = "cd " + agentInfo.getLastUploadedPath() + ";chmod 775 ./*;./" + runSh;
				cmds += ";touch " + fileStorageProperties.getInitAgentDir() + agentInfo.getAssetCd() + "\n";
			}
		}

		logger.debug(agentInfo.getConnectIpAddress() + "/" + agentInfo.getAgentInstallPath());
		
		Thread.currentThread();
		//		logger.debug("SLEEPING FOR 3 SEC");
		Thread.sleep(3000);
		
		String result = "";
		result =  connectionManager.runRemoteScriptViaSSH_Su(agentInfo,cmds);
		
		if(result.toUpperCase().contains("[CONNECTION_FAILURE]") || result.toUpperCase().contains("[COMMAND_FAILURE]")){			
			agentInfo.setAgentRegiFlag(3);
			agentInfo.setSetupStatus(result);
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP DONE SUCCESSFULLY VIA SSH.");	
			agentInfo.setAgentRegiFlag(2);
			return true;
		}
		
	}

	public boolean runInstallViaSSHProxy(AgentInfo agentInfo,AgentInfo relayInfo) throws Exception {

		String runSh = agentInfo.getSetupShellFile();
		String cmds = "";
		if(agentInfo.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
			cmds = agentInfo.getLastUploadedPath()+"\\"+runSh+"\n";
		}
		else{
			if (agentInfo.getUseDiagSudo() != null && agentInfo.getUseDiagSudo().equals("Y")) {
				cmds = "cd " + agentInfo.getLastUploadedPath() + ";sudo sh ./" + runSh  + " " + agentInfo.getUserIdOs();
				cmds += ";touch " + fileStorageProperties.getInitAgentDir() + agentInfo.getAssetCd() + "\n";
			} else {
				cmds = "cd " + agentInfo.getLastUploadedPath() + ";./" + runSh + "\n";
				cmds += ";touch " + fileStorageProperties.getInitAgentDir() + agentInfo.getAssetCd() + "\n";
			}
		}

		Thread.currentThread();
		Thread.sleep(3000);

		String result = "";
		result =  connectionManager.runRemoteScriptViaSSH_Su_sshToolsProxy(agentInfo,relayInfo,cmds);

		if(result.toUpperCase().contains("[CONNECTION_FAILURE]") || result.toUpperCase().contains("[COMMAND_FAILURE]")){
			agentInfo.setAgentRegiFlag(3);
			agentInfo.setSetupStatus(result);
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP DONE SUCCESSFULLY VIA SSH.");
			agentInfo.setAgentRegiFlag(2);
			return true;
		}

	}
	
	public boolean runSupportMultiRelaySSHProxy(JobEntity jobEntity) throws Exception {
		AgentInfo agentInfo = jobEntity.getRelay2AgentInfo();
		
		String cmd = "cd "+agentInfo.getLastUploadedPath()+";touch /usr/local/snet/agent/knownhosts;/usr/local/snet/agent/jre/bin/java -cp /usr/local/snet/agent/libs/SupportMultiRelay.jar:/usr/local/snet/agent/libs/* com.igloosec.smartguard.multirelay.SupportMain " + jobEntity.getAssetCd() + ".json " + jobEntity.getRelay2AgentInfo().getLastUploadedPath();

		Thread.currentThread();
		Thread.sleep(3000);

		String result = "";
		result =  connectionManager.runRemoteScriptViaSSH_Su_sshToolsProxy(agentInfo,jobEntity.getRelayAgentInfo(),cmd);
		logger.debug("runSupportMultiRelaySSHProxy result : " + result);
		agentInfo = jobEntity.getAgentInfo();
		if(result.toUpperCase().contains("[CONNECTION_FAILURE]") || result.toUpperCase().contains("[COMMAND_FAILURE]")){
			agentInfo.setAgentRegiFlag(3);
			if (result.length() > 512){
				agentInfo.setSetupStatus("AGENT SETUP FAIL VIA JAVA PROGRAM.");
			}else{
				agentInfo.setSetupStatus(result);
			}
			return false;
		}else{
			agentInfo.setSetupStatus("AGENT SETUP DONE SUCCESSFULLY VIA SSH.");
			agentInfo.setAgentRegiFlag(2);
			return true;
		}
	}

	public boolean cleanUpSetupDir(AgentInfo agentInfo) {
		
		boolean isConnected = false;
		boolean isCleaned = false;
		String errMsg = null;
	
		SSHHandler instance = new SSHHandler(agentInfo);
		errMsg = instance.connect();
		if (errMsg != null && errMsg.contains("UNKNOWNHOST")) {
			logger.debug("RECONNECT BY UNKNOWNHOST EXCEPTION.");
//			instance = new SSHHandler(agentInfo);
			errMsg = instance.connect();
			if (errMsg == null) {
				isConnected = true;
			}
			else{
				logger.debug("SSH CONNECTION FAILURE(2) : "+errMsg);
				agentInfo.setSetupStatus("SSH LOGIN FAILURE(2) : "+errMsg);
				agentInfo.setAgentRegiFlag(3);	
				instance.close();
				isCleaned = false;
			}
		} else if (errMsg == null) {
			isConnected = true;					
		}
		
		String cleanPath = INMEMORYDB.absolutePath(INMEMORYDB.AGENT_ROOT_DIR,
				agentInfo.getOsType());
		
		String trashPath = INMEMORYDB.absolutePath(INMEMORYDB.AGENT_ROOT_TRASH_DIR+ DateUtil.getCurrDateByminute()+"[SLASH]",
				agentInfo.getOsType());
		
		logger.debug("DIR TO BE CLEANED UP : "+cleanPath + " DIR TO BE BACK-UP : "+ trashPath);
		
		try {
			if(isConnected){
			//TrashCan directory create			
			instance.mkdir(agentInfo, trashPath);
			
			if(instance.isDirExist(agentInfo, cleanPath)){
				instance.mvFile(agentInfo, cleanPath, trashPath);				
				instance.mkdir(agentInfo, cleanPath);
			}else{
				instance.mkdir(agentInfo, cleanPath);
			}			
			
			logger.info("SUCCESSFULLY CLEANED UP AGENT DIR :"+cleanPath );
			isCleaned = true;
			
			}
		
		} catch ( Exception e) {
            logger.error("ERROR WHILE CLEAN UP AGENT INSTALL DIR."+ e.getMessage());
			e.printStackTrace();			
			isCleaned = false;
		}
		finally{
			instance.close();
		}
		
		return isCleaned;
	}

	
	/**
	 * 
	 * getOutboundFiles(ai, patchType) 
	 *  
	 * patchType: null-> FULL PACKAGE, jar-> JAR PATCH ONLY 
	 *  
	 * RETURNS:
	 * File[0] = windows64.zip or linux64.tar ... (설치파일)
	 * File[1] = agentsetup.sh or agentsetup.bat (설치 실행파일)
	 **/
	public File[] getOutboundFiles(AgentInfo agentInfo, String patchType) {
		String[] installInfo = null;
		boolean isPatch = false;
		boolean isExtraPatchExist = false;
		
		if(patchType == null){
			installInfo = installFilePath(agentInfo);	
		}else{
		
			installInfo = patchFilePath(agentInfo);
			isPatch = true;
		}
		
		String version = installInfo[0]; //(@Manager)setupFileDir
		String setupFileName = installInfo[1]; //setupFileName
		String setupScriptFileName = installInfo[2]; //setup script File Name
		String extraPatchFileName = "";//Extra setup script File Name
		if(isPatch){
			extraPatchFileName = installInfo[3]; 	
		}
		int agentType = agentInfo.getAgentType(); //1:joonAgent 2:Agent
		
		String setupFilePath = INMEMORYDB.SETUP_FILE_MANAGER_DIR+ version +INMEMORYDB.MANAGER_USE_SLASH;
		if(agentType == 1){
			setupFilePath = INMEMORYDB.SETUP_FILE_MANAGER_JOON_DIR+ version +INMEMORYDB.MANAGER_USE_SLASH;
		}
		logger.debug("?4");
		String osType = agentInfo.getOsType().toUpperCase();
		File[] files;
		File extraZipFile = null;
		logger.debug("?5");
		if(isPatch){
		  extraZipFile = new File(setupFilePath+extraPatchFileName);//Extra setup script File Name
			if(extraZipFile.exists()){
				isExtraPatchExist = true;
				logger.debug("EXTRA PATCH FILE EXISTS. !!!!");
			}
		}
		
		if(osType.contains("WINDOW")){
			if(isExtraPatchExist){
				files = new File[4];
			}else{
				files = new File[3];
			}				
			files[0] = new File(setupFilePath + setupFileName); //SETUP PACKAGE FILE
			files[1] = new File(setupFilePath + setupScriptFileName); //SETUP SCRIPT FILE
			files[2] = new File(setupFilePath + "unzip.exe"); //	TAR.EXE FILE
			if(isExtraPatchExist){files[3] = extraZipFile;} 			
			logger.debug("?6");
		}else{
			if(isExtraPatchExist){
				files = new File[3];
			}else{
				files = new File[2];
			}				
			files[0] = new File(setupFilePath + setupFileName);
			files[1] = new File(setupFilePath + setupScriptFileName);
			if(isExtraPatchExist){files[2] = extraZipFile;}
			
			logger.debug("?7");
		}
		logger.debug("?8");
		
		int sendCnt = 0;
		for(File f :files){
			logger.debug("FILES TO SEND :"+sendCnt+" " +f.getName());	
		}
		
		return files;
	}

	/**
	 * 에이전트 업데이트시 이용.
	 * 3.0 에서 부터는 patchfile.zip 안에
	 * 1. NeAgent.jar, 2. pathches.tar(or .zip), 3. agentsetup_pathJar.sh(or .bat), 4. unzip.exe
	 * 위 3개 파일(unix 1,2,3) 혹은 4개 파일(windows 1,2,3,4)을 스마트가드 최초 설치시에 patchfiles.zip으로 묶어놓고 이 파일만을 내린다.
	 */
	public File getOutboundUpdateFiles(AgentInfo agentInfo) {
		String[] installInfo = null;

		installInfo = patchFilePath(agentInfo);

		String version = installInfo[0]; //(@Manager)setupFileDir
		String patchFileNm = agentContextProperties.getAgentPatchFiles() + INMEMORYDB.ZIP; // patch file list.

		String setupFilePath = INMEMORYDB.SETUP_FILE_MANAGER_DIR + version + INMEMORYDB.MANAGER_USE_SLASH;
		File file = new File(setupFilePath + patchFileNm);//Extra setup script File Name
		if(!file.exists()) {
			logger.debug("PATCH FILE doesn`t EXISTS. !!!!");
			return null;
		}

		return file;
	}

	private String[] patchFilePath(AgentInfo agentInfo) {
		/****
		patchFileInfo[0]= Setup File Path
		patchFileInfo[1]= Setup File Name
		patchFileInfo[2]= Setup Script
		patchFileInfo[3]= patches.zip/patches.tar File to Patch
		****/
		String patchFileInfo[] = new String[4];
		int bitType = agentInfo.getOsBit();
		String osType = agentInfo.getOsType();
		String setupScriptFileName = "agentsetup_patchJar";
		String jarName = "NeAgent.jar";
		String patchFileName = "patches";
		
		String retOsName = "";
		String retBit="";

			if (osType == null || bitType == 0){
				patchFileInfo[0] = "linux64";
				patchFileInfo[1] = jarName;
				patchFileInfo[2] = setupScriptFileName+".sh";
				patchFileInfo[3] = patchFileName+".tar";
				return patchFileInfo;
			}

			if (osType.toUpperCase().contains("WINDOW")){
				retOsName = "window";
			}else if (osType.toUpperCase().contains("SPARC")){
				retOsName = "solarisSPARC";
			}else if (osType.toUpperCase().contains("SOLARIS") && !osType.toUpperCase().contains("SPARC")){
				retOsName = "solaris";
			}else if (osType.toUpperCase().contains("SUN") && !osType.toUpperCase().contains("SPARC")){
				retOsName = "solaris";
			}else if (osType.toUpperCase().contains("LINUX") || osType.toUpperCase().contains("UNIX")){
				retOsName = "linux";
			}else if (osType.toUpperCase().contains("HP") && osType.toUpperCase().contains("UX")){
				retOsName = "hpux";
			}else if (osType.toUpperCase().contains("AIX")){
				retOsName = "aix";
			}else if (osType.toUpperCase().contains("FREEBSD")){
				retOsName = "linux";
			}else if (osType.toUpperCase().contains("ALPINE")){
				retOsName = "alpine";
			}else{
				logger.error("OS TYPE NOT FOUND.!  TRY INSTALLATION WITH linux VERSION.");
				retOsName = "linux";
			}
			
	
			if (bitType == 2){
				retBit="64";
			}else{
				retBit="32";
			}
			
			patchFileInfo[0] = retOsName+retBit;
			patchFileInfo[1] = jarName;
			
			if(osType.toUpperCase().contains("WINDOW")){				
				patchFileInfo[2] = setupScriptFileName+".bat";
				patchFileInfo[3] = patchFileName+".zip";
			}else{				
				patchFileInfo[2] = setupScriptFileName+".sh";
				patchFileInfo[3] = patchFileName+".tar";
			}

		return patchFileInfo;

	}
	
	
	/**
	 * installFileInfo[] installFileInfo[0] = Setup File Path
	 * installFileInfo[1] = Setup FileName  
	 * installFileInfo[2] = Setup Script file Name
	 * installFileInfo[3] = ONLY FOR WINDOW TAR.EXE
	 * **/
	private String[] installFilePath(AgentInfo agentInfo) {
		String installFileInfo[] = new String[3];
		int bitType = agentInfo.getOsBit();
		String osType = agentInfo.getOsType();
		String setupScriptFileName = INMEMORYDB.AGENT_SETUP_SCRIPT_FILENAME;
		String retOsName = "";
		String retBit="";

			if (osType == null || bitType == 0){
				installFileInfo[0] = "linux64";
				installFileInfo[1] = installFileInfo[0]+".tar";
				installFileInfo[2] = setupScriptFileName+".sh";
				return installFileInfo;
			}

			if (osType.toUpperCase().contains("WINDOW")){
				retOsName = "window";
			}else if (osType.toUpperCase().contains("SPARC")){
				retOsName = "solarisSPARC";
			}else if (osType.toUpperCase().contains("SOLARIS") && !osType.toUpperCase().contains("SPARC")){
				retOsName = "solaris";
			}else if (osType.toUpperCase().contains("SUN") && !osType.toUpperCase().contains("SPARC")){
				retOsName = "solaris";
			}else if (osType.toUpperCase().contains("LINUX") || osType.toUpperCase().contains("UNIX")){
				retOsName = "linux";
			}else if (osType.toUpperCase().contains("HP") && osType.toUpperCase().contains("UX")){
				retOsName = "hpux";
			}else if (osType.toUpperCase().contains("AIX")){
				retOsName = "aix";
			}else if (osType.toUpperCase().contains("FREEBSD")){
				retOsName = "linux";
			}else if (osType.toUpperCase().contains("SVOS")){
				retOsName = "linux";
			}else if (osType.toUpperCase().contains("ALPINE")){
				retOsName = "alpine";
			}else{
				logger.error("OS TYPE NOT FOUND.!  TRY INSTALLATION WITH linux VERSION.");
				retOsName = "linux";
			}
			
	
			if (bitType == 2){
				retBit="64";
			}else{
				retBit="32";
			}
			
			   installFileInfo[0] = retOsName+retBit;
			
			if(osType.toUpperCase().contains("WINDOW")){
				installFileInfo[1] = installFileInfo[0]+".zip";
				installFileInfo[2] = setupScriptFileName+".bat";
			}else{
				installFileInfo[1] = installFileInfo[0]+".tar";
				installFileInfo[2] = setupScriptFileName+".sh";
			}
						
		return installFileInfo;

	}


	public static void main(String args[]) throws Exception {
		
		
//		@SuppressWarnings("resource")
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
//				"applicationContext.xml");

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(AgentContextProperties.class, FileStorageProperties.class, Dao.class, DBConfig.class,
				ConnectionManager.class, RemoteControlManager.class, JobHandleManager.class, DeployManager.class);

		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
		yaml.setResources(new ClassPathResource("application.yml"));

		ConfigurableEnvironment environment = ctx.getEnvironment();
		environment.getPropertySources().addFirst(new PropertiesPropertySource("custom", yaml.getObject()));
		ctx.refresh();

		DeployManager dm = (DeployManager)ctx.getBean("deployManager");
		System.out.println(
				"/usr/local/snetManager/java/bin/java -cp AgentManager.jar:./manager/libs/* com.mobigen.snet.agentmanager.services.DeployManager AC10000 150.23.25.131 haauc auc$1234 LINUX 64 root auc$1234 0 23 0 21");
		System.out.println("0:assetCD 1:IP 2:ID 3:pw 4:LINUX 5:bit 6:rootid 7:rootpw 8:sftpPort 9:ftpPort 10:sshPort 11:telnePort");
		String assectCd = "AC151117190527";
		String ip = "150.23.25.131";
		String id = "haauc";
		String pw = "auc$1234";
		String os = "LINUX";
		String bit = "64";
		String rid = "root";
		String rpw = "auc$1234";
		String portSF = "22";
		String portF = "21";
		String portS = "22";
		String portT = "23";
		if(args.length > 0){
			assectCd = args[0];
			ip = args[1];
			id = args[2];
			pw = args[3];
			os = args[4];
			bit = args[5];
			rid = args[6];
			rpw = args[7];
			portSF = args[8];
			portF = args[9];
			portS = args[10];
			portT = args[11];
			System.out.println("0:"+args[0]+" 1:"+args[1]+" 2:"+args[2]+" 3:"+args[3]+" 4:"+args[4]+" 5:"+args[5]+" 6:"+args[6]+" 7:"+args[7]+" 8:"+args[8]+" 9:"+args[9]+" 10:"+args[10]+" 11:"+args[11]);

		}
		System.out.println("ASSETCD : "+ assectCd);




		AgentInfo ai = new AgentInfo();

///HP-UX
//		IP : 150.23.25.131
//
//		haauc/auc$1234
//		root/auc$1234

		ai.setAssetCd(assectCd);
		ai.setConnectIpAddress(ip);
		ai.setUserIdOs(id);
		ai.setPasswordOs(pw);
		ai.setOsType(os);
		ai.setOsBit(Integer.parseInt(bit));
		ai.setUserIdRoot(rid);
		ai.setPasswordRoot(rpw);
		ai.setPortFtp(Integer.parseInt(portF));
		ai.setPortSftp(Integer.parseInt(portSF));
		ai.setPortSsh(Integer.parseInt(portS));
		ai.setPortTelnet(Integer.parseInt(portT));
		ai.setAgentType(2);

		ai.completeAgentInfo();

//		ai.setAssetCd("assetCd");
//		ai.setConnectIpAddress("redrug.co.kr");
//		ai.setUserIdOs("devmaster");
//		ai.setPasswordOs("devmaster2ol2");
//		ai.setOsType("LINUX");
//		ai.setOsBit(64);
//		ai.setPortFtp(21);
//		ai.setPortSftp(22);
//		ai.setPortSsh(22);
//		ai.setPortTelnet(23);
//		ai.setUserIdRoot("root");
//		ai.setPasswordRoot("devhub2oll");

//
//		ai.setAssetCd("assetCd12345");
//		ai.setConnectIpAddress("1.234.87.119");
//		ai.setUserIdOs("administrator");
//		ai.setPasswordOs("skguest2016!");
//		ai.setOsType("WINDOWS"); //LINUX
//		ai.setOsBit(64);
//		ai.setPortFtp(8888);
//		ai.setPortSftp(0);
//		ai.setPortSsh(0);
//		ai.setPortTelnet(0);
//		ai.setUserIdRoot("root");
//		ai.setPasswordRoot("devhub2oll");
//		ai.completeAgentInfo();


//		JobHandleManager jobHandleManager = new JobHandleManager();
//		JobHandleManager jobHandleManagerTest = (JobHandleManager) applicationContext.getBean("jobHandleManager");
//		AgentInfo ai = jobHandleManagerTest.initAgentInfo(assectCd);


		System.out.println("!!!!!  PORT:"+ai.getUsePort() + ", CHANNEL TYPE:" + ai.getChannelType());
		System.out.println("!!!!!  ROOTPASS:"+ai.getPasswordRoot());
//		 SSHHandler instance = new SSHHandler(ai);
//		 instance.connect();



		 ai.setSetupShellFile("agentsetup.sh");
		 ai.setLastUploadedPath("/usr/local/tomcat/setup/");
		 dm.installAgent(ai);

//		 dm.uploadViaFTP(ai, INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR);
		/** upload setup File Test **/
        //dm.uploadViaSFTP(ai, INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR);

		/** install-Script Run Test **/
//         ai.setSetupShellFile("agentsetup.sh");
//         ai.setLastUploadedPath("/usr/local/tomcat/setup/");
//		 try {
//			dm.runInstallViaSSH(ai);
//		} catch (IOException | JSchException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		/** ls - al Test **/
//
//		String dir = INMEMORYDB.absolutePath(INMEMORYDB.AGENT_ROOT_DIR,ai.getOsType());
//		 String result = instance.getList(ai,dir);
//
//


	    /** mkdir Test **/
//		 try {
//		String dir = INMEMORYDB.absolutePath(INMEMORYDB.AGENT_ROOT_DIR,ai.getOsType());
//		 instance.mkdir(ai,"/abc/ddd/");
//
//		 } catch (IOException | JSchException e) {
//		 // TODO Auto-generated catch block
//		 e.printStackTrace();
//		 }

		/** prompt String Test **/
//		 try {
//		 String result = dm.getPromptPhrase(ai.getUserIdRoot(),
//		 ai.getPasswordRoot(), instance, true);
//		 System.out.println("!!!!!!!!!!!!!result :"+result);
//		 } catch (IOException | JSchException e) {
//		 // TODO Auto-generated catch block
//		 e.printStackTrace();
//		 }

		/** Clean directory(ROOT DIR) Test **/
		// System.out.println( "rm -rf "+
		// INMEMORYDB.absolutePath(INMEMORYDB.AGENT_ROOT_DIR, ai.getOsType()));

		// ai.setGetScriptFileName("./files/status2.sh");
		// ai.setId("root");
		// ai.setPw("rootpw123");
		// ai.setIp("127.0.0.1");
		// ai.setPort("22");
		//
		// boolean isUploaded = dm.sendInitialGetScript(ai);
		// if(isUploaded){
		//
		// }

	}

}