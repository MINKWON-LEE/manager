package com.igloosec.smartguard.next.agentmanager.api.util;

import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.FileUtils;
import com.igloosec.smartguard.next.agentmanager.utils.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UtilAsyncService {
    /**
     * 1. 파일의 복사본 생성(.log, .dat)
     * 2. zip 압축(.log, .dat -> .zip)
     * 3. aes 암호화 (.zip ->  .des)
     * 4 .des 파일의 해쉬 파일 생성 (.hash)
     * 5. zip 압축(.des, .iv. .salt, .hash)
     */
    @Async
    public CompletableFuture<ApiResult> uploadFileTest(String path, String fileName, String assetCd, String desType, String onlyLog) {

        return CompletableFuture.completedFuture(uploadFileTestForAsync(path, fileName, assetCd, desType, onlyLog));
    }

    private ApiResult uploadFileTestForAsync(String path, String fileName, String assetCd, String desType, String onlyLog) {

        String zip = path + File.separator + "zip";
        String aes = zip + File.separator + "aes";
        String tmp = aes + File.separator + "tmp";
        checkDirs(zip);
        checkDirs(aes);
        checkDirs(tmp);

        CommonUtils.deleteFiles(zip);
        CommonUtils.deleteFiles(aes);
        CommonUtils.deleteFiles(tmp);

        try {
            String orgNm;
            String orgResult, orgLog, orgIv, orgSalt;
            String mvResult, mvLog, mvIv, mvSalt;
            ArrayList<File> files = new ArrayList<File>();
            ArrayList<File> dest = new ArrayList<File>();

            orgNm = path + File.separator + assetCd;
            if (desType.equals("get")) {
                orgResult = orgNm + ".dat";
            } else if (desType.equals("diagnosis")) {
                orgResult = orgNm + ".xml";
            } else if (desType.equals("event")) {
                orgResult = orgNm + ".xml";
            } else {
                log.error("desType must be \"get\" or \"diagnosis\" or \"event\".");
                return ApiResult.getBadRequestResult();
            }

            orgLog = orgNm + ".log";
            orgIv = orgNm + ".iv";
            orgSalt = orgNm + ".salt";

            if (desType.equals("get")) {
                mvResult = zip + File.separator + fileName + ".dat";
            } else /*if (desType.equals("diagnosis"))*/ {
                mvResult = zip + File.separator + fileName + ".xml";
            }

            mvLog = zip + File.separator + fileName + ".log";
            mvIv = zip + File.separator + fileName + ".iv";
            mvSalt = zip + File.separator + fileName + ".salt";

            FileUtils.fileCopy(orgLog, mvLog);

            if (!onlyLog.toLowerCase().equals("y")) {
                FileUtils.fileCopy(orgResult, mvResult);
                FileUtils.fileCopy(orgSalt, mvSalt);
                FileUtils.fileCopy(orgIv, mvIv);

                files.add(new File(mvResult));
            }

            files.add(new File(mvLog));
            new ZipUtil().makeZip(files, aes + File.separator + fileName + ".zip");

            String desNm = tmp + File.separator + fileName + ".des";
            new AESCryptography().encryptionShFile2(aes);
            File tmpFile = new File(tmp);
            File aesFile;
            try {
                for (File f : tmpFile.listFiles()) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    aesFile = f;
                    FileUtils.fileCopy(aesFile.getAbsolutePath(), desNm);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
                return ApiResult.getFailResult();
            }

//          deprecated (des파일만 전송)
//            String section = "[" + fileName + ".des" + "]";
//            String hashPath = tmp + File.separator + fileName + INMEMORYDB.HASH;
//            BufferedOutputStream bs = null;
//            try {
//                bs = new BufferedOutputStream(new FileOutputStream(hashPath, true));
//                bs.write(section.getBytes());
//                bs.write(System.lineSeparator().getBytes());
//                String hash = new AESCryptography().encryption(Paths.get(tmp + File.separator + fileName + ".des"));
//                bs.write(hash.getBytes());
//            } catch (Exception e) {
//                log.error("Test program Decryption Fail.");
//                e.printStackTrace();
//            }finally {
//                bs.close();
//            }
//
//            dest.add(new File(desNm));
//            dest.add(new File(mvIv));
//            dest.add(new File(mvSalt));
//            dest.add(new File(hashPath));
//
//            new ZipUtil().makeZip(dest, tmp + File.separator + fileName + ".zip");

            return ApiResult.builder().build();

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());

            return ApiResult.getFailResult();
        }
    }

    @Async
    public CompletableFuture<ApiResult> uploadNeAgentLog(String path, String fileName, String assetCd) {

        return CompletableFuture.completedFuture(uploadNeAgentLogForAsync(path, fileName, assetCd));
    }

    public ApiResult uploadNeAgentLogForAsync(String path, String fileName, String assetCd) {

        path += File.separator + "NeAgentLog";
        String zip = path + File.separator + "zip";
        String aes = zip + File.separator + "aes";
        String tmp = aes + File.separator + "tmp";

        checkDirs(zip);
        checkDirs(aes);
        checkDirs(tmp);

        CommonUtils.deleteFiles(zip);
        CommonUtils.deleteFiles(aes);
        CommonUtils.deleteFiles(tmp);

        try {
            String orgNm = "NeAgent.log";
            String orgFullPath = path + File.separator + orgNm;
            String mvNm = zip + File.separator + orgNm;

            FileUtils.fileCopy(orgFullPath, mvNm);

            ArrayList<File> files = new ArrayList<File>();
            files.add(new File(mvNm));

            new ZipUtil().makeZip(files, aes + File.separator + fileName + ".zip");

            String desNm = tmp + File.separator + fileName + ".des";
            new AESCryptography().encryptionShFile2(aes);
            File tmpFile = new File(tmp);
            File aesFile;
            for (File f : tmpFile.listFiles()) {
                if (f.isDirectory()) {
                    continue;
                }
                aesFile = f;
                FileUtils.fileCopy(aesFile.getAbsolutePath(), desNm);
            }

            return ApiResult.builder().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());

            return ApiResult.getFailResult();
        }
    }

    /**
     * 1. 파일의 복사본 생성(.log, .dat)
     * 2. zip 압축(.log, .txt -> .zip)
     * 3. aes 암호화 (.zip ->  .des)
     * 4 .des 파일의 해쉬 파일 생성 (.hash)
     * 5. zip 압축(.des, .iv. .salt, .hash)
     */
    @Async
    public CompletableFuture<ApiResult> uploadFileDiagInfoTest(String path, String fileName, String assetCd) {

        return CompletableFuture.completedFuture(uploadFileDiagInfoTestForAsync(path, fileName, assetCd));
    }

    private ApiResult uploadFileDiagInfoTestForAsync(String path, String fileName, String assetCd) {

        String zip = path + File.separator + "zip";
        checkDirs(zip);

        CommonUtils.deleteFiles(zip);
        if (new File(zip + File.separator + "tmp").exists()) {
            CommonUtils.deleteFiles(zip + File.separator + "tmp");
        }

        try {
            ArrayList<File> files = new ArrayList<File>();

            File orgFileDir = new File(path);
            try {
                for (File f : orgFileDir.listFiles()) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    files.add(new File(f.getAbsolutePath()));
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
                return ApiResult.getFailResult();
            }

            String outFileName = zip + File.separator + fileName;
            new ZipUtil().makeZip(files, outFileName + ".zip");

            new AESCryptography().encryptionShFile2(zip);

            String desPath = zip + File.separator + "tmp";
            File desDir = new File(desPath);
            for (File f : desDir.listFiles()) {
                if (f.isDirectory()) {
                    continue;
                }

                FileUtils.fileCopy(f.getAbsolutePath(), desPath + File.separator + fileName + ".des");
            }

            return ApiResult.builder().build();

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());

            return ApiResult.getFailResult();
        }
    }

    private void checkDirs(String path) {
        File exist = new File(path);
        if (!exist.exists()) {
            exist.mkdir();
        }
    }
}
