package com.igloosec.smartguard.next.agentmanager.api.asset;

import com.igloosec.smartguard.next.agentmanager.api.asset.model.*;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.model.CollectionValidator;
import com.igloosec.smartguard.next.agentmanager.entity.*;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.property.FileStorageProperties;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.services.JobHandleManager;
import com.igloosec.smartguard.next.agentmanager.api.model.ResponseEntityUtil;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(value= {"/manager/v3/sgs-api/asset", "/manager/v3/sga-api/asset"},
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
@Slf4j
public class AssetManagerController {

    private FileStorageProperties fileStorageProperties;
    private final AssetManagerService assetManagerService;
    private final AssetManagerAsyncService assetManagerAsyncService;
    private final JobHandleManager jobHandleManager;
    private final CollectionValidator collectionValidator;
    private final OptionProperties optionProperties;

    public AssetManagerController(AssetManagerService agentManagerService, AssetManagerAsyncService assetManagerAsyncService,
                                  JobHandleManager jobHandleManager, FileStorageProperties fileStorageProperties,
                                  OptionProperties optionProperties, CollectionValidator collectionValidator) {
        this.assetManagerService = agentManagerService;
        this.assetManagerAsyncService = assetManagerAsyncService;
        this.jobHandleManager = jobHandleManager;
        this.fileStorageProperties = fileStorageProperties;
        this.optionProperties = optionProperties;
        this.collectionValidator = collectionValidator;
    }

    /**
     * 진단 실행 요청. AJ100
     * 수집 진단 분리 실행 요청. AJ100
     * 긴급 진단 실행 요청. AJ101
     * using WAS
     */
    @PostMapping("/diagnosis/regist")
    public ResponseEntity<ApiResult> diagnosisRegist(@RequestBody @Valid List<DiagnosisReq> daigReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(daigReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        assetManagerAsyncService.createJobEntityAssetDiagS(daigReqS);
        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    /**
     * 수집 진단 분리 실행시 암호화된 파일 복호화하여
     * 웹에서 다운로드 가능한 zip파일로 생성
     * 환경 수집 파일 확인 요청. AJ202
     * using WAS
     */
    @PostMapping("/diagnosis/diaginfo")
    public ResponseEntity<ApiResult> diagInfoRegist(@RequestBody @Valid List<DiagInfoReq> daigInfoReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(daigInfoReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        assetManagerAsyncService.makeDownloadableDiagInfoForAsync(daigInfoReqS);
        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    /**
     * 진단 실행 취소. AJ150
     * using WAS
     */
    @RequestMapping(value = "/diagnosis/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
    public ResponseEntity<ApiResult> diagnosisDelete(@RequestBody @Valid List<JobDel> daigDelS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(daigDelS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        return ResponseEntityUtil.returnApiResult(assetManagerService.deleteJobEntityAssetDiagS(daigDelS));
    }

    /**
     * 1. 장비정보수집 실행 중 에이전트 처리과정 알림
     * 2. 에이전트 진단 처리 과정 알림
     * 3. 에이전트 긴급진단 처리과정 알림
     * assetCd, agentCd, auditFileCd, notiType, jobType
     */
    @GetMapping("/process")
    public ResponseEntity<ApiResult> handleProcess(@Valid HandledProcess handledProcess) throws Exception {

        try {
            assetManagerService.updateHandledStatus(handledProcess);
        } catch (Exception ex) {
            return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(400).message(ex.getMessage()).build());
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().message(handledProcess.getAssetCd() + " updated handled status successfully.").build());
    }

    /**
     * 장비정보 수집 요청 - 신규등록 팝업 1단계.
     * using WAS
     */
    @PostMapping("/get/regist")
    public Callable<ApiResult> getRegistOnlySsh(@RequestBody @Valid List<GetReq> getReqS, BindingResult bindingResult,
                                                HttpServletRequest request) throws Exception {
        collectionValidator.validate(getReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        if (getReqS.size() > 1) {
            return () -> ApiResult.builder().result(400)
                    .message("just call one request.").build();
        }

        GetReq getReq = getReqS.get(0);
        String managerCd = getReq.getPersonManagerCd();
        if (StringUtils.isEmpty(managerCd )) {
            managerCd = CommonUtils.getManagerCdFromAuth(request);
            log.debug("managerCd is set by Server");
        }

        JobEntity jobEntity = assetManagerService.createJobEntityAssetGet(getReq.getAssetCd(), getReq.getJobType(), managerCd);
        if (jobEntity == null) {
            return ApiResult::getBadRequestResult;
        }

        if (jobEntity.getAgentInfo().getAgentRegiFlag() == 2) {
            return () -> ApiResult.builder()
                    .message("you need to request second api.")
                    .installed("Y").build();
        } else {
            // 자산별(장비별)로 ssh 기능이 하나라도 실행 중이면 실행되지 않게 막는다.
            if (jobHandleManager.checkRunningSshService(jobEntity))
                return () -> assetManagerService.FailedRunedSshService(jobEntity);

            // 에이전트가 설치되기 전이라면 ssh로 접속하여 수집스크립트 직접 실행
            return () -> assetManagerAsyncService.assetGet(jobEntity);
        }
    }

    /**
     * 장비정보 수집 요청 - 장비 / 에이전트 관리
     * using WAS, AJ200
     * assetCd, jobType, personManagerCd
     */
    @PostMapping("/get/list/regist")
    public ResponseEntity<ApiResult> getRegist(@RequestBody @Valid List<GetReq> getReqS, BindingResult bindingResult,
                                               HttpServletRequest request) throws Exception {

        collectionValidator.validate(getReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        log.debug("personManagerCd : " + getReqS.get(0).getAssetCd() + " - " + getReqS.get(0).getPersonManagerCd());
        log.debug("ManagerCd : " + getReqS.get(0).getAssetCd() + " - " + getReqS.get(0).getManagerCd());

        String managerCd = CommonUtils.getManagerCdFromAuth(request);

        assetManagerAsyncService.createJobEntityAssetGetS(getReqS, managerCd);

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    /**
     * 1. 장비정보 수집파일 요청 (다운로드)
     *    전달되는 파일은 zip파일을 aes128로 암호화한 des파일.
     *    zip 파일안에 get 스크립트 와 해쉬 값파일 두가지가 포함됨.
     *    즉, get 스크립트와 해쉬 값파일 생성 및 압축 -> 압축된 zip파일 aes128로 암호화 -> 에이전트에 전송.
     *
     * 2. 진단파일 요청 (다운로드), 긴급진단파일 요청 (다운로드)
     *
     * 3. 에이전트 최신 파일 요청 (다운로드)
     * @throws Exception
     */
    @GetMapping("/file/dn")
    public ResponseEntity<Resource> downloadScriptsFileDn(@RequestParam("assetCd") String assetCd, @RequestParam("agentCd") String agentCd,
                                                          @RequestParam(value = "auditFileCd", required = false) String auditFileCd, @RequestParam("jobType") String jobType,
                                                          HttpServletRequest request) throws Exception {
        if(StringUtil.isEmpty(assetCd) | StringUtil.isEmpty(jobType)){
            return ResponseEntityUtil.returnResource(null, request);
        }

        return ResponseEntityUtil.returnResource(assetManagerService.downloadAuditFile(assetCd, agentCd, jobType, auditFileCd), request);
    }

    /**
     * 1. 장비 정보 수집 결과 파일 전달 (업로드)
     * 2. 신규, 긴급, 일반 진단 결과 파일 전달 (업로드)
     * 3. 로그 파일 업로드
     * assetCd, agentCd, jobType, files
     * List<MultipartFile> files
     */
    @PostMapping("/file/up")
    public ResponseEntity<ApiResult> handleResultsFileUp(@ModelAttribute @Valid ResultFile getFile,
                                                      BindingResult bindingResult) throws Exception {

        collectionValidator.validate(getFile, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            String uploadPath = fileStorageProperties.getUploadPath(getFile.getJobType());
            if (uploadPath.isEmpty()) {
                throw new Exception("cannot get the upload path.");
            }

            JobEntity jobEntity = assetManagerService.uploadResultFile(getFile, uploadPath);

            if (optionProperties.getAsync().equals("true")) {
                assetManagerAsyncService.handleResultFile(getFile.getAssetCd(), getFile.getJobType(), getFile.getAgentCd(), uploadPath, jobEntity);
            } else {
                assetManagerService.handleResultFile(getFile.getAssetCd(), getFile.getJobType(), getFile.getAgentCd(), uploadPath, jobEntity);
            }

        } catch (SnetException ex) {
            return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(500).message(ex.getMessage()).build());
        } catch (Exception ex) {
            return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(400).message(ex.getMessage()).build());
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().message("finished handling get result data.").build());
    }

    /**
     * 네트워크 장비 정보 수집 요청 - 자동 (온라인)
     * using WAS, WM300
     * assetCd, jobType, personManagerCd, swNm
     */
    @PostMapping("/nw/get/regist")
    public ResponseEntity<ApiResult> nwGetRegist(@RequestBody @Valid List<NwGetReq> nwGetReqS, BindingResult bindingResult,
                                                 HttpServletRequest request) throws Exception {

        collectionValidator.validate(nwGetReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        String managerCd = CommonUtils.getManagerCdFromAuth(request);
        assetManagerAsyncService.createJobEntityAssetGetS(nwGetReqS, managerCd);

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());

    }

    /**
     * 네트워크 진단 요청 - 자동(온라인)
     * using WAS, WM302
     * assetCd, jobType, swType, swNm, swInfo, swDir, swUser, swEtc, managerCd, agentUseStime, agentUseEtime, auditSpeed
     */
    @PostMapping("/nw/diagnosis/regist")
    public ResponseEntity<ApiResult> nwDiagnosisRegist(@RequestBody @Valid List<NwDgReq> nwDgReqS, BindingResult bindingResult,
                                                       HttpServletRequest request) throws Exception {

        collectionValidator.validate(nwDgReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        String managerCd = CommonUtils.getManagerCdFromAuth(request);
        assetManagerAsyncService.createJobEntityAssetGetS(nwDgReqS, managerCd);

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }
}
