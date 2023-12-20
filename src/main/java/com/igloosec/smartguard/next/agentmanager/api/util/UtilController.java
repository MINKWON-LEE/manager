package com.igloosec.smartguard.next.agentmanager.api.util;

import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.api.model.CollectionValidator;
import com.igloosec.smartguard.next.agentmanager.utils.MultipartUpload;
import com.igloosec.smartguard.next.agentmanager.api.model.ResponseEntityUtil;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.property.FileStorageProperties;
import com.igloosec.smartguard.next.agentmanager.property.OptionProperties;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

@RestController
@RequestMapping(value={"/"},
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
@Slf4j
public class UtilController {

    private final FileStorageProperties fileStorageProperties;
    private final UtilService utilService;
    private final UtilAsyncService utilAsyncService;
    private final OptionProperties optionProperties;
    private final CollectionValidator collectionValidator;

    public UtilController(UtilService utilService, FileStorageProperties fileStorageProperties
            , UtilAsyncService utilAsyncService, OptionProperties optionProperties
            , CollectionValidator collectionValidator) {
        this.utilService = utilService;
        this.fileStorageProperties = fileStorageProperties;
        this.utilAsyncService = utilAsyncService;
        this.optionProperties = optionProperties;
        this.collectionValidator = collectionValidator;
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResult> Ping() {

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(200)
                .message("pint test run. (started at : " + INMEMORYDB.serverStartedAt + " )").build());
    }

    @PostMapping("/manager/fileUpload/diag")
    public ResponseEntity<ApiResult> diagFileUploadTest(@RequestParam("assetCd") String assetCd, @RequestParam("agentCd") String agentCd,
                                          @RequestParam("auditFileCd") String auditFileCd, @RequestParam("fileName") String fileName,
                                          @RequestParam("uri") String uri, @RequestParam("path") String path) throws Exception {

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("assetCd", assetCd);
        parts.add("agentCd", agentCd);
        parts.add("auditFileCd", auditFileCd);

        String destUri = "", destPath = "", destFileNm = "";
        if (uri.isEmpty()) {
            destUri = "http://192.168.80.118:10225/manager/v3/sga-api/asset/diagnosis/results/file/up";
        } else {
            destUri = uri;
        }

        if (path.isEmpty()) {
            destPath = "D:\\dev\\NEXT\\source\\sg_manager\\testTool\\sampledata\\diagnosis\\zip\\aes\\tmp";
        } else {
            destPath = path;
        }

        destFileNm = fileName;
        parts.add("files", new FileSystemResource(path + File.separator + destFileNm));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(new URI(uri), parts, String.class);

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(response.getStatusCodeValue()).message(response.getBody()).build());
    }

    @PostMapping("/manager/fileUpload/urlcon/diag")
    public ResponseEntity<ApiResult> diagFileUploadTest2(@RequestParam("assetCd") String assetCd, @RequestParam("agentCd") String agentCd,
                                                        @RequestParam("auditFileCd") String auditFileCd, @RequestParam("fileName") String fileName,
                                                        @RequestParam("uri") String uri, @RequestParam("path") String path) throws Exception {


        String destUri = "", destPath = "", destFileNm = "";
        if (uri.isEmpty()) {
            destUri = "http://192.168.80.118:10225/manager/v3/sga-api/asset/diagnosis/results/file/up";
        } else {
            destUri = uri;
        }

        if (path.isEmpty()) {
            destPath = "D:\\dev\\NEXT\\source\\sg_manager\\testTool\\sampledata\\diagnosis\\zip\\aes\\tmp";
        } else {
            destPath = path;
        }

        destFileNm = fileName;

        HashMap<String, String> params = new HashMap<>();
        HashMap<String, String> files = new HashMap<>();
        MultipartUpload multipartUpload = new MultipartUpload(destUri, "UTF-8");
        params.put("assetCd", assetCd);
        params.put("agentCd", agentCd);
        params.put("auditFileCd", auditFileCd);
        files.put("files", path + File.separator + destFileNm);

        String remoteMsg = multipartUpload.upload(params, files);

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(200).message(remoteMsg).build());
    }

    @RequestMapping(value = "/manager/uploadTest/{jobType}", method = RequestMethod.POST)
    public ResponseEntity<ApiResult> uploadTest(@PathVariable("jobType") String jobType, @RequestParam("path") String path, @RequestParam("fileName") String fileName,
                                                @RequestParam("assetCd") String assetCd, @RequestParam(value = "onlyLog", required = false) String onlyLog) throws Exception {

        String defaultPath;
        if (StringUtils.isEmpty(path)) {
            defaultPath = System.getProperty("user.dir");
            defaultPath += File.separator + "testTool" + File.separator + "sampledata" + File.separator;
        } else {
            String osType = System.getProperty("os.name").toLowerCase();
            if (osType.contains("win")) {
                path = StringUtil.replace(path, "/", File.separator);
            }

            defaultPath = path;
        }

        String desType;
        if (jobType.equals("get")) {
            desType = "get";
        } else if (jobType.equals("diagnosis")) {
            desType = "diagnosis";
        } else if (jobType.equals("event")) {
            desType = "event";
        } else {
            return ResponseEntityUtil.returnBadRequest();
        }

        if (onlyLog.toLowerCase().equals("y")) {
            defaultPath += File.separator + "log";
        } else {
            defaultPath += File.separator + desType;
        }

        log.debug("zip and des TEST defaultPath - " + defaultPath);
        // 2781634814116490385
        log.debug("zip and des TEST fileName - " + fileName);
        // "AC20191002133134.dat";
        log.debug("zip and des TEST orgName - " + assetCd);

        if (optionProperties.getAsync().equals("true")) {
            utilAsyncService.uploadFileTest(defaultPath, fileName, assetCd, desType, onlyLog);
        } else {
            utilService.uploadFileTest(defaultPath, fileName, assetCd, desType);
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    @RequestMapping(value = "/manager/logFileTest/NeAgent", method = RequestMethod.POST)
    public ResponseEntity<ApiResult> uploadTest(@RequestParam("path") String path, @RequestParam("fileName") String fileName,
                                                @RequestParam("assetCd") String assetCd) throws Exception {
        String defaultPath;
        if (StringUtils.isEmpty(path)) {
            defaultPath = System.getProperty("user.dir");
            defaultPath += File.separator + "testTool" + File.separator + "sampledata" + File.separator;
        } else {
            defaultPath = path;
        }

        if (optionProperties.getAsync().equals("true")) {
            utilAsyncService.uploadNeAgentLog(defaultPath, fileName, assetCd);
        } else {
            utilService.uploadNeAgentLog(defaultPath, fileName, assetCd);
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }

    @RequestMapping(value = "/manager/uploadTest/diagInfo", method = RequestMethod.POST)
    public ResponseEntity<ApiResult> uploadDiagInfoTest(@RequestParam("path") String path, @RequestParam("fileName") String fileName,
                                                @RequestParam("assetCd") String assetCd) throws Exception {

        String defaultPath;
        if (StringUtils.isEmpty(path)) {
            defaultPath = System.getProperty("user.dir");
            defaultPath += File.separator + "testTool" + File.separator + "sampledata" + File.separator;
        } else {
            String osType = System.getProperty("os.name").toLowerCase();
            if (osType.contains("win")) {
                path = StringUtil.replace(path, "/", File.separator);
            }

            defaultPath = path;
        }

        log.debug("zip and des TEST defaultPath - " + defaultPath);
        // 2781634814116490385
        log.debug("zip and des TEST fileName - " + fileName);
        // "AC20191002133134.dat";
        log.debug("zip and des TEST orgName - " + assetCd);

        if (optionProperties.getAsync().equals("true")) {
            utilAsyncService.uploadFileDiagInfoTest(defaultPath, fileName, assetCd);
        }

        return ResponseEntityUtil.returnApiResult(ApiResult.builder().build());
    }
}
