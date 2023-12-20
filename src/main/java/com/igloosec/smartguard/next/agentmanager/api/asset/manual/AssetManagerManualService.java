package com.igloosec.smartguard.next.agentmanager.api.asset.manual;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagnosisManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.GetManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.NwDgManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.NwGetManualReq;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.util.jobs.JobEntityInitData;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.services.AssetUpdateManager;
import com.igloosec.smartguard.next.agentmanager.services.DiagnosisManager;
import com.igloosec.smartguard.next.agentmanager.services.NetworkSwitchManager;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.mobigen.snet.networkdiagnosis.services.NetworkDiagnosisService;
import com.skt.main.GetnwMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class AssetManagerManualService {

    private Dao dao;
    private AssetUpdateManager assetUpdateManager;
    private DiagnosisManager diagnosisManager;
    private NetworkSwitchManager networkSwitchManager;
    private OptionProperties optionProperties;

    public AssetManagerManualService(Dao dao,
                                     AssetUpdateManager assetUpdateManager, DiagnosisManager diagnosisManager,
                                     NetworkSwitchManager networkSwitchManager, OptionProperties optionProperties) {
        this.dao = dao;
        this.assetUpdateManager = assetUpdateManager;
        this.diagnosisManager = diagnosisManager;
        this.networkSwitchManager = networkSwitchManager;
        this.optionProperties = optionProperties;
    }

    public ApiResult recvManualGetscriptResult(List<GetManualReq> getManualReqS) throws Exception {
        JobEntity jobEntity = null;
        try {
            for (GetManualReq getManualReq : getManualReqS) {
                JobEntityInitData jobEntityInitData = new JobEntityInitData(getManualReq);
                jobEntity = jobEntityInitData.getJobEntity();
                assetUpdateManager.recvManualGetscriptResult(getManualReq.getSwUrlCd(), jobEntity); // was로 업로드된 파일 옮기고 파싱.
                Thread.sleep(5);
            }
        } catch (Exception e) {
            throw new SnetException(dao, e.getMessage(), jobEntity, "G");
        }

        return ApiResult.builder().build();
    }

    public ApiResult recvManualDgscriptResult(List<DiagnosisManualReq> diagnosisManualReqS) throws Exception {
        JobEntity jobEntity = null;
        try {
            for (DiagnosisManualReq diagnosisManualReq : diagnosisManualReqS) {
                JobEntityInitData jobEntityInitData = new JobEntityInitData(diagnosisManualReq);
                jobEntity = jobEntityInitData.getJobEntity();
                diagnosisManager.recvManualDGscriptResult(diagnosisManualReq.getSwUrlCd(), jobEntity); // was로 업로드된 파일 옮기고 파싱.
                Thread.sleep(5);
            }
        } catch (Exception e) {
            throw new SnetException(dao, e.getMessage(), jobEntity, "G");
        }

        return ApiResult.builder().build();
    }

    public ApiResult recvSrvManualGscriptResult(List<NwGetManualReq> nwGetManualReqS) throws Exception {

        for (NwGetManualReq nwGetManualReq : nwGetManualReqS) {

            JobEntityInitData jobEntityInitData = new JobEntityInitData(nwGetManualReq);
            JobEntity jobEntity = jobEntityInitData.getJobEntity();

            String nwUrlCd = "", forDiagsVendorCd = "", resultFileNm = "";
            try {
                nwUrlCd = nwGetManualReq.getNwUrlCd();
                if (nwUrlCd != null && !nwUrlCd.isEmpty()) {
                    // window에서 개발시 필요.
                    if(optionProperties.getDiagnosis().equals("true") && nwUrlCd.contains("/")) {
                        nwUrlCd = CommonUtils.replacePathSeperator(nwUrlCd);
                    }
                    log.debug("[zip file] Nw 장비 등록 (" + nwUrlCd + ")");
                } else {
                    //To retrieve file type and make vendor code for diagnosis
                    HashMap<String, String> args = new HashMap<String, String>();
                    args.put("assetCd", jobEntity.getAssetCd());
                    args.put("swNm", jobEntity.getSwNm());
                    int fileType = dao.selectDiagnosisFileType(args);
                    forDiagsVendorCd = networkSwitchManager.retrieveNWDiagCriteria(fileType, jobEntity.getSwNm());
                    log.debug("[" + jobEntity.getAssetCd() + "] Nw 장비 등록 (" + forDiagsVendorCd + ")");
                }

                synchronized (this) {
                    GetnwMain getnwMain = null;
                    if (nwUrlCd != null && !nwUrlCd.isEmpty()) {
                        getnwMain = new GetnwMain(jobEntity.getAssetCd(), jobEntity.getManagerCode(),jobEntity.getSwNm(), nwGetManualReq.getFileNm(), nwUrlCd);
                    } else {
                        getnwMain = new GetnwMain(jobEntity.getAssetCd(), jobEntity.getManagerCode(), forDiagsVendorCd, nwGetManualReq.getFileNm());
                    }

                    resultFileNm = getnwMain.GetnwMake();
                    log.debug("Result File Name : {}", resultFileNm);
                }
            } catch (Exception e) {
                log.error("GetnwMain Exception :: {}", e.getMessage());
                throw new Exception(e);
            }
            jobEntity.setFileName(resultFileNm);

            assetUpdateManager.recvSRVManualGetScriptResult(jobEntity);
            Thread.sleep(5);
        }

        return ApiResult.builder().build();
    }

    public ApiResult recvSrvManualDgscriptResult(List<NwDgManualReq> nwDgManualReqS) throws Exception {
        for (NwDgManualReq nwDgManualReq : nwDgManualReqS) {
            JobEntityInitData jobEntityInitData = new JobEntityInitData(nwDgManualReq);
            JobEntity jobEntity = jobEntityInitData.getJobEntity();

            String nwUrlCd = "", forDiagsVendorCd = "", resultFileNm = "";
            try {
                nwUrlCd = nwDgManualReq.getNwUrlCd();
                if (nwUrlCd != null && !nwUrlCd.isEmpty()) {
                    // window에서 개발시 필요.
                    if(optionProperties.getDiagnosis().equals("true") && nwUrlCd.contains("/")) {
                        nwUrlCd = CommonUtils.replacePathSeperator(nwUrlCd);
                    }
                    log.debug("[zip file] Nw 장비 진단 (" + nwUrlCd + ")");
                } else {
                    //To retrieve file type and make vendor code for diagnosis
                    HashMap<String, String> args = new HashMap<String, String>();
                    args.put("assetCd", jobEntity.getAssetCd());
                    args.put("swNm", jobEntity.getSwNm());
                    int fileType = dao.selectDiagnosisFileType(args);
                    forDiagsVendorCd = networkSwitchManager.retrieveNWDiagCriteria(fileType, jobEntity.getSwNm());
                    log.debug("[" + jobEntity.getAssetCd() + "] Nw 장비 진단 (" + forDiagsVendorCd + ")");
                }

                synchronized (this){
                    NetworkDiagnosisService networkDiagnosisService = null;
                    if (nwUrlCd != null && !nwUrlCd.isEmpty()) {
                        networkDiagnosisService = new NetworkDiagnosisService(jobEntity.getSwNm(), jobEntity.getAssetCd(), nwDgManualReq.getFileNm(), nwUrlCd);
                    } else {
                        networkDiagnosisService = new NetworkDiagnosisService(forDiagsVendorCd, jobEntity.getAssetCd(), nwDgManualReq.getFileNm());
                    }

                    resultFileNm = networkDiagnosisService.makeNetworkDiag();
                }

                jobEntity.setJobType(ManagerJobType.WM202.toString());
                jobEntity.setFileName(resultFileNm);
                jobEntity.setAssetCd(jobEntity.getAssetCd());
                jobEntity.setFileType(INMEMORYDB.DGRESULTTYPE);

                if(!resultFileNm.equals("")){
                    diagnosisManager.recvSRVManualDGscriptResult(jobEntity);
                } else {
                    log.error("resultFileNm is empty. please check again.");
                }
            } catch (Exception e) {
                log.error("NetworkDiagnosisService Exception :: {}", e.getMessage());
                throw new Exception(e);
            }
        }

        return ApiResult.builder().build();
    }
}
