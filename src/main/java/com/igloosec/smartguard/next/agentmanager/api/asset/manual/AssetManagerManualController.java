package com.igloosec.smartguard.next.agentmanager.api.asset.manual;

import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.model.CollectionValidator;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.DiagnosisManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.GetManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.NwDgManualReq;
import com.igloosec.smartguard.next.agentmanager.api.asset.model.NwGetManualReq;
import com.igloosec.smartguard.next.agentmanager.api.model.ResponseEntityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value= {"/manager/v3/sgs-api/manual/asset", "/manager/v3/sga-api/manual/asset"},
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
@Slf4j
public class AssetManagerManualController {

    private final AssetManagerManualService assetManagerManualService;
    private final AssetManagerManualAsyncService assetManagerManualAsyncService;
    private final OptionProperties optionProperties;
    private final CollectionValidator collectionValidator;

    public AssetManagerManualController(AssetManagerManualService assetManagerManualService, AssetManagerManualAsyncService assetManagerManualAsyncService,
                                        OptionProperties optionProperties, CollectionValidator collectionValidator) {
        this.assetManagerManualService = assetManagerManualService;
        this.assetManagerManualAsyncService = assetManagerManualAsyncService;
        this.optionProperties = optionProperties;
        this.collectionValidator = collectionValidator;
    }

    /**
     * 장비정보 수집 요청 - 수동 업로드
     * Using WAS
     */
    @PostMapping("/get/regist")
    public ResponseEntity<ApiResult> getRegist(@RequestBody @Valid List<GetManualReq> getManualReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(getManualReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            if (optionProperties.getAsync().equals("true")) {
                assetManagerManualAsyncService.recvManualGetscriptResult(getManualReqS);
            } else {
                assetManagerManualService.recvManualGetscriptResult(getManualReqS);
            }
        } catch (Exception ex) {
            return ResponseEntityUtil.returnApiResult(ApiResult.getFailResult());
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    /**
     * 진단실행 요청 - 수동 업로드
     * Using WAS
     */
    @PostMapping("/diagnosis/regist")
    public ResponseEntity<ApiResult> diagnosisRegist(@RequestBody @Valid List<DiagnosisManualReq> diagnosisManualReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(diagnosisManualReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            if (optionProperties.getAsync().equals("true")) {
                assetManagerManualAsyncService.recvManualDgscriptResult(diagnosisManualReqS);
            } else {
                assetManagerManualService.recvManualDgscriptResult(diagnosisManualReqS);
            }
        } catch (Exception ex) {
            return ResponseEntityUtil.returnApiResult(ApiResult.getFailResult());
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    /**
     * 네트워크 수집 요청 - 수동 업로드
     * WM200, WM201
     * auditType "REGI" 일 때
     */
    @PostMapping("/nw/get/regist")
    public ResponseEntity<ApiResult> nwGetRegist(@RequestBody @Valid List<NwGetManualReq> nwGetManualReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(nwGetManualReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            if (optionProperties.getAsync().equals("true")) {
                assetManagerManualAsyncService.recvSrvManualGscriptResult(nwGetManualReqS);
            } else {
                assetManagerManualService.recvSrvManualGscriptResult(nwGetManualReqS);
            }
        } catch (Exception ex) {
            return ResponseEntityUtil.returnApiResult(ApiResult.getFailResult());
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    /**
     * 네트워크 진단 요청 - 수동 업로드
     * WM200, WM201 ("네트워크 수집 요청 - 수동 업로드"와 같이 사용)
     * auditType "AUDIT" 일 때 진단
     */
    @PostMapping("/nw/diagnosis/regist")
    public ResponseEntity<ApiResult> nwDiagnosisRegist(@RequestBody @Valid List<NwDgManualReq> nwDgManualReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(nwDgManualReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            if (optionProperties.getAsync().equals("true")) {
                assetManagerManualAsyncService.recvSrvManualDgscriptResult(nwDgManualReqS);
            } else {
                assetManagerManualService.recvSrvManualDgscriptResult(nwDgManualReqS);
            }
        } catch (Exception ex) {
            return ResponseEntityUtil.returnApiResult(ApiResult.getFailResult());
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }
}