package com.igloosec.smartguard.next.agentmanager.api.agent;

import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.model.CollectionValidator;
import com.igloosec.smartguard.next.agentmanager.api.agent.model.ControlAgentReq;
import com.igloosec.smartguard.next.agentmanager.api.agent.model.LogCollection;
import com.igloosec.smartguard.next.agentmanager.api.model.ResponseEntityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value={"/manager/v3/sgs-api/agent", "/manager/v3/sga-api/agent"},
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
@Slf4j
public class AgentManagerController {

    private final AgentManagerService agentManagerService;
    private final CollectionValidator collectionValidator;

    public AgentManagerController(AgentManagerService agentManagerService, CollectionValidator collectionValidator) {

        this.agentManagerService = agentManagerService;
        this.collectionValidator = collectionValidator;
    }

    /**
     * 인증 요청
     * @throws Exception
     */
    @GetMapping("/auth")
    public ResponseEntity<ApiResult> agentAuth(@RequestParam(value = "ip", required = false) String ip, @RequestParam(value = "hostName", required = false) String hostName,
                                               @RequestParam(value = "os", required = false) String os, @RequestParam(value = "assetCd", required = false) String assetCd,
                                               HttpServletRequest request) throws Exception {

        //String[] rcvIpArr = StringUtil.split(ip, "|");
        String chkIp = request.getHeader("X-FORWORD-FOR");
        if (chkIp == null) {
            chkIp = request.getRemoteAddr();
            log.debug("request.getRemoteAddr - " + chkIp);
        } else {
            log.debug("X-FORWORD-FOR - " + chkIp);
        }

        if (chkIp.contains("127.0.0.1") || chkIp.contains("localhost")) {
            chkIp = ip;
        }

        if (StringUtils.isEmpty(chkIp)) {
            chkIp = "";
        }

        // todo
        // rcvIpArr와  request에서 가져온 ip에서 매칭되는게 있는지 확인하는 로직 추가 필요.
        // 지금은 에이전트에서 전달되는 ip는 사용하지 않고 request로 들어온 ip사용
        // ip = chkIp;

        return ResponseEntityUtil.returnApiResult(agentManagerService.getAuthInfo(chkIp, hostName, os, assetCd));
    }

    /**
     * job 스케쥴링 요청
     * @throws Exception
     */
    @GetMapping("/jobs")
    public ResponseEntity<ApiResult> agentJobs(@RequestParam("version") String version, @RequestParam("assetCd") String assetCd,
                                                             @RequestParam(value = "agentCd", required = false) String agentCd,
                                               @RequestParam(value = "cpuUseRate", required = false) String cpuUseRate,
                                               @RequestParam(value = "memTotal", required = false) String memTotal,
                                               @RequestParam(value = "memFree", required = false) String memFree,
                                               @RequestParam(value = "memUse", required = false) String memUse,
                                               @RequestParam(value = "memUseRate", required = false) String memUseRate) throws Exception {

        if (assetCd.isEmpty()) {
            return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(400).message("assetCd is empty. assetCd is required.").build());
        }

        return ResponseEntityUtil.returnApiResult(agentManagerService.getJobsAgent(version, assetCd, agentCd,
                cpuUseRate, memTotal, memFree, memUse, memUseRate));
    }

    /**
     * 설치 / 재시작 / 중지 요청
     * using WAS
     */
    @PostMapping("/control/regist")
    public ResponseEntity<ApiResult> controlTunnelRegist(@RequestBody @Valid List<ControlAgentReq> controlAgentReqS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(controlAgentReqS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        ApiResult apiResult;
        try {
            apiResult = agentManagerService.controlAgent(controlAgentReqS);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            apiResult = ApiResult.getFailResult();
        }


        return ResponseEntityUtil.returnApiResult(apiResult);
    }

    /**
     * 로그 파일 수집 요청
     * using WAS
     * assetCd, jobType
     */
    @PostMapping("/collection/logs/regist")
    public ResponseEntity<ApiResult> collectionLogs(@RequestBody @Valid List<LogCollection> logCollectionS, BindingResult bindingResult) throws Exception {

        collectionValidator.validate(logCollectionS, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        ApiResult apiResult = null;

        try {
            apiResult = agentManagerService.createJobEntityLogCollectionS(logCollectionS);

        } catch (Exception ex) {
            apiResult = ApiResult.getFailResult();
        }

        return ResponseEntityUtil.returnApiResult(apiResult);
    }
}
