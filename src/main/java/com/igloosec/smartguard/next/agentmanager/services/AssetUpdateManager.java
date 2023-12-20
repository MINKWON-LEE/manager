/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.AssetUpdateManager.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 3.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.google.gson.Gson;
import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.GetScriptException;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.memory.NotiType;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import com.igloosec.smartguard.next.agentmanager.utils.SocketClient;
import com.igloosec.smartguard.next.agentmanager.utils.ZipUtil;
import com.jcraft.jsch.JSchException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.HashMap;

@Service
public class AssetUpdateManager extends AbstractManager{
	
	  @Autowired
	  private Dao dao;
	  
	  @Autowired
	  private DataParseManager dataParseManager;
	  
	  @Autowired
      private ConnectionManager connectionManager;

	  @Autowired
	  private OptionProperties optionProperties;

	  public boolean doAssetUpdateBFA(JobEntity jobEntity) throws Exception {
		  boolean isJobDone = false;
		  HashMap<String, String> uploadResult;
		  boolean isUploaded = false;
		  File[] files = null;
		  String uploadPath = INMEMORYDB.GETSCRIPT_RECV_FILE_BF_AGENT_DIR;
		  String accessType = jobEntity.getAgentInfo().getChannelType();

		  try {
			  logger.debug("Start Get Script Send");

			  if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
				  jobEntity.setFileType(INMEMORYDB.JAR);
			  }else {
				  jobEntity.setFileType(INMEMORYDB.SH);
			  }
			  

			  /*************GET SRCRIPT CREATE******************/

			  logger.debug("Start Get Script Send");
			  files = createGetscript_java14(jobEntity);
			  
			  /*************UPLOAD GET FILE******************/			
			  
			    if(accessType.endsWith("H") && accessType.length() > 1 )// SSH & SFTP  
			    {
					uploadResult = connectionManager.uploadViaSFTP(jobEntity.getAgentInfo(), uploadPath , files);
					String errStr = uploadResult.get("ERROR");
					if (errStr != null && errStr.contains("[SFTP-ERROR]")){
						logger.debug("sftp Login Fail try to Ssh Tools Login. BY "+errStr);
						uploadResult = connectionManager.uploadViaSFTP_SshTools(jobEntity.getAgentInfo(), uploadPath , files);
					}
				}
			    else if(accessType.endsWith("F") && accessType.length() > 1 )// TELNET & FTP
			    {	
					uploadResult = connectionManager.uploadViaFTP(jobEntity.getAgentInfo(), uploadPath, files);
			    }else if (accessType.endsWith("R") && accessType.length() > 1){// tunnel 을 사용한 파일전송 
			    	if (jobEntity.getRelay2AgentInfo() != null)
			    		uploadResult = connectionManager.uploadViaSFTP_Proxy(jobEntity.getRelay2AgentInfo(),jobEntity.getRelayAgentInfo(), uploadPath, files);
			    	else 
			    		uploadResult = connectionManager.uploadViaSFTP_Proxy(jobEntity.getAgentInfo(),jobEntity.getRelayAgentInfo(), uploadPath, files);
				} else {
					logger.error("장비에서 FTP/TELENT/SSH 서비스를 사용할 수 없습니다. accessType=["
							+ accessType + "]");
					INMEMORYDB.deleteShFile(jobEntity, INMEMORYDB.SEND);
					throw new Exception("장비에서 FTP/TELENT/SSH 서비스를 사용할 수 없습니다.");
				}
			    
			    if("true".equalsIgnoreCase(uploadResult.get("RESULT"))){
					isUploaded = true;
				}else {
					INMEMORYDB.deleteShFile(jobEntity, INMEMORYDB.SEND);
					throw new JSchException(uploadResult.get("ERROR"));
				}

			  	//Delete Send GetScript File.
			  	INMEMORYDB.deleteShFile(jobEntity, INMEMORYDB.SEND);
			  	CommonUtils.deleteFile(files[0].getAbsolutePath());

				/*************Send Execute Command VIA TELNET or SSH  && read Input And Save into A file.******************/		
			  	String inStream = "";
			  	AgentInfo ai = jobEntity.getAgentInfo();
			  	
			  	if (jobEntity.getRelay2AgentInfo() != null)
			  		ai = jobEntity.getRelay2AgentInfo();
				
//			  	String cmd  = "acnt=0 ;cset=`locale -a|grep -i en_US.UTF` && acnt=1 ; if [ $acnt -eq 1 ]; then c=$cset; export LANG; fi; cd "+ai.getLastUploadedPath()+"; "+ai.getLastUploadedPath()+ai.getBfaShellFile()+" "+jobEntity.getManagerCode()+" "+jobEntity.getAgentInfo().getAssetCd() +"\n";
//			  	String cmd  = "LANG=C;export LANG; cd "+ai.getLastUploadedPath()+"; chmod 755 "+ai.getLastUploadedPath()+ai.getBfaShellFile()+"; "+ai.getLastUploadedPath()+ai.getBfaShellFile()+" "+jobEntity.getManagerCode()+" "+jobEntity.getAgentInfo().getAssetCd() +"\n";
			  	String cmd;
			    if	(ai.getUseSudo().toUpperCase().equals("Y")) {
			        cmd = "cd "+ai.getLastUploadedPath()+";LANG=C;export LANG; chmod 755 "+ai.getLastUploadedPath()+ai.getBfaShellFile()+"; sudo sh "+ai.getLastUploadedPath()+ai.getBfaShellFile()+" "+jobEntity.getManagerCode()+" "+jobEntity.getAgentInfo().getAssetCd() +"\n";
                } else {
			        cmd = "cd "+ai.getLastUploadedPath()+";LANG=C;export LANG; chmod 755 "+ai.getLastUploadedPath()+ai.getBfaShellFile()+"; "+ai.getLastUploadedPath()+ai.getBfaShellFile()+" "+jobEntity.getManagerCode()+" "+jobEntity.getAgentInfo().getAssetCd() +"\n";
                }

				logger.debug(">> CMD : "+cmd);

				if (accessType.startsWith("S")) {
					if(isUploaded){

						if (ai.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
							inStream = connectionManager.runRemoteScriptViaSSH(ai, cmd);
						} else{
							inStream = connectionManager.runRemoteScriptViaSSH_Su(ai, cmd);
						}

						// Get 프로그램 결과를 받기위해 jobType을 GSCRPTFIN으로 변경
						jobEntity.setJobType(ManagerJobType.AJ200.toString());
						jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);
						if(inStream.contains("[CONNECTION_FAILURE]") || inStream.contains("[COMMAND_FAILURE]")|| inStream.contains("[SFTP-")){
							throw new JSchException(inStream);
						}else{
							doGSBFAGENTresult(jobEntity,inStream);
						}
						logger.debug(inStream);
					}else{
						logger.debug(inStream);
						return false;
					}

				}
				else if(accessType.startsWith("T")){
					String lastUploadedPath = "";
					String homePath = connectionManager.getHomeDirViaTelnet(ai);
					if(!"".equals(homePath)){
						lastUploadedPath = homePath+INMEMORYDB.SETUP_RECV_FILE_AGENT_DIR+ AgentInfo.slash;
						logger.debug("FIND LAST UPLOADED PATH :"+ lastUploadedPath);
						ai.setLastUploadedPath(lastUploadedPath);
					}
					//윈도우는 su 명령을 보내지 않기 때문에 구분해준다.
					if (ai.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
						inStream = connectionManager.runRemoteScriptViaTelnet(ai, cmd);
					} else{
						inStream = connectionManager.runRemoteScriptViaTelnet_Su(ai ,ai.getUserIdRoot(), ai.getPasswordRoot(), cmd);
					}
					if(inStream.contains("FAILURE")){
						throw new JSchException(inStream);
					}
					
					// Get 프로그램 결과를 받기위해 jobType을 GSCRPTFIN으로 변경
					jobEntity.setJobType(ManagerJobType.AJ200.toString());
					jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);

					doGSBFAGENTresult(jobEntity,inStream);
				}else if(accessType.startsWith("P")){
					
					// 릴레이 서버를 통해서 java 를 실행하여  파일 전송 및 장비정보수집 스크립트 실행
					if (jobEntity.getRelay2AgentInfo() != null) {
				  		cmd = "cd "+ai.getLastUploadedPath()+";touch /usr/local/snet/agent/knownhosts;/usr/local/snet/agent/jre/bin/java -cp /usr/local/snet/agent/libs/SupportMultiRelay.jar:/usr/local/snet/agent/libs/* com.igloosec.smartguard.multirelay.SupportMain " + jobEntity.getFileName() + ".json " + jobEntity.getRelay2AgentInfo().getLastUploadedPath();
				  		logger.debug(">> CMD : "+cmd); 
				  	}
					
					//relay 서버 접속 할 때
					if (ai.getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
						inStream = connectionManager.runRemoteScriptViaSSH(ai, cmd);
					} else{
						inStream = connectionManager.runRemoteScriptViaSSH_Su_sshToolsProxy(ai,jobEntity.getRelayAgentInfo(), cmd);
					}
					if(inStream.contains("[CONNECTION_FAILURE]") || inStream.contains("[COMMAND_FAILURE]")|| inStream.contains("[SFTP-ERROR]") || inStream.contains("FAILED SWITCHING USER TO ROOT")){
						throw new JSchException(inStream);
					}

					// Get 프로그램 결과를 받기위해 jobType을 GSCRPTFIN으로 변경
					jobEntity.setJobType(ManagerJobType.AJ200.toString());
					jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);

					doGSBFAGENTresult(jobEntity,inStream);
				}
				else{
					logger.debug("CANNOT CONNECT TELNET SERVICE.");
				}

			  return isJobDone;
		  }catch (JSchException e){
			  logger.error(CommonUtils.printError(e));
			  INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
			  INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.SEND);
			  INMEMORYDB.deleteShFile(jobEntity, INMEMORYDB.SEND);

			  throw new Exception(e.getMessage());
		  }catch (SnetException e){
			  INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
			  INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.SEND);
			  INMEMORYDB.deleteShFile(jobEntity, INMEMORYDB.SEND);

			  throw new Exception(e.getMessage());
		  }
	  }

	public String doAssetUpdate_V3(JobEntity jobEntity) throws Exception {

		logger.info("get file information of get script.");

		if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
			jobEntity.setFileType(INMEMORYDB.JAR);
		}else {
			jobEntity.setFileType(INMEMORYDB.SH);
		}

		//Get Script create
		createGetscript_java14_V3(jobEntity);

		/**Make Zip File**/
		CommonUtils.makeZipFile(jobEntity);

		// deprecated 파일 별 해쉬파일 설정
		// make hash value list to be transferred.
		// CommonUtils.makeScriptHashList(jobEntity);
		File[] files = null;
		files = new AESCryptography().encryptionFile(jobEntity);

		if(optionProperties.getDiagnosis().equals("false")) {
			String sendPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType());
			CommonUtils.deleteFile(sendPath + jobEntity.getFileName() + ".class");
			CommonUtils.deleteFile(sendPath + jobEntity.getFileName() + ".jar");
			CommonUtils.deleteFile(sendPath + jobEntity.getFileName() + ".zip");
			CommonUtils.deleteFile(sendPath + jobEntity.getFileName() + INMEMORYDB.DgScriptDisableExt);
			if (INMEMORYDB.useLog4JChecker) {
				INMEMORYDB.deleteLog4JFiles(jobEntity.getSOTP());
			}
		}
		// deprecated 파일 리스트 전송로직 삭제. (AES256고려 안함)
		// 수집파일만 전달.
		// /**Make Zip File**/
		// return CommonUtils.makeZipFileForAES256(jobEntity);

		// 에이전트가 수집스크립트를 정상적으로 다운로드 받았다고 생각하고
		// snet_connect_master의 connect_log를 AN00으로 업데이트
		String msg = NotiType.getStatusLog(ManagerJobType.AJ200.toString(), NotiType.AN001.toString(), "");
		jobEntity.getAgentInfo().setConnectLog(msg + " [" + DateUtil.getCurrDateBySecondFmt() + "]");
		dao.updateConnectMaster(jobEntity);

		return files[0].getAbsolutePath();
	}

	  //수동으 로Get 스크립트 결과를 업로드를 수행하여 받은 결과 파일
	public void recvManualGetscriptResult(String path, JobEntity jobEntity) throws Exception {

		String upladPath = INMEMORYDB.MANUAL_GSR;
		if (path != null && !path.isEmpty()) {
			upladPath += path + File.separatorChar;
		}
		upladPath += jobEntity.getFileName();

		String desPath = "", datPath = "", movePath = "", moveDatPath = "";
		desPath += upladPath + INMEMORYDB.DESRESULTTYPE;
		datPath += upladPath + jobEntity.getFileType();

		movePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType()) + jobEntity.getFileName();
		moveDatPath += movePath + jobEntity.getFileType();

		logger.debug("upladPath : "+upladPath);
		logger.debug("movePath : "+movePath);

		boolean isCopy;
		File file = new File(desPath);
		// 암호화 파일이면 복호화부터....
		if (file.exists()) {
			isCopy = new AESCryptography().decryptionShFile(desPath, moveDatPath);
		} else {
			isCopy = CommonUtils.fileCopy(datPath, moveDatPath);
		}

		if (isCopy){

			try {
				/**
				 * Get Script 결과파일 파싱
				 */
				insertCOTP(jobEntity);

				//file Parsing & validation Check
				dataParseManager.gscriptResult(jobEntity);

			} catch (GetScriptException e) {
				logger.error(CommonUtils.printError(e));
				throw new GetScriptException(e.getMessage());
			} catch (Exception e) {
				logger.error(CommonUtils.printError(e));
				throw new Exception(e.getMessage());
			}

		}else{
			logger.error("File copy Exception !!");
			throw new Exception(SnetCommonErrCode.ERR_0011.getMessage());
		}
	}

	//수동으로 네트워크 장비 get 결과를 업로드하여 받은 결과 파일
	public void recvSRVManualGetScriptResult(JobEntity jobEntity) throws Exception {

		try {
			/**
			 * Get Script 결과파일 파싱
			 */
			insertCOTP(jobEntity);

			//file Parsing & validation Check
			dataParseManager.gscriptResult(jobEntity);

			//ASSET_MASTER , AGENT_MASTER 테이블에 agent_cd 를 저장.
			int agentCdCount = dao.selectAgentMasterCd(jobEntity.getAssetCd());

			if(agentCdCount == 0)
				dao.insertAgentMasterCd(jobEntity.getAssetCd());


			//asset_master 에 agent_cd 업데이트
			dao.updateAgentMasterCd(jobEntity.getAssetCd());

		} catch (Exception e) {
			logger.error("== NW device : "+jobEntity.getAssetCd(), e);
			throw new SnetException(dao, e.getMessage(), jobEntity, "G");
		}

	}

	//Agent로 Get 스크립트룰 수행하여 받은 결과 파일
	public void recvGetscriptResult(JobEntity jobEntity) throws Exception {

		int errorStep = 0;
		logger.debug("Send GSFIN and Receive Get Script result file");
		try {

			File[] f = new File[0];

			errorStep = 1;

			// deprecated (AES256 삭제)
			// des 파일만 전송
			// /**Unzip File**/
			// CommonUtils.unZipFile(jobEntity);
			// Delete Zip File
			// CommonUtils.deleteZipFile(jobEntity);

			// deprecated
			// Check hash
			// CommonUtils.isValidHash(jobEntity);

			// Receive Zip File
			jobEntity.setFileType(INMEMORYDB.ZIP);
			new AESCryptography().decryptionFile(jobEntity);

			/**Unzip File**/
			CommonUtils.unZipFile(jobEntity);

			//전송받은 get script 결과를 decryption 할떄 확장자를 .dat 로 하기 위하여.
			jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);

			CommonUtils.convertUTF8(jobEntity);

			errorStep = 2;
			
			/**Get Script 결과파일 파싱**/
			//file Parsing & validation Check
			dataParseManager.gscriptResult(jobEntity);

			errorStep = 3;
			//진단결과 백업
			doDiagnosisResultBackup(jobEntity);

			//Delete GetScript result File.
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

			if (jobEntity.isKillAgent() && jobEntity.getAgentInfo().getAgentType() == 1) {// kill agent
				logger.debug("Kill j_Agent from Get programm");
				SocketClient socket = new SocketClient(jobEntity);
				jobEntity.setJobType(ManagerJobFactory.KILLAGENT);
				socket.sendHeader(f, jobEntity);
			} else {
				errorStep = 5;
			}

		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			if(errorStep == 0){
				throw new Exception(SnetCommonErrCode.ERR_0018.getMessage()+" : "+e.getMessage());}
			else if(errorStep == 1){
				throw new Exception(SnetCommonErrCode.ERR_0019.getMessage());
			}else if(errorStep == 2){
				throw new Exception(SnetCommonErrCode.ERR_0020.getMessage()+" : "+e.getMessage());
			}else if(errorStep == 3){				
				throw new Exception(SnetCommonErrCode.ERR_0021.getMessage()+" : "+e.getMessage());
			}else if(errorStep == 4){
				//진단오류 메세지
				throw new Exception(e.getMessage());
			}else if(errorStep == 5){
				throw new Exception(SnetCommonErrCode.ERR_0022.getMessage()+" : "+e.getMessage());
			}
			
		}
	}

	
	

	//Socket으로 한줄씩 결과내용을 전송 할때
	public void doGSBFAGENTresult(JobEntity jobEntity, BufferedReader br) throws Exception {
		FileWriter fw = null;
		BufferedWriter bw = null;

		String bfGetpath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType());
		try {

			fw = new FileWriter(bfGetpath+jobEntity.getFileName()+INMEMORYDB.GSRESULTTYPE);
			bw = new BufferedWriter(fw);

			String line;
			while((line = br.readLine()) != null){
				logger.debug(line);
				bw.write(line);
				bw.newLine();
			}


			logger.debug("Before Receive socket Get Script result Path : " + bfGetpath + jobEntity.getFileName() + INMEMORYDB.GSRESULTTYPE);

			if(br != null) try{br.close();}catch(IOException e){}
			if(bw != null) try{bw.close();}catch(IOException e){}
			if(fw != null) try{fw.close();}catch(IOException e){}

			// 파일 OTP 로직 삭제
			//OTP validation을 위해 cOTP값을 추가한다.
//			insertCOTP(jobEntity);

			
			/**
			 * TODO.
			 * 
			 * 1. PUT TO QUEUE
			 * 2. dataParseManager.gscriptResult(jobEntity); MONITORING THE QUEUE  
			 * **/
			
			/**
			 * Get Script 결과파일 파싱
			 */
			//file Parsing & validation Check

			dataParseManager.gscriptResult(jobEntity);
			//Delete GetScript result File.
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

		} catch (IOException ie) {
			ie.printStackTrace();
			throw new Exception(SnetCommonErrCode.ERR_0010.getMessage());
		} 

	}

	public void doGSBFAGENTresult_v3(JobEntity jobEntity) throws Exception {

		try {

			//전송받은 get script 결과를 decryption 할떄 확장자를 .dat 로 하기 위하여.
			jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);

			//CommonUtils.convertUTF8(jobEntity);

			dataParseManager.gscriptResult(jobEntity);
			//Delete GetScript result File.
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new Exception(SnetCommonErrCode.ERR_0013.getMessage());
		}
	}

	//FTP로 Get 스크립트 파일 업로드하고 Telnet 으로 실행한 결과를 telnet 연결로 받아서 파일로 저장할때.
	public void doGSBFAGENTresult(JobEntity jobEntity, String str) throws Exception {
		FileWriter fw = null;
		BufferedWriter bw = null;

		String bfGetpath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType());
		try {

			logger.debug("Before Get Script result Path : " + bfGetpath + jobEntity.getFileName() + INMEMORYDB.GSRESULTTYPE);
			fw = new FileWriter(bfGetpath+jobEntity.getFileName()+INMEMORYDB.GSRESULTTYPE);
			bw = new BufferedWriter(fw);

			bw.write(str);


			if(bw != null) try{bw.close();}catch(IOException e){}
			if(fw != null) try{fw.close();}catch(IOException e){}

			//OTP validation을 위해 cOTP값을 추가한다.
			insertCOTP(jobEntity);

			/**
			 * Get Script 결과파일 파싱
			 */
			//file Parsing & validation Check
			dataParseManager.gscriptResult(jobEntity);

			//Delete GetScript result File.
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

		} catch (IOException e) {
			logger.error(CommonUtils.printError(e));
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
			throw new Exception(e.getMessage());
		} catch (NullPointerException e){
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
			throw new Exception(SnetCommonErrCode.ERR_0031.getMessage());
		} catch (SnetException e){
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
			throw new Exception(e.getMessage());
		}
	}


	private void insertCOTP(JobEntity jobEntity) throws IOException {
		String resultPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,jobEntity.getJobType());
		String filePath = resultPath +jobEntity.getFileName() + jobEntity.getFileType();
		File file = new File(filePath);

		logger.debug("insert cOTP Getscript file Path : " + filePath);

		removeFirstLine(file);

		RandomAccessFile rf = null;
		try {
			rf = new RandomAccessFile(file, "rw");

			String line;
			while ((line = rf.readLine()) != null){
				if (line.equalsIgnoreCase("<END>")){
					logger.debug("insert cOTP");
					if (jobEntity.getJobType().equalsIgnoreCase(ManagerJobFactory.GSBFAGENT) || jobEntity.getJobType().equalsIgnoreCase(ManagerJobType.WM100.toString())){
						rf.setLength(rf.getFilePointer()-7);
						rf.writeBytes("\ncOTP="+jobEntity.getCOTP()+"\n");
						rf.writeBytes("<END>\n");
					}else {
						rf.setLength(rf.getFilePointer()-7);

						rf.writeBytes("\ncOTP="+jobEntity.getCOTP()+"\n");
						rf.writeBytes("<END>\n");
					}
				}
			}

		}catch (Exception e){throw new EOFException(e.getMessage());}
		finally {
			if (rf != null) rf.close();
		}
	}

	private void removeFirstLine(File fileName) throws IOException {

		RandomAccessFile raf = null;

		try {
			 raf = new RandomAccessFile(fileName, "rw");
			//Initial write position
			long writePosition = raf.getFilePointer();
			// Shift the next lines upwards.
			long readPosition = -1;
			boolean isRemove = false;

			String s;
			while ((s = raf.readLine()) != null){
				if(s.contains("GSBFAGENT")){
					readPosition = raf.getFilePointer();
					logger.debug(s);
					logger.debug("readPosition : "+readPosition);
					isRemove = true;
					break;
				}else if(s.contains("SBFAGENT")){
					readPosition = raf.getFilePointer();
					logger.debug(s);
					logger.debug("readPosition : "+readPosition);
					isRemove = true;
					break;
				}
			}
			if (isRemove){
				byte[] buff = new byte[1024];
				int n;
				while (-1 != (n = raf.read(buff))) {
					raf.seek(writePosition);
					raf.write(buff, 0, n);
					readPosition += n;
					writePosition += n;
					raf.seek(readPosition);
				}
				raf.setLength(writePosition);
				raf.close();
			}

		}catch (Exception e){}
		finally {
			if (raf != null) raf.close();
		}

	}

	// ssh, sftp, telnet, ssh 등을 이용한 수집시 사용 (AJ201)
	private File[] createGetscript_java14(JobEntity jobEntity) throws Exception {


		String gsFileName;
		String gsJarFileFullPath;
		String gsOrgiFilePath;
		String gsFilePath;
		File[] files = new File[1];
		File outFile;

		boolean isGetDecryption = false;

		if(jobEntity.getRelay2AgentInfo() != null) {
			Gson gson = new Gson();
			String jobEntityJson = gson.toJson(jobEntity);
			BufferedWriter out = new BufferedWriter(new FileWriter("/usr/local/snetManager/manager/libs/" + jobEntity.getFileName() + ".json"));
			out.write(jobEntityJson);
			out.close();
			files = new File[2];
			files[0] = new File("/usr/local/snetManager/manager/libs/"  + jobEntity.getFileName() + ".json");
		}

		if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
			gsFileName = INMEMORYDB.GetWindows+INMEMORYDB.JAR;
			gsOrgiFilePath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType());
			gsJarFileFullPath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType()) + gsFileName;
			gsFilePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP()+INMEMORYDB.JAR;
			outFile = new File(gsFilePath);
		}else {
			gsFileName = INMEMORYDB.GetLinux+INMEMORYDB.SH;
			gsOrgiFilePath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType());
			gsJarFileFullPath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType()) + gsFileName;
			gsFilePath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP()+INMEMORYDB.SH;
			outFile = new File(gsFilePath);
		}

		if(jobEntity.getRelay2AgentInfo() != null)
			files[1] = outFile;
		else
			files[0] = outFile;


		logger.debug("get script shell : " + gsJarFileFullPath);
		logger.debug("Send : " + gsFilePath);

		try {
			logger.debug("Get program Decryption.");
			gsFileName = CommonUtils.toMux(gsFileName);
			gsJarFileFullPath = gsOrgiFilePath + gsFileName;

			isGetDecryption = new AESCryptography().decryptionShFile(gsJarFileFullPath,gsFilePath);

			if(!isGetDecryption){
				logger.error("Get program Decryption Fail.");
				throw new Exception(SnetCommonErrCode.ERR_0006.getMessage());
			}

		} catch (Exception e) {
			logger.error("Get program Decryption Fail.");
			throw new Exception(SnetCommonErrCode.ERR_0006.getMessage());
		}

        // 정상적으로 파일 decrypt되면 해당 파일의 hash값을 뜨고 db에 조회하여 값이 맞는지 확인 후 진행 다를 경우 익셉션 처리.
        AESCryptography aesCryptography = new AESCryptography();
        String hash = aesCryptography.encryption(Paths.get(gsFilePath));
        logger.debug("get script checksum hash : " + hash);
        if (jobEntity.getInfoChecksumHash() == null) {
			if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
				jobEntity.setInfoChecksumHash(dao.selectGetScriptChecksum("getwindows.jar"));
			} else {
				jobEntity.setInfoChecksumHash(dao.selectGetScriptChecksum("getunixagent.class"));
			}
		}
		logger.debug("get script checksum hash db : " + jobEntity.getInfoChecksumHash());

        if (!hash.equals(jobEntity.getInfoChecksumHash())){
            logger.error("get script File Hash mismatch.");
            throw new Exception(SnetCommonErrCode.ERR_0036.getMessage());
        }

		return files;
	}

	// 이미 수집 된 자산에 대한 장비정비업데이트시 (AJ200)
	private void createGetscript_java14_V3(JobEntity jobEntity) throws Exception {
		String gsFileName;
		String gsJarFileFullPath;
		String gsOrgiFilePath;
		String gsFilePath;
		String log4JChkwork = "";

		boolean isGetDecryption = false;
		boolean isLog4ChkDecryption = false;

		String sendPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType());  //work 디렉터리

		if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
			gsFileName = INMEMORYDB.GetWindows+INMEMORYDB.JAR;
			gsOrgiFilePath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType());     // get 디렉터리
			gsFilePath = sendPath + jobEntity.getSOTP()+INMEMORYDB.JAR;
		}else {
			gsFileName = INMEMORYDB.GetLinux+INMEMORYDB.SH;
			gsOrgiFilePath = INMEMORYDB.scriptFileAbsolutePath(jobEntity.getJobType(),jobEntity.getAgentInfo().getOsType());    // get 디렉터리
			gsFilePath = sendPath + jobEntity.getSOTP()+INMEMORYDB.SH;   // work 디렉터리안에 전송할 파일들
		}

		logger.debug("get script shell path : " + gsOrgiFilePath + ", file : " + gsFileName);
		logger.debug("Send : " + gsFilePath);

		try {
			logger.debug("Get program Decryption.");
			gsFileName = CommonUtils.toMux(gsFileName);
			gsJarFileFullPath = gsOrgiFilePath + gsFileName;

			isGetDecryption = new AESCryptography().decryptionShFile(gsJarFileFullPath,gsFilePath);

			if(!isGetDecryption){
				logger.error("Get program Decryption Fail. <1> ");
				throw new Exception(SnetCommonErrCode.ERR_0006.getMessage());
			}

			if (INMEMORYDB.useLog4JChecker) {
				String log4JChk = "";
				CommonUtils.mkdir(sendPath + jobEntity.getSOTP());
				if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
					log4JChk = CommonUtils.toMux(INMEMORYDB.Log4JChkZIP);
				} else {
					log4JChk = CommonUtils.toMux(INMEMORYDB.Log4JChkZIPUnix);
				}

				String log4JChkFileFullPath = gsOrgiFilePath + log4JChk;
				log4JChkwork = sendPath + jobEntity.getSOTP() + File.separator + jobEntity.getSOTP() + INMEMORYDB.ZIP;
				logger.debug("Get < Log4J > program : " + log4JChkFileFullPath + ", " + log4JChkwork);

				isLog4ChkDecryption = new AESCryptography().decryptionShFile(log4JChkFileFullPath, log4JChkwork);

				if (!isLog4ChkDecryption) {
					logger.error("Get < Log4J > program Decryption Fail. thus cannot check log4j cve.");
				} else {
					new ZipUtil(log4JChkwork,sendPath + jobEntity.getSOTP()).unzip();
				}
			}

		} catch (Exception e) {
			logger.error("Get program Decryption Fail. <2> ");
			throw new Exception(SnetCommonErrCode.ERR_0006.getMessage());
		}

		// 정상적으로 파일 decrypt되면 해당 파일의 hash값을 뜨고 db에 조회하여 값이 맞는지 확인 후 진행 다를 경우 익셉션 처리.
		AESCryptography aesCryptography = new AESCryptography();
		String hash = aesCryptography.encryption(Paths.get(gsFilePath));
		logger.debug("get script checksum hash : " + hash);
		if (jobEntity.getInfoChecksumHash() == null) {
			if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
				jobEntity.setInfoChecksumHash(dao.selectGetScriptChecksum("getwindows.jar"));
			} else {
				jobEntity.setInfoChecksumHash(dao.selectGetScriptChecksum("getunixagent.class"));
			}
		}
		logger.debug("get script checksum hash db : " + jobEntity.getInfoChecksumHash());

		if (!hash.equals(jobEntity.getInfoChecksumHash())){
			logger.error("get script File Hash mismatch.");
			throw new Exception(SnetCommonErrCode.ERR_0036.getMessage());
		}

		if (INMEMORYDB.useLog4JChecker && isLog4ChkDecryption) {
			String l4hash = aesCryptography.encryption(Paths.get(log4JChkwork));
			logger.debug("get script < for log4J > checksum hash : " + l4hash);

			String l4DbHash = "";
			if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
				l4DbHash = dao.selectGetScriptChecksum(INMEMORYDB.Log4JChkZIP);
			} else {
				l4DbHash = dao.selectGetScriptChecksum(INMEMORYDB.Log4JChkZIPUnix);
			}

			logger.debug("get script < for log4J > checksum hash db : " + l4DbHash);

			if (!l4hash.equals(l4DbHash)){
				logger.error("get script < for log4J > File Hash mismatch.");
				throw new Exception(SnetCommonErrCode.ERR_0036.getMessage());
			}

			makeLog4JConfig(jobEntity, sendPath);
		}
	}

	private void doDiagnosisResultBackup(JobEntity jobEntity){
		String resultPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,
				jobEntity.getJobType()) + jobEntity.getFileName() + jobEntity.getFileType();
		String movePath = INMEMORYDB.diagnoisResultAbsolutePath();
		String moveFile = jobEntity.getHostNm()+"_"+jobEntity.getAgentInfo().getConnectIpAddress()+"_"+ DateUtil.getCurrDateBySecond()+jobEntity.getFileType();

		movePath += moveFile;

		CommonUtils.fileCopy(resultPath,movePath);
	}

	// useLog4JFile와 useLog4JPath 정보를 파일로 생성.
	private void makeLog4JConfig(JobEntity jobEntity, String sendPath) throws Exception {
		logger.debug("make log4jchecker config....start - " + jobEntity.getAssetCd());

		FileWriter fw = null;
		BufferedWriter bw = null;
		String text = "";
		int useLog4JFile = 0;

		fw = new FileWriter(sendPath + jobEntity.getSOTP() + File.separator + INMEMORYDB.useLog4JConfig);
		bw = new BufferedWriter(fw);

		if (INMEMORYDB.useLog4JFile) {
			useLog4JFile = 1;
		}
		if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)) {
			text = useLog4JFile + System.lineSeparator() + INMEMORYDB.useLog4JPath;
		} else {
			text = useLog4JFile + INMEMORYDB.DELIMITER + INMEMORYDB.useLog4JPath;
		}

		bw.write(text);
		bw.close();
		fw.close();

		logger.debug("make log4jchecker config....successfully - " + jobEntity.getAssetCd() + " - " + text);
	}
}
