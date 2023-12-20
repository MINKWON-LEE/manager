package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.api.agent.model.LogCollection;
import com.igloosec.smartguard.next.agentmanager.crypto.AESCryptography;
import com.igloosec.smartguard.next.agentmanager.crypto.AESFileDecryption;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class AgentLogManager {

    private Dao dao;

    private AgentLogManager(Dao dao) {
        this.dao = dao;
    }

    public void createJobEntities(List<LogCollection> logColS) throws Exception {

        if (logColS.size() <= 0) {
            String msg = "request List<LogCollection> is empty.";
            log.error(msg);
            throw new Exception(msg);
        }

        HashMap<String, Object> param = new HashMap<>();
        param.put("reqS", logColS);
        param.put("personManagerCd", "notUsed");

        List<AgentInfo> ais = dao.selectAgentInfoMulti(param);
        if (ais != null) {
            for(AgentInfo agentInfo : ais) {
                if(agentInfo != null ) {
                    agentInfo.completeAgentInfo();
                    JobEntity jobEntity = initJobEntity(agentInfo);
                    if (jobEntity != null) {
                        // 에이전트를 통한 로그 수집 큐에 등록
                        INMEMORYDB.createRunningLogJobList(jobEntity);
                        log.debug("succeed in inserting item aeestCd({}) into RUNNINGLOGJOBLIST( size - {} ), JOBLISTPERASSET( size - {} )",
                                jobEntity.getAssetCd(), INMEMORYDB.RUNNINGLOGJOBLIST.size(), INMEMORYDB.JOBLISTPERASSET.size());
                    }
                }
            }
        } else {
            throw new Exception("please check assetCd again.");
        }
    }

    public JobEntity initJobEntity(AgentInfo ai) {

        JobEntity jobEntity = new JobEntity();
        jobEntity.setAgentInfo(ai);
        jobEntity.setAssetCd(ai.getAssetCd());
        jobEntity.setJobType(ManagerJobType.AJ300.toString());

        String fileName = CommonUtils.makeFileName();
        jobEntity.setSOTP(fileName);
        jobEntity.setFileName(fileName);

        log.debug("{}, {}, {}, {}", jobEntity.getAssetCd(), jobEntity.getJobType(), jobEntity.getAgentInfo().getAgentCd(), jobEntity.getFileName());

        return jobEntity;
    }

    public void handleAgentLogFile(JobEntity jobEntity) throws Exception {
        //Receive Zip File
        jobEntity.setFileType(INMEMORYDB.ZIP);

        //File decryption
        new AESCryptography().decryptionFile(jobEntity);

        /**Unzip File**/
        CommonUtils.unZipFile(jobEntity);

        //Delete zip result File.
        INMEMORYDB.deleteFile(jobEntity, INMEMORYDB.RECV);

        log.debug("succeed in handling \"NeAgnet.log\" file. ");
    }
}
