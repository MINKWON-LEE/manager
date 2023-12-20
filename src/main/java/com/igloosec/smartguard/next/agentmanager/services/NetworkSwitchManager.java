/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.NetworkSwitchManager.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 6. 3.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.GscriptResultEntity;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.NwConnectClient;

import com.mobigen.snet.networkdiagnosis.services.NetworkDiagnosisService;
import com.skt.main.GetnwMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class NetworkSwitchManager {

    private AssetUpdateManager assetUpdateManager;
    private DiagnosisManager diagnosisManager;
    private Dao dao;

    public NetworkSwitchManager(AssetUpdateManager assetUpdateManager, DiagnosisManager diagnosisManager,
                                Dao dao) {
        this.assetUpdateManager = assetUpdateManager;
        this.diagnosisManager = diagnosisManager;
        this.dao = dao;
    }

    public boolean doNetworkBFA(JobEntity jobEntity) throws Exception {
        boolean isJobDone = false;
        boolean checkParsed = false;

        String assetCd, vendorCd, managerCd;
        assetCd = jobEntity.getAssetCd();
        vendorCd = jobEntity.getSwNm();
        managerCd =  jobEntity.getManagerCode();

        try {
            log.debug("Start cfg file make");
            NwConnectClient nw = new NwConnectClient(jobEntity);

            if(jobEntity.getAgentInfo().getChannelType().startsWith("S")) {
                isJobDone = nw.nwSshHandler();
            }else {
                isJobDone = nw.nwTelnetHandler();
            }

            if (isJobDone){
                String filePath = INMEMORYDB.NWCFG_RESULT_FILE_MANAGER_DIR;

                String get_resultFileName = "";
                String configfileName = jobEntity.getFileName()+INMEMORYDB.CFG;


                log.debug(filePath + " :: "+configfileName);


                /* Get 프로그램 수행 */
                synchronized (this){
                    GetnwMain getnwMain = new GetnwMain(assetCd,managerCd,vendorCd,configfileName,filePath);
                    get_resultFileName = getnwMain.GetnwMake();
                }


                jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);
                jobEntity.setFileName(get_resultFileName);
                jobEntity.setJobType(ManagerJobType.WM301.toString());

                checkParsed = true;
                assetUpdateManager.recvSRVManualGetScriptResult(jobEntity); // 네트워크 장비 config 파일을 생성하고 get 프로그램 수행

                return isJobDone;
            }else {
                return false;
            }
        } catch (Exception e) {
            log.error(CommonUtils.printError(e));

            if (!checkParsed) {
                throw new SnetException(dao, e.getMessage(), jobEntity, "G");
            }
            throw new SnetException(e.getMessage());
        }
    }

    public boolean doNetworkDGEXE(JobEntity jobEntity) throws Exception {
        boolean isJobDone = false;
        String assetCd, vendorCd, managerCd;
        assetCd = jobEntity.getAssetCd();
        vendorCd = jobEntity.getSwNm();
        managerCd =  jobEntity.getManagerCode();

        try {
            jobEntity.setAgentJobFlag(2);
            dao.updateAgentJobHistory(jobEntity);

            HashMap<String , String> args = new HashMap<String,String>();
            args.put("assetCd", jobEntity.getAssetCd());
            args.put("swNm", vendorCd);
            int fileType = dao.selectDiagnosisFileType(args);
            String forDiagsVendorCd = retrieveNWDiagCriteria(fileType, vendorCd);

            log.debug("Start cfg file make");
            NwConnectClient nw = new NwConnectClient(jobEntity);

            if(jobEntity.getAgentInfo().getChannelType().startsWith("S")) {
                isJobDone = nw.nwSshHandler();
            }else {
                isJobDone = nw.nwTelnetHandler();
            }

            if (isJobDone){
                String filePath = INMEMORYDB.NWCFG_RESULT_FILE_MANAGER_DIR;

                String diag_resultFileName = "";
                String configfileName = jobEntity.getFileName()+INMEMORYDB.CFG;


                log.debug(filePath + " :: "+configfileName);

                synchronized (this){
                    NetworkDiagnosisService networkDiagnosisService = new NetworkDiagnosisService(forDiagsVendorCd,assetCd,configfileName,filePath);
                    diag_resultFileName = networkDiagnosisService.makeNetworkDiag();
                }

                log.debug("DIAG NW path: "+diag_resultFileName);

                /* 진단 프로그램 수행 */
                jobEntity.setFileType(INMEMORYDB.DGRESULTTYPE);
                jobEntity.setFileName(diag_resultFileName);
                jobEntity.setJobType(ManagerJobType.WM303.toString());
                diagnosisManager.recvSRVManualDGscriptResult(jobEntity);

                // 진단 완료시 Connectlog success 로 업데이트되도록 수정. 2018-09-17
                GscriptResultEntity resultsEntityConnect = new GscriptResultEntity();
                resultsEntityConnect.setAssetCd(jobEntity.getAssetCd());
                resultsEntityConnect.setConnectLog("success");

                dao.updateConnectMaster(resultsEntityConnect);

                return isJobDone;
            }else {
                return false;
            }
        } catch (Exception e) {
            log.error(CommonUtils.printError(e));
            throw new SnetException(dao, e.getMessage(), jobEntity, "D");
        }
    }

    public boolean doNetworkFtpDiag(JobEntity jobEntity, String vendor_Cd, String fileFullPath){
        boolean isJobDone = false;
        String asset_Cd = jobEntity.getAssetCd();
        String manager_Cd = jobEntity.getManagerCode();
        try {
            log.debug("Start cfg file make ["+jobEntity.getAssetCd()+"]");


            String filePath = fileFullPath;

            String get_resultFileName = "";
            String diag_resultFileName = "";
            String configfileName = jobEntity.getFileName();


            log.debug(filePath + " :: "+configfileName);

            GetnwMain getnwMain = new GetnwMain(asset_Cd,manager_Cd,vendor_Cd,configfileName,filePath);
            get_resultFileName = getnwMain.GetnwMake();


            /* Get 프로그램 수행 */
            jobEntity.setFileType(INMEMORYDB.GSRESULTTYPE);
            jobEntity.setFileName(get_resultFileName);
            jobEntity.setJobType(ManagerJobType.WM301.toString());
            assetUpdateManager.recvSRVManualGetScriptResult(jobEntity); // 네트워크 장비 config 파일을 생성하고 get 프로그램 수행

            return isJobDone;

        } catch (Exception e) {
            log.error(CommonUtils.printError(e));
            return isJobDone;
        }
    }

    public String retrieveNWDiagCriteria(int fileType, String vendorCd) {
        String result = vendorCd;

        switch(fileType) {
            case 1:
                break;
            case 2:
                result = "Infra_" + result;
                break;
            case 3:
                result = "Cmt_" + result;
                break;
            case 4:
                result = "Scrt_" + result;
                break;
            case 5:
                result = "Ncia_" + result;
                break;
        }

        log.info(String.format("NotificationListener-retrieveNWDiagCriteria INFO: fileType(%d), vendorCd(%s), result(%s)", fileType, vendorCd, result));
        return result;
    }
}
