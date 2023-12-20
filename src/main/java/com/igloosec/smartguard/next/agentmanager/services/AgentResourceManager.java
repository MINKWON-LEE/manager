package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentResource;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;

@Slf4j
@Service
public class AgentResourceManager {

    private Dao dao;

    public AgentResourceManager(Dao dao) {
        this.dao = dao;
    }

    public void insertAgentResource(String assetCd, String cpuUseRate, String memTotal, String memFree,
                              String memUse, String memUseRate) {
        try {
            AgentResource agentResource = new AgentResource(assetCd, cpuUseRate, memTotal, memFree, memUse, memUseRate);
            String CDATE = dao.selectAgentResource(assetCd);
            String cMin = DateUtil.getCurrDateByminute();

            if (StringUtils.isEmpty(CDATE)) {
                CDATE = "0";
            }
            log.debug("resource time stored checked. - " + assetCd + " : " + CDATE + ", (cur " + cMin);

            Long sep = Long.parseLong(cMin) - Long.parseLong(CDATE);
            if (sep > INMEMORYDB.agentResourceTime) {
                dao.insertAgentResource(agentResource);
            } else {
                log.debug("skipped resource memory time to store -" + assetCd );
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public void checkCurrentAgentManagerVersion() {
        int lv1 = 0, lv2 = 0, lv3 = 0, lv4 = 0;
        int tv1 = 0, tv2 = 0, tv3 = 0, tv4 = 0;

        getSmartGuardLatestVersionStr();

        String latestVer = INMEMORYDB.smartGuardVersion;
        if (latestVer.startsWith("V")) {
            latestVer = latestVer.substring(1);
        }
        log.debug("latestVer:" + latestVer);
        log.debug("toBeVer:" + INMEMORYDB.toBeVersion);
        String[] lvparsed = latestVer.split("\\.");
        String[] tvparsed = INMEMORYDB.toBeVersion.split("\\.");

        try {
            if (lvparsed.length == 3) {
                lv1 = Integer.parseInt(lvparsed[0]);
                lv2 = Integer.parseInt(lvparsed[1]);
                lv3 = Integer.parseInt(lvparsed[2]);
            } else {
                lv1 = Integer.parseInt(lvparsed[0]);
                lv2 = Integer.parseInt(lvparsed[1]);
                lv3 = Integer.parseInt(lvparsed[2]);
                lv4 = Integer.parseInt(lvparsed[3]);
            }

            tv1 = Integer.parseInt(tvparsed[0]);
            tv2 = Integer.parseInt(tvparsed[1]);
            tv3 = Integer.parseInt(tvparsed[2]);
            tv4 = Integer.parseInt(tvparsed[3]);

            if (lv1 > tv1) {
                log.debug("lv1 > tv1");
                INMEMORYDB.toBeVersionUse = false;
            } else if (lv1 == tv1) {
                if (lv2 > tv2) {
                    log.debug("lv2 > tv2");
                    INMEMORYDB.toBeVersionUse = false;
                } else if (lv2 == tv2) {
                    if (lv3 > tv3) {
                        log.debug("lv3 > tv3");
                        INMEMORYDB.toBeVersionUse = true;
                    } else if (lv3 == tv3) {
                        if (lv4 >= tv4) {
                            log.debug("lv4 > tv4");
                            INMEMORYDB.toBeVersionUse = false;
                            log.info("SmartGuard VERSION IS UP TO DATE.!\n");
                        } else {
                            INMEMORYDB.toBeVersionUse = true;
                        }
                    } else {
                       INMEMORYDB.toBeVersionUse = true;
                    }
                } else {
                    INMEMORYDB.toBeVersionUse = true;
                }
            } else {
                INMEMORYDB.toBeVersionUse = true;
            }
        } catch (Exception e) {
            log.error("Version Context ERROR[1]  " +
                    " latestVer:" + latestVer + " toBeVer:" + INMEMORYDB.toBeVersion
                    + "ERR MSG :" + e.getMessage() + "\n");
            INMEMORYDB.toBeVersionUse = false;
        }
    }

    public String getSmartGuardLatestVersionStr() {

        String versionFile = INMEMORYDB.MODULES_VERSION;
        log.debug("MODULES_VERSION File Path : " + versionFile);
        String latestVer = "";


        File file = new File(versionFile);
        if(file.exists()){
            latestVer = readSmartGuardVerFile(file);
        } else {
            log.debug("MODULES_VERSION File is Not Exist.");
        }

        INMEMORYDB.smartGuardVersion = latestVer;

        return latestVer;
    }

    public String readSmartGuardVerFile(File f) {
        String lineStr = "";
        int lineCnt = 0;

        try {
            BufferedReader input = new BufferedReader(new FileReader(f));

            try{
                String line = null;
                while( (line = input.readLine()) != null)
                {
                    if(line.startsWith("SmartGuardVersion=")){
                        log.debug("MODULES.VERSION : " + line);
                        lineStr = line.replaceAll("SmartGuardVersion=", "");
                        break;
                    }
                }
                input.close();
            }catch(IOException ex){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return lineStr;
    }
}
