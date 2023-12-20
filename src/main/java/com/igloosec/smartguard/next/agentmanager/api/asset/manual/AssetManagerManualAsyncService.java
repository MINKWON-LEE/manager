package com.igloosec.smartguard.next.agentmanager.api.asset.manual;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagnosisManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.GetManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.NwDgManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.NwGetManualReq;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.util.jobs.JobEntityInitData;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.services.*;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.mobigen.snet.networkdiagnosis.services.NetworkDiagnosisService;
import com.skt.main.GetnwMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@EnableAsync
@Slf4j
@Service
public class AssetManagerManualAsyncService {

    private Dao dao;
    private AssetUpdateManager assetUpdateManager;
    private DiagnosisManager diagnosisManager;
    private NetworkSwitchManager networkSwitchManager;
    private OptionProperties optionProperties;
    private JobHandleManager jobHandleManager;

    public AssetManagerManualAsyncService(Dao dao, AssetUpdateManager assetUpdateManager,
                                          DiagnosisManager diagnosisManager, NetworkSwitchManager networkSwitchManager,
                                          OptionProperties optionProperties, JobHandleManager jobHandleManager) {

        this.dao = dao;
        this.assetUpdateManager = assetUpdateManager;
        this.diagnosisManager = diagnosisManager;
        this.networkSwitchManager = networkSwitchManager;
        this.optionProperties = optionProperties;
        this.jobHandleManager = jobHandleManager;
    }

    @Async
    public CompletableFuture<ApiResult> recvManualGetscriptResult(List<GetManualReq> getManualReqS ) throws Exception {

        return CompletableFuture.completedFuture(this.recvManualGetscriptResultForAsync(getManualReqS))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                });
    }

    public ApiResult recvManualGetscriptResultForAsync(List<GetManualReq> getManualReqS) {
        JobEntity jobEntity = null;
        for (GetManualReq getManualReq : getManualReqS) {
            try {
                JobEntityInitData jobEntityInitData = new JobEntityInitData(getManualReq);
                jobEntity = jobEntityInitData.getJobEntity();
                assetUpdateManager.recvManualGetscriptResult(getManualReq.getSwUrlCd(), jobEntity); // was로 업로드된 파일 옮기고 파싱.
                Thread.sleep(5);
            } catch (Exception ex) {
                if (jobEntity == null) {
                    jobEntity = new JobEntity();
                }
                jobEntity.setJobType(ManagerJobType.AJ200.toString());
                AgentInfo agentInfo = jobEntity.getAgentInfo();
                if (agentInfo == null) {
                    agentInfo = new AgentInfo();
                }
                agentInfo.setAssetCd(getManualReq.getAssetCd());
                jobEntity.setAgentInfo(agentInfo);
                jobEntity.setAssetCd(getManualReq.getAssetCd());
                jobHandleManager.handleException(jobEntity, ex);
            }
        }

        return ApiResult.builder().build();
    }

    @Async
    public CompletableFuture<ApiResult> recvManualDgscriptResult(List<DiagnosisManualReq> diagnosisManualReqS) throws Exception {
        return CompletableFuture.completedFuture(this.recvManualDgscriptResultForAsync(diagnosisManualReqS))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                });
    }

    public ApiResult recvManualDgscriptResultForAsync(List<DiagnosisManualReq> diagnosisManualReqS) {
        JobEntity jobEntity = null;
        for (DiagnosisManualReq diagnosisManualReq : diagnosisManualReqS) {
            try {
                JobEntityInitData jobEntityInitData = new JobEntityInitData(diagnosisManualReq);
                jobEntity = jobEntityInitData.getJobEntity();

                diagnosisManager.recvManualDGscriptResult(diagnosisManualReq.getSwUrlCd(), jobEntity); // was로 업로드된 파일 옮기고 파싱.
                Thread.sleep(5);
            } catch (Exception ex) {
                if (jobEntity != null) {
                    INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);
                    jobHandleManager.handleException(jobEntity, ex);
                } else {
                    log.error("jobEntity is null. please check this again. - " + diagnosisManualReq.getAssetCd());
                }
            }
        }

        return ApiResult.builder().build();
    }

    @Async
    public CompletableFuture<ApiResult> recvSrvManualGscriptResult(List<NwGetManualReq> nwGetManualReqS) throws Exception {
        return CompletableFuture.completedFuture(this.recvSrvManualGscriptResultForAsync(nwGetManualReqS))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                });
    }

    public ApiResult recvSrvManualGscriptResultForAsync(List<NwGetManualReq> nwGetManualReqS) {

        boolean dgFlag = false;
        List<NwDgManualReq> nwDgManualReqS = null;
                // 네트워크 수집 및 진단 동시 실행인 경우인지 체크
        if (INMEMORYDB.nwRunDg.toUpperCase().equals("Y") ) {
            dgFlag = true;
            nwDgManualReqS = new ArrayList<>();
        }

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
            }
            jobEntity.setFileName(resultFileNm);

            try {
                assetUpdateManager.recvSRVManualGetScriptResult(jobEntity);
                Thread.sleep(5);

                // 네트워크 수집과 진단실행 동시 진행.
                // 진단실행을 위한 파라미터 만들기.
                if (INMEMORYDB.nwRunDg.toUpperCase().equals("Y") ) {
                    jobEntity.setAgentCd(jobEntity.getAssetCd());
                    jobEntity.setAgentJobFlag(2);

                    List<Map> nwInfo = dao.selectSwInfoOfNw(jobEntity);
                    for (Map map : nwInfo) {
                        jobEntity.setSwInfo(map.get("SW_INFO") ==  null ? "-" : map.get("SW_INFO").toString());
                    }
                    dao.insertSnetManualAgentJobHistory(jobEntity);
                    dao.insertSnetAgentJobRdate(jobEntity);

                    nwGetManualReq.setSwInfo(jobEntity.getSwInfo());
                    nwDgManualReqS.add(nwGetManualReq.toNwDgManualReq());
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
                // 수집 중 예외가 발생했을 경우엔 동시진단 실행이 있어도 중단.
                dgFlag = false;
            }
        }

        // 네트워크 수집 및 진단 동시 실행인 경우.
        if (!dgFlag) {
            return ApiResult.builder().build();
        }

        return recvSrvManualDgscriptResultForAsync(nwDgManualReqS);
    }

    @Async
    public CompletableFuture<ApiResult> recvSrvManualDgscriptResult(List<NwDgManualReq> nwDgManualReqS) throws Exception {
        return CompletableFuture.completedFuture(this.recvSrvManualDgscriptResultForAsync(nwDgManualReqS))
                .exceptionally(ex -> {
                    log.error(ex.getMessage());
                    return ApiResult.getFailResult();
                });
    }

    public ApiResult recvSrvManualDgscriptResultForAsync(List<NwDgManualReq> nwDgManualReqS) {

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
                    Thread.sleep(5);
                } else {
                    String err = "resultFileNm is empty. please check again.";
                    log.error(err);
                    throw new Exception(err);
                }
            } catch (Exception e) {
                jobHandleManager.handleException(jobEntity, e);
                log.error("NetworkDiagnosisService Exception :: {}", e.getMessage());
            }
        }

        return ApiResult.builder().build();
    }
}
