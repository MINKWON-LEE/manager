/**
 * proMject : AgentManager
 * program name : com.mobigen.snet.agentmanager.concurrents.NoSleepThread.java
 * @author : Je-Joong Lee
 * created at : 2016. 1. 5.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.concurrents.NoSleepThread;
import com.igloosec.smartguard.next.agentmanager.concurrents.OneTimeThread;
import com.igloosec.smartguard.next.agentmanager.concurrents.SleepThread;
import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.*;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.MoniterMemUtil;

import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Slf4j
@Service("notificationListener")
public class NotificationListener  extends AbstractManager{
	
	ServerSocket serverSocket = null;
//	Socket socket = null;
	private Dao dao;
	private JobHandleManager jobHandleManager;
	private DeployManager deployManager;
	private AgentVersionManager agentVersionManager;
	private AssetUpdateManager assetUpdateManager;
	private NetworkSwitchManager networkSwitchManager;
	private RemoteControlManager remoteControlManager;

	public NotificationListener(Dao dao, JobHandleManager jobHandleManager,
								DeployManager deployManager, AgentVersionManager agentVersionManager,
								AssetUpdateManager assetUpdateManager, NetworkSwitchManager networkSwitchManager,
								RemoteControlManager remoteControlManager) {

		this.dao = dao;
		this.jobHandleManager = jobHandleManager;
		this.deployManager = deployManager;
		this.agentVersionManager = agentVersionManager;
		this.assetUpdateManager = assetUpdateManager;
		this.networkSwitchManager = networkSwitchManager;
		this.remoteControlManager = remoteControlManager;
	}

	public void initNotificationListener() {

		NoSleepThread serverIntiator = new NoSleepThread() {
			@Override
			public void task() throws Exception {
				loadServerSocket();
			}
		};

		serverIntiator.start();
	}
	
	public void initNotificationListener(int port) {

		NoSleepThread serverIntiator = new NoSleepThread() {
			@Override
			public void task() throws Exception {
				loadServerSocket(port);
			}
		};

		serverIntiator.start();
	}

	private void loadServerSocket(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (Throwable e) {
			log.error("=== ServerSocket error" + e.getMessage(), e);
			return;
		}

		while (true) {
			try {
				Socket socket = serverSocket.accept();
				createWorker(socket);
			} catch (Throwable e) {
				log.error("== createWorker : " + e.getMessage(), e);
			}
		}

	}

	private void loadServerSocket(){
		try {
			serverSocket = new ServerSocket(Integer.parseInt(INMEMORYDB.LISTENER_PORT));
		} catch (Throwable e) {
			log.error("=== ServerSocket error" + e.getMessage(), e);
			return;
		}

		while (true) {
			try {
				Socket socket = serverSocket.accept();
				new Thread(){
					@Override
					public void run() {
						try {
							log.info("C="+ Thread.currentThread().getId());
							createWorker(socket);
						} catch (Throwable e) {
							log.error("== createWorker in " + e.getMessage(), e);
						} finally {
							CommonUtils.close(socket);
							log.info("== socket close, K={}", Thread.currentThread().getId());
//							log.info("K="+Thread.currentThread().getId());
						}
					}
				}.start();
				
			} catch (Throwable e) {
				log.error("== createWorker : " + e.getMessage(), e);
				// 어떤 이유에든 순식간에 무한반복이 되는 것을 막기 위해서
				CommonUtils.sleep(2);
			}
		}

	}


	private void createWorker(final Socket socket) throws Exception {
		socket.setSoTimeout(30 * 1000);
		InetSocketAddress remoteAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
		log.info("== accept : {}", remoteAddress.getAddress() );

		final BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		final String recvMsg = inFromClient.readLine();
		if(recvMsg == null || "".equals(recvMsg)){				
			CommonUtils.close(inFromClient);
			log.info("== socket close");
			
		} else{
			doHandleStreamMessage(socket,recvMsg,inFromClient);
		}		
	}


	private void doHandleStreamMessage(Socket socket, String recvMsg, BufferedReader inFromClient) throws Exception {
		log.debug("recvMsg = {} , remoteIP : {}", recvMsg, socket.getInetAddress());
		try {
			if (recvMsg.contains("WEBCHGNOTI")) {
				//WebSource Monitoring
				doHandleWebSrcMonitoring(recvMsg, socket);
			} else if (recvMsg.contains("NETCHKATOM")) {
				doCheckNetworkAtoM(socket);
				log.info("net check agent to manager");
			}

		} finally {
			CommonUtils.close(inFromClient);
		}

	}

	private void doCheckNetworkAtoM(Socket socket) throws IOException {
		OutputStream os = null;
		Socket sock = new Socket();

		try {
			InetSocketAddress ipep = new InetSocketAddress(socket.getInetAddress().toString().replace("/", ""), Integer.parseInt(INMEMORYDB.AGENT_PORT));

			sock.connect(ipep);

			if(sock.isConnected()) {
				String msg = "NETCHKOK";
				os = sock.getOutputStream();
				os.write(msg.getBytes());
			}

		} catch (SocketException ex) {
			log.info(ex.getMessage());
		} finally {
			os.close();
			sock.close();
		}
	}

	private void doHandleWebSrcMonitoring(String recvMsg, Socket socket){
		/*
		WEBCHGNOTI|osType|HOSTNAME|LOCALIP|encMONROOTDIR|encMsg
		i.e.)
		WEBCHGNOTI|Window 10|MONWASHOST|1.2.3.4|adfadfasdfasdfasdfasf:C|asdfasdfasdfasdfadfasdfasdfasd		
		*/
//		String deli = "|";
		String header[] = recvMsg.split("\\|");
		String pathRepDeli = "=slsh=";
		String osType = header[1];
		String hostNm = header[2];
		String agentIp = header[3];
		String monDir =  header[4];
		String chgStr =  header[5];
//		String pathDeli = "/";
//		log.debug("1111");
//		if(osType.toUpperCase().contains("WIN")){
//			pathDeli = "\\";
//		}
		monDir = decodeAscii(monDir);
		monDir = CommonUtils.toMux(monDir);
		log.debug("1111:"+monDir);
		chgStr = decodeAscii(chgStr);
		chgStr = CommonUtils.toMux(chgStr);
		log.debug("1111:"+chgStr);
		monDir = monDir.replaceAll(pathRepDeli, "/");
		chgStr = chgStr.replaceAll(pathRepDeli, "/");
		
		try {
			monDir = new String(monDir.getBytes(),"UTF-8");
			chgStr =  new String(chgStr.getBytes(),"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		log.debug("FINAL MONDIR "+monDir);
		log.debug("FINAL CHGSTR "+chgStr);
		log.info("WEB SRC CHANGE MONITOR : "
				+" osType:"+ osType
				+" hostNm:" +hostNm 
				+" agentIp:"+agentIp
				+" monDir:"+monDir
				+" chgStr:"+chgStr 
				);
		
		HashMap<String, String> args = new HashMap<String, String>();
		
		
		
		args.put("osType", osType);
		args.put("hostNm", hostNm);
		args.put("agentIp", agentIp);
		args.put("monDir", monDir);
		args.put("chgStr", chgStr);
		
		try {
			log.debug("INSERT CHG LOG--------------------");
			dao.insertWebSrcChgLog(args);
			log.debug("--------------------INSERT CHG LOG");
		} catch (SnetException e) {
			log.error(e.getMessage(), e);
		}
		
	}
    private String decodeAscii(String msg){
    	String deli="2325.";
    	int heck = 2325;
    	String retStr = "";
    	String[] arr = msg.split(deli);
    	int arrLeng = arr.length;
    	for(int i =0; i < arrLeng ; i++){
    		retStr = retStr+ (char)(Integer.parseInt(arr[i]) - heck);
    	}
    	
    	return retStr;
    	
    }

	//Queue 모니터링 1초간격
	//3.0에선 ssh를 통한 장비정보 수집(GETQUEUE)의 경우에만 사용. phil
	public void getGSWorker() {

		SleepThread worker = new SleepThread() {
			@Override
			public void task() throws Exception {
				String asset_Cd="";
				if (INMEMORYDB.GETQUEUE.size() > 0){
					asset_Cd = INMEMORYDB.GETQUEUE.pop();
					if(!isGSRunning(asset_Cd) || INMEMORYDB.RUNNINGGSJOBLIST.size() > INMEMORYDB.maxGSexec){
						INMEMORYDB.GETQUEUE.put(asset_Cd);
						Thread.sleep(10000);
					}
					else {
						log.debug(asset_Cd + " 장비 정보 수집 실행. assetCd=" + asset_Cd + " INMEMORYDB.GETQUEUE.size()="+INMEMORYDB.GETQUEUE.size());

						RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(asset_Cd);
						JobEntity jobEntity = runnigJobEntity.getJobEntity();
						if (jobEntity != null) {
							// ssh를 통한 장비 정보 수집인 경우 ssh 큐에 등록
							INMEMORYDB.createSshMem(jobEntity);
							log.debug("succeed in inserting item aeestCd({}) into RUNNING_SSHJOBLIST, size - {}",
									jobEntity.getAssetCd(), INMEMORYDB.RUNNING_SSHJOBLIST.size());

							OneTimeThread worker = new OneTimeThread() {
								@Override
								public void task() throws Exception {
									try {
										try {
											doGSCRPTEXECREQSSH(jobEntity);
										} catch (Throwable throwable) {
											arrangeGsJobList(jobEntity);
											throwable.printStackTrace();
										} finally {
											finish();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							};
							worker.start();
						}
					}
				}else {
					Thread.currentThread();
					Thread.sleep(1000);
					//log.debug("진단 작업이 없습니다.");
				}
			}

			//RUNNINGJOBLIST. 존재여부
			private boolean isGSRunning(String asset_Cd){
				return !INMEMORYDB.RUNNING_SSHJOBLIST.containsKey(asset_Cd);
			}
		};
		worker.start();
	}

	//Queue 모니터링 1초간격
	//네트워크 장비정보 수집 혹은 네트워크 진단실행(NETWORKQUEUE)의 경우에만 사용. phil
	public void getNWWorker() {

		SleepThread worker = new SleepThread() {
			@Override
			public void task() throws Exception {
				String asset_Cd="";
				if (INMEMORYDB.NETWORKQUEUE.size() > 0){
					asset_Cd = INMEMORYDB.NETWORKQUEUE.pop();
					if(!isNWRunning(asset_Cd) || INMEMORYDB.RUNNINGNWJOBLIST.size() > INMEMORYDB.maxNWexec){
						INMEMORYDB.NETWORKQUEUE.put(asset_Cd);
						Thread.sleep(10000);
					}
					else {
						log.debug(asset_Cd + " 네트워크 장비 정보 수집 실행. assetCd=" + asset_Cd + ", NETWORKQUEUE size - "+INMEMORYDB.NETWORKQUEUE.size());

						RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGNWJOBLIST.get(asset_Cd);
						JobEntity jobEntity = runnigJobEntity.getJobEntity();
						if (jobEntity != null) {
							// ssh를 통한 네트워크 장비 정보 수집이므로 경우 ssh 큐에 등록
							INMEMORYDB.createSshMem(jobEntity);
							log.debug("succeed in inserting item aeestCd({}) into RUNNING_SSHJOBLIST, size - {}",
									jobEntity.getAssetCd(), INMEMORYDB.RUNNING_SSHJOBLIST.size());

							OneTimeThread worker = new OneTimeThread() {
								@Override
								public void task() throws Exception {
									try {
										try {
											if (jobEntity.getJobType().equals(ManagerJobType.WM300.toString())) {
												networkSwitchManager.doNetworkBFA(jobEntity);
												arrangeNwJobList(jobEntity);
											} else if (jobEntity.getJobType().equals(ManagerJobType.WM302.toString())) {
												networkSwitchManager.doNetworkDGEXE(jobEntity);
												arrangeNwJobList(jobEntity);
											}

										} catch (Throwable throwable) {
											arrangeNwJobList(jobEntity);
											throwable.printStackTrace();
										} finally {
											finish();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							};
							worker.start();
						}
					}
				}else {
					Thread.currentThread();
					Thread.sleep(1000);
					//log.debug("진단 작업이 없습니다.");
				}
			}

			//RUNNINGJOBLIST. 존재여부
			private boolean isNWRunning(String asset_Cd){
				return !INMEMORYDB.RUNNING_SSHJOBLIST.containsKey(asset_Cd);
			}
		};
		worker.start();
	}

	//Queue 모니터링 1초간격
	public void agentInstallWorker() {

		SleepThread worker = new SleepThread() {
			@Override
			public void task() throws Exception {

				String assetCd = "";

				if (INMEMORYDB.AGENTSETUPQUEUE.size() > 0) {
					assetCd = INMEMORYDB.AGENTSETUPQUEUE.pop();

					if(!isSetupRunning(assetCd) || INMEMORYDB.RUNNINGSETUPJOBLIST.size() > INMEMORYDB.maxSETUPexec) {
						INMEMORYDB.AGENTSETUPQUEUE.put(assetCd);
						Thread.sleep(10000);
					} else {
						log.debug(assetCd + " Start Agent Install [assetCd=" + assetCd + "] INMEMORYDB.AGENTSETUPQUEUE.size()="+INMEMORYDB.AGENTSETUPQUEUE.size());

						RunnigJobEntity runnigJobEntity = INMEMORYDB.RUNNINGSETUPJOBLIST.get(assetCd);
						JobEntity jobEntity = runnigJobEntity.getJobEntity();
						if (jobEntity != null) {
							// ssh를 통한 네트워크 장비 정보 수집이므로 경우 ssh 큐에 등록
							INMEMORYDB.createSshMem(jobEntity);
							log.debug("succeed in inserting item aeestCd({}) into RUNNING_SSHJOBLIST, size - {}",
									jobEntity.getAssetCd(), INMEMORYDB.RUNNING_SSHJOBLIST.size());

							OneTimeThread worker = new OneTimeThread() {
								@Override
								public void task() throws Exception {
									try {
										try {
											AgentInfo ai = jobEntity.getAgentInfo();
											deployManager.installAgent(ai);
											dao.updateAgentSetupStatus(ai);
											arrangeSetupJobList(jobEntity);
										} catch (Throwable throwable) {
											arrangeSetupJobList(jobEntity);
											throwable.printStackTrace();
										}
										finally{
											finish();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							};
							worker.start();
						}
					}
				}else {
					Thread.currentThread();
					Thread.sleep(500);
					//log.debug("진단 작업이 없습니다.");
				}
			}

			private boolean isSetupRunning(String msg){
				//스플릿
				String asset_Cd = msg;

				//RUNNINGJOBLIST. 존재여부
				return !INMEMORYDB.RUNNING_SSHJOBLIST.containsKey(asset_Cd);
			}
		};
		worker.start();
	}
		
	private void doREMOTERUNCMD(String[] header){
		try{
			log.debug("RUN " + header[2]+ " for asset:"+ header[1]);
		    // "RCR|assetCd|command"
			JobEntity jobEntity = createWASjobEntity(header);

			//Agent Kill & UP
	        log.debug("Agent TYPE : "+jobEntity.getAgentInfo().getAgentType() );
			if(!jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID) && jobEntity.getAgentInfo().getAgentType() == 1){
				remoteControlManager.runProcessUP(jobEntity);
			}			
			agentVersionManager.doSENDCMDService(jobEntity, header[2]);
			
		}catch(Exception e){
			
		}
		
	}

	private void doGSCRPTEXECREQSSH(JobEntity jobEntity) throws Exception {

		try {
			assetUpdateManager.doAssetUpdateBFA(jobEntity);
			arrangeGsJobList(jobEntity);
		} catch (Exception e){
			log.error(CommonUtils.printError(e), e);
			arrangeGsJobList(jobEntity);
			throw new SnetException(dao, e.getMessage(), jobEntity, "G");
		}
	}

	public void arrangeJobList(JobEntity jobEntity) {

		String jobType = jobEntity.getJobType();
		if (jobType.equals(ManagerJobType.AJ200.toString())) {
			arrangeGsJobList(jobEntity);
		} else if (jobType.equals(ManagerJobType.AJ100.toString()) || jobType.equals(ManagerJobType.AJ101.toString())) {
			arrangeDgJobList(jobEntity);
		} else if (jobType.equals(ManagerJobType.WM300.toString()) || jobType.equals(ManagerJobType.WM302.toString())) {
			arrangeNwJobList(jobEntity);
		} else if (jobType.equals(ManagerJobType.AJ300.toString())) {
			arrangeLogJobList(jobEntity);
		} else if (jobType.equals(ManagerJobType.AJ600.toString())) {
			arrangeSetupJobList(jobEntity);
		}
	}

	public void arrangeGsJobList(JobEntity jobEntity) {
		//현재 실행 중인 장비 정보 수집 큐에서 제거
		INMEMORYDB.removeGSJOBList(jobEntity);
		// 현재 실행 중인 ssh 서비스 종료.
		INMEMORYDB.removeSshJobList(jobEntity);
		// 현재 실행 중인 자산별로 등록된 JOB을 제거
		INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		log.debug("(assetCd {}), RUNNINGGSJOBLIST size - {}, RUNNING_SSHJOBLIST size - {}, JOBLISTPERASSET size - {}",
				jobEntity.getAssetCd(), INMEMORYDB.RUNNINGGSJOBLIST.size(),
				INMEMORYDB.RUNNING_SSHJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
	}

	public void arrangeDgJobList(JobEntity jobEntity) {

		// SG2019-850
		// SKT의 경우 한개라도 진단 실행 중인 장비가 있다면 진단 실행 업무를 전달하지 않는다.
		// INMEMORYDB.maxDGexe이 1로 셋팅되어 있음.
		if (INMEMORYDB.maxDGexec > 0 && INMEMORYDB.RUNNINGAGENTDGJOBLIST.size() > 0) {
			INMEMORYDB.RUNNINGAGENTDGJOBLIST.remove(jobEntity.getAssetCd());
		}

		//현재 실행 중인 진단 실행 큐에서 제거
		INMEMORYDB.removeDGJOBList(jobEntity);
		// 현재 실행 중인 자산별로 등록된 JOB을 제거
		INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		log.debug("(assetCd {}), RUNNINGDGJOBLIST size - {}, JOBLISTPERASSET size - {}",
				jobEntity.getAssetCd(), INMEMORYDB.RUNNINGDGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
	}

	public void arrangeNwJobList(JobEntity jobEntity) {
		//현재 실행 중인 진단 실행 큐에서 제거
		INMEMORYDB.removeNWJOBList(jobEntity);
		// 현재 실행 중인 ssh 서비스 종료.
		INMEMORYDB.removeSshJobList(jobEntity);
		// 현재 실행 중인 자산별로 등록된 JOB을 제거
		INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		log.debug("(assetCd {}), RUNNINGNWJOBLIST size - {}, RUNNING_SSHJOBLIST size - {}, JOBLISTPERASSET size - {}",
				jobEntity.getAssetCd(), INMEMORYDB.RUNNINGNWJOBLIST.size(),
				INMEMORYDB.RUNNING_SSHJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
	}

	public void arrangeLogJobList(JobEntity jobEntity) {
		//현재 실행 중인 진단 실행 큐에서 제거
		INMEMORYDB.removeLOGJOBList(jobEntity);
		// 현재 실행 중인 자산별로 등록된 JOB을 제거
		INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		log.debug("(assetCd {}), RUNNINGLOGJOBLIST size - {}, JOBLISTPERASSET size - {}",
				jobEntity.getAssetCd(), INMEMORYDB.RUNNINGLOGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
	}

	public void arrangeSetupJobList(JobEntity jobEntity) {
		//현재 실행 중인 진단 실행 큐에서 제거
		INMEMORYDB.removeSETUPJOBList(jobEntity);
		// 현재 실행 중인 ssh 서비스 종료.
		INMEMORYDB.removeSshJobList(jobEntity);
		// 현재 실행 중인 자산별로 등록된 JOB을 제거
		INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		log.debug("(assetCd {}), RUNNINGSETUPJOBLIST size - {}, JOBLISTPERASSET size - {}",
				jobEntity.getAssetCd(), INMEMORYDB.RUNNINGSETUPJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
	}

	public void arrangeControlJobList(JobEntity jobEntity) {
		//현재 실행 중인 진단 실행 큐에서 제거
		INMEMORYDB.removeCONTROLJOBList(jobEntity);
		// 현재 실행 중인 자산별로 등록된 JOB을 제거
		INMEMORYDB.removeJobListPerAsset(jobEntity.getAssetCd(), jobEntity.getJobType());

		log.debug("(assetCd {}), RUNNINGCONTROLJOBLIST size - {}, JOBLISTPERASSET size - {}",
				jobEntity.getAssetCd(), INMEMORYDB.RUNNINGCONTROLJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
	}

	private void doMONITER() throws Exception {
		//INMEMORYDB moniter util.
		new MoniterMemUtil().moniterMem();
	}
	
	public void managerJobfactory(String msg, BufferedReader inFromClient) throws Exception {

		String[] header = StringUtil.split(msg, "|");
		String msgType = header[0];

		log.debug("Notification Message :: {}", msg);
		log.debug("Notification msgType :: {}", msgType);

        switch(msgType){
        case ManagerJobFactory.RUNCMD:  //WAS 의 remote command 요청 // RCR|assetCd|msg
			    doREMOTERUNCMD(header);
				break;
		case ManagerJobFactory.MONITER:
				doMONITER();
				break;

        default: throw new Exception("잘못된 메세지 타입입니다.");
        }
	}
	
	

	/**
	 * update : 201603032030
	 * Header 규격
	 * WAS에서 GetScript 수행 수신 : GSCRPTEXECREQ|AC151201160727|T4486597
	 * WAS에서 진단스크립트 수행 신: GSCRPTEXECREQ|AC151201160727|OS|Linux|CentOS release 6.4 (Final)|T4486597
	 * WAS에서 HealthChk 수행 수신: HEALTHCHECK|AC151201160727|
	 *
	 * Agent에서 HealthChk 응답 Noti 수신 : HEALTHCHECKFIN|AC151201160727
	 * Agent에서 GetScript 결과 수신 Noti수신  : DGFIN|288122
	 * Agent에서 GetScript 결과 수신 Noti 수신 : GSCRPTFIN|222534
	 *
	 * //GSCRPTFIN, DGFIN 은
	 * //createWASjobEntity 에서 jobEntity를 만드는게 아니라 메모리에서 불러온다,
	 * Agent에서 GetScript 결과파일 수신 : GSCRPTFIN|3|982902|des|880|salt|8|iv|16|
	 * Agent에서 진단스크립트 결과파일 수신 : DGFIN|3|288122|des|66304|salt|8|iv|16|
	 *
	 * Agent에서 OTP 받을 떄 : OTP|3|982902|des|48|salt|8|iv|16|
	 * RCR|assetCd|command
	 */

	protected JobEntity createWASjobEntity(String[] headers) throws Exception {

		String[] parsedMsg = headers;
		String jobType = parsedMsg[0];
		JobEntity jobEntity = null;

		if(jobType.equalsIgnoreCase(ManagerJobFactory.RUNCMD)){
			AgentInfo ai = jobHandleManager.initAgentInfo(parsedMsg[1]);
			jobEntity = new JobEntity();
			jobEntity.setAgentInfo(ai);
			jobEntity.setAssetCd(ai.getAssetCd());
			jobEntity.setJobType(ManagerJobFactory.RUNCMD);

			return jobEntity;

		}else if(jobType.equalsIgnoreCase(ManagerJobFactory.GSBFAGENT)){ //에이전트 첫 설치시
			String assetCd = parsedMsg[1];
			AgentInfo ai = new AgentInfo();
			ai.setAssetCd(assetCd);

			jobEntity = new JobEntity();
			jobEntity.setAgentInfo(ai);
			jobEntity.setJobType(ManagerJobFactory.GSBFAGENT);
			jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);
			jobEntity.setFileName(assetCd + "_" + ManagerJobFactory.GSBFAGENT);

			int otp = new Random().nextInt(900000)+100000;
			String S_OTP = String.valueOf(otp);
			jobEntity.setCOTP(S_OTP);


			return jobEntity;

		} else {
			throw new Exception("잘못된 메세지 타입입니다.");
		}
	}

	public static void main(String args[]) {

		System.out.println(INMEMORYDB.absolutePath(INMEMORYDB.AGENT_LIBS_DIR,"UNIX"));
	}

}
