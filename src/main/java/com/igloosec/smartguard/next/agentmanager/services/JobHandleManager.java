/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.JobHandleManager.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 27.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.api.agent.model.ControlAgentReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagnosisReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.IReq;
import com.igloosec.smartguard.next.agentmanager.api.util.jobs.JobEntityInitData;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;

import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;
import com.igloosec.smartguard.next.agentmanager.entity.SystemInfoEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.memory.NotiType;
import com.igloosec.smartguard.next.agentmanager.property.AgentContextProperties;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("jobHandleManager")
@Slf4j
public class JobHandleManager {

	private Dao dao;
	private RemoteControlManager remoteControlManager;
	private AgentContextProperties agentContextProperties;
    private MessageSourceAccessor messageSourceAccessor;

	public JobHandleManager(Dao dao, RemoteControlManager remoteControlManager,  AgentContextProperties agentContextProperties,
                            MessageSourceAccessor messageSourceAccessor) {
		this.dao = dao;
		this.remoteControlManager = remoteControlManager;
		this.agentContextProperties = agentContextProperties;
		this.messageSourceAccessor = messageSourceAccessor;
	}

	public AgentInfo initAgentInfoAuth(String assetCd) throws Exception {

		try {
			return dao.selectAgentInfoAuthWithAssetCd(assetCd);
		}catch (NullPointerException e){
			throw new Exception("Agent 정보가 없습니다.");
		}
	}

	public AgentInfo initAgentInfoAuth(String ip, String hostName, String os) throws Exception {

		try {
			return dao.selectAgentInfoAuth(ip, hostName, os);
		}catch (NullPointerException e){
			throw new Exception("Agent 정보가 없습니다.");
		}
	}

	public AgentInfo initAgentInfo(String assetCd) throws Exception {
		AgentInfo agentInfo;

		try {
			agentInfo = dao.selectAgentInfo(assetCd);
			agentInfo.completeAgentInfo();

			return agentInfo;
		}catch (NullPointerException e){
			throw new Exception("Agent 정보가 없습니다.");
		}
	}

	public void initAgentInfoMulti(List<ControlAgentReq> controlAgentReqS) throws Exception {

		String jobType = controlAgentReqS.get(0).getJobType();

		HashMap<String, Object> param = new HashMap<>();
		param.put("reqS", controlAgentReqS);
		param.put("personManagerCd", "notUsed");

		List<AgentInfo> ais = dao.selectAgentInfoMulti(param);
		if (ais.size() == 0) {
			throw new Exception("it does not exist for this request.");
		}
		for (AgentInfo ai : ais) {
			ai.completeAgentInfo();

			JobEntity jobEntity = new JobEntity();
			jobEntity.setAssetCd(ai.getAssetCd());
			jobEntity.setJobType(jobType);

			// 1. 에이전트 설치
			if (jobType.equals(ManagerJobType.AJ600.toString())) {
				ai.setSetupStatus("AGENT SETUP IN PROGRESS.");
				ai.setAgentRegiFlag(1);
				jobEntity.setAgentInfo(ai);

				dao.updateAgentSetupStatus(ai);

				if (checkRunningSshService(jobEntity)) {
					log.info("item aeestCd({}) is already running ssh service for get information.", jobEntity.getAssetCd());
					continue;
				}
				INMEMORYDB.AGENTSETUPQUEUE.put(jobEntity.getAssetCd());

				// 에이전트 설치 큐에 등록
				INMEMORYDB.createRunningSetupJobList(jobEntity);
				log.debug("succeed in inserting item aeestCd({}) into RUNNINGSETUPJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
						jobEntity.getAssetCd(), INMEMORYDB.RUNNINGSETUPJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

			// 2. 에이전트 재시작 or 중지 or 업데이트
			} else if (jobType.equals(ManagerJobType.AJ601.toString())
					|| jobType.equals(ManagerJobType.AJ602.toString())
					|| jobType.equals(ManagerJobType.AJ603.toString())) {
				jobEntity.setAgentInfo(ai);

				// 에이전트 제어 큐에 등록
				INMEMORYDB.createRunningControlJobList(jobEntity);
				log.debug("succeed in inserting item aeestCd({}) into RUNNINGCONTROLJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
						jobEntity.getAssetCd(), INMEMORYDB.RUNNINGCONTROLJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
			}
		}
	}

	public AgentInfo initAgentInfoUpdate(String assetCd) throws Exception {
		AgentInfo agentInfo;

		try {
			agentInfo = dao.selectAgentInfoUpdate(assetCd);

			return agentInfo;
		} catch (NullPointerException e){
			throw new Exception("Agent 정보가 없습니다.");
		}

	}

	// .conf 파일 생성시 추가할 정보 있는지 확인
	public AgentInfo checkParmasForConfig(JobEntity jobEntity) throws Exception {

		return dao.selectConnectAuthInfo(jobEntity);
	}

	public AgentInfo initDiagAgentInfo(JobEntity jobEntity) throws Exception {
		AgentInfo agentInfo;

		try {
			agentInfo = dao.selectDiagAgentInfo(jobEntity);
			agentInfo.completeAgentInfo();

			AgentInfo checkAi = checkParmasForConfig(jobEntity);
			if (checkAi != null) {
				checkAi.chkParams();
				agentInfo.setParam1(checkAi.getParam1());
				agentInfo.setParam2(checkAi.getParam2());
				agentInfo.setParam3(checkAi.getParam3());
			}

			return agentInfo;
		} catch (NullPointerException e) {
			log.error("initDiagAgentInfo :: {}", e.getMessage(), e.fillInStackTrace());
			if(jobEntity.getSwType().equals("DB")) {
				throw new Exception(SnetCommonErrCode.ERR_0040.getMessage());
			}

			throw new Exception(SnetCommonErrCode.ERR_0041.getMessage());

		} catch (Exception e) {
			log.error("initDiagAgentInfo :: {}", e.getMessage(), e.fillInStackTrace());
			if(jobEntity.getSwType().equals("DB")) {
				throw new Exception(SnetCommonErrCode.ERR_0040.getMessage());
			}

			throw new Exception(SnetCommonErrCode.ERR_0041.getMessage());
		}

	}

	public Map<String, AgentInfo> initDiagAgentInfoList(List<JobEntity> jobEntities) throws Exception {
		try {
			Map<String, AgentInfo> agentInfoMap = new HashMap<>();
			for (JobEntity jobEntity: jobEntities) {
				AgentInfo agentInfo = dao.selectDiagAgentInfo(jobEntity);
				if (agentInfo.getAssetCd().isEmpty()) {
					throw new Exception("Agent 정보가 없습니다.");
				}
				agentInfoMap.put(agentInfo.getAssetCd(), agentInfo);
			}

			if (agentInfoMap.size() == 0) {
				throw new Exception("Agent 정보가 없습니다.");
			}

			return agentInfoMap;
		}catch (NullPointerException e){
			log.error("initDiagAgentInfoList :: {}", e.getMessage(), e.fillInStackTrace());
			throw new Exception("Agent 정보가 없습니다.");
		}catch (Exception e){
			log.error("initDiagAgentInfoList :: {}", e.getMessage(), e.fillInStackTrace());
			throw new Exception("Agent 정보가 없습니다.");
		}
	}
	
	public AgentInfo initAgentInfoForBF(String assetCd) throws Exception {
		AgentInfo agentInfo;

		agentInfo = dao.selectAgentInfo(assetCd);

		if(agentInfo != null){
			agentInfo.completeAgentInfo();
			return agentInfo;
		} else {
			agentInfo = dao.selectAgentInfoBF(assetCd);

			if (agentInfo != null){
				agentInfo.completeAgentInfo();
				return agentInfo;
			}else {
				throw new Exception("장비 코드를 확인해 주십시요");
			}
		}
	}

	public void initAgentInfoForBF(List<? extends IReq> reqS, String managerCd, String autoGet) throws Exception {
		List<AgentInfo> ais, notAis;
		List<String> notReqS = new ArrayList<>();

		if (reqS.size() <= 0) {
			String msg = "request List<GetReq> is empty.";
			log.error(msg);
			throw new Exception(msg);
		}

		HashMap<String, AgentInfo> installedMap = new HashMap<>();

		HashMap<String, Object> param = new HashMap<>();
		param.put("reqS", reqS);
		String recvedManagerCd = reqS.get(0).getPersonManagerCd();
		if (recvedManagerCd == null || recvedManagerCd.isEmpty()) {
			param.put("personManagerCd", managerCd);
			log.debug("ManagerCd is set by Server.");
		} else {
			param.put("personManagerCd", recvedManagerCd);
		}

		ais = dao.selectAgentInfoMulti(param);
		if (ais != null) {
			Map<String, IReq> reqsMap = null;
			if (autoGet.equals(INMEMORYDB.autoGet)) {
				reqsMap = reqS.stream().collect(Collectors.toMap(IReq::getAssetCd, c -> c, (c1, c2) -> c1));
			}

			for(AgentInfo agentInfo : ais) {
				if(agentInfo != null ) {
					agentInfo.completeAgentInfo();
					JobEntity jobEntity = initJobEntity(agentInfo, agentInfo.getManagerCd(), autoGet);
					if (jobEntity != null) {
						// 에이전트를 통한 장비 정보 수집 큐에 등록
						INMEMORYDB.createRunningGsJobList(jobEntity);
						log.debug("succeed in inserting item aeestCd({}) into RUNNINGGSJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
								jobEntity.getAssetCd(), INMEMORYDB.RUNNINGGSJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());

						installedMap.put(jobEntity.getAssetCd(), agentInfo);

						if (autoGet.equals(INMEMORYDB.autoGet)) {
							IReq req = reqsMap.get(jobEntity.getAssetCd());
							agentInfo.setJobType(ManagerJobType.AJ200.toString());
							agentInfo.setDgUserId(req.getManagerCd());
							agentInfo.setDgUserNm(req.getPersonManagerNm());
							agentInfo.setConnectLog(messageSourceAccessor.getMessage("asset.get.running", Locale.KOREA));
							dao.insertAgentGetJobHistory(agentInfo);
						}
					}
				}
			}
		}

        if (autoGet.equals(INMEMORYDB.autoGet)) {
            List<String> assetCdList = new ArrayList<>(installedMap.keySet());
            HashMap<String, Object> param2 = new HashMap<>();
            param2.put("connectLog", messageSourceAccessor.getMessage("asset.get.running", Locale.KOREA));
            param2.put("assetCdList", assetCdList);
            dao.updateConnectMasterConnectLogS(param2);
            log.debug("update connect log by auto getS.");
        }

		log.debug("reqS size - {}, ais size - {}", reqS.size(), ais.size());

		// agentCd가 없는 (즉 에이전트가 설치 되지 않은 장비) 자산이 있다면
		// ssh를 통한 장비 정보 수집인 경우 ssh 큐에 등록 하기 위해
		// assetCd로만 다시 조회.
		if (ais.size() < reqS.size()) {

			// agentCd가 없는 (즉 에이전트가 설치 되지 않은 장비) 자산 조회
			for (IReq req : reqS) {
				if (installedMap.containsKey(req.getAssetCd())) {
					continue;
				} else {
					notReqS.add(req.getAssetCd());
				}
			}

			param.clear();
			param.put("assetCdList", notReqS);
			param.put("personManagerCd", reqS.get(0).getPersonManagerCd());
			notAis = dao.selectAgentInfoBFMulti(param);
			for(AgentInfo agentInfo : notAis) {
				if(agentInfo != null ) {
					JobEntity jobEntity = null;
					try {

						agentInfo.completeAgentInfo();
						jobEntity = initJobEntity(agentInfo, agentInfo.getManagerCd(), autoGet);
						if (jobEntity != null) {
							if (checkRunningSshService(jobEntity)) {
								log.info("item aeestCd({}) is already running ssh service for get information.", jobEntity.getAssetCd());
								continue;
							}

							// 1. 차례로 ssh 접속하여 장비정보 수집
							INMEMORYDB.GETQUEUE.put(jobEntity.getAssetCd());
							log.debug("succeed in inserting item aeestCd({}) into GETQUEUE, size - {}",
									jobEntity.getAssetCd(), INMEMORYDB.GETQUEUE.size());
							// 2. 에이전트를 통한 장비 정보 수집 큐에 등록
							INMEMORYDB.createRunningGsJobList(jobEntity);
							log.debug("succeed in inserting item aeestCd({}) into RUNNINGGSJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
									jobEntity.getAssetCd(), INMEMORYDB.RUNNINGGSJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
						}
					} catch (Exception ex) {
						if (jobEntity == null) {
							jobEntity = new JobEntity();
						}

						jobEntity.setJobType(ManagerJobType.AJ200.toString());
						jobEntity.setAgentInfo(agentInfo);
						jobEntity.setAssetCd(agentInfo.getAssetCd());
						handleException(jobEntity, ex);
					}
				}
			}
		}
	}

	public void handleException(JobEntity jobEntity, Exception ex) {
		try {
			if (jobEntity.getJobType().equals(ManagerJobType.AJ200.toString())
					|| jobEntity.getJobType().equals(ManagerJobType.WM300.toString())) {
				throw new SnetException(dao, ex.getMessage(), jobEntity, "G");
			} else if (jobEntity.getJobType().equals(ManagerJobType.AJ100.toString())
					|| jobEntity.getJobType().equals(ManagerJobType.WM302.toString())
					|| jobEntity.getJobType().equals(ManagerJobType.WM102.toString())
					|| jobEntity.getJobType().equals(ManagerJobType.WM202.toString())) {
				throw new SnetException(dao, ex.getMessage(), jobEntity, "D");
			} else {  // WM103
				log.error(ex.getMessage());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	// ManagerJobType : WM300, WM302
	public void initAgentInfoForBFNw(List<? extends IReq> iReqS, String managerCd) throws Exception {
		List<String> notReqS = new ArrayList<>();

		HashMap<String, JobEntity> jobEntities = new HashMap<>();
		for (IReq req : iReqS) {
			String recvedManagerCd = getManagerCd(req, managerCd);
			req.setPersonManagerCd(recvedManagerCd);
			req.setManagerCd(recvedManagerCd);

			JobEntityInitData jobEntityInitData = new JobEntityInitData(req);
			jobEntities.put(req.getAssetCd(), jobEntityInitData.getJobEntity());
			notReqS.add(req.getAssetCd());
		}

		List<AgentInfo> notAis;
		HashMap<String, Object> param = new HashMap<>();
		param.put("assetCdList", notReqS);

		notAis = dao.selectAgentInfoBFMulti(param);
		for(AgentInfo agentInfo : notAis) {
			if(agentInfo != null ) {
				JobEntity jobEntity = null;

				try {
					agentInfo.completeAgentInfo();
					jobEntity = jobEntities.get(agentInfo.getAssetCd());
					if (jobEntity != null) {
						jobEntity.setAgentInfo(agentInfo);
						if (checkRunningSshService(jobEntity)) {
							log.info("item aeestCd({}) is already running ssh service for get information.", jobEntity.getAssetCd());
							continue;
					}

						// 1. 차례로 ssh 접속하여 장비정보 수집 혹은 진단
						INMEMORYDB.NETWORKQUEUE.put(jobEntity.getAssetCd());
						log.debug("succeed in inserting item aeestCd({}) into NETWORKQUEUE, size - {}",
								jobEntity.getAssetCd(), INMEMORYDB.NETWORKQUEUE.size());
						// 2. 장비 정보 수집 혹은 진단 분석 큐에 등록
						INMEMORYDB.createRunningNwJobList(jobEntity);
						log.debug("succeed in inserting item aeestCd({}) into RUNNINGNWJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
								jobEntity.getAssetCd(), INMEMORYDB.RUNNINGNWJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
					}
				} catch (Exception ex) {

					handleException(jobEntity, ex);
				}
			}
		}
	}

	// 현재 장비별로 ssh서비스가 실행중인지 체크.
	public boolean checkRunningSshService(JobEntity jobEntity) {
		if (INMEMORYDB.RUNNING_SSHJOBLIST.size() > 0) {
			for (Map.Entry<String, RunnigJobEntity> entry : INMEMORYDB.RUNNING_SSHJOBLIST.entrySet()) {
				RunnigJobEntity rJe = entry.getValue();
				if (rJe.getJobEntity().getAgentInfo().getConnectIpAddress().equals(jobEntity.getAgentInfo().getConnectIpAddress())) {
					String msg = "assetcd (" + jobEntity.getAssetCd() + ") and ip ("
							+ jobEntity.getAgentInfo().getConnectIpAddress() + "(is running ssh service.";
					log.debug(msg);

					return true;
				}
			}
		}

		return false;
	}

	/***
	 * GETSCRIPT
	 ****/
	public JobEntity initJobEntity(AgentInfo ai, String managerCd, String autoGet) throws Exception {
		JobEntity jobEntity = new JobEntity();
		jobEntity.setJobType(ManagerJobType.AJ200.toString());
		jobEntity.setReqType(ManagerJobType.AJ200.toString());
		jobEntity.setAssetCd(ai.getAssetCd());
		jobEntity.setManagerCode(managerCd);
		jobEntity.setAgentInfo(ai);
		jobEntity.setSDate(DateUtil.getCurrDateBySecond());

		if (StringUtils.isEmpty(managerCd) && autoGet.equals(INMEMORYDB.autoGet)) {
			String mgCd = dao.selectManagerCd(ai.getAssetCd());
			jobEntity.setManagerCode(mgCd);
		}

		// 파일 OTP 로직 -> 여러대 장비 동시 실행시 필요
		// 같은 파일이름(OTP)으로 파일 생성하면
		// 하나의 장비에 장비 정보 수집 완료시 파일 삭제할 때 문제가 됨. phil
		String fileName = CommonUtils.makeFileName();
		jobEntity.setSOTP(fileName);
		jobEntity.setFileName(fileName);
//		int otp = new Random().nextInt(900000)+100000;
//		String S_OTP = String.valueOf(otp);
//		jobEntity.setcOTP(S_OTP);

		SystemInfoEntity sie = new SystemInfoEntity();
		if (ai.getOsType().equals(INMEMORYDB.WIN_ID)) {
			String auditFileNm = INMEMORYDB.GetWindows + INMEMORYDB.JAR;
			jobEntity.setAuditFileName(auditFileNm);
			sie.setScript(auditFileNm);
		} else {
			String auditFileNm = INMEMORYDB.GetLinux + INMEMORYDB.SH;
			jobEntity.setAuditFileName(auditFileNm);
			sie.setScript(auditFileNm);
		}
		jobEntity.setAuditFileCd(dao.selectConfigAuditFileCd(sie));

		if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)){
			jobEntity.setFileType(INMEMORYDB.JAR);
		}else {
			jobEntity.setFileType(INMEMORYDB.SH);
		}

		jobEntity.setAgentGetprog(true);

		runJunAgent(jobEntity);

		log.debug("{} , {} , {} , {} ", jobEntity.getAssetCd(),	jobEntity.getAuditFileName(), jobEntity.getAuditFileCd(), jobEntity.getFileName());

		return jobEntity;
	}

	/***
	 * DIAGNOSIS
	 * AJ100
	 ****/
	public JobEntity initJobEntity(DiagnosisReq diagReq) throws Exception {

		JobEntity jobEntity = null;

		try {
			jobEntity = dao.selectJobInfo(diagReq.getAssetCd(), diagReq.getSwType(), diagReq.getSwNm(),
					diagReq.getSwInfo(), diagReq.getSwDir(), diagReq.getSwUser(), diagReq.getSwEtc());

			if (jobEntity == null) {
				throw new Exception("jobEntity is null");
			} else if (jobEntity.getAuditFileCd() == null || jobEntity.getAuditFileName() == null) {
				throw new Exception(SnetCommonErrCode.ERR_0012.getMessage());
			}

			// 진단 기준 추가
			if (jobEntity.getGovFlag() == null || jobEntity.getGovFlag().isEmpty()) {
				String govFlag = ConfigGlobalManager.getConfigGlobalValue("DefaultGovFlag");
				if (govFlag != null) {
					jobEntity.setGovFlag(govFlag);
				} else {
					jobEntity.setGovFlag("1");
				}
			}

			AgentInfo ai = initDiagAgentInfo(jobEntity);

			jobEntity.setJobType(ManagerJobType.AJ100.toString());
			jobEntity.setReqType(ManagerJobType.AJ100.toString());
			jobEntity.setSDate(DateUtil.getCurrDateBySecond());
			jobEntity.setAgentInfo(ai);

			jobEntity.setManagerCode(diagReq.getManagerCd());
			jobEntity.setAssetCd(diagReq.getAssetCd());
			jobEntity.setAgentJobSDate(DateUtil.getCurrDateBySecond());

            jobEntity.setGetForDiag(agentContextProperties.getGetForDiag());
            jobEntity.setPassWordType(agentContextProperties.getPassWordType());
			jobEntity.setPassWordOptionType(agentContextProperties.getPassWordOptionType());

			checkJunAgent(jobEntity);

			if (jobEntity.getSwNm().toUpperCase().contains(INMEMORYDB.WIN_ID)
					|| jobEntity.getSwNm().toUpperCase().contains("IIS")
					|| jobEntity.getSwNm().toUpperCase().contains("MSSQL")
					|| jobEntity.getAuditFileName().toUpperCase().endsWith(".ZIP")) {
				jobEntity.setFileType(INMEMORYDB.ZIP);
			} else {
				jobEntity.setFileType(INMEMORYDB.SH);
			}

			// 파일 OTP 로직 -> 여러대 장비 동시 실행시 필요
			// 같은 파일이름(OTP)으로 파일 생성하면
			// 하나의 장비에 장비 정보 수집 완료시 파일 삭제할 때 문제가 됨. phil
			String fileName = CommonUtils.makeFileName();
			jobEntity.setSOTP(fileName);
			jobEntity.setFileName(fileName);

			jobEntity.setAgentGetprog(false);

			// 수집 진단 분리.. WAS에서 수집/진단 분리인지 보내 주지만 매니저서버에서 한번 더 DB 체크
			if (diagReq.getDiagInfoUse() == null) {
				diagReq.setDiagInfoUse("N");
			}
			if (diagReq.getDiagInfoUse().toLowerCase().equals("true") || diagReq.getDiagInfoUse().toLowerCase().equals("y")) {
				jobEntity.setDiagInfoUse("Y");
			}

			if(!StringUtils.isEmpty(jobEntity.getDiagInfoUse())
					&& (jobEntity.getDiagInfoUse().toLowerCase().equals("y"))) {
					jobEntity.setDiagDir(INMEMORYDB.DIAG_INFO_FILE_MANAGER_DIR + jobEntity.getAssetCd());
			} else {
				jobEntity.setDiagInfoUse("N");
			}

			log.debug("create job entity for dianosis - {} , {} , {} , {} , {} , {} , {} , {}, {}, {}",
					jobEntity.getAssetCd(), jobEntity.getSwType(), jobEntity.getSwNm(),
					jobEntity.getSwInfo(), jobEntity.getSwDir(), jobEntity.getSwUser(),
					jobEntity.getSwEtc(), jobEntity.getFileName(), jobEntity.getDiagInfoUse(), jobEntity.getDiagDir());

			runJunAgent(jobEntity);

		/* OTP 체크 수행 X phil
		//최초에는 무조건 OTP수행.
		jobEntity.setJobType(ManagerJobFactory.OTP);
		jobEntity.setFileType(INMEMORYDB.OTPTYPE);
		*/

			log.debug("created the jobEntity successfully. ");

			return jobEntity;
		} catch (Exception ex) {
			String errMsg = ex.getMessage();
			if (jobEntity == null) {
				log.error("jobEntity is NULL.");
				errMsg = "진단기준 변경을 실행해주시기 바랍니다.";
			}
			jobEntity = new JobEntity();
			jobEntity.setAssetCd(diagReq.getAssetCd());
			jobEntity.setSwType(diagReq.getSwType());
			jobEntity.setSwNm(diagReq.getSwNm());
			jobEntity.setSwInfo(diagReq.getSwInfo());
			jobEntity.setSwDir(diagReq.getSwDir());
			jobEntity.setSwUser(diagReq.getSwUser());
			jobEntity.setSwEtc(diagReq.getSwEtc());
			jobEntity.setSOTP("");
			jobEntity.setJobType(diagReq.getJobType());

			errMsg = NotiType.AN077.toString() + errMsg;

			log.error("Asset Cd : "+jobEntity.getAssetCd());
			log.error(CommonUtils.printError(ex));
			throw new SnetException(dao, errMsg , jobEntity, "D");
		}
	}

	/***
	 * deprecated
	 * 일괄 처리
	 * REQTYPE : DIAG,SETUP,TEST
	 * phil
	 *
	 ****/
	public List<JobEntity> initJobEntity(List<DiagnosisReq> diagReqS) throws Exception {

		Map<String, AgentInfo> aiMap = null;
		HashMap<String, Object> param = new HashMap<>();
		param.put("diagReqs", diagReqS);
		List<JobEntity> jobEntities = dao.selectJobInfoList(param);
		if (jobEntities != null && jobEntities.size() > 0) {
			aiMap = initDiagAgentInfoList(jobEntities);
		} else {
			return null;
		}

		HashMap<String, DiagnosisReq> reqMap = new HashMap<>();
		for (DiagnosisReq req: diagReqS) {
			reqMap.put(req.getAssetCd(), req);
		}

		for (JobEntity jobEntity : jobEntities) {
			if(jobEntity == null)
				return null;
			else if(jobEntity.getAuditFileCd() == null || jobEntity.getAuditFileName() == null){
				throw new Exception(SnetCommonErrCode.ERR_0012.getMessage());
			}

			AgentInfo ai = aiMap.get(jobEntity.getAssetCd());
			DiagnosisReq diagReq = reqMap.get(jobEntity.getAssetCd());

			if (diagReq == null || ai == null) {
				throw new Exception("AgentInfo or DiagnosisReq is not set.");
			}

			jobEntity.setJobType(ManagerJobType.AJ100.toString());
			jobEntity.setReqType(ManagerJobType.AJ100.toString());
			jobEntity.setSDate(DateUtil.getCurrDateBySecond());
			jobEntity.setAgentInfo(ai);

			jobEntity.setManagerCode(diagReq.getManagerCd());
			jobEntity.setAssetCd(diagReq.getAssetCd());
			jobEntity.setAgentJobSDate(DateUtil.getCurrDateBySecond());

			checkJunAgent(jobEntity);

			if (jobEntity.getSwNm().toUpperCase().contains(INMEMORYDB.WIN_ID)
					|| jobEntity.getSwNm().toUpperCase().contains("IIS")
					|| jobEntity.getSwNm().toUpperCase().contains("MSSQL")
					|| jobEntity.getAuditFileName().toUpperCase().endsWith(".ZIP") ) {
				jobEntity.setFileType(INMEMORYDB.ZIP);
			} else {
				jobEntity.setFileType(INMEMORYDB.SH);
			}

			jobEntity.setAgentGetprog(false);

			log.debug("{} - {} - {} - {} - {} - {} - {}",
					jobEntity.getAssetCd(), jobEntity.getSwType(),
					jobEntity.getSwNm(), jobEntity.getSwInfo(),
					jobEntity.getSwDir(), jobEntity.getSwUser(), jobEntity.getSwEtc());

			runJunAgent(jobEntity);

		/* OTP 체크 수행 X phil
		//최초에는 무조건 OTP수행.
		jobEntity.setJobType(ManagerJobFactory.OTP);
		jobEntity.setFileType(INMEMORYDB.OTPTYPE);
		*/

			log.debug("created the jobEntity successfully. ");
		}


		return jobEntities;
	}

	/**
	 * EVENT 진단시 사용
	 * AJ101
	 */
	public JobEntity initEventJobEntity(String assetCd, String prgId, String swNm) throws Exception {

		JobEntity jobEntity = null;

		try {

			jobEntity = dao.selectEventJobInfo(assetCd, prgId);

			//긴급진단을 위한 기본값 추가
			jobEntity.setSwType("-");
			jobEntity.setSwNm(swNm);
			jobEntity.setSwUser("-");
			jobEntity.setSwDir("-");
			jobEntity.setSwEtc("-");
			jobEntity.setSwInfo("-");
			if (jobEntity.getAuditFileCd() == null || jobEntity.getAuditFileName() == null) {
				throw new Exception(SnetCommonErrCode.ERR_0012.getMessage());
			}

			AgentInfo ai = initDiagAgentInfo(jobEntity);
			jobEntity.setEventFlag("Y");
			jobEntity.setSDate(DateUtil.getCurrDateBySecond());

			jobEntity.setJobType(ManagerJobType.AJ101.toString());

			if (jobEntity.getSwNm().toUpperCase().contains(INMEMORYDB.WIN_ID)
					|| jobEntity.getSwNm().toUpperCase().contains("IIS")
					|| jobEntity.getSwNm().toUpperCase().contains("MSSQL")
					|| jobEntity.getAuditFileName().toUpperCase().endsWith(".ZIP")) {
				jobEntity.setFileType(INMEMORYDB.ZIP);
			} else {
				jobEntity.setFileType(INMEMORYDB.SH);
			}

			jobEntity.setAgentInfo(ai);

			jobEntity.setAssetCd(assetCd);
			jobEntity.setAgentJobSDate(DateUtil.getCurrDateBySecond());

			//OTP 통과 되었다치고..
			jobEntity.setSwNm(jobEntity.getAgentInfo().getOsType());
			String fileName = CommonUtils.makeFileName();
			jobEntity.setSOTP(fileName);
			jobEntity.setFileName(fileName);

			checkJunAgent(jobEntity);

			log.debug("created the event jobEntity successfully. ");

			return jobEntity;
		} catch (Exception ex) {

			jobEntity = new JobEntity();
			jobEntity.setAssetCd(assetCd);
			jobEntity.setSwNm(swNm);
			jobEntity.setPrgId(prgId);
			jobEntity.setJobType(ManagerJobType.AJ101.toString());

			log.error("Asset Cd : "+jobEntity.getAssetCd());
			log.error(CommonUtils.printError(ex));

			throw new SnetException(dao, ex.getMessage() , jobEntity, "E");
		}
	}

	// to do 독립 스레드 처리 필요 phil
	private void runJunAgent(JobEntity jobEntity) throws Exception {

		if (jobEntity != null) {
			//Agent Kill & UP  (준 에이전트 일 경우. 실제 사용 X)
			log.debug("Agent TYPE : "+jobEntity.getAgentInfo().getAgentType() );
			if(jobEntity.getAgentInfo().getAgentRegiFlag() == 2 &&
					!jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID)
					&& jobEntity.getAgentInfo().getAgentType() == 1) {
				jobEntity.setJobType(ManagerJobFactory.AGNTUP);
				remoteControlManager.runProcessUP(jobEntity);
				jobEntity.setJobType(jobEntity.getReqType());
			}
		}
	}

	private void checkJunAgent(JobEntity jobEntity) {
		if (jobEntity.getAgentInfo().getOsType().toUpperCase().contains(INMEMORYDB.WIN_ID) || jobEntity.getAgentInfo().getAgentType() == 2){
			jobEntity.setKillAgent(false);
		}
		else jobEntity.setKillAgent(true);
	}

	private String getManagerCd(IReq iReq, String managerCd) {
		String recvedManagerCd = "";
		if (iReq.getJobType().equals(ManagerJobType.WM300.toString())) {
			recvedManagerCd = iReq.getPersonManagerCd();
			if (StringUtils.isEmpty(recvedManagerCd)) {
				recvedManagerCd = managerCd;
				log.debug("ManagerCd is set by Server.");
			}
		} else {
			recvedManagerCd = iReq.getManagerCd();
			if (StringUtils.isEmpty(recvedManagerCd)) {
				recvedManagerCd = managerCd;
				log.debug("ManagerCd is set by Server.");
			}
		}

		return recvedManagerCd;
	}
}
