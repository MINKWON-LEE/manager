package com.igloosec.smartguard.next.agentmanager.api.etc;

import com.igloosec.smartguard.next.agentmanager.api.etc.model.SettingReq;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.model.CollectionValidator;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.api.etc.model.CheckSumReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Callable;

@EnableAsync
@RestController
@RequestMapping(value= {"/manager/v3/sgs-api"},
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
@Slf4j
public class EtcManagerController {

    private final EtcManagerService etcManagerService;
    private final EtcManagerAsyncService etcManagerAsyncService;
    private final CollectionValidator collectionValidator;

    public EtcManagerController(EtcManagerService etcManagerService, EtcManagerAsyncService etcManagerAsyncService,
                                CollectionValidator collectionValidator) {
        this.etcManagerService = etcManagerService;
        this.etcManagerAsyncService = etcManagerAsyncService;
        this.collectionValidator = collectionValidator;
    }

    /**
     * 수집, 진단스크립트 및 설치 파일 무결성 업데이트 요청
     * Using WAS
     */
    @PostMapping(value = "/checksum/updates")
    public Callable<ApiResult> checksumUpdates(@RequestBody @Valid List<CheckSumReq> checkSumReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(checkSumReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        String message;

        if (etcManagerAsyncService.isRunningCheckSumUpdate()) {
            return ApiResult::getFailResult;
        }

        if (checkSumReqS.size() > 1) {
            message = "just call one request.";
            log.error(message);
            return () -> ApiResult.builder().result(400).message(message).build();
        }

        CheckSumReq checkSumReq = checkSumReqS.get(0);
        message = "checksumUpdates : " + checkSumReq.getJobType() + ", " + checkSumReq.getFileType() + ", " + checkSumReq.getSpecial();
        log.debug(message);

        // 수집 스크립트 hash 값 일괄 업데이트
        if (checkSumReq.getJobType().equals(ManagerJobType.WM400.toString()) && checkSumReq.getFileType().equals("scripts")) {
            etcManagerAsyncService.batchUpdateCheckSum(checkSumReq.getSpecial());
        // 설치 파일 hash 값 일괄 업데이트
        } else if (checkSumReq.getJobType().equals(ManagerJobType.WM401.toString()) && checkSumReq.getFileType().equals("installer")) {
            etcManagerAsyncService.batchUpdateSetupFileCheckSum();
        } else {
            return () -> ApiResult.builder().result(400).message(message).build();
        }

        return () -> ApiResult.builder().build();
    }

    /**
     * 관리자 설정 변경 API
     * using WAS
     */
    @RequestMapping(value = "/setting", method = {RequestMethod.DELETE, RequestMethod.POST})
    public Callable<ApiResult> updateSetting(@RequestBody @Valid List<SettingReq> settingReqs, BindingResult bindingResult,
                                             HttpServletRequest request) throws Exception {

        collectionValidator.validate(settingReqs, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        if (settingReqs.size() > 1) {
            String message = "just call one request.";
            log.error(message);
            return () -> ApiResult.builder().result(400).message(message).build();
        }

        SettingReq settingReq = settingReqs.get(0);

        if (settingReq.getJobType().equals(ManagerJobType.WM500.toString())) {
            etcManagerAsyncService.reloadConfigGlobal(settingReq.getOption());
        }

        return  () -> ApiResult.builder().build();
    }
}
