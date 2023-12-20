package com.igloosec.smartguard.next.agentmanager.api.util;

import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.FileUtils;
import com.igloosec.smartguard.next.agentmanager.utils.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;

@Slf4j
@Service
public class UtilService {
    /**
     * 1. 파일의 복사본 생성(.log, .dat)
     * 2. zip 압축(.log, .dat -> .zip)
     * 3. aes 암호화 (.zip ->  .des)
     * deprecated 4 .des 파일의 해쉬 파일 생성 (.hash)
     * deprecated 5. zip 압축(.des, .iv. .salt, .hash)
     */
    public ApiResult uploadFileTest(String path, String fileName, String assetCd, String desType) {
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
            } else {
                return ApiResult.getFailResult();
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

            FileUtils.fileCopy(orgResult, mvResult);
            FileUtils.fileCopy(orgLog, mvLog);
            FileUtils.fileCopy(orgSalt, mvSalt);
            FileUtils.fileCopy(orgIv, mvIv);

            files.add(new File(mvResult));
            files.add(new File(mvLog));
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

//            deprecated (des파일만 전송)
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

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
        }

        return ApiResult.builder().build();
    }

    public void uploadNeAgentLog(String path, String fileName, String assetCd) {
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
            String mvNm = zip + File.separator + orgNm;

            FileUtils.fileCopy(orgNm, mvNm);

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

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
        }
    }

    private void checkDirs(String path) {
        File exist = new File(path);
        if (!exist.exists()) {
            exist.mkdir();
        }
    }
}
