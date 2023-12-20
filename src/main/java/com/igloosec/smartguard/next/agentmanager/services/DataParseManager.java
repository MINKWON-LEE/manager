/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.DataParseManager.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 3.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.google.gson.Gson;
import com.igloosec.smartguard.next.agentmanager.component.GetScriptResultComponent;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;

import com.igloosec.smartguard.next.agentmanager.entity.*;
import com.igloosec.smartguard.next.agentmanager.exception.GetScriptException;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.property.AgentContextProperties;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import com.igloosec.smartguard.next.agentmanager.utils.FileUtils;
import jodd.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DataParseManager extends AbstractManager{

//	@Value("${snet.asset.swauditday.auto.url.use}")
//	private String autoUrlTypeUse = "false";

//	@Value("${snet.asset.swauditday.auto.url.swnm}")
//	private String autoUrlSwName = "sss";

	@Autowired
	private Dao dao;
	
	@Autowired
	private GetScriptResultComponent getScriptResultComponent;


	@Autowired
	private NotificationService notificationService;

	@Autowired
	private AgentContextProperties agentContextProperties;


	/**
	 * 장비 정보 수집 결과 파일을 파싱한다.
	 * @param jobEntity
	 * @throws Exception
	 */
	@Transactional
	public void gscriptResult(JobEntity jobEntity) throws Exception {

		String dir = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());
		String filePath = dir + jobEntity.getFileName()	+ jobEntity.getFileType();
		logger.debug("gscriptResult dir - {}, filePath - {}", dir, filePath);

		File file = new File(filePath);
		
		boolean getScriptFileValidation = gsResultFileValidation(file);
		
		if(!getScriptFileValidation){
			FileUtils.fileCopy(filePath,dir+jobEntity.getAssetCd()+jobEntity.getFileType());
			throw new GetScriptException(SnetCommonErrCode.ERR_0013.getMessage());
		}else{
			try {
				GscriptResultEntity resultEntity = getScriptResultComponent.getResultEntityToFile(file);

				//WAS 로그인한 매니저CD 가져와서 넣어주는 로직 추가
				if(jobEntity.getManagerCode() != null){
					resultEntity.setManagerCd(jobEntity.getManagerCode());
				}

				logger.debug("Get Script Result :: {}", new Gson().toJson(resultEntity));
				
				if (jobEntity.getAgentInfo().getAssetCd() != null) {
					resultEntity.setAssetCd(jobEntity.getAgentInfo().getAssetCd());
				}

				// NW 장비가 아닌 것만 사용자 정보를 등록한다
				// NW 장비만 사용자 등록안되는 것이 이상해서 사용자정보 등록되도록 수정함. : 2018-09-06
				ConfigUserViewDBEntity userViewEntity = dao.selectConfigUserView(resultEntity);
				if(userViewEntity!=null)
					resultEntity.setUserViewDBEntity(userViewEntity);
				else{
					//관리자 계정 넣어줌.
					userViewEntity = dao.selectAgentManualSetupConfigUserView();
					if(userViewEntity != null){
						resultEntity.setUserViewDBEntity(userViewEntity);
					}else{
						throw new GetScriptException(SnetCommonErrCode.ERR_0023.getMessage());
					}

				}

				// IP가 없을 경우 EXCEPTION 발생
				if(resultEntity.getListAssetIp() == null || resultEntity.getListAssetIp().size() == 0 ){
					logger.error(SnetCommonErrCode.ERR_0027.getMessage());
					throw new GetScriptException(SnetCommonErrCode.ERR_0027.getMessage());
				}

				// HOSTNAME 없을 경우 EXCEPTION 발생
				if(resultEntity.getAssetMasterDBEntity() == null || resultEntity.getAssetMasterDBEntity().getHostNm().equals("-") || resultEntity.getAssetMasterDBEntity().getHostNm().isEmpty()){
					logger.error(SnetCommonErrCode.ERR_0027.getMessage());
					throw new GetScriptException(SnetCommonErrCode.ERR_0027.getMessage());
				}

				if(!containSwType(resultEntity.getListSwAuditDay(), "NW")){

					// TODO: 2018-06-28
					// 에이전트 수동설치시 관리자계정으로 장비등록 기능 변경 기능 추가
					// 수동설치시 managerCd 값 최소 관리자 넘김
					// http://jira.igloosec.com:8080/browse/SGM-164
					if (jobEntity.isAgentManualSetup()) {
						logger.debug("manually installed agent.");
					// 지금 아래 코드는 주석처리 (자동설치시에도 중복처리를 막던지 아니면 수동설치나 자동설치시 둘다 풀어주던지 해야됨.)
					// 수동설치시에는 IP, hostNm을 중복체크하여 현존하는 장비가 있다면 assetCd를 가져다 쓰는 코드
//						HashMap<String, Object> manualParam = new HashMap<>();
//						List<String> ipAddressList = new ArrayList<String>();
//
//						for(AssetIpDBEntity assetIpDBEntity : resultEntity.getListAssetIp()){
//							ipAddressList.add(assetIpDBEntity.getIpAddress());
//						}
//
//						manualParam.put("ipAddressList",ipAddressList);
//						manualParam.put("hostNm",resultEntity.getAssetMasterDBEntity().getHostNm());
//
//						int selectAgentManualSetupCount = dao.selectAgentManualSetupCount(manualParam);
//
//						if(selectAgentManualSetupCount > 0){
//							//수동 설치시 Hostname, IP 중복 있을 경우 예외처리
//							List<String> assetCdList = dao.setAssetCd(resultEntity);
//							if (assetCdList.size() == 1){
//								resultEntity.setAssetCd(assetCdList.get(0));
//							}else
//								throw new SnetException("수동 설치시 IP 중복 있을 경우 예외처리.");
//						}
					}
				} else {
					
					// NW 장비일 경우 WEB진단은 제외한다.
					List<SwAuditDayDBEntity> newSwAuditDb = new ArrayList<SwAuditDayDBEntity>();
					for(SwAuditDayDBEntity swAudit : resultEntity.getListSwAuditDay()){
						if(!swAudit.getSwType().equals("WEB"))
							newSwAuditDb.add(swAudit);
					}
					resultEntity.setListSwAuditDay(newSwAuditDb);
				}

				/**
				 * hostname, ip 중복 허용하도록 수정
				 */
				logger.debug("Unique Asset Registration Process.... ASSET_CD :: {}", resultEntity.getAssetCd());
				int assetMasterchk = 0;
				AssetMasterDBEntity assetMasterDBEntity = new AssetMasterDBEntity();
				assetMasterDBEntity.setAssetCd(resultEntity.getAssetCd());
				assetMasterchk = dao.selectAssetMasterchk(resultEntity.getAssetCd());

				if (resultEntity.getcOtp() == null && jobEntity.getJobType().equals(ManagerJobType.AJ201.toString())) {
					logger.debug("GSBFAGENT");
					dealingWithResults(assetMasterchk, resultEntity, assetMasterDBEntity, jobEntity);
				} else {
					logger.debug("NOT GSBFAGENT");
					dealingWithResults(assetMasterchk, resultEntity, assetMasterDBEntity, jobEntity);
					updateAssetMaster(assetMasterDBEntity);
				}

			} catch (GetScriptException e) {
				logger.error(e.getMessage(), e);
				throw new GetScriptException(e.getMessage());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				FileUtils.fileCopy(filePath,dir+jobEntity.getAssetCd()+jobEntity.getFileType());
				throw new SnetException(e.getMessage());
			} 
		}
	}

	/**
	 * 장비 정보 수집 결과를 처리한다.
	 * @param assetMasterchk
	 * @param resultEntity
	 * @param assetMasterDBEntity
	 * @param jobEntity
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private void dealingWithResults(int assetMasterchk,	GscriptResultEntity resultEntity,
									AssetMasterDBEntity assetMasterDBEntity, JobEntity jobEntity) throws Exception {

		SnetAssetGetHistoryModel assetGetHistoryOrgModel = null;
		//장비 이력 조회
		assetGetHistoryOrgModel = dao.selectSnetAssetGetHistory(resultEntity.getAssetCd());

		setAssetMaster(assetMasterchk, resultEntity, assetMasterDBEntity);
		assetMasterDBEntity = dao.selectAssetMaster(resultEntity.getAssetCd());
		
		logger.debug("GscriptResultEntity_ORG :: {} ", new Gson().toJson(resultEntity));

		// 장비정보 수집 - 정보 수집 예외 장비 체크
		Map<String, String> exceptList = new HashMap<>();
		List<Map> recs = dao.selectSoftWareExcept();
		for (Map rec : recs) {
			exceptList.put(rec.get("SW_NM").toString().toLowerCase(), rec.get("SW_ID").toString().toLowerCase());
		}

		List<SwAuditDayDBEntity> swList = resultEntity.getListSwAuditDay();
		String osKind = "";
		boolean urlTypeExist = false;
		boolean webWasTypeExist = false;

		for (Iterator<SwAuditDayDBEntity> it = swList.iterator(); it.hasNext();) {
			SwAuditDayDBEntity swe = it.next();

			// 수집 결과 DAT에서 sw_info, sw_dir, sw_etc, sw_user 값이 비어서 오면 "-" 값으로 채우기
			if (StringUtils.isEmpty(swe.getSwDir())) {
				swe.setSwDir("-");
			}
			if (StringUtils.isEmpty(swe.getSwInfo())) {
				swe.setSwInfo("-");
			}
			if (StringUtils.isEmpty(swe.getSwEtc())) {
				swe.setSwEtc("-");
			}
			if (StringUtils.isEmpty(swe.getSwUser())) {
				swe.setSwUser("-");
			}

			if("OS".equalsIgnoreCase(swe.getSwType())){
				osKind = swe.getSwNm();
			}

			if (exceptList.size() > 0 && !swe.getSwNm().isEmpty()) {
				if (exceptList.get(swe.getSwNm().toLowerCase()) != null) {
					logger.debug("except equipment item : {}", swe.getSwNm());
					it.remove();
				}
			}

			if (agentContextProperties.getAutoUrlTypeUse().equals("true")) {
				if ((swe.getSwType().equals("WEB")) || (swe.getSwType().equals("WAS"))) {
					webWasTypeExist = true;
				}
				if ((swe.getSwType().equals("URL"))) {
					urlTypeExist = true;
				}
			}
		}

		if (webWasTypeExist && !urlTypeExist) {
			SwAuditDayDBEntity swe = new SwAuditDayDBEntity();
			swe.setSwType("URL");
			swe.setAssetCd(resultEntity.getAssetCd());
			swe.setSwInfo("http://ip");
			swe.setSwNm(agentContextProperties.getAutoUrlSwName());
			swe.setAuditDay(DateUtil.getCurrDate());

			swList.add(swe);

			logger.debug("added Url Type swAuditDay item: Url {}", resultEntity.getAssetCd());
		}

		for (SwAuditDayDBEntity auditDayDBEntity : resultEntity.getListSwAuditDay()) {

			auditDayDBEntity.setAssetCd(resultEntity.getAssetCd());
			auditDayDBEntity.setFileType(assetMasterDBEntity.getGovFlag());
			auditDayDBEntity.setOsKind(osKind);

			List<String> strings = new ArrayList<>();

			boolean forcedAcd = false;
			try {
				int replaceDbInf = 0;
				String orgMsSqlInfo = auditDayDBEntity.getSwInfo();
				if ((auditDayDBEntity.getFileType() == 1) && auditDayDBEntity.getSwNm().equalsIgnoreCase("MSSQL")) {
					String temp = CommonUtils.replaceOneOfAll(orgMsSqlInfo, "2012", "2016", "2017", "2019", "2020", "2021", "2022");
					auditDayDBEntity.setSwInfo(temp);
					replaceDbInf = 1;
				}

				forcedAcd = setTempFromSwInfo(auditDayDBEntity);

				strings = dao.selectConfigAuditFile(auditDayDBEntity);
				if (auditDayDBEntity.getFileType() != 1 && strings.size() == 0){
					auditDayDBEntity.setFileType(1);
					strings = dao.selectConfigAuditFile(auditDayDBEntity);
				}

				if (replaceDbInf == 1) {
					auditDayDBEntity.setSwInfo(orgMsSqlInfo);
				}

			}catch (Exception e){
				logger.error("??");
			}

			List<Map> auditFileCd = dao.selectAuditDayFileCd(auditDayDBEntity);

			if (auditFileCd.size() > 0) {

				for(Map map : auditFileCd){
					// CVE 관련 정보 유지되도록 수정
					auditDayDBEntity.setCveCode(map.get("CVE_CODE") == null ? null : map.get("CVE_CODE").toString());
					auditDayDBEntity.setCveCodeCnt(map.get("CVE_CODE_CNT") == null ? null : Integer.parseInt(map.get("CVE_CODE_CNT").toString()));
					auditDayDBEntity.setCveUpdateDate(map.get("CVE_UPDATE_DATE") == null ? null : map.get("CVE_UPDATE_DATE").toString());
					auditDayDBEntity.setAssetName(map.get("ASSET_NAME") == null ? null : map.get("ASSET_NAME").toString());

					if(map.containsValue(auditDayDBEntity.getSwType())){
						try {
							String fileCd = map.get("AUDIT_FILE_CD").toString();
							if(fileCd == null){
								auditDayDBEntity.setAuditFileCd("");
							}else {
								if (fileCd.isEmpty() && strings.size() > 0) {
									logger.debug("check adding new audit_file_cd.");
									auditDayDBEntity.setAuditFileCd(strings.get(0));
								} else {
									auditDayDBEntity.setAuditFileCd(map.get("AUDIT_FILE_CD").toString());
								}
							}
						}catch (NullPointerException e){auditDayDBEntity.setAuditFileCd("");}
						try {
							auditDayDBEntity.setSwOperator(map.get("SW_OPERATOR").toString());
						}catch (NullPointerException e){auditDayDBEntity.setSwOperator("");}
					}
				}
			} else if (auditFileCd.size() == 0 && strings.size() > 0 ) {
				auditDayDBEntity.setAuditFileCd(strings.get(0));
			} else if (forcedAcd && StringUtils.isNotEmpty(auditDayDBEntity.getTemp()) && strings.size() > 0 ) {
				auditDayDBEntity.setAuditFileCd(strings.get(0));
			} else {
				auditDayDBEntity.setAuditFileCd("");
			}

		}

		List<SnetAssetSwChangeHistoryModel> assetSwOrgList = null;
		//자산정보변경이력 삭제 전 리스트
		assetSwOrgList = dao.selectAssetSwList(resultEntity.getAssetCd());

		// 2022-04-06 매니저서버에선 SNET_ASSET_USER 팀,부서 데이터는 업데이트 하지 않음
		// 신규 장비등록시에만 인서트함.
//		if(resultEntity.getUserViewDBEntity()!=null)
//			dao.deleteAssetUser(resultEntity);
		dao.deleteSwAuditDay(resultEntity);
		dao.deleteAssetIp(resultEntity);
		dao.deleteOpenPort(resultEntity);


		List<SwAuditDayDBEntity> orgDbEntity = new ArrayList<SwAuditDayDBEntity>();
		List<SwAuditDayDBEntity> newDbEntity = new ArrayList<SwAuditDayDBEntity>();
		
		/*
		 * 사용자 입력 정보 제외
		 * - 사용자가 입력한 정보를 제외하고 데이터 생성
		 * 	2018.06.11 이상준 SW_DIR, SW_ETC 값 수동 설정시 입력값 제외 정보 추가
		 */
		for (SwAuditDayDBEntity auditDayDBEntity : resultEntity.getListSwAuditDay()) {
			
			SwAuditDayDBEntity newEntity = new SwAuditDayDBEntity();
			newEntity = auditDayDBEntity;

			auditDayDBEntity.setAssetCd(resultEntity.getAssetCd());
			String auditDay = dao.selectSwAuditHistory(auditDayDBEntity);
			
			logger.debug("selectSwAuditHistory auditDay :: {}", auditDay);
			auditDayDBEntity.setAuditDay(auditDay !=null ? auditDay : "19990101");

			try {
				if(dao.selectAssetSwAuditDayUserRegi(auditDayDBEntity)>0)
					logger.debug("Remove entity..");
				else
					newDbEntity.add(newEntity);
			}catch (Exception e){}
		}
		orgDbEntity = resultEntity.getListSwAuditDay();
		
		resultEntity.setListSwAuditDay(newDbEntity);

		try {
			dao.insertGSResult(assetMasterchk, resultEntity.getAssetCd(), resultEntity);

			//장비자산이력
			setAssetChangeHistory(resultEntity, jobEntity, assetGetHistoryOrgModel, assetSwOrgList);
		} catch (Exception ex) {
			if (ex instanceof DataAccessException) {
				throw new SnetException(SnetCommonErrCode.ERR_0024.getMessage());
			}
		}

		int chkConnectMaster = 0;
		chkConnectMaster = dao.selectChkConnectMaster(resultEntity.getAssetCd());

		/*
		 * Original data로 복구
		 */
//		resultEntity.setListSwAuditDay(orgDbEntity);
		if (chkConnectMaster != 0) {
			for(SwAuditDayDBEntity list : orgDbEntity) {
				if(list.getSwType().equalsIgnoreCase("OS") || list.getSwType().equalsIgnoreCase("NW")){
					List<SwAuditDayDBEntity> swAuditDaylist = new ArrayList<>();
					swAuditDaylist.add(list);
					
					GscriptResultEntity resultsEntityConnect;
					resultsEntityConnect = (GscriptResultEntity) resultEntity.clone();

					resultsEntityConnect.setListSwAuditDay(swAuditDaylist);
					resultsEntityConnect.setAssetCd(resultEntity.getAssetCd());

					logger.debug("장비정보 수집 Asset_cd : "+resultsEntityConnect.getAssetCd());
					logger.debug("Connection Log :: {}", resultsEntityConnect.getConnectLog());
					
					dao.updateConnectMaster(resultsEntityConnect);
					if(resultsEntityConnect.getConnectLog() == null)
						if (list.getSwType().equalsIgnoreCase("NW") && INMEMORYDB.nwRunDg.toUpperCase().equals("Y") ) {
							resultsEntityConnect.setConnectLog("success (diagnose continuously.)");
						} else {
							resultsEntityConnect.setConnectLog("success");
						}
					dao.updateConnectMasterLog(resultsEntityConnect);
				}
			}
		} else {
			//SNET_CONNECT_MASTER에 Get 정보 Insert 할때 swNm을 추가하기 위해 SwAuditDaylist의 리스트를 getSwType == OS 인 항목만 추가하여 파라미터로 넘긴다.
			for(SwAuditDayDBEntity list : orgDbEntity) {
				if (list.getSwType().equalsIgnoreCase("OS") || list.getSwType().equalsIgnoreCase("NW")) {
					List<SwAuditDayDBEntity> SwAuditDaylist = new ArrayList<>();
					SwAuditDaylist.add(list);

					GscriptResultEntity resultsEntityConnect;
					resultsEntityConnect = (GscriptResultEntity) resultEntity.clone();

					resultsEntityConnect.setListSwAuditDay(SwAuditDaylist);

					try {
						dao.insertConnectMaster(resultsEntityConnect);
					} catch (Exception ex) {
						if (ex instanceof DataAccessException) {
							throw new SnetException(SnetCommonErrCode.ERR_0024.getMessage());
						}
					}
				}
			}
		}

		if ( jobEntity.getJobType().equals(ManagerJobType.AJ201.toString()) ) {
			assetMasterDBEntity.setDgUserId(resultEntity.getUserViewDBEntity().getUserId());
			assetMasterDBEntity.setDgUserNm(resultEntity.getUserViewDBEntity().getUserNm());
			dao.insertAgentGetJobHistoryByManIns(assetMasterDBEntity);
		} else {
			jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
			jobEntity.setAgentJobFlag(3);
			jobEntity.setAgentJobDesc("SUCCESS");
			String getSeq = dao.selectAgentGetJobHistory(jobEntity.getAssetCd());
			jobEntity.setGetSeq(Integer.parseInt(getSeq));
			dao.updateAgentGetJobHistorySuccess(jobEntity);
			jobEntity.setAgentJobDesc("Already Finished. ( " + getSeq + " )");
			dao.updateAgentGetJobHistoryOtherSuccess(jobEntity);
		}

		// save shadow file
		fileOutShadow(resultEntity.getAssetCd(), resultEntity.getShadow());
	}

	/**
	 * 장비정보수집/자산변경이력
	 */
	public void setAssetChangeHistory(GscriptResultEntity resultEntity, JobEntity jobEntity, SnetAssetGetHistoryModel assetGetHistoryOrgModel, List<SnetAssetSwChangeHistoryModel> assetSwOrgList) throws Exception {

		//장비정보수집일자 업데이트 GET_DAY
		dao.updateAssetMasterGetDay(resultEntity.getAssetCd());

		SnetAssetGetHistoryModel snetAssetGetHistoryModel = dao.selectSnetAssetGetHistory(resultEntity.getAssetCd());

		//장비정보가 없을 경우 이력 처리 불가
		if(snetAssetGetHistoryModel == null){
			return;
		}
		String getType = "";
		//수동
		if(jobEntity.getJobType().startsWith("W")){
			getType = "2";

		}else {
			getType = "1";
		}

		snetAssetGetHistoryModel.setGetType(getType);

		//자산수집이력 존재 여부 확인
		//자산수집이력 내역이 없을 경우 현재 데이터를 저장
		if(assetGetHistoryOrgModel != null){
			if(StringUtil.isEmpty(assetGetHistoryOrgModel.getGetDay())){
				//장비 수집 이력 데이터 등록
				assetGetHistoryOrgModel.setGetDay(assetGetHistoryOrgModel.getMasterUpdateDate());
				assetGetHistoryOrgModel.setGetType(getType);

				dao.insertSnetAssetGetHistory(assetGetHistoryOrgModel);

				//자산 이력 데이터 등록
				if(assetSwOrgList != null && !assetSwOrgList.isEmpty()){
					for (SnetAssetSwChangeHistoryModel data : assetSwOrgList){
						data.setGetDay(assetGetHistoryOrgModel.getGetDay());
						data.setChangeType("1");
					}
					dao.insertAssetSwChangeList(assetSwOrgList);
				}
			}
		}

		if(snetAssetGetHistoryModel != null){
			if(StringUtil.isEmpty(snetAssetGetHistoryModel.getGetDay())){
				//장비 수집 이력 데이터 등록
				snetAssetGetHistoryModel.setGetDay(snetAssetGetHistoryModel.getMasterUpdateDate());
			}
		}

		//장비정보수집이력
		dao.insertSnetAssetGetHistory(snetAssetGetHistoryModel);

		//자산변경 이력
		List<SnetAssetSwChangeHistoryModel> assetSwChangeList = new ArrayList<>();

		List<SnetAssetSwChangeHistoryModel> assetSwNewList = dao.selectAssetSwList(resultEntity.getAssetCd());
		if(assetSwOrgList == null){
			//기존 데이터 없을 경우 new List 등록
			for (SnetAssetSwChangeHistoryModel data : assetSwNewList){
				data.setGetDay(snetAssetGetHistoryModel.getGetDay());
				data.setChangeType("1");
			}

			assetSwChangeList.addAll(assetSwNewList);
		}else {
			// 삭제
			assetSwChangeList.addAll(assetSwOrgList.stream().filter(d-> assetSwNewList.stream().noneMatch(d2 -> {
				return d2.toString().equals(d.toString());
			})).map(d->{
				d.setChangeType("2");
				d.setGetDay(snetAssetGetHistoryModel.getGetDay());
				return d;
			}).collect(Collectors.toList()));

			// 추가
			assetSwChangeList.addAll(assetSwNewList.stream().filter(d-> assetSwOrgList.stream().noneMatch(d2 -> {
				return d2.toString().equals(d.toString());
			})).map(d->{
				d.setChangeType("1");
				d.setGetDay(snetAssetGetHistoryModel.getGetDay());
				return d;
			}).collect(Collectors.toList()));
		}

		if(assetSwChangeList != null && !assetSwChangeList.isEmpty()){
			dao.insertAssetSwChangeList(assetSwChangeList);
		}
	}

	/**
	 * 진단 결과 XML 데이터 수집후 진단율 계산 후 DB에 저장한다.
	 * @param jobEntity
	 * @throws Exception
	 */
	@Transactional
	public void dscriptResult(JobEntity jobEntity) throws Exception {
		String dir = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV,
				jobEntity.getJobType());
		String filePath = dir.concat(jobEntity.getFileName())
				.concat(jobEntity.getFileType());

		String xml = readFile(filePath);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		InputStream xmlInput = new ByteArrayInputStream(xml.getBytes());
		SAXParser saxParser = factory.newSAXParser();
		DscriptResultSaxHandler dscriptResultSaxHandler = new DscriptResultSaxHandler();
		saxParser.parse(xmlInput, dscriptResultSaxHandler);

		try{
			xmlInput.close();
		}catch(Exception e){
			logger.error(e.getMessage());
		}

		DscriptResultEntity dscriptResultEntity = dscriptResultSaxHandler.result;
		List<String> assetCdList = new ArrayList<>();//dao.setDscriptAssetCd(dscriptResultEntity.getSystemInfoEntity());

		// 자동일 때도 xml과 sw_audit_day 데이터 키값들 맞춰보고 실행을 위해 아래 if문 주석 처리
		//수동 업로드 일경우 AssetCd 검색
	//	if (jobEntity.getJobType().equals(ManagerJobType.WM102.toString()) || jobEntity.getJobType().equals(ManagerJobType.WM103.toString())
	//	    || jobEntity.getJobType().equals(ManagerJobType.WM202.toString())) {

		// 진단 결과 XML에서 sw_info, sw_dir, sw_etc, sw_user 값이 비어서 오면 "-" 값으로 채우기
		if (StringUtils.isEmpty(dscriptResultEntity.getSystemInfoEntity().getSwInfo())) {
			dscriptResultEntity.getSystemInfoEntity().setSwInfo("-");
		}
		if (StringUtils.isEmpty(dscriptResultEntity.getSystemInfoEntity().getSwDir())) {
			dscriptResultEntity.getSystemInfoEntity().setSwDir("-");
		}
		if (StringUtils.isEmpty(dscriptResultEntity.getSystemInfoEntity().getSwEtc())) {
			dscriptResultEntity.getSystemInfoEntity().setSwEtc("-");
		}
		if (StringUtils.isEmpty(dscriptResultEntity.getSystemInfoEntity().getSwUser())) {
			dscriptResultEntity.getSystemInfoEntity().setSwUser("-");
		}

			if (jobEntity.getAssetCd() == null) {
				//AssetCd 값이 UI 에서 받지 못했을 경우 AssetCd 검색
				assetCdList = dao.setDscriptAssetCd(dscriptResultEntity.getSystemInfoEntity());
				jobEntity.setHostNm(dscriptResultEntity.getSystemInfoEntity().getHostNm());
			} else {
				//2018.08.01 이상준
				//수동업로드시 AssetCd 를 받으면 대상장비와 맞는지 체크 후 수동업로드 진행.
				JobEntity manualJobEntity = jobEntity;

				HashMap<String, String> param = new HashMap<>();

				param.put("assetCd",jobEntity.getAssetCd());
				param.put("swType",dscriptResultEntity.getSystemInfoEntity().getSwType());
				param.put("swNm",dscriptResultEntity.getSystemInfoEntity().getSwNm());
				param.put("swInfo",dscriptResultEntity.getSystemInfoEntity().getSwInfo());
				param.put("swDir",dscriptResultEntity.getSystemInfoEntity().getSwDir());
				param.put("swUser",dscriptResultEntity.getSystemInfoEntity().getSwUser());
				param.put("swEtc",dscriptResultEntity.getSystemInfoEntity().getSwEtc());
				String xmlFileIpAddres = dscriptResultEntity.getSystemInfoEntity().getIpAddress();
				String xmlFileHostNm = dscriptResultEntity.getSystemInfoEntity().getHostNm();

				//1. ASSET_CD, SW_TYPE, SW_NM, SW_INFO, SW_DIR, SW_USER, SW_ETC 비교
				int selectAgentAssetCdCountCheck = dao.selectAgentAssetCdCountCheck(param);
				if(selectAgentAssetCdCountCheck == 1){
					assetCdList.add(jobEntity.getAssetCd());
				}else{
					String comp = String.format("\n선택된 장비 : 1. %s, 2. %s, 3. %s, 4. %s, 5. %s, 6. %s\n" +
									"진단된 장비 : 1. %s, 2. %s, 3. %s, 4. %s, 5. %s, 6. %s",
							jobEntity.getSwType(), jobEntity.getSwNm(), jobEntity.getSwInfo(), jobEntity.getSwDir(), jobEntity.getSwUser(), jobEntity.getSwEtc(),
							param.get("swType"), param.get("swNm"), param.get("swInfo"), param.get("swDir"), param.get("swUser"), param.get("swEtc"));
					throw new Exception(SnetCommonErrCode.ERR_0038.getMessage() + comp);
				}

				//2. hostNm, Ip 비교
				if (jobEntity.getJobType().equals(ManagerJobType.WM102.toString()) || jobEntity.getJobType().equals(ManagerJobType.WM103.toString())) {
					if (StringUtils.isEmpty(xmlFileHostNm) || StringUtils.isEmpty(xmlFileIpAddres)) {
						xmlFileHostNm = "empty";
						xmlFileIpAddres = "empty";
					}
					if (xmlFileHostNm.toLowerCase().equals(jobEntity.getHostNm().toLowerCase()) && xmlFileIpAddres.contains(jobEntity.getIpAddres())) {
						logger.debug("checked ip and hostname.");
					} else {
						String comp = String.format("\n선택된 장비 : Ip - %s, 호스트 이름 - %s, \n진단된 장비 : Ip - %s, 호스트 이름 - %s",
								jobEntity.getIpAddres(), jobEntity.getHostNm(), xmlFileIpAddres, xmlFileHostNm);
						throw new Exception(SnetCommonErrCode.ERR_0038.getMessage() + comp);
					}
				}
			}

	//	} else {
			// assetcd 기준으로 진단결과를 저장하도록 수정.
	//		assetCdList.add(jobEntity.getAgentInfo().getAssetCd());
	//	}


		if (assetCdList.size() == 0) {
			throw new SnetException(SnetCommonErrCode.ERR_0014.getMessage());
		} else if(assetCdList.size() > 0) {

			SystemInfoEntity systemInfoEntity = dscriptResultEntity.getSystemInfoEntity();
			String auditDay = resolveAuditDay(systemInfoEntity.getDate());
			dscriptResultEntity.setAuditDay( auditDay );

			/*
			 * 2016.11.16
			 * - 이상대 선임 의견으로 수용함
			 * 동일한 장비일 경우 해당 ASSET_CD별로 데이터를 넣어준다.
			 */
			int manualCount = 0;

			for(String assetCd : assetCdList){
				//수동 업로드일시 assectCd 카운트 체크
				dscriptResultEntity.setManualAssetCount(manualCount);

				jobEntity.setAssetCd(assetCd);
				dscriptResultEntity.setAssetCd(assetCd);


				AssetMasterDBEntity assetMasterDBEntity;
				SwAuditHistoryDBEntity auditHistoryDBEntity = new SwAuditHistoryDBEntity();
				assetMasterDBEntity = dao.selectAssetMaster(dscriptResultEntity.getAssetCd());
//				dscriptResultEntity.setAuditDay(populateAuditDay(systemInfoEntity.getDate()));
				assetMasterDBEntity.setAuditDay(resolveMasterAuditDay(auditDay, assetMasterDBEntity));
				dscriptResultEntity.getSystemInfoEntity().setSwInfo(dscriptResultEntity.getSystemInfoEntity().getSwInfo() != null && !dscriptResultEntity.getSystemInfoEntity().getSwInfo().isEmpty() ?
						dscriptResultEntity.getSystemInfoEntity().getSwInfo() : "-");

				if (assetMasterDBEntity != null) {
					dscriptResultEntity.getSystemInfoEntity().setHostNm(assetMasterDBEntity.getHostNm());
					auditHistoryDBEntity.setBranchId(assetMasterDBEntity.getBranchId());
					auditHistoryDBEntity.setBranchNm(assetMasterDBEntity.getBranchNm());
					auditHistoryDBEntity.setTeamId(assetMasterDBEntity.getTeamId());
					auditHistoryDBEntity.setTeamNm(assetMasterDBEntity.getTeamNm());
					dscriptResultEntity.getSystemInfoEntity().setIpAddress(assetMasterDBEntity.getIpAddress());
				}

				auditHistoryDBEntity.setAssetCd(dscriptResultEntity.getAssetCd());
				auditHistoryDBEntity.setSwType(dscriptResultEntity.getSystemInfoEntity().getSwType());
				auditHistoryDBEntity.setSwNm(dscriptResultEntity.getSystemInfoEntity().getSwNm());
				auditHistoryDBEntity.setSwInfo(
						dscriptResultEntity.getSystemInfoEntity().getSwInfo() != null && !dscriptResultEntity.getSystemInfoEntity().getSwInfo().isEmpty() ?
								dscriptResultEntity.getSystemInfoEntity().getSwInfo() : "-");

				// 2016.11.17 추가됨
				auditHistoryDBEntity.setSwDir(dscriptResultEntity.getSystemInfoEntity().getSwDir() !=null && !dscriptResultEntity.getSystemInfoEntity().getSwDir().isEmpty()
						? dscriptResultEntity.getSystemInfoEntity().getSwDir() :"-");
				auditHistoryDBEntity.setSwUser(dscriptResultEntity.getSystemInfoEntity().getSwUser() !=null && !dscriptResultEntity.getSystemInfoEntity().getSwUser().isEmpty()
						? dscriptResultEntity.getSystemInfoEntity().getSwUser() : "-");
				auditHistoryDBEntity.setSwEtc(dscriptResultEntity.getSystemInfoEntity().getSwEtc() !=null && !dscriptResultEntity.getSystemInfoEntity().getSwEtc().isEmpty()
						? dscriptResultEntity.getSystemInfoEntity().getSwEtc() : "-");
				auditHistoryDBEntity.setAuditDay(dscriptResultEntity.getAuditDay());

				if (jobEntity.getAuditFileCd() != null) {
					auditHistoryDBEntity.setAuditFileCd(jobEntity.getAuditFileCd());
				} else if(/*assetMasterDBEntity.getGovFlag() == 2 &&*/ jobEntity.getAuditFileCd() == null) {
					// select SNET_CONFIG_AUDIT_FILE 에서 기반시설 진단 파일 코드
					SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
					swAuditDayDBEntity.setSwType(dscriptResultEntity.getSystemInfoEntity().getSwType());
					swAuditDayDBEntity.setSwNm(dscriptResultEntity.getSystemInfoEntity().getSwNm());
					if (jobEntity.getJobType().equals(ManagerJobType.WM102.toString()) ||
							jobEntity.getJobType().equals(ManagerJobType.WM103.toString()) ||
							jobEntity.getJobType().equals(ManagerJobType.WM200.toString()) ||
							jobEntity.getJobType().equals(ManagerJobType.WM201.toString()) ||
							jobEntity.getJobType().equals(ManagerJobType.WM202.toString())) {
						Diagnosis diag = dscriptResultEntity.getDiagnosis().get(0);
						swAuditDayDBEntity.setFileType(dao.selectManualDiagnosisFileType(diag.getCode()));
					} else {
						HashMap<String, String> args = new HashMap<String, String>();
						args.put("assetCd", jobEntity.getAssetCd());
						args.put("swNm", jobEntity.getSwNm());

						swAuditDayDBEntity.setFileType(dao.selectDiagnosisFileType(args));
					}

					if (StringUtils.isEmpty(swAuditDayDBEntity.getOsKind())) {
						swAuditDayDBEntity.setOsKind(dao.selectSwOsKind(jobEntity.getAssetCd()));
					}

					List<String> listStr = null;
					try {
						setTempFromSwInfo(swAuditDayDBEntity);
						listStr = dao.selectConfigAuditFile(swAuditDayDBEntity);
                        auditHistoryDBEntity.setAuditFileCd(listStr.get(0));
                    } catch (Exception e){}
				}
				auditHistoryDBEntity.setHostNm(
						dscriptResultEntity.getSystemInfoEntity().getHostNm());
				auditHistoryDBEntity.setIpAddress(
						dscriptResultEntity.getSystemInfoEntity().getIpAddress());

				/*
				 * 2016-09-26 추가
				 * SNET_ASSET_SW_AUDIT_HISTORY
				 * -> USER_ID, USER_NM
				 */
				AssetUserDBEntity assetUser = new AssetUserDBEntity();
				assetUser.setAssetCd(assetCd);
				assetUser = dao.selectAssetUser(assetUser);
				if(assetUser!=null){
					auditHistoryDBEntity.setUserNm(assetUser.getUserNm());
					auditHistoryDBEntity.setUserId(assetUser.getUserId());
				}

				List<AssetSwAuditCok> listcok = dao.selectAssetSwAuditCok(dscriptResultEntity);
				if (!listcok.isEmpty()) {
					for (Diagnosis diagnosis : dscriptResultEntity.getDiagnosis()) {
						for (AssetSwAuditCok assetSwAuditCok : listcok) {
							if (diagnosis.getCode().equals(assetSwAuditCok.getDiagnosisCd())
									&& assetSwAuditCok.getItemCokReason() != null) {
								// audit_cok에 의해서 result code를 수정할때, 변경전 진단결과도 집어 넣기 (홍순풍 2017-03-08)
								diagnosis.setOrgItemResult(diagnosis.getResult());

								diagnosis.setItemCokReason(assetSwAuditCok.getItemCokReason());
								/*
								 * 2016-06-23
								 * 해당 진단 코드에 대한 결과값을 DB에 있는 값으로 넣어준다.
								 */
								diagnosis.setResult(assetSwAuditCok.getActionItemResult());
							}
						}
					}
				}

				// 진단 제외 항목 있는지 검사.
				Map<String, ConfigAuditItemExcept> configItemsExceptMap = null;

				if (INMEMORYDB.DiagExcept) {
					List<ConfigAuditItemExcept> configItemsExcept = dao.selectConfigAuditItemExceptList(auditHistoryDBEntity.getAuditFileCd());
					if (configItemsExcept != null && configItemsExcept.size() > 0) {
						configItemsExceptMap = configItemsExcept.stream()
								.collect(Collectors.toMap(ConfigAuditItemExcept::getDiagnosisCd, c -> c, (c1, c2) -> c1));
					}
				}

				// DB에 저장되어 있는 판단 기준, 중요도, 조치 방법, 조치TIP 가져오기.
				Map<String, ConfigAuditItem> configItemsMap = null;
						List<ConfigAuditItem> configItems = dao.selectConfigAuditItemList(auditHistoryDBEntity.getAuditFileCd());
				int govFlag = 1;
				if(configItems != null && configItems.size() > 0) {
					govFlag = configItems.get(0).getDiagnosisType();
					configItemsMap = configItems.stream().collect(Collectors.toMap(ConfigAuditItem::getDiagnosisCd, c -> c, (c1, c2) -> c1));
				}

				String checkErrDiag = "";
				String checkErrDiagLog = "";
				for (Diagnosis diagnosis : dscriptResultEntity.getDiagnosis()) {
					// 진단 기준 추가
					diagnosis.setGovFlag(govFlag);

					if (StringUtils.isEmpty(diagnosis.getResult()) || diagnosis.getResult().trim().equals("-")) {
						diagnosis.setResult("R");
					}

					// 진단 제외 항목 체크
					if (configItemsExceptMap!= null && configItemsExceptMap.size() > 0) {
						if (configItemsExceptMap.containsKey(diagnosis.getCode())) {
							diagnosis.setExceptYn("Y");
							diagnosis.setResult("NA");
						}
					}

					String diagCode = diagnosis.getCode();
					if (configItemsMap != null && configItemsMap.size() > 0) {
						if (configItemsMap.containsKey(diagCode)) {
							ConfigAuditItem cai = configItemsMap.get(diagCode);
							diagnosis.setStandard(cai.getItemStandard());                // 판단 기준
							diagnosis.setItemGrade(cai.getItemGrade());                    // 중요도
							diagnosis.setCountermeasure(cai.getItemCounterMeasure());    // 조치 방법
							diagnosis.setTip(cai.getItemCounterMeasureDetail());            // 조치TIP
						} else {
							logger.error("auditFileCd {}, diagCode {} does not exist from SNET_CONFIG_AUDIT_ITEM {} - {} - {} - {}",
									auditHistoryDBEntity.getAuditFileCd(), diagCode, diagnosis.getStandard(), diagnosis.getItemGrade(), diagnosis.getCountermeasure(), diagnosis.getTip());
						}
					}

					if ("T".equals(diagnosis.getResult())) {
						auditHistoryDBEntity.setAdResultOk(
								auditHistoryDBEntity.getAdResultOk() + 1);
						if ("H".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightOk(
									auditHistoryDBEntity.getAdWeightOk() + 3);
						} else if ("M".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightOk(
									auditHistoryDBEntity.getAdWeightOk() + 2);
						} else if ("L".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightOk(
									auditHistoryDBEntity.getAdWeightOk() + 1);
						} else{
							auditHistoryDBEntity.setAdWeightOk(
									auditHistoryDBEntity.getAdWeightOk() + 2);
						}
					} else if ("F".equals(diagnosis.getResult())) {
						auditHistoryDBEntity.setAdResultNok(
								auditHistoryDBEntity.getAdResultNok() + 1);
						if ("H".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightNok(
									auditHistoryDBEntity.getAdWeightNok() + 3);
						} else if ("M".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightNok(
									auditHistoryDBEntity.getAdWeightNok() + 2);
						} else if ("L".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightNok(
									auditHistoryDBEntity.getAdWeightNok() + 1);
						} else{
							auditHistoryDBEntity.setAdWeightNok(
									auditHistoryDBEntity.getAdWeightNok() + 2);
						}
					} else if ("C".equals(diagnosis.getResult())) {
						auditHistoryDBEntity.setAdResultPass(
								auditHistoryDBEntity.getAdResultPass() + 1);
						if ("H".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightPass(
									auditHistoryDBEntity.getAdWeightPass() + 3);
						} else if ("M".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightPass(
									auditHistoryDBEntity.getAdWeightPass() + 2);
						} else if ("L".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightPass(
									auditHistoryDBEntity.getAdWeightPass() + 1);
						} else{
							auditHistoryDBEntity.setAdWeightPass(
									auditHistoryDBEntity.getAdWeightPass() + 2);
						}
					} else if ("NA".equals(diagnosis.getResult())) {
						auditHistoryDBEntity.setAdResultNa(
								auditHistoryDBEntity.getAdResultNa() + 1);
						if ("H".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightNa(
									auditHistoryDBEntity.getAdWeightNa() + 3);
						} else if ("M".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightNa(
									auditHistoryDBEntity.getAdWeightNa() + 2);
						} else if ("L".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightNa(
									auditHistoryDBEntity.getAdWeightNa() + 1);
						} else {
							auditHistoryDBEntity.setAdWeightNa(
									auditHistoryDBEntity.getAdWeightNa() + 2);
						}
					} else if ("R".equals(diagnosis.getResult())){
						auditHistoryDBEntity.setAdResultReq(auditHistoryDBEntity.getAdResultReq()+1);
						if ("H".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightReq(
									auditHistoryDBEntity.getAdWeightReq() + 3);
						} else if ("M".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightReq(
									auditHistoryDBEntity.getAdWeightReq() + 2);
						} else if ("L".equals(diagnosis.getItemGrade())) {
							auditHistoryDBEntity.setAdWeightReq(
									auditHistoryDBEntity.getAdWeightReq() + 1);
						} else {
							auditHistoryDBEntity.setAdWeightReq(auditHistoryDBEntity.getAdWeightReq() + 2);
						}
					}

					if ( diagnosis.getResult().length() > 2 ) {
						checkErrDiagLog = "Script Error ( " + diagCode + " ) : need to check diagnosis result from diagnosis-script.";
						logger.error(checkErrDiagLog);
						checkErrDiag = diagCode;
						break;
					}
				}

				auditHistoryDBEntity
				.setAdWeightTotal(auditHistoryDBEntity.getAdWeightOk()
						+ auditHistoryDBEntity.getAdWeightNok()
						+ auditHistoryDBEntity.getAdWeightPass()
						+ auditHistoryDBEntity.getAdWeightNa()
						+ auditHistoryDBEntity.getAdWeightReq());

				// audit_file_cd 가 jobType에 없으면 SNET_CONFI_AUDIT_FILE에서 찾아 set.
				if (auditHistoryDBEntity.getAuditFileCd() == null) {
					auditHistoryDBEntity.setAuditFileCd(dao.selectConfigAuditFileCd(
							dscriptResultEntity.getSystemInfoEntity()));
				}

				dscriptResultEntity.setAuditHistoryDBEntity(auditHistoryDBEntity);

				try {
					if (!StringUtil.isEmpty(checkErrDiag) && !StringUtil.isEmpty(checkErrDiagLog)) {
						throw new SnetException(checkErrDiagLog);
					}

					dao.deleteAssetSwAuditHistory(dscriptResultEntity);
					dao.deleteAssetSwAuditReport(dscriptResultEntity);

					dao.insertDSResult(dscriptResultEntity);
					dao.updateSwAuditDay(dscriptResultEntity.getAuditHistoryDBEntity());
					assetMasterDBEntity.setAuditRate(dao.selectAuditRate(dscriptResultEntity.getAssetCd()));
					// 장비 보안준수율을 재계산하여 업데이트하도록 수정
					updateAssetMaster(assetMasterDBEntity);

					if (jobEntity.getSwType().equalsIgnoreCase("NW") && INMEMORYDB.nwRunDg.toUpperCase().equals("Y") ) {
						AgentInfo agentInfo = new AgentInfo();
						agentInfo.setAssetCd(jobEntity.getAssetCd());
						agentInfo.setConnectLog("success");
						jobEntity.setAgentInfo(agentInfo);

						dao.updateConnectMaster(jobEntity);
					}

				if (INMEMORYDB.useNotification) {
						//noti는 오류 발생해도 무시
						try {
							//자동진단실행 일 경우에 알림 처리
							if (ManagerJobType.AJ100.toString().equals(jobEntity.getJobType())) {
								//SNET_NOTIFICATION_CONFIG 알림 사용 체크
								String notiType = "3";
								String useYn = dao.getNotificationUseYn(notiType);

								if ("Y".equalsIgnoreCase(useYn)) {
									//진단실행한 유저
									String notiUserId = jobEntity.getUserId();
									//notificationmodel
									SnetNotificationModel snetNotificationModel = SnetNotificationModel
											.builder()
											.notiType(notiType)
											.notiUserId(notiUserId)
											.notiDataYn("Y")
											.notiLinkUrl("")
											.reqUserId(notiUserId)
											.build();

									//notificationdatamodel
									SnetNotificationDataModel snetNotificationDataModel = SnetNotificationDataModel
											.builder()
											.assetCd(dscriptResultEntity.getAuditHistoryDBEntity().getAssetCd())
											.swType(dscriptResultEntity.getAuditHistoryDBEntity().getSwType())
											.swNm(dscriptResultEntity.getAuditHistoryDBEntity().getSwNm())
											.swInfo(dscriptResultEntity.getAuditHistoryDBEntity().getSwInfo())
											.swDir(dscriptResultEntity.getAuditHistoryDBEntity().getSwDir())
											.swUser(dscriptResultEntity.getAuditHistoryDBEntity().getSwUser())
											.swEtc(dscriptResultEntity.getAuditHistoryDBEntity().getSwEtc())
											.hostNm(dscriptResultEntity.getAuditHistoryDBEntity().getHostNm())
											.ipAddress(dscriptResultEntity.getAuditHistoryDBEntity().getIpAddress())
											.auditDay(dscriptResultEntity.getAuditHistoryDBEntity().getAuditDay())
											.auditRate(dscriptResultEntity.getAuditHistoryDBEntity().getAuditRate())
											.assetUserId(dscriptResultEntity.getAuditHistoryDBEntity().getUserId())
											.build();
									notificationService.insertNotification(snetNotificationModel, snetNotificationDataModel);

									//설정에 추가된 알림 유저
									List<String> notiUserList = dao.selectNotificationUserList(notiType);
									if (notiUserList != null && !notiUserList.isEmpty()) {
										for (String userId : notiUserList) {
											if (!StringUtil.isEmpty(userId) && !userId.equals(notiUserId)) {
												snetNotificationModel.setNotiUserId(userId);
												notificationService.insertNotification(snetNotificationModel, snetNotificationDataModel);
											}
										}
									}
								}
							}
						} catch (Exception ee) {
							logger.error("notification insert error {}", ee.getLocalizedMessage());
						}
					}

					manualCount++;
				} catch (Exception ex) {
					logger.error(ex.toString(), ex);
					if (ex instanceof DataAccessException) {
						throw new SnetException(SnetCommonErrCode.ERR_0024.getMessage());
					} else {
						try {
							throw new SnetException(dao, ex.getMessage(), jobEntity, "D");
						} catch (Exception e) {
							logger.error(ex.toString(), ex);
						}
					}
				}
			}
		}
	}

	private String readFile(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
		{
			stream.forEach(s -> contentBuilder.append(s.replaceAll("®", "r").replaceAll("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFF]+", "")).append("\n"));
//			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}
	
	private void updateAssetMaster(AssetMasterDBEntity ao) {
		double mstAdWeightOk = 0.000;
		double mstAdWeightNok = 0.000;
		double mstAdWeightR = 0.000;
		double mstAdWeightPass = 0.000;
		double mstAuditRate = 0.000;
		double mstAuditRateFirewall = 0.000;
		
		try {
			// 보안준수율 계산을 위한 장비의 진단대상 (미진단 포함) 정보 조회
			SnetAssetSwAuditReportTotModel masterResult = dao.selectSnetAssetMasterTot(ao);
			
			mstAdWeightOk = masterResult.getAdWeightOk();
			mstAdWeightNok = masterResult.getAdWeightNok();
			mstAdWeightR = masterResult.getAdWeightReq();
			mstAdWeightPass = masterResult.getAdWeightPass();
			
			mstAuditRate = ( mstAdWeightOk / ( mstAdWeightOk + mstAdWeightNok + mstAdWeightR ) ) * 100;		//  보안준수율 = T  /  T + F + R
			mstAuditRate = Math.round( mstAuditRate * 100d ) / 100d ;		// 소수 둘째 자리
			
			mstAuditRateFirewall = ( mstAdWeightOk / ( mstAdWeightOk + mstAdWeightNok + mstAdWeightR + mstAdWeightPass ) ) * 100;		//  보안준수율 = T  /  T + F + R
			mstAuditRateFirewall = Math.round( mstAuditRateFirewall * 100d ) / 100d ;		// 소수 둘째 자리
			
			System.out.println(">>>>>>>>>> " + mstAdWeightOk + ", " + mstAdWeightNok + ", " + mstAdWeightR + ", sum=" + ( mstAdWeightOk + mstAdWeightNok + mstAdWeightR ));
			System.out.println(">>>>>>>>>> mstAuditRate : " + mstAuditRate);
			
			System.out.println(">>>>>>>>>> " + mstAdWeightOk + ", " + mstAdWeightNok + ", " + mstAdWeightR + ", " + mstAdWeightPass + ", sum=" + ( mstAdWeightOk + mstAdWeightNok + mstAdWeightR + mstAdWeightPass ));
			System.out.println(">>>>>>>>>> mstAuditRateFirewall : " + mstAuditRateFirewall);
			
			if (mstAuditRate > 100) {
				mstAuditRate = 100;
			}
			
			ao.setAuditRate((float)mstAuditRate);
			ao.setAuditRateFirewall((float)mstAuditRateFirewall);

			// 마스터 테이블에 해당장비의 보안 준수율 업데이트
			dao.updateAssetMaster(ao);
		} catch (Exception ex ){
			logger.error(ex.toString(), ex);
		}
	}

	/**
	 * 장비 정보 수집 결과 파일에 대한 파일 유효성 검사
	 * @param file
	 * @return
	 */
	private boolean gsResultFileValidation(File file) {
		boolean result = false;
		RandomAccessFile rf = null;
		try {
			 rf = new RandomAccessFile(file, "r");
			
			String line;
			int cnt = 0;
			if (rf.readLine().equals(ManagerJobFactory.BEGINELEMENT)) {
				while ((line = rf.readLine()) != null) {
					String[] str = StringUtil.split(line, "=");
					if ("ASSETCD".equals(str[0]))
						cnt++;
				}
				if (cnt == 0)
					result = false;
				rf.seek(rf.getFilePointer() - 6);
				if (rf.readLine().equals(ManagerJobFactory.ENDELEMENT)) {
					rf.seek(0);
					result =  true;
				}
			}
		} catch (Exception e) {
			result = false;
			logger.error("GetScript File Exception :: " + e.getMessage(), e);
		}finally{
			try {
				rf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 *  SNET_CONNECT_MASTER 에서 CONNECT_IP_ADDRESS 기준으로 대표 아이피를 변경
	 *  수집된 아이피 리스트에 없을 경우 수집된 아이피 기준으로 접속 아이피를 수정한다. 
	 *  위에 설정된 대표 아이피 기준으로 SNET_ASSET_MASTER에 대표 아이피를 설정한다.
	 *  
	 *  인서트(신규) 또는 업데이트(기존)되는 테이블
	 *  SNET_CONNECT_MASTER, SNET_ASSET_MASTER
	 *  
	 * @param assetMasterchk
	 * @param resultEntity
	 * @param assetMasterDBEntity
	 * @throws Exception
	 */
	private void setAssetMaster(int assetMasterchk, GscriptResultEntity resultEntity,
								AssetMasterDBEntity assetMasterDBEntity) throws Exception {
		
		String presentIp = setConnectIpAfterPresentIP(resultEntity);
		
		if (assetMasterchk != 0) {
			assetMasterDBEntity = dao.selectAssetMaster(resultEntity.getAssetCd());

			if (!resultEntity.getListAssetIp().isEmpty()) {
				setAssetMasterResource(resultEntity, assetMasterDBEntity, presentIp);

				dao.updateAssetMaster(assetMasterDBEntity);
			}
		} else {
			if (!resultEntity.getListAssetIp().isEmpty()) {
				assetMasterDBEntity.setAssetCd(resultEntity.getAssetCd());
				setAssetMasterResource(resultEntity, assetMasterDBEntity, presentIp);

				String govFlag = ConfigGlobalManager.getConfigGlobalValue("DefaultGovFlag");
				if (govFlag != null)
					assetMasterDBEntity.setGovFlag(Integer.parseInt(govFlag));
				else
					assetMasterDBEntity.setGovFlag(1);
				try {
					dao.insertAssetMasterExt(assetMasterDBEntity);
				} catch (Exception ex ){
					if (ex instanceof DataAccessException) {
						throw new SnetException(SnetCommonErrCode.ERR_0024.getMessage());
					}
				}
			}
		}
	}

	private void setAssetMasterResource(GscriptResultEntity resultEntity, AssetMasterDBEntity assetMasterDBEntity, String presentIp) {
		assetMasterDBEntity.setIpAddress(presentIp);

		assetMasterDBEntity.setBranchId(
				resultEntity.getUserViewDBEntity().getBranchId());
		assetMasterDBEntity.setBranchNm(
				resultEntity.getUserViewDBEntity().getBranchNm());
		assetMasterDBEntity.setTeamId(
				resultEntity.getUserViewDBEntity().getTeamId());
		assetMasterDBEntity.setTeamNm(
				resultEntity.getUserViewDBEntity().getTeamNm());

		assetMasterDBEntity.setHostNm(
				resultEntity.getAssetMasterDBEntity().getHostNm());

		assetMasterDBEntity.setGetDay(resultEntity.getCdate());

		assetMasterDBEntity.setVendor(resultEntity.getAssetMasterDBEntity().getVendor());
		assetMasterDBEntity.setSerial(resultEntity.getAssetMasterDBEntity().getSerial());
		assetMasterDBEntity.setCpu(resultEntity.getAssetMasterDBEntity().getCpu());
		assetMasterDBEntity.setMem(resultEntity.getAssetMasterDBEntity().getMem());
		assetMasterDBEntity.setDisk(resultEntity.getAssetMasterDBEntity().getDisk());
		assetMasterDBEntity.setAssetRmk(resultEntity.getAssetMasterDBEntity().getAssetRmk());
	}


	/**
	 * CASE 1. 
	 * -> SNET_CONNECT_MASTER : CONNECT_IP_ADDRESS 가 없을 경우
	 * 
	 * CASE 2. 
	 * -> CONNECT_IP_ADDRESS 가 수집된 아이피 리스트에 없을 경우
	 * 
	 * THEN 
	 * -> SNET_CONNECT_MASTER -> CONNECT_IP_ADDRESS 수집된 아이피 첫번째 정보를 넣어준다.
	 * @param resultEntity
	 * @return
	 * @throws Exception
	 */
	private String setConnectIpAfterPresentIP(GscriptResultEntity resultEntity) throws Exception {
	
		// 대표 아이피 수정시 IP 정보 업데이트
		String presentIp = null;
		boolean presentIpResult = false;
		try {
			presentIp= dao.selectConnectIp(resultEntity);					
		} catch (Exception e) {
			logger.info("None Represent IP");
		}
		
		// 1.대표아이피 설정 후 수집된 아이피 정보가 있는지 여부 판단
		presentIpResult = changeRepresentIp(resultEntity.getListAssetIp(), presentIp);
		
		if(presentIp==null || !presentIpResult){
			presentIp = resultEntity.getListAssetIp().get(0).getIpAddress();
			changeRepresentIp(resultEntity.getListAssetIp(), presentIp);
			resultEntity.setConnectIpAddress(presentIp);
			/*
			 *  UPDATE 
			 *  -> SNET_CONNECT_MASTER : CONNECT_IP_ADDRESS
			 */
			//2017.12.12 UI 설정 아이피를 변경했을 경우 체크 (매니저 자동 아이피변경 기능) - 이상준
//			int selectSnetAssetIpUserExistentCount = dao.selectSnetAssetIpUserExistentCount();
//			if(selectSnetAssetIpUserExistentCount == 0){
				dao.updateConnectMasterIp(resultEntity);
				dao.updateAssetMasterIp(resultEntity);
//			}

		}
		
		return presentIp;
	}
	
	/**
	 * 기준이 되는 대표 아이피로 리스트 데이터 수정
	 * 기준이 되는 아이피가 없을 경우 false, 정상 수정일 경우 true
	 * 
	 * @param iplist
	 * @param presentIp
	 * @return boolean
	 */
	private boolean changeRepresentIp(List<AssetIpDBEntity> iplist, String presentIp){
		boolean result = false;
		if (presentIp!=null) {
			for(AssetIpDBEntity entity : iplist) {
				if(entity.getIpAddress().equalsIgnoreCase(presentIp)){
					entity.setIpRepresent(1);
					entity.setUserRegi(1);
					result = true;
				}
				else
					entity.setIpRepresent(0);
			}
		}
		return result;
	}
	
	
	private void fileOutShadow(String assetCd, String outData){
		
		String outFileName = INMEMORYDB.SHADOW_DIR + File.separator + assetCd + INMEMORYDB.SHADOW_EXTENSION;
		try {
			if (outData !=null) {
				outData = StringUtil.replace(outData, ",", "\n");
				File dir = new File(INMEMORYDB.SHADOW_DIR);
				
				if(!dir.isDirectory())
					dir.mkdirs();
				
				FileUtils.fileWrite(outFileName, outData);
				logger.debug("Make shadow file :: {}", outFileName);
			} else {
				logger.info("Shadow is null.. ");
			}
		} catch (Exception e) {
			logger.error("Make shadow file Exception :: {}", e.getMessage(), e.fillInStackTrace());
		}
	}
	
	private boolean containSwType(List<SwAuditDayDBEntity> swAuditEntityList, String type) {
		boolean result = false;
		for(SwAuditDayDBEntity entity : swAuditEntityList) {
			if(entity.getSwType().equals(type))
				result = true;
		}
		return result;
	}

	
	private String resolveAuditDay(String day) {
		// 16-02-19 => 20160219
		if (day==null) {
			return DateUtil.getCurrDate();
		}
		day = day.trim().replaceAll("[- ]","");
		if (day.length()<6) {
			return DateUtil.getCurrDate();
		}
		if (day.length()==6) {
			// 입력값이 16-02-19 인 경우
			return "20" + day;
		}
		if (day.length()==8) {
			return day;
		}
		return DateUtil.getCurrDate();
	}
	
	private String resolveMasterAuditDay(String newAuditDay, AssetMasterDBEntity assetMasterDBEntity) {
		newAuditDay = resolveAuditDay(newAuditDay);
		String oldAuditDay = newAuditDay;
		if (assetMasterDBEntity!=null) {
			String thisAuditDay = assetMasterDBEntity.getAuditDay();
			if (thisAuditDay!=null && thisAuditDay.length()==8) {
				oldAuditDay = thisAuditDay;
			}
		}
		if (newAuditDay.compareTo(oldAuditDay)>0) {
			return newAuditDay;
		} else {
			return oldAuditDay;
		}
	}

	private boolean setTempFromSwInfo(SwAuditDayDBEntity auditDayDBEntity) {
		boolean forcedAcd = false;
		if (auditDayDBEntity.getSwType().equals("WAS")
				&& auditDayDBEntity.getSwNm().equalsIgnoreCase("WebLogic")
				&& auditDayDBEntity.getFileType() == 1) {
			String tmpV = auditDayDBEntity.getSwInfo().replaceAll("[^0-9]", "");
			if (tmpV.startsWith("8")) {
				auditDayDBEntity.setTemp("8");
			} else if (tmpV.startsWith("9")) {
				auditDayDBEntity.setTemp("9");
			} else {
				auditDayDBEntity.setTemp("11");
			}
			forcedAcd = true;
		}

		return forcedAcd;
	}
}