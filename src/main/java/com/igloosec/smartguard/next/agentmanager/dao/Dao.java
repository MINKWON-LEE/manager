/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.dao.Dao.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 16.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.dao;

import com.google.gson.Gson;
import com.igloosec.smartguard.next.agentmanager.entity.*;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.api.etc.model.InstallFile;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.sk.snet.manipulates.EncryptUtil;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class Dao {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SqlSessionTemplate sqlSession;


    public int selectAssetSwAuditDayUserRegi(SwAuditDayDBEntity entity) throws Exception {
        try {
            return sqlSession.selectOne("selectAssetSwAuditDayUserRegi", entity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertGSResult(int assetMasterchk, String assetCd, GscriptResultEntity resultEntity) throws Exception {

        try {
        	if(assetMasterchk == 0 && resultEntity.getUserViewDBEntity() != null)
        		sqlSession.insert("insertGSResult", resultEntity);

            for (AssetIpDBEntity entity : resultEntity.getListAssetIp()) {
                entity.setAssetCd(assetCd);

                //2017.12.12 IP 등록 사용여부 체크(수동으로 넣었을 경우)
                int selectSnetAssetIpUserExistentCount = selectSnetAssetIpUserExistentCount();
                if(selectSnetAssetIpUserExistentCount > 0) {
					entity.setIpRepresent(0);
				}

                try{
                    sqlSession.insert("insertGSResultIPExt", entity);
                }catch (Exception e) {
				}
            }
            for (AssetOpenPort entity : resultEntity.getListassetOpenPort()) {
                entity.setAssetCd(assetCd);
                sqlSession.insert("insertGSResultPort", entity);
            }

            if (resultEntity.getAssetMasterDBEntity().getAssetCd() != null)
                sqlSession.update("updateGsAssetMaster2", resultEntity.getAssetMasterDBEntity());

            for (SwAuditDayDBEntity entity : resultEntity.getListSwAuditDay()) {
                entity.setAssetCd(assetCd);
                sqlSession.insert("insertGSResultAuditDay", entity);
            }
            
            String[] swType = {"OS", "DB", "WEB", "WAS", "NW", "SE"};
            List<String> list = Arrays.asList(swType);
            for (SwAuditDayDBEntity entity : resultEntity.getListSwAuditDay()) {
            	
            	if(list.contains(entity.getSwType().toUpperCase())){
            		// 1. DELETE SNET_ASSET_SW_AUDIT_HISTORY  , AUDIT_DAY = 19990101
            		int result = 0;
            		
            		result = sqlSession.delete("deleteDummyAssetSwAuditHistory", entity);
            		logger.debug("DUMMY_AUDIT_HISTORY_DELETE RESULT:: {} , :: {}", result,  new Gson().toJson(entity));
            		
            		// 2. INSERT SNET_ASSET_SW_AUDIT_HISTORY AD_WEIGHT_TOTAL <= WEIGHT_TOT, AD_RESULT_NOK <= RESULT_TOT
            		result = sqlSession.insert("insertDSResultDummy", entity);
            		logger.debug("DUMMY_AUDIT_HISTORY_INSERT RESULT:: {} , :: {}", result,  new Gson().toJson(entity));
            	}
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectAgentInfoAuthWithAssetCd(String assetCd) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo For assetcd=" + assetCd);
            return sqlSession.selectOne("selectAgentInfoAuthWithAssetCd", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectAgentInfoAuth(String ip, String hostName, String os) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo For ip = {}, hostName = {}, os = {}", ip, hostName, os);
            // to do agentCd, assetCd 가져오는 쿼리작성 필요
            HashMap<String, String> param = new HashMap<>();
            param.put("ipAddr", ip);
            param.put("hostNm", hostName);

            return sqlSession.selectOne("selectAgentInfoAuth", param);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectAgentInfo(String assetCd) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo For assetcd=" + assetCd);
            AgentInfo ai = sqlSession.selectOne("selectAgentInfo", assetCd);
            return ai;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectAgentInfoUpdate(String assetCd) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo For assetcd=" + assetCd);
            AgentInfo ai = sqlSession.selectOne("selectAgentInfoUpdate", assetCd);
            return ai;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentInfo> selectAgentInfoMulti(HashMap<String, Object> param) throws SnetException {
        try {
            return sqlSession.selectList("selectAgentInfoMulti", param);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectKILLAgentInfo(String assetCd) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo For assetcd=" + assetCd);
            AgentInfo ai = sqlSession.selectOne("selectKILLAgentInfo", assetCd);
            return ai;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectConnectAuthInfo(JobEntity jobEntity) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo For assetcd=" + jobEntity.getAssetCd());
            AgentInfo ai = sqlSession.selectOne("selectConnectAuthInfo", jobEntity);
            return ai;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectDiagAgentInfo(JobEntity jobEntity) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo For assetcd=" + jobEntity.getAssetCd());
            AgentInfo ai = sqlSession.selectOne("selectDiagAgentInfo", jobEntity);
            return ai;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentInfo> selectDiagAgentInfoList(HashMap<String, Object> param) throws SnetException {
        try {
            return sqlSession.selectList("selectDiagAgentInfoList", param);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentInfo selectAgentInfoBF(String assetCd) throws SnetException {
        try {
            logger.debug("QUERY AgentInfo BF For assetcd=" + assetCd);
            AgentInfo ai = sqlSession.selectOne("selectAgentInfoBF", assetCd);
            return ai;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentInfo> selectAgentInfoBFMulti(HashMap<String, Object> param) throws SnetException {
        try {
            return sqlSession.selectList("selectAgentInfoBFMulti", param);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentInfo> selectAgentList() throws SnetException {
        try {
            return sqlSession.selectList("selectAgentList");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentInfo> selectRelayServerList() throws SnetException {
        try {
            return sqlSession.selectList("selectRelayServerList");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AssetUserDBEntity selectAssetUser(AssetUserDBEntity assetUser) throws SnetException {
        try {
            return sqlSession.selectOne("selectAssetUser", assetUser);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAgentSetupStatus(AgentInfo agentInfo) throws SnetException {
        try {
            sqlSession.update("updateAgentSetupStatus", agentInfo);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAgentVersion(AgentInfo agentInfo) throws SnetException {
        try {
            sqlSession.update("updateAgentVersion", agentInfo);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectCurrentAgentVersion(String agentCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectCurrentAgentVersion", agentCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAgentVersion_v3(AgentInfo agentInfo) throws SnetException {
        try {
            sqlSession.update("updateAgentVersion_v3", agentInfo);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }


    public JobEntity selectJobInfo(String assetCd, String swType, String swNm,
                                   String swInfo, String swDir, String swUser, String swEtc) throws SnetException {
        try {

            HashMap<String, String> PARAMS = new HashMap<>();
            PARAMS.put("ASSETCD", assetCd);
            PARAMS.put("SWTYPE", swType);
            PARAMS.put("SWNM", swNm);
            PARAMS.put("SWINFO", swInfo);
            PARAMS.put("SWDIR", swDir);
            PARAMS.put("SWUSER", swUser);
            PARAMS.put("SWETC", swEtc);

            JobEntity jobEntity = null;

            if (INMEMORYDB.MultiServices) {
                jobEntity = sqlSession.selectOne("selectJobInfoMultiServicesForSrvDiags", PARAMS);
            } else {
                jobEntity = sqlSession.selectOne("selectJobInfoForSrvDiags", PARAMS);
            }
            if (jobEntity != null) {
                jobEntity.setAssetCd(assetCd);
            }
            return jobEntity;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<JobEntity> selectJobInfoList(HashMap<String, Object> param) throws SnetException {
        try {
            List<JobEntity> jobEntities = sqlSession.selectList("selectJobInfoList", param);
            return jobEntities;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentInfo> selectRunningGetJobInfoList(HashMap<String, Object> param) throws SnetException {
        try {
            List<AgentInfo> AgentInfos = sqlSession.selectList("selectRunningGetJobInfoList", param);
            return AgentInfos;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<JobEntity> selectCompletedJobInfoList(HashMap<String, Object> param) throws SnetException {
        try {
            List<JobEntity> jobEntities = sqlSession.selectList("selectCompletedJobInfoList", param);
            return jobEntities;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public JobEntity selectEventJobInfo(String assetCd, String prgId) throws SnetException {
    	try {
    		
    		HashMap<String, String> PARAMS = new HashMap<>();
    		PARAMS.put("assetCd", assetCd);
    		PARAMS.put("prgId", prgId);
    		JobEntity jobEntity = sqlSession.selectOne("selectEventJobInfo", PARAMS);
    		jobEntity.setAssetCd(assetCd);
    		return jobEntity;
    	} catch (Exception e) {
    		logger.error(CommonUtils.printError(e));
    		throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
    	}
    }

    public List<AgentInfo> selectAgentInfoList() throws SnetException {
        try {
            List<AgentInfo> list = sqlSession.selectList("selectAgentInfoList");
            logger.debug("Query @ selectAgentInfo length=" + list.size());
            return list;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertAgentMasterCd(String asset) throws SnetException {
        try {
            sqlSession.insert("insertAgentMasterCd", asset);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int selectAgentMasterCd(String asset) throws SnetException {
        try {
            return sqlSession.selectOne("selectAgentMasterCd", asset);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }


    public void insertAgentStatus(AgentStatus agentStatus) throws SnetException {
        try {
            sqlSession.insert("insertAgentStatus", agentStatus);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertAgentStatus_v3(AgentStatus agentStatus) throws SnetException {
        try {
            sqlSession.insert("insertAgentStatus_v3", agentStatus);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertRelayStatus(RelayStatus relayStatus) throws SnetException {
        try {
            sqlSession.insert("insertRelayStatus", relayStatus);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentInfo> selectUpdateTargetAssetList() throws SnetException {
        try {
            List<AgentInfo> list = sqlSession
                    .selectList("selectUpdateTargetAssetList");
            return list;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public ConfigUserViewDBEntity selectConfigUserView(
            GscriptResultEntity resultEntity) throws SnetException {
        try {
            ConfigUserViewDBEntity viewDBEntity = new ConfigUserViewDBEntity();
            viewDBEntity = sqlSession.selectOne("selectConfigUserView",
                    resultEntity);

            return viewDBEntity;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertDSResult(DscriptResultEntity dscriptResultEntity) throws SnetException {
        try {

            sqlSession.insert("insertDSResult", dscriptResultEntity);
            for (Diagnosis entity : dscriptResultEntity.getDiagnosis()) {
                entity.setAssetCd(dscriptResultEntity.getAssetCd());
                entity.setSwType(dscriptResultEntity.getSystemInfoEntity().getSwType());
                entity.setSwNm(dscriptResultEntity.getSystemInfoEntity().getSwNm());
                entity.setSwInfo(
                        dscriptResultEntity.getSystemInfoEntity().getSwInfo() != null && !dscriptResultEntity.getSystemInfoEntity().getSwInfo().isEmpty()
                                ? dscriptResultEntity.getSystemInfoEntity().getSwInfo() : "-");

                // 2016. 11. 18 진단 대상 정보 추가
                entity.setSwDir(dscriptResultEntity.getSystemInfoEntity().getSwDir() != null && !dscriptResultEntity.getSystemInfoEntity().getSwDir().isEmpty()
                        ? dscriptResultEntity.getSystemInfoEntity().getSwDir() : "-");
                entity.setSwUser(dscriptResultEntity.getSystemInfoEntity().getSwUser() != null && !dscriptResultEntity.getSystemInfoEntity().getSwUser().isEmpty()
                        ? dscriptResultEntity.getSystemInfoEntity().getSwUser() : "-");
                entity.setSwEtc(dscriptResultEntity.getSystemInfoEntity().getSwEtc() != null && !dscriptResultEntity.getSystemInfoEntity().getSwEtc().isEmpty()
                        ? dscriptResultEntity.getSystemInfoEntity().getSwEtc() : "-");
                entity.setIpAddress(dscriptResultEntity.getSystemInfoEntity().getIpAddress());
                entity.setAuditDay(dscriptResultEntity.getAuditDay());
                entity.setHostNm(dscriptResultEntity.getSystemInfoEntity().getHostNm());

                //수동업로드 첫시도시만 암호화 로직 추가
                if(dscriptResultEntity.getManualAssetCount() == 0){
					entity.setItemName(EncryptUtil.aes_encrypt(entity.getItemName()));
					entity.setStatus(EncryptUtil.aes_encrypt(entity.getStatus()));
					entity.setStandard(EncryptUtil.aes_encrypt(entity.getStandard()));
					entity.setCountermeasure(EncryptUtil.aes_encrypt(entity.getCountermeasure()));
					entity.setTip(EncryptUtil.aes_encrypt(entity.getTip()));
				}

                if (INMEMORYDB.DiagExcept) {
                    sqlSession.insert("insertDSResultReportDiagType", entity);
                } else {
                    sqlSession.insert("insertDSResultReport", entity);
                }
            }
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AssetMasterDBEntity selectAssetMaster(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectAssetMaster", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int selectAssetMasterchk(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectAssetMasterchk", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAgentMasterCd(String asset) throws SnetException {
        try {
            sqlSession.update("updateAgentMasterCd", asset);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetMaster(AssetMasterDBEntity assetMasterDBEntity) throws SnetException {
        try {
            sqlSession.update("updateAssetMaster2", assetMasterDBEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetMasterSgwRegiIn() throws SnetException {
        try {
            sqlSession.update("updateAssetMasterSgwRegiIn");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetMasterSgwRegiNotIn() throws SnetException {
        try {
            sqlSession.update("updateAssetMasterSgwRegiNotIn");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetMasterAliveChkNotIn() throws SnetException {
        try {
            sqlSession.update("updateAssetMasterAliveChkNotIn");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetMasterAliveChk(List<AssetMasterDBEntity> assetMasterDBEntityList) throws SnetException {
        try {
            for (AssetMasterDBEntity assetMasterDBEntity : assetMasterDBEntityList)
                sqlSession.update("updateAssetMasterAliveChk", assetMasterDBEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void deleteAssetUser(GscriptResultEntity resultEntity) throws SnetException {
        try {
            sqlSession.delete("deleteAssetUser", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }

    }

    public void deleteSwAuditDay(GscriptResultEntity resultEntity) throws SnetException {
        try {
            sqlSession.delete("deleteSwAuditDay", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void deleteAssetIp(GscriptResultEntity resultEntity) throws SnetException {
        try {
            sqlSession.delete("deleteAssetIp", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int deleteAgentMaster(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.delete("deleteAgentMaster", jobEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertAssetMaster(AssetMasterDBEntity assetMasterDBEntity) throws Exception {
        try {
            sqlSession.insert("insertAssetMaster", assetMasterDBEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertAssetMasterExt(AssetMasterDBEntity assetMasterDBEntity) throws Exception {
        try {
            sqlSession.insert("insertAssetMasterExt", assetMasterDBEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateConnectMasterIp(GscriptResultEntity resultEntity) throws Exception {
        try {
            sqlSession.update("updateConnectMasterIp", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetMasterIp(GscriptResultEntity resultEntity) throws Exception {
        try {
            sqlSession.update("updateAssetMasterIp", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateConnectMaster(GscriptResultEntity resultEntity) throws Exception {
        try {
            sqlSession.update("updateConnectMaster", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateConnectMasterLog(GscriptResultEntity resultEntity) throws Exception {
        try {
            sqlSession.update("updateConnectMasterLog", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateConnectMasterConnectLogS(HashMap<String, Object> param) throws Exception {
        try {
            sqlSession.update("updateConnectMasterConnectLogS", param);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateConnectMaster(JobEntity jobEntity) throws Exception {
        try {
            sqlSession.update("updateConnectMasterConnectLog", jobEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }

    }

    public List<String> selectConfigAuditFile(SwAuditDayDBEntity auditDayDBEntity) throws Exception {
        try {
            return sqlSession.selectList("selectConfigAuditFile", auditDayDBEntity);

        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public Map<String, ConfigAuditItem> selectConfigAuditItem(String auditFileCd) throws Exception {
        try {
            HashMap<String, String> param = new HashMap();
            param.put("auditFileCd", auditFileCd);
            return sqlSession.selectMap("selectConfigAuditItem", param, "diagnosisCd");

        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<ConfigAuditItem> selectConfigAuditItemList(String auditFileCd) throws Exception {
        try {
            return sqlSession.selectList("selectConfigAuditItemList", auditFileCd);

        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<ConfigAuditItemExcept> selectConfigAuditItemExceptList(String auditFileCd) throws Exception {
        try {
            return sqlSession.selectList("selectConfigAuditItemExceptList", auditFileCd);

        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectSwAuditHistory(SwAuditDayDBEntity auditDayDBEntity) throws SnetException {
        try {
            return sqlSession.selectOne("selectSwAuditHistory", auditDayDBEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
	public List<Map> selectAuditDayFileCd(SwAuditDayDBEntity auditDayDBEntity) throws SnetException {
        try {
            return sqlSession.selectList("selectAuditDayFileCd", auditDayDBEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateSwAuditDay(SwAuditHistoryDBEntity auditHistoryDBEntity) throws SnetException {
        if (auditHistoryDBEntity.getAuditDay() == null || auditHistoryDBEntity.getAuditDay().equals(""))
            auditHistoryDBEntity.setAuditDay("19990101");
        try {
            sqlSession.update("updateSwAuditDay", auditHistoryDBEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }

    }

    public float selectAuditRate(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectAuditRate", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public SnetAssetSwAuditReportTotModel selectSnetAssetMasterTot(AssetMasterDBEntity entity) throws SnetException {
        try {
        	SnetAssetSwAuditReportTotModel resultEntity = new SnetAssetSwAuditReportTotModel();
        	resultEntity = sqlSession.selectOne("selectSnetAssetMasterTot",
        			entity.getAssetCd());

            return resultEntity;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }
    
    
    public void deleteAssetSwAuditHistory(DscriptResultEntity dscriptResultEntity) throws SnetException {
        try {
            sqlSession.delete("deleteAssetSwAuditHistory", dscriptResultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }

    }

    public List<String> setAssetCd(GscriptResultEntity resultEntity) throws SnetException {
        try {
            return sqlSession.selectList("setAssetCd", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0027.getMessage());
        }
    }

    public int selectChkConnectMaster(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectChkConnectMaster", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertConnectMaster(GscriptResultEntity resultEntity) throws SnetException {
        try {
            sqlSession.insert("insertConnectMaster", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<String> setDscriptAssetCd(SystemInfoEntity systemInfoEntity) throws SnetException {
        try {
            return sqlSession.selectList("setDscriptAssetCd", systemInfoEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void deleteAssetSwAuditReport(
            DscriptResultEntity dscriptResultEntity) throws SnetException {
        try {
            sqlSession.delete("deleteAssetSwAuditReport", dscriptResultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectConfigAuditFileCd(SystemInfoEntity systemInfoEntity) throws SnetException {
        try {
            return sqlSession.selectOne("selectConfigAuditFileCd", systemInfoEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<?> selectAssetMasterAll() throws SnetException {
        try {
            return sqlSession.selectList("selectAssetMasterAll");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int selectAssetIpCnt(AssetMasterStatusDBEntity entity) throws SnetException {
        try {
            return sqlSession.selectOne("selectAssetIpCnt", entity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertSnetManualAgentJobHistory(JobEntity jobEntity) throws SnetException {
        try {
            sqlSession.update("insertSnetManualAgentJobHistory", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertSnetAgentJobRdate(JobEntity jobEntity) throws SnetException {
        try {
            sqlSession.update("insertSnetAgentJobRdate", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    public List<Map> selectSwInfoOfNw(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.selectList("selectSwInfoOfNw", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectAgentGetJobHistory(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectAgentGetJobHistory", assetCd);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int updateAgentGetJobHistory(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.update("updateAgentGetJobHistory", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int updateAgentGetJobHistorySuccess(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.update("updateAgentGetJobHistorySuccess", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int updateAgentGetJobHistoryOtherSuccess(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.update("updateAgentGetJobHistoryOtherSuccess", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int updateAgentJobHistory(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.update("updateAgentJobHistory", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int updateLogFileAgentJobHistory(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.update("updateLogFileAgentJobHistory", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertDiagInfo(JobEntity jobEntity) throws SnetException {
        try {
            sqlSession.insert("insertDiagInfo", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public JobEntity selectDiagInfo(JobEntity jobEntity) throws SnetException {
        try {
            return sqlSession.selectOne("selectDiagInfo", jobEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateDiagInfo(JobEntity jobEntity) throws SnetException {
        try {
            sqlSession.update("updateDiagInfo", jobEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void deleteDiagInfo(JobEntity jobEntity) throws SnetException {
        try {
            sqlSession.delete("deleteDiagInfo", jobEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertAgentGetJobHistory(AgentInfo agentInfo) throws SnetException {
        try {
            sqlSession.insert("insertAgentGetJobHistory", agentInfo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertAgentGetJobHistoryByManIns(AssetMasterDBEntity assetMasterDBEntity) throws SnetException {
        try {
            sqlSession.insert("insertAgentGetJobHistoryByManIns", assetMasterDBEntity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int updateAgentGetJobHistoryCheckOld(HashMap<String, String> args) throws SnetException {
        try {
            return sqlSession.update("updateAgentGetJobHistoryCheckOld", args);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }
    public int updateAgentJobHistoryCheckOld(HashMap<String, String> args) throws SnetException {
        try {
            return sqlSession.update("updateAgentJobHistoryCheckOld", args);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateEventAgentJobHistory(JobEntity jobEntity) throws SnetException {
    	try {
    		sqlSession.update("updateEventAgentJobHistory", jobEntity);
    	} catch (Exception e) {
    		logger.error(CommonUtils.printError(e));
    		throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
    	}
    }

    public void updateConnectMasterPort(List<AgentInfo> listAgenInfo) {
//		sqlSession.update("updateConnectMasterPort", info);

    }

    public List<AssetSwAuditCok> selectAssetSwAuditCok(
            DscriptResultEntity dscriptResultEntity) throws SnetException {
        try {
            return sqlSession.selectList("selectAssetSwAuditCok", dscriptResultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<?> selectDiagnosisJob() throws SnetException {
        try {
            if (INMEMORYDB.MultiServices) {
                return sqlSession.selectList("selectDiagnosisJobMultiServices");
            } else {
                return sqlSession.selectList("selectDiagnosisJob");
            }
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void deleteOpenPort(GscriptResultEntity resultEntity) throws SnetException {
        try {
            sqlSession.delete("deleteOpenPort", resultEntity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectConnectIp(GscriptResultEntity result) throws SnetException {
        try {
            return sqlSession.selectOne("selectConnectIp", result);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertWebSrcChgLog(HashMap<String, String> args) throws SnetException {
        try {
            sqlSession.insert("insertWebSrcChgLog", args);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }
    
    public int insertCustomDiagJob(DscriptResultEntity entity) throws SnetException{
        try {
            return sqlSession.insert("insertCustomDiagJob", entity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int insertCustomDiagReport(DscriptResultEntity entity) throws SnetException{
        try {
            return sqlSession.insert("insertCustomDiagReport", entity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }
    
    public int deleteCustomDiagReport(DscriptResultEntity entity) throws SnetException{
        try {
            return sqlSession.delete("deleteCustomDiagReport", entity);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int selectSnetAssetIpUserExistentCount() throws SnetException{
        try {
            return sqlSession.selectOne("selectSnetAssetIpUserExistentCount");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<Map> selectSnetConfigGlobalList() throws SnetException{
    	try{
    		return sqlSession.selectList("selectSnetConfigGlobalList");
		}catch(Exception e){
			logger.error(CommonUtils.printError(e));
			throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
		}
	}

    public int selectDiagnosisFileType(HashMap<String, String> args) throws SnetException {
        try {
            Integer res = sqlSession.selectOne("selectDiagnosisFileType", args);
            if(res == null)
                return 1;
            else
                return res;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int selectManualDiagnosisFileType(String diagCode) throws SnetException {
        try {
            Integer res = sqlSession.selectOne("selectManualDiagnosisFileType", diagCode);
            if(res == null)
                return 1;
            else
                return res;
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectGetScriptChecksum(String fileNm) throws Exception {
        try {
            return sqlSession.selectOne("selectGetScriptChecksum", fileNm);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateDiagnosisFileChecksumHash(HashMap<String, String> args) throws SnetException {
        try {
            sqlSession.update("updateDiagnosisFileChecksumHash", args);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int selectAgentManualSetupCount(HashMap<String, Object> manualParam) throws SnetException {
		try {
			return sqlSession.selectOne("selectAgentManualSetupCount", manualParam);
		} catch (Exception e) {
			logger.error(CommonUtils.printError(e));
			throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
		}
	}
    public ConfigUserViewDBEntity selectAgentManualSetupConfigUserView() throws SnetException {
		try {
			return sqlSession.selectOne("selectAgentManualSetupConfigUserView");
		} catch (Exception e) {
			logger.error(CommonUtils.printError(e));
			throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
		}
	}

	public int selectAgentAssetCdCountCheck(HashMap<String, String> param) throws SnetException {
		try {
			return sqlSession.selectOne("selectAgentAssetCdCountCheck", param);
		} catch (Exception e) {
			logger.error(CommonUtils.printError(e));
			throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
		}
	}

    public List<Map> selectSoftWareExcept() throws SnetException {
        try {
            return sqlSession.selectList("selectSoftWareExcept");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public AgentMaster selectAgentMaster(String agentCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectAgentMaster", agentCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<AgentJobHistory> selectAgentJobHistoryList(String agentCd) throws SnetException {
        try {
            return sqlSession.selectList("selectAgentJobHistoryList", agentCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public SnetConfigAuditFile selectSnetConfigAuditFile(String auditFileCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectSnetConfigAuditFile", auditFileCd);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<InstallFile> selectSnetSetupFileList(InstallFile installFile) throws SnetException {
        try {
            return sqlSession.selectList("selectSnetSetupFileList", installFile);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateInstallFileChecksumHash(InstallFile installFile) throws Exception {
        try {
            sqlSession.update("updateInstallFileChecksumHash", installFile);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectUseDiagSudo(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectUseDiagSudo", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectOldJobsFromJobHistory() throws SnetException {
        try {
            return sqlSession.selectOne("selectOldJobsFromJobHistory");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateOldJobHistory(String desc) throws SnetException {
        try {
            sqlSession.update("updateOldJobHistory", desc);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectAllOldJobsFromJobHistory() throws SnetException {
        try {
            return sqlSession.selectOne("selectAllOldJobsFromJobHistory");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAllOldJobHistory(String desc) throws SnetException {
        try {
            sqlSession.update("updateAllOldJobHistory", desc);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    //==============================
    // 알림 관련
    public long getDuplicateNotiSeq(SnetNotificationModel snetNotificationModel) throws SnetException {
        try {
            return sqlSession.selectOne("getDuplicateNotiSeq", snetNotificationModel);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
        return 0;
    }

    public void insertNotification(SnetNotificationModel snetNotificationModel) throws SnetException {
        try {
            sqlSession.insert("insertNotification", snetNotificationModel);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }

    public void updateNotificationTypeCnt(SnetNotificationModel snetNotificationModel) throws SnetException {
        try {
            sqlSession.insert("updateNotificationTypeCnt", snetNotificationModel);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }

    public void insertNotificationData(SnetNotificationDataModel snetNotificationDataModel) throws SnetException {
        try {
            sqlSession.insert("insertNotificationData", snetNotificationDataModel);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }

    public String getNotificationUseYn(String notiType) throws SnetException {
        try {
            return sqlSession.selectOne("getNotificationUseYn", notiType);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
        return null;
    }

    public List<String> selectNotificationUserList(String notiType) throws SnetException {
        try {
            return sqlSession.selectList("selectNotificationUserList", notiType);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
        return null;
    }
    //=====================================

    public int getAssetDiagnosisJobCount(String assetCd) throws Exception {
        try {
            return sqlSession.selectOne("getAssetDiagnosisJobCount", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void insertAgentResource(AgentResource agentResource) throws SnetException {
        try {
            sqlSession.insert("insertAgentResource", agentResource);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }

    public String selectAgentResource(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectAgentResource", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAgentResource(AgentResource agentResource) throws SnetException {
        try {
            sqlSession.update("updateAgentResource", agentResource);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void deleteAgentResource(String assetCd) throws SnetException {
        try {
            sqlSession.delete("deleteAgentResource", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }

    }

    //=====================================
    // 장비정보이력 관련
    public SnetAssetGetHistoryModel selectSnetAssetGetHistory(String assetCd) throws SnetException{
        try {
            return sqlSession.selectOne("selectSnetAssetGetHistory", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
        return null;
    }
    public void insertSnetAssetGetHistory(SnetAssetGetHistoryModel snetAssetGetHistoryModel) throws SnetException{
        try {
            sqlSession.insert("insertSnetAssetGetHistory", snetAssetGetHistoryModel);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }

    public List<SnetAssetSwChangeHistoryModel> selectAssetSwList(String assetCd) throws SnetException{
        try {
            return sqlSession.selectList("selectAssetSwList", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
        return null;
    }

    public void insertAssetSwChangeList(List<SnetAssetSwChangeHistoryModel> param) throws SnetException{
        try {
            sqlSession.insert("insertAssetSwChangeHistory", param);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }

    //수집일자 업데이트
    public void updateAssetMasterGetDay(String assetCd) throws SnetException{
        try {
            sqlSession.update("updateAssetMasterGetDay", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
        }
    }

    public List<?> selectBatchGet() throws SnetException {
        try {
            return sqlSession.selectList("selectBatchGet");
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectManagerCd(String assetCD) throws SnetException {
        try {
            return sqlSession.selectOne("selectManagerCd", assetCD);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public SnetAssetSwChangeHistoryModel selectAssetSw(HashMap param) throws SnetException {
        try {
            return sqlSession.selectOne("selectAssetSw", param);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public int checkAssetSwHistory(SnetAssetSwChangeHistoryModel snetAssetSwChangeHistoryModel) throws SnetException {
        try {
            return sqlSession.selectOne("checkAssetSwHistory", snetAssetSwChangeHistoryModel);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetSwHistoryAuditDay(SnetAssetSwChangeHistoryModel snetAssetSwChangeHistoryModel) throws SnetException {
        try {
            sqlSession.update("updateAssetSwHistoryAuditDay", snetAssetSwChangeHistoryModel);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public void updateAssetGetHistoryAgentInfo(AgentInfo agentInfo) throws SnetException {
        try {
            sqlSession.update("updateAssetGetHistoryAgentInfo", agentInfo);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public List<ConfigAuditItemOption> selectConfigAuditItemOption(JobEntity jobEntity) throws Exception {
        try {
            return sqlSession.selectList("selectConfigAuditItemOption", jobEntity);

        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }

    public String selectSwOsKind(String assetCd) throws SnetException {
        try {
            return sqlSession.selectOne("selectSwOsKind", assetCd);
        } catch (Exception e) {
            logger.error(CommonUtils.printError(e));
            throw new SnetException(SnetCommonErrCode.ERR_0026.getMessage());
        }
    }
}