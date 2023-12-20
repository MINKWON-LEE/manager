package com.igloosec.smartguard.next.agentmanager.api.etc;

import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.services.*;
import com.igloosec.smartguard.next.agentmanager.api.etc.model.InstallFile;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class EtcManagerAsyncService {

    private boolean isRunningCheckSumUpdate;

    private Dao dao;
    private InitConfigMemory initConfigMemory;

    public EtcManagerAsyncService(Dao dao, InitConfigMemory initConfigMemory) {
        this.dao = dao;
        this.initConfigMemory = initConfigMemory;
    }

    public boolean isRunningCheckSumUpdate() {
        return isRunningCheckSumUpdate;
    }

    @Async
    public ApiResult batchUpdateCheckSum(String special) {
        isRunningCheckSumUpdate = true;

        if (special == null) {
            special = new String("");
        }

        String srcPath = "", destPath = "";

        // 장비정보수집 스크립트 hash 값 일괄 업데이트
        log.info("start batch update checksum hash.");

        // Window 장비정보 수집스크립트 복호화
        if (special.equals("gwin") || special.isEmpty()) {
            srcPath = INMEMORYDB.scriptFileAbsolutePath(ManagerJobType.AJ200.toString(), INMEMORYDB.WIN_ID);
            destPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, ManagerJobType.AJ200.toString()) + "wtp";
            updateChecksumHash(srcPath, destPath);
        }

        // Linux 장비정보 수집스크립트 복호화
        if (special.equals("gunix") || special.isEmpty()) {
            srcPath = INMEMORYDB.scriptFileAbsolutePath(ManagerJobType.AJ200.toString(), "");
            destPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, ManagerJobType.AJ200.toString()) + "ltp";
            updateChecksumHash(srcPath, destPath);
        }

        // Window 진단 스크립트 복호화
        if (special.equals("dwin") || special.isEmpty()) {
            srcPath = INMEMORYDB.scriptFileAbsolutePath(ManagerJobType.AJ100.toString(), INMEMORYDB.WIN_ID);
            destPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, ManagerJobType.AJ100.toString()) + "wtp";
            updateChecksumHash(srcPath, destPath);
        }

        // Linux 진단 스크립트 복호화
        if (special.equals("dunix") || special.isEmpty()) {
            srcPath = INMEMORYDB.scriptFileAbsolutePath(ManagerJobType.AJ100.toString(), "");
            if (INMEMORYDB.diagInfoNotUse) {
                srcPath = INMEMORYDB.scriptFileAbsolutePathDiagInfoNotUse(ManagerJobType.AJ100.toString(), "");
            }
            destPath = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, ManagerJobType.AJ100.toString()) + "1tp";
            updateChecksumHash(srcPath, destPath);
        }

        isRunningCheckSumUpdate = false;
        // 진단스크립트 hash 값 일괄 업데이트
        log.info("end batch update checksum hash.");

        return ApiResult.builder().build();
    }

    @Async
    public ApiResult batchUpdateSetupFileCheckSum() {
        updateSetupFileChecksumHash("2");
        updateSetupFileChecksumHash("3");

        return ApiResult.builder().build();
    }

    private void updateSetupFileChecksumHash(String agentType) {
        isRunningCheckSumUpdate = true;

        // 설치파일 hash 값 일괄 업데이트
        String hash = "";

        InstallFile installFile = new InstallFile();
        AESCryptography aesCryptography = new AESCryptography();

        log.info("start batch update checksum setup file hash. agentType - {}", agentType);

        try {
            // DB에서 설치파일 리스트 조회(1회용, 수동)
            // 전체 목록에 대해서 해쉬값을 뜨고 업데이트 한다.
            installFile.setAgentType(agentType);
            List<InstallFile> installFileList = dao.selectSnetSetupFileList(installFile);

            for (InstallFile model : installFileList) {
                String insFileDir = CommonUtils.getWinPath(model.getInstallFileDir());
                hash = aesCryptography.encryption(Paths.get(insFileDir + File.separator + model.getInstallFileNm()));
                log.info("file path : " + insFileDir + File.separator + model.getInstallFileNm());
                log.info("checksum hash in db : " + model.getChecksumHash());
                log.info("new checksum hash : " + hash);
                if ("ERROR".equals(hash)) {
                    continue;
                }

                InstallFile update = new InstallFile();
                update.setChecksumHash(hash);
                update.setSwNm(model.getSwNm());
                update.setBitType(model.getBitType());
                update.setAgentType(agentType);
                dao.updateInstallFileChecksumHash(update);
            }
        } catch (SnetException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        isRunningCheckSumUpdate = false;
        log.info("end batch update checksum setup file hash. agentType - {}", agentType);
    }

    private void updateChecksumHash(String srcPath, String destPath){
        decScriptFile(srcPath, destPath);
        updateChecksumHashDB(destPath);
        CommonUtils.deleteDirectory(new File(destPath));
    }

    private String decScriptFile(String srcPath, String destPath){
        String result = "";
        log.debug("src path : " + srcPath);
        log.debug("dest path : " + destPath);
        try{
            new AESCryptography().decScannDir(srcPath, destPath);
        }catch (Exception e){
            log.error("get script decrypt fail : ", e);
            result = e.getMessage();
        }
        return result;
    }

    private void updateChecksumHashDB(String path){
        File file = new File(path);

        HashMap<String, String> param = new HashMap<>();
        AESCryptography aesCryptography = new AESCryptography();
        String hash = "";
        for(File f:file.listFiles()){
            param.put("fileNm", f.getName());
            hash = aesCryptography.encryption(Paths.get(f.getAbsolutePath()));
            if("ERROR".equals(hash))
                continue;
            param.put("hash", hash);
            try {
                dao.updateDiagnosisFileChecksumHash(param);
            } catch (Exception e){
                log.error("checksum hash update fail.", e);
            }
        }
    }

    @Async
    public void reloadConfigGlobal(String option) {
        if (StringUtils.isNotEmpty(option) && option.toLowerCase().equals("y")) {
            initConfigMemory.initMemory();
            initConfigMemory.resetProperties();
        }
    }
}
